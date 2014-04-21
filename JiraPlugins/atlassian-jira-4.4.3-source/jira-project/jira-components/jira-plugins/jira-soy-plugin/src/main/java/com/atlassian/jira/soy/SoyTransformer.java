package com.atlassian.jira.soy;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.SoyModule;
import com.google.template.soy.jssrc.SoyJsSrcOptions;
import com.google.template.soy.shared.restricted.SoyFunction;
import com.google.template.soy.xliffmsgplugin.XliffMsgPluginModule;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.4
 */
public class SoyTransformer implements WebResourceTransformer
{
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public SoyTransformer(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public DownloadableResource transform(Element element, ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
        return new SoyResource(nextResource, location.getLocation());
    }

    private class SoyResource extends AbstractStringTransformedDownloadableResource
    {
        private final String location;

        private SoyResource(DownloadableResource originalResource, String location)
        {
            super(originalResource);
            this.location = location;
        }

        @Override
        public String getContentType()
        {
            return "text/javascript";
        }

        @Override
        protected String transform(String originalContent)
        {
            List<Module> guiceModules = new ArrayList<Module>();
            guiceModules.add(new SoyModule());
            guiceModules.add(new XliffMsgPluginModule());
            guiceModules.add(new PicoBridgeModule());
            guiceModules.add(new OurFunctionsModule());

            final Injector injector = Guice.createInjector(guiceModules);

            SoyFileSet.Builder sfsBuilder = injector.getInstance(SoyFileSet.Builder.class);

            SoyJsSrcOptions jsSrcOptions = new SoyJsSrcOptions();
            jsSrcOptions.setShouldGenerateJsdoc(false);

            sfsBuilder.add(originalContent, location);

            SoyFileSet sfs = sfsBuilder.build();

            final List<String> output = sfs.compileToJsSrc(jsSrcOptions, null);
            if (output.size() != 1)
            {
                throw new IllegalStateException("Did not manage to compile soy template size=" + output.size());
            }
            return output.get(0);
        }
    }

    private class PicoBridgeModule extends AbstractModule
    {
        @Override
        public void configure()
        {
            binder().bind(JiraAuthenticationContext.class).toInstance(jiraAuthenticationContext);
        }
    }

    private static class OurFunctionsModule extends AbstractModule
    {
        @Override
        public void configure()
        {
            Multibinder<SoyFunction> binder = Multibinder.newSetBinder(binder(), SoyFunction.class);
            binder.addBinding().to(ContextFunction.class);
            binder.addBinding().to(GetTextFunction.class);
        }
    }
}
