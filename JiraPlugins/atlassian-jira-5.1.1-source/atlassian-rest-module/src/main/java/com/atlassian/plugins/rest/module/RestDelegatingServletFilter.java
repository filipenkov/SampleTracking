package com.atlassian.plugins.rest.module;

import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.google.common.base.Preconditions;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * A delegating servlet that swaps the context class loader with a {@link ChainingClassLoader} that delegates to both
 * the class loader of the {@link RestModuleDescriptor} and the current context class loader.
 */
class RestDelegatingServletFilter implements Filter
{
    private static final Logger log = LoggerFactory.getLogger(RestDelegatingServletFilter.class);
    /**
     * Helper object for installing and un-installing the SLF4J bridge.
     */
    private static final Slf4jBridge.Helper SLF4J_BRIDGE = Slf4jBridge.createHelper();

    /**
     * Overriden servlet container to use our own {@link OsgiResourceConfig}.
     */
    private final ServletContainer servletContainer;
    private final ResourceConfigManager resourceConfigManager;

    /**
     * This class loader is set as the thread context class loader whenever we are calling into Jersey. We need to reuse
     * the same class loader every time because when this filter is initialised it sets up the JUL->SLF4J bridge (the
     * same one that allows ).
     *
     * The thread context class loader (TCCL) is set to this class loader when calling into Jersey. We keep a reference
     * to it as opposed to creating a new one in every invocation because Jersey uses java.util.logging, which on
     * Tomcat is implemented by JULI.
     */
    private volatile ClassLoader chainingClassLoader;

    RestDelegatingServletFilter(OsgiPlugin plugin, RestApiContext restContextPath)
    {
        resourceConfigManager = new ResourceConfigManager(plugin, plugin.getBundle());
        this.servletContainer = new JerseyOsgiServletContainer(plugin, restContextPath, resourceConfigManager);
    }

    public void init(FilterConfig config) throws ServletException
    {
        initChainingClassLoader();
        initServletContainer(config);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(chainingClassLoader);
        try
        {
            servletContainer.doFilter(request, response, chain);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(currentThreadClassLoader);
        }
    }

    public void destroy()
    {
        destroyServletContainer();
        resourceConfigManager.destroy();
    }

    private void initChainingClassLoader()
    {
        chainingClassLoader = new ChainingClassLoader(RestModuleDescriptor.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
    }

    private void initServletContainer(FilterConfig config) throws ServletException
    {
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(chainingClassLoader);
        try
        {
            SLF4J_BRIDGE.install();
            servletContainer.init(config);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(currentThreadClassLoader);
        }
    }

    private void destroyServletContainer()
    {
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(chainingClassLoader);
        try
    {
        servletContainer.destroy();
            SLF4J_BRIDGE.uninstall();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(currentThreadClassLoader);
        }
    }

    /**
     *
     */
    private static class JerseyOsgiServletContainer extends ServletContainer
    {
        private final OsgiPlugin plugin;
        private final RestApiContext restApiContext;
        private final ResourceConfigManager resourceConfigManager;

        private static final String PARAM_EXTENSION_FILTER_EXCLUDES = "extension.filter.excludes";

        public JerseyOsgiServletContainer(OsgiPlugin plugin, RestApiContext restApiContext, final ResourceConfigManager resourceConfigManager)
        {
            this.resourceConfigManager = Preconditions.checkNotNull(resourceConfigManager);
            this.plugin = Preconditions.checkNotNull(plugin);
            this.restApiContext = Preconditions.checkNotNull(restApiContext);
        }

        @Override
        protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props, WebConfig webConfig) throws ServletException
        {
            // get the excludes parameter: support the old param name for Atlassian Gadgets
            final String deprecatedName = "com.atlassian.plugins.rest.module.filter.ExtensionJerseyFilter" + "#excludes";
            final String excludeParam = webConfig.getInitParameter(deprecatedName) != null ?
                    webConfig.getInitParameter(deprecatedName) : webConfig.getInitParameter(PARAM_EXTENSION_FILTER_EXCLUDES);

            final String[] excludes = StringUtils.split(excludeParam, " ,;");

            return resourceConfigManager.createResourceConfig(props, excludes, restApiContext.getPackages());
        }

        @Override
        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws IOException, ServletException
        {
            // This is overridden so that we can change the base URI, and keep the context path the same
            String baseUriPath;
            if (request.getRequestURI().contains(restApiContext.getPathToLatest()))
            {
                baseUriPath = request.getContextPath() + restApiContext.getPathToLatest();
                // TODO Remove this when REST-159 is fixed
                // Added logging to help find the root cause of REST-159
                log.debug("Setting base uri for REST to 'latest'");
                log.debug("Incoming URI : " + request.getRequestURI());
            }
            else
            {
                baseUriPath = request.getContextPath() + restApiContext.getPathToVersion();
            }
            final UriBuilder absoluteUriBuilder = UriBuilder.fromUri(request.getRequestURL().toString());

            final URI baseUri = absoluteUriBuilder.replacePath(baseUriPath).path("/").build();

            final URI requestUri = absoluteUriBuilder.replacePath(request.getRequestURI()).replaceQuery(
                    request.getQueryString()).build();

            service(baseUri, requestUri, request, response);
        }

        @Override
        protected void initiate(ResourceConfig resourceConfig, WebApplication webApplication)
        {
            webApplication.initiate(resourceConfig, new OsgiComponentProviderFactory(resourceConfig, plugin));
        }
    }
}
