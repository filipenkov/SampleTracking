package com.atlassian.gadgets.renderer.internal.guice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.atlassian.gadgets.LocalGadgetSpecProvider;
import com.atlassian.gadgets.opensocial.spi.Whitelist;
import com.atlassian.gadgets.renderer.internal.AtlassianContainerConfig;
import com.atlassian.gadgets.renderer.internal.cache.ClearableCacheProvider;
import com.atlassian.gadgets.renderer.internal.http.HttpClientFetcher;
import com.atlassian.gadgets.renderer.internal.local.LocalGadgetSpecFactory;
import com.atlassian.gadgets.renderer.internal.rewrite.AtlassianGadgetsContentRewriter;
import com.atlassian.gadgets.renderer.internal.servlet.TrustedAppMakeRequestHandler;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;

import org.apache.shindig.auth.SecurityTokenDecoder;
import org.apache.shindig.common.ContainerConfig;
import org.apache.shindig.common.cache.CacheProvider;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.gadgets.DefaultGadgetSpecFactory;
import org.apache.shindig.gadgets.GadgetSpecFactory;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.preload.HttpPreloader;
import org.apache.shindig.gadgets.preload.Preloader;
import org.apache.shindig.gadgets.render.RenderingContentRewriter;
import org.apache.shindig.gadgets.rewrite.ContentRewriter;
import org.apache.shindig.gadgets.rewrite.lexer.DefaultContentRewriter;
import org.apache.shindig.gadgets.servlet.MakeRequestHandler;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Injects everything from the shindig.properties and atlassian-gadgets.properties as a Named value and sets up
 * custom components to override the default Shindig implementations.
 */
public class ShindigModule extends AbstractModule
{
    private final static String SHINDIG_PROPERTIES = "shindig.properties";
    private final static String AG_PROPERTIES = "atlassian-gadgets.properties";

    private final Properties properties;
    private final SecurityTokenDecoder decoder;
    private final Iterable<LocalGadgetSpecProvider> localGadgetSpecProviders;
    private final Whitelist whitelist;
    private final ClearableCacheProvider clearableCacheProvider;

    public ShindigModule(SecurityTokenDecoder decoder,
            Iterable<LocalGadgetSpecProvider> localGadgetSpecProviders,
            Whitelist whitelist, ClearableCacheProvider clearableCacheProvider)
    {
        this.decoder = decoder;
        this.localGadgetSpecProviders = localGadgetSpecProviders;
        this.whitelist = whitelist;
        this.clearableCacheProvider = clearableCacheProvider;

        properties = new Properties();
        loadPropertiesFrom(SHINDIG_PROPERTIES, properties);
        loadPropertiesFrom(AG_PROPERTIES, properties);
    }

    private void loadPropertiesFrom(String propertiesFile, Properties properties)
    {
        InputStream is = null;
        try
        {
            is = ResourceLoader.openResource(propertiesFile);
            properties.load(is);
        }
        catch (IOException e)
        {
            throw new CreationException(Arrays.asList(
                    new Message("Unable to load properties: " + propertiesFile)));
        }
        finally
        {
            closeQuietly(is);
        }
    }

    @Override
    protected void configure()
    {
        Names.bindProperties(this.binder(), properties);
        
        /** bind our implementations that override the default shindig implementations */
        bind(DefaultContentRewriter.class).to(AtlassianGadgetsContentRewriter.class);
        bind(MakeRequestHandler.class).to(TrustedAppMakeRequestHandler.class);
        bind(ContainerConfig.class).to(AtlassianContainerConfig.class);
        bind(SecurityTokenDecoder.class).toInstance(decoder);
        bind(HttpFetcher.class).to(HttpClientFetcher.class);
        bind(CacheProvider.class).toInstance(clearableCacheProvider);
        bind(new TypeLiteral<Iterable<LocalGadgetSpecProvider>>() {}).toInstance(localGadgetSpecProviders);
        bind(GadgetSpecFactory.class).annotatedWith(Names.named("fallback")).to(DefaultGadgetSpecFactory.class);
        bind(GadgetSpecFactory.class).to(LocalGadgetSpecFactory.class);
        
        /** bind other shindig dependencies - copied from {@link DefaultGuiceModule} but modified to not depend on caja */ 
        ExecutorService service = Executors.newCachedThreadPool();
        bind(Executor.class).toInstance(service);
        bind(ExecutorService.class).toInstance(service);

        /** bind whitelist for the http fetcher **/
        bind(Whitelist.class).toInstance(whitelist);
        
        this.install(new XercesParseModule());

        bind(new TypeLiteral<List<ContentRewriter>>(){}).toProvider(ContentRewritersProvider.class);
        bind(new TypeLiteral<List<Preloader>>(){}).toProvider(PreloaderProvider.class);

        // We perform static injection on HttpResponse for cache TTLs.
        requestStaticInjection(HttpResponse.class);
    }


    static class ContentRewritersProvider implements Provider<List<ContentRewriter>>
    {
        private final List<ContentRewriter> rewriters;

        @Inject
        public ContentRewritersProvider(DefaultContentRewriter optimizingRewriter, 
                RenderingContentRewriter renderingRewriter)
        {
            rewriters = ImmutableList.of(optimizingRewriter, renderingRewriter);
        }

        public List<ContentRewriter> get()
        {
            return rewriters;
        }
    }

    static class PreloaderProvider implements Provider<List<Preloader>>
    {
        private final List<Preloader> preloaders;

        @Inject
        public PreloaderProvider(HttpPreloader httpPreloader)
        {
            preloaders = ImmutableList.<Preloader>of(httpPreloader);
        }

        public List<Preloader> get() {
            return preloaders;
        }
    }
}
