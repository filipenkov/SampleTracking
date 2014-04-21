package com.atlassian.jira.web.less;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.web.util.CssSubstitutionWebResourceTransformer;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.servlet.ServletContextFactory;
import com.atlassian.plugin.util.PluginUtils;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.base.Supplier;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import javax.servlet.ServletContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Transforms .less files into .css files, see http://lesscss.org/
 * @since v4.3
 */
public class LessTransformer implements WebResourceTransformer
{






    private static final Pattern CSS_LITERAL = Pattern.compile("^(" +
            "([A-Za-z-]+)" + // keywords like: border-collapse
            "|" +
            "(#[A-Za-z0-9]+)" + // hash colours
            "|" +
            "(\\d*\\.?\\d+? *(px|%|em|pc|ex|in|deg|s|ms|pt|cm|mm|rad|grad|turn)?)" + // dimension (or dimensionless number)
            "|" +
            "[\"']" + // anything that already looks quoted
            ")$"
    );

    /** copied from com.atlassian.plugin.servlet.AbstractDownloadableResource */
    private static final String ATLASSIAN_WEBRESOURCE_DISABLE_MINIFICATION = "atlassian.webresource.disable.minification";

    /**
     * This is too expensive (memory and time) to construct every time. It is immutable, so just use a global one.
     */
    private static final Supplier<LessCompiler> LESSC = new LazyReference<LessCompiler>()
    {
        @Override
        protected LessCompiler create() throws Exception
        {
            return new LessCompiler();
        }
    };

    private final ApplicationProperties applicationProperties;
    private final ServletContextFactory servletContextFactory;
    private final PluginAccessor pluginAccessor;

    public LessTransformer(ApplicationProperties applicationProperties, ServletContextFactory servletContextFactory, PluginAccessor pluginAccessor)
            throws IOException
    {
        this.applicationProperties = applicationProperties;
        this.servletContextFactory = servletContextFactory;
        this.pluginAccessor = pluginAccessor;
    }

    public DownloadableResource transform(Element element, ResourceLocation resourceLocation, String extrapath, DownloadableResource downloadableResource)
    {

        return new LessResource(pluginKeyForElement(element), servletContextFactory.getServletContext(), resourceLocation, downloadableResource);
    }

    private static String pluginKeyForElement(Element element)
    {
        // Just traverse up the XML document to find the plugin key
        // Don says: Don't ever do this (but there's no other mechanism)
        String pluginKey = null;
        Element p = element;
        while ((p = p.getParent()) != null)
        {
            if (p.getName().equals("atlassian-plugin"))
            {
                pluginKey = p.attributeValue("key");
            }
        }
        return pluginKey;
    }

