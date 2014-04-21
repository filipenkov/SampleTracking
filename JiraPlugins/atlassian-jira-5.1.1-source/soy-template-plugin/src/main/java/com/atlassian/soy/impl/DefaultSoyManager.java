package com.atlassian.soy.impl;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.plugin.servlet.ServletContextFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.soy.renderer.SoyClientFunction;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyFunctionModuleDescriptor;
import com.atlassian.soy.renderer.SoyResourceModuleDescriptor;
import com.atlassian.soy.renderer.SoyServerFunction;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.SoyModule;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.parseinfo.SoyTemplateInfo;
import com.google.template.soy.shared.SoyCssRenamingMap;
import com.google.template.soy.shared.restricted.SoyFunction;
import com.google.template.soy.tofu.SoyTofu;
import com.google.template.soy.xliffmsgplugin.XliffMsgPluginModule;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSoyManager implements SoyManager
{
    public static final String JIRA_DEV_MODE = "jira.dev.mode";
    public static final String ATLASSIAN_DEV_MODE = "atlassian.dev.mode";

    private static final SoyTofu DIDNOTCOMPILE = new NullTofu();
    
    private static final Logger log = LoggerFactory.getLogger(DefaultSoyManager.class);

    private final PluginAccessor pluginAccessor;
    private final ServletContextFactory servletContextFactory;

    private final I18nResolver i18nResolver;
    private final ApplicationProperties applicationProperties;
    private final WebResourceManager webResourceManager;

    /**
     * Compiled Soy templates keyed on complete plugin-module key.
     */
    private ConcurrentMap<String, SoyTofu> cache;

    private final Injector defaultInjector;
    private final Iterable<Module> defaultModules;

    public DefaultSoyManager(PluginAccessor pluginAccessor, PluginEventManager pluginEventManager,
                             ServletContextFactory servletContextFactory, ApplicationProperties applicationProperties,
                             I18nResolver i18nResolver, WebResourceManager webResourceManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.servletContextFactory = servletContextFactory;
        this.applicationProperties = applicationProperties;
        this.i18nResolver = i18nResolver;
        this.webResourceManager = webResourceManager;

        List<Module> guiceModules = new ArrayList<Module>();
        guiceModules.add(new SoyModule());
        guiceModules.add(new XliffMsgPluginModule());
        guiceModules.add(new SpringBridgeModule());
        guiceModules.add(new OurFunctionsModule());
        guiceModules.add(new GlobalFunctionsModule(pluginAccessor));

        defaultModules = guiceModules;
        defaultInjector = Guice.createInjector(defaultModules);

        cache = new MapMaker().makeComputingMap(new Function<String, SoyTofu>()
        {
            @Override
            public SoyTofu apply(String s)
            {
                SoyTofu soyTofu = strainTofu(s);
                if (soyTofu != null)
                {
                    return soyTofu;
                }
                else {
                    return DIDNOTCOMPILE;
                }
            }
        });

        pluginEventManager.register(this);
    }

    @Override
    public SoyFileSet.Builder makeBuilder(Module... additionalModules)
    {
        Injector injector = defaultInjector;
        if (additionalModules.length > 0) {
            // creating the injector is relatively expensive, so only create one if the defaultInjector cannot be used
            injector = Guice.createInjector(Iterables.concat(defaultModules, Arrays.asList(additionalModules)));
        }

        return injector.getInstance(SoyFileSet.Builder.class);
    }

    @Override
    public void render(Appendable appendable, String completeModuleKey, String templateName, Map<String, Object> data, Map<String, Object> injectedData) throws SoyException
    {
        if (isDevMode())
        {
            cache.clear();
        }
        SoyTofu tofu = cache.get(completeModuleKey);
        if (tofu == DIDNOTCOMPILE)
        {
            // Will only occur if there is a Soy exception compiling one of the templates for
            // this module.
            throw new SoyException("Unable to compile Soy template in plugin module: " + completeModuleKey);
        }
        try {
            // TODO: modify SoyTofu to use a streaming approach to rendering
            appendable.append(tofu.newRenderer(templateName)
                    .setData(SoyDataConverter.convertToSoyMapData(data))
                    .setIjData(SoyDataConverter.convertToSoyMapData(injectedData))
                    .render());
        } catch (IOException e) {
            throw new SoyException(e);
        }
    }

    @PluginEventListener
    public void pluginModuleEnabled(PluginModuleEnabledEvent event)
    {
        cache.clear();
    }

    @PluginEventListener
    public void pluginModuleDisabled(PluginModuleDisabledEvent event)
    {
        cache.clear();
    }

    /*
     * Rebuilds the tofu that represents all the current soy web resource files registered in the plugin system.
     */
    private SoyTofu strainTofu(String completeModuleKey)
    {
        return makeBuilder(completeModuleKey).build().compileToJavaObj();
    }

    public SoyFileSet.Builder makeBuilder(String... functionModuleKeys)
    {
        // This might be simplified by having a SoyFileSetBuilderBuilder, but that would be silly.
        Set<SoyServerFunction<?>> wrappedFunctions = Sets.newHashSet();
        Set<SoyFunction> soyFunctions = Sets.newHashSet();
        Set<URL> fileSet = Sets.newHashSet();
        Set<String> alreadyAddedModules = Sets.newHashSet();

        for (String functionModuleKey : functionModuleKeys)
            addSoyResourceForModuleKey(functionModuleKey,
                    fileSet,
                    wrappedFunctions,
                    soyFunctions,
                    alreadyAddedModules);

        SoyFileSet.Builder builder = makeBuilder(new FunctionsModule(wrappedFunctions, soyFunctions));
        for (URL file : fileSet)
        {
            builder.add(file);
        }

        return builder;
    }

    private void addSoyResourceForModuleKey(String completeModuleKey, Set<URL> fileSet, Set<SoyServerFunction<?>> wrappedFunctions, Set<SoyFunction> soyFunctions, Set<String> alreadyAddedModules)
    {
        if (alreadyAddedModules.contains(completeModuleKey))
            return;

        ModuleDescriptor<?> moduleDescriptor = pluginAccessor.getEnabledPluginModule(completeModuleKey);

        if (moduleDescriptor == null)
            throw new IllegalStateException("Required plugin module " + completeModuleKey + " was either missing or disabled");

        if (moduleDescriptor instanceof WebResourceModuleDescriptor) {
            WebResourceModuleDescriptor webResourceModuleDescriptor = (WebResourceModuleDescriptor) moduleDescriptor;

            for (String dependencyModuleKey : webResourceModuleDescriptor.getDependencies())
            {
                addSoyResourceForModuleKey(dependencyModuleKey, fileSet, wrappedFunctions, soyFunctions, alreadyAddedModules);
            }
        }

        for (ResourceDescriptor resource : moduleDescriptor.getResourceDescriptors())
        {
            if (isSoyTemplate(resource))
            {
                URL url = getSoyResourceURL(moduleDescriptor, resource);
                if (url != null)
                {
                    fileSet.add(url);
                }
            }
        }

        if (moduleDescriptor instanceof SoyResourceModuleDescriptor) {
            SoyResourceModuleDescriptor soyModuleDescriptor = (SoyResourceModuleDescriptor) moduleDescriptor;

            for (SoyServerFunction<?> function : soyModuleDescriptor.getFunctions())
            {
                wrappedFunctions.add(function);
            }

            for (Object nativeFunction : soyModuleDescriptor.getNativeFunctions())
            {
                if (nativeFunction instanceof SoyFunction)
                    soyFunctions.add((SoyFunction) nativeFunction);
                else
                    log.error("Could not load Soy function " + nativeFunction + " because it is not a compatible class");
            }
        }

        alreadyAddedModules.add(completeModuleKey);
    }

    private boolean isSoyTemplate(ResourceDescriptor resource)
    {
        return resource.getLocation().endsWith(".soy");
    }

    private URL getSoyResourceURL(ModuleDescriptor moduleDescriptor, ResourceDescriptor resource)
    {
        final String sourceParam = resource.getParameter("source");
        if ("webContextStatic".equalsIgnoreCase(sourceParam))
        {
            try
            {
                return servletContextFactory.getServletContext().getResource(resource.getLocation());
            }
            catch (MalformedURLException e)
            {
                log.error("Ignoring soy resource. Could not locate soy with location: " + resource.getLocation());
                return null;
            }
        }
        return moduleDescriptor.getPlugin().getResource(resource.getLocation());
    }

    /*
        Placeholder SoyTofu implementation to go into ConcurrentMap that can't take null.
     */
    private static class NullTofu implements SoyTofu
    {
        @Override
        public String getNamespace()
        {
            return null;
        }

        @Override
        public SoyTofu forNamespace(String namespace)
        {
            return null;
        }

        @Override
        public String render(SoyTemplateInfo templateInfo, Map<String, ?> data, SoyMsgBundle msgBundle)
        {
            return null;
        }

        @Override
        public String render(SoyTemplateInfo templateInfo, SoyMapData data, SoyMsgBundle msgBundle)
        {
            return null;
        }

        @Override
        public String render(String templateName, Map<String, ?> data, SoyMsgBundle msgBundle)
        {
            return null;
        }

        @Override
        public String render(String templateName, SoyMapData data, SoyMsgBundle msgBundle)
        {
            return null;
        }

        @Override
        public boolean isCaching() {
            return false;
        }

        @Override
        public void addToCache(SoyMsgBundle soyMsgBundle, SoyCssRenamingMap soyCssRenamingMap) {
        }

        @Override
        public Renderer newRenderer(SoyTemplateInfo soyTemplateInfo) {
            return null;
        }

        @Override
        public Renderer newRenderer(String s) {
            return null;
        }
    }

    /**
     * Provides the Spring beans for the soy functions
     */
    private class SpringBridgeModule extends AbstractModule
    {
        @Override
        public void configure()
        {
            binder().bind(I18nResolver.class).toInstance(i18nResolver);
            binder().bind(ApplicationProperties.class).toInstance(applicationProperties);
            binder().bind(WebResourceManager.class).toInstance(webResourceManager);
        }
    }

    /**
     * Our custom soy functions are added here
     */
    private static class OurFunctionsModule extends AbstractModule
    {
        @Override
        public void configure()
        {
            Multibinder<SoyFunction> binder = Multibinder.newSetBinder(binder(), SoyFunction.class);
            binder.addBinding().to(ContextFunction.class);
            binder.addBinding().to(GetTextFunction.class);
            binder.addBinding().to(IncludeResourcesFunction.class);
            binder.addBinding().to(RequireResourceFunction.class);
            binder.addBinding().to(RequireResourcesForContextFunction.class);
        }
    }

    private static class GlobalFunctionsModule extends AbstractModule
    {

        private final PluginAccessor pluginAccessor;

        public GlobalFunctionsModule(PluginAccessor pluginAccessor)
        {

            this.pluginAccessor = pluginAccessor;
        }

        @Override
        public void configure()
        {
            Multibinder<SoyFunction> binder = Multibinder.newSetBinder(binder(), SoyFunction.class);
            for (SoyFunctionModuleDescriptor md : pluginAccessor.getEnabledModuleDescriptorsByClass(SoyFunctionModuleDescriptor.class))
            {
                final Object function = md.getModule();
                if (function instanceof SoyServerFunction)
                {
                    if (function instanceof SoyClientFunction)
                    {
                        binder.addBinding().toInstance(new CompositeFunctionAdaptor(function));
                    }
                    else
                    {
                        binder.addBinding().toInstance(new SoyTofuFunctionAdapter((SoyServerFunction) function));
                    }
                }
                else if (function instanceof SoyClientFunction)
                {
                    binder.addBinding().toInstance(new SoyJsSrcFunctionAdapter((SoyClientFunction) function));
                }
            }
        }
    }

    private static class FunctionsModule extends AbstractModule
    {

        private final Iterable<SoyServerFunction<?>> functions;
        private final Iterable<SoyFunction> soyFunctions;

        public FunctionsModule(Iterable<SoyServerFunction<?>> wrappedFunctions, Iterable<SoyFunction> soyFunctions) {
            this.functions = wrappedFunctions;
            this.soyFunctions = soyFunctions;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void configure()
        {
            Multibinder<SoyFunction> binder = Multibinder.newSetBinder(binder(), SoyFunction.class);

            for (SoyServerFunction function : functions)
                binder.addBinding().toInstance(new SoyTofuFunctionAdapter(function));

            for (SoyFunction soyFunction : soyFunctions)
                binder.addBinding().toInstance(soyFunction);
        }
    }

    private boolean isDevMode()
    {
       return Boolean.getBoolean(JIRA_DEV_MODE) || Boolean.getBoolean(ATLASSIAN_DEV_MODE);
    }
}