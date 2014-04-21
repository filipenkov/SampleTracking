package com.atlassian.plugins.rest.module;

import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.google.common.base.Preconditions;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import org.apache.commons.lang.StringUtils;

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
    /**
     * Overriden servlet container to use our own {@link OsgiResourceConfig}.
     */
    private final ServletContainer servletContainer;
    private final ResourceConfigManager resourceConfigManager;

    RestDelegatingServletFilter(OsgiPlugin plugin, RestApiContext restContextPath)
    {
        resourceConfigManager = new ResourceConfigManager(plugin, plugin.getBundle());
        this.servletContainer = new JerseyOsgiServletContainer(plugin, restContextPath, resourceConfigManager);
    }

    public void init(FilterConfig config) throws ServletException
    {
        final ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(new ChainingClassLoader(RestModuleDescriptor.class.getClassLoader(), currentThreadClassLoader));
            servletContainer.init(config);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(currentThreadClassLoader);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        final ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(new ChainingClassLoader(RestModuleDescriptor.class.getClassLoader(), currentThreadClassLoader));
            servletContainer.doFilter(request, response, chain);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(currentThreadClassLoader);
        }
    }

    public void destroy()
    {
        servletContainer.destroy();
        resourceConfigManager.destroy();
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