    private boolean isMinificationDisabled()
    {
        return Boolean.getBoolean(ATLASSIAN_WEBRESOURCE_DISABLE_MINIFICATION) || Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE);
    }

    private String makeLookAndFeelLess()
    {
        final LookAndFeelBean laf = LookAndFeelBean.getInstance(applicationProperties);
        CssSubstitutionWebResourceTransformer.VariableMap variableMap = new CssSubstitutionWebResourceTransformer.VariableMap(laf);

        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> entry : variableMap.getVariableMap(false).entrySet())
        {
            out.append("@").append(entry.getKey()).append(": ");
            out.append(encodeValue(entry.getValue())).append(";\n");
        }

        return out.toString();
    }

    private String encodeValue(String value)
    {
        value = StringUtils.trimToEmpty(value);
        // keep any CSS literals verbatim, quote everything else
        if (CSS_LITERAL.matcher(value).matches()) {
            return value;
        }

        value = value.replaceAll("['\"]", "\\$0");
        return '"' + value + '"'; // good enough, escape " and ' as necessary
    }

    private class LessResource extends AbstractStringTransformedDownloadableResource
    {
        private final String pluginKey;
        private final ServletContext servletContext;
        private final ResourceLocation originalLocation;
        private final BaseLoader loader;

        private LessResource(String pluginKey, ServletContext servletContext, ResourceLocation originalLocation, DownloadableResource originalResource)
        {
            super(originalResource);
            this.pluginKey = pluginKey;
            this.servletContext = servletContext;
            this.originalLocation = originalLocation;

            final String sourceParam = originalLocation.getParameter("source");
            if ("webContextStatic".equalsIgnoreCase(sourceParam))
            {
                loader = new WebContextLoader();
            }
            else
            {
                loader = new PluginLoader();
            }

        }

        @Override
        public String getContentType()
        {
            return "text/css";
        }

        @Override
        protected String transform(final String originalContent)
        {
            boolean compress = !isMinificationDisabled();
            return LESSC.get().compile(originalLocation.getLocation(), loader, originalContent, compress);
        }


        public abstract class BaseLoader
        {
            public String load(String url) throws URISyntaxException, IOException
            {
                if ("dynamic:lookandfeel.less".equals(url)) {
                    return makeLookAndFeelLess();
                }

                final URI destUri = new URI(originalLocation.getLocation()).resolve(url);
                final String destPath = destUri.getPath();
                InputStream in = null;
                try
                {
                    in = getResourceInputStream(destPath);
                    if (in == null) {
                        throw new FileNotFoundException("Could not find resource: " + destUri + ", from " + url);
                    }
                    return IOUtils.toString(in);
                }
                finally
                {
                    IOUtils.closeQuietly(in);
                }
            }

            protected abstract InputStream getResourceInputStream(String destPath);

        }

        public class PluginLoader extends BaseLoader
        {

            @Override
            protected InputStream getResourceInputStream(String destPath)
            {
                if (pluginKey == null)
                {
                    throw new IllegalStateException("Cannot load resource because could not find plugin key from transformer, resource=" + destPath);
                }

                final Plugin plugin = pluginAccessor.getPlugin(pluginKey);
                if (plugin == null)
                {
                    throw new IllegalStateException("Cannot load resource because could not find plugin plugin=" + pluginKey);
                }

                return plugin.getResourceAsStream(destPath);
            }
        }

        public class WebContextLoader extends BaseLoader
        {
            protected InputStream getResourceInputStream(String destPath)
            {
                // in future, we will need to get it from plugin.getResourceAsStream(destPath);
                // but for now in JIRA, get it straight from the servlet context
                return servletContext.getResourceAsStream(destPath);
            }
        }
    }

    static class LessCompiler
    {
        private final Scriptable sharedScope;

        LessCompiler() throws IOException
        {
            final ContextFactory cf = new ContextFactory();
            final Context cx = cf.enterContext();
            try
            {
                final ScriptableObject scope = cx.initStandardObjects();

                cx.setOptimizationLevel(9);
                loadjs(scope, cx, "setup-env.js");
                loadjs(scope, cx, "less-concat.js");

                final Function init = (Function) scope.get("init", scope);
                init.call(cx, scope, scope, new Object[0]);

                scope.sealObject();

                sharedScope = scope;

            }
            finally
            {
                Context.exit();
            }

        }

        private void loadjs(Scriptable topScope, Context cx, String name) throws IOException
        {
            final InputStream in = getClass().getResourceAsStream(name);
            if (in == null)
            {
                throw new FileNotFoundException("Could not find JS resource " + name);
            }

            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            cx.evaluateReader(topScope, reader, name, 1, null);
        }

        public String compile(String location, LessResource.BaseLoader loader, String input, boolean compress)
        {
            final ContextFactory cf = new ContextFactory();
            final Context cx = cf.enterContext();
            try
            {
                final Function runLessRun = (Function) sharedScope.get("runLessRun", sharedScope);

                Scriptable newScope = cx.newObject(sharedScope);
                newScope.setPrototype(sharedScope);
                newScope.setParentScope(null);
                
                try
                {
                    final Object[] args = {location, loader, input, compress};
                    final Object result = runLessRun.call(cx, newScope, newScope, args);
                    return Context.toString(result);
                }
                catch (JavaScriptException e)
                {
                    throw new RuntimeException(debugJsObject(cx, newScope, e.getValue()), e);
                }
            }
            finally
            {
                Context.exit();
            }

        }

        private String debugJsObject(Context cx, Scriptable scope, Object value)
        {
            if (value instanceof Scriptable) {
                Scriptable obj = (Scriptable)value;
                if (ScriptableObject.hasProperty(obj, "toSource")) {
                    Object v = ScriptableObject.getProperty(obj, "toSource");
                    if (v instanceof Function) {
                        Function f = (Function)v;
                        return String.valueOf(f.call(cx, scope, obj, new Object[0]));
                    }
                }
            }
            return String.valueOf(value);
        }
    }
}
