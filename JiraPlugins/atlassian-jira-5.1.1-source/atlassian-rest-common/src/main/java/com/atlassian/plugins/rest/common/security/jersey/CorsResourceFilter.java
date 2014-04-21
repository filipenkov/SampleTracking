package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.plugins.rest.common.security.CorsPreflightCheckCompleteException;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaults;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaultsModuleDescriptor;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.plugins.rest.common.security.CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static com.atlassian.plugins.rest.common.security.CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static com.atlassian.plugins.rest.common.security.CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static com.atlassian.plugins.rest.common.security.CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.atlassian.plugins.rest.common.security.CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static com.atlassian.plugins.rest.common.security.CorsHeaders.ACCESS_CONTROL_MAX_AGE;
import static com.atlassian.plugins.rest.common.security.CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static com.atlassian.plugins.rest.common.security.CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static com.atlassian.plugins.rest.common.security.CorsHeaders.ORIGIN;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * A filter that handles Cross-Origin Resource Sharing preflight checks and response headers.  Handles simple and preflight
 * requests.
 *
 * See spec at http://www.w3.org/TR/cors
 *
 * @since 2.6
 */
public class CorsResourceFilter implements ResourceFilter, ContainerRequestFilter, ContainerResponseFilter
{
    private static final String CORS_PREFLIGHT_FAILED = "Cors-Preflight-Failed";
    private static final String CORS_PREFLIGHT_SUCCEEDED = "Cors-Preflight-Succeeded";
    public static final String CORS_PREFLIGHT_REQUESTED = "Cors-Preflight-Requested";
    private static final Logger log = LoggerFactory.getLogger(CorsResourceFilter.class);

    private final PluginModuleTracker<CorsDefaults, CorsDefaultsModuleDescriptor> pluginModuleTracker;
    private final String allowMethod;

    public CorsResourceFilter(PluginModuleTracker<CorsDefaults, CorsDefaultsModuleDescriptor> pluginModuleTracker, String allowMethod)
    {
        this.allowMethod = allowMethod;
        this.pluginModuleTracker = pluginModuleTracker;
    }

    public ContainerRequest filter(final ContainerRequest request)
    {
        // if origin is present, match exactly or terminate
        // if any tokens are not a case-sensitive match for the whitelist, terminate
        // method = Access-Control-Request-Method, if null terminate
        // headers = Access-Control-Request-Headers values, or empty list
        // if method is not a case-sensitive match in list, terminate
        // If any of 'headers' is not a case-insensitive match for values, terminate
        // add single Access-Control-Allow-Origin with the Origin value and add Access-Control-Allow-Credentials to true
        // add Access-Control-Max-Age header in seconds
        // add Access-Control-Allow-Methods (optional)
        // add one or more Access-Control-Allow-Headers for each header to expose (optional)

        if (request.getProperties().containsKey(CORS_PREFLIGHT_REQUESTED))
        {
            Iterable<CorsDefaults> defaults = pluginModuleTracker.getModules();
            try
            {
                String origin = validateSingleOriginInWhitelist(defaults, request);
                Iterable<CorsDefaults> defaultsWithAllowedOrigin = allowsOrigin(defaults, origin);

                Response.ResponseBuilder response = Response.ok();
                validateAccessControlRequestMethod(allowMethod, request);
                Set<String> allowedRequestHeaders = getAllowedRequestHeaders(defaultsWithAllowedOrigin, origin);
                validateAccessControlRequestHeaders(allowedRequestHeaders, request);

                addAccessControlAllowOrigin(response, origin);
                conditionallyAddAccessControlAllowCredentials(response, origin, defaultsWithAllowedOrigin);
                addAccessControlMaxAge(response);
                addAccessControlAllowMethods(response, allowMethod);
                addAccessControlAllowHeaders(response, allowedRequestHeaders);

                request.getProperties().put(CORS_PREFLIGHT_SUCCEEDED, "true");
                // exceptions are the only way to return a response here in Jersey
                throw new CorsPreflightCheckCompleteException(response.build());
            }
            catch (PreflightFailedException ex)
            {
                Response.ResponseBuilder response = Response.ok();
                request.getProperties().put(CORS_PREFLIGHT_FAILED, "true");
                log.info("CORS preflight failed: " + ex.getMessage());
                throw new CorsPreflightCheckCompleteException(response.build());
            }
        }
        else
        {
            return request;
        }
    }

    public ContainerResponse filter(ContainerRequest request, ContainerResponse containerResponse)
    {
        // if origin is present, split otherwise terminate
        // if any tokens are not a case-sensitive match for the whitelist, terminate
        // add single Access-Control-Allow-Origin with the Origin value and add Access-Control-Allow-Credentials to true
        // add one or more Access-Control-Expose-Headers for each header to expose

        if (!request.getProperties().containsKey(CORS_PREFLIGHT_FAILED) &&
                !request.getProperties().containsKey(CORS_PREFLIGHT_SUCCEEDED) &&
                extractOrigin(request) != null)
        {
            Iterable<CorsDefaults> defaults = pluginModuleTracker.getModules();
            try
            {
                String origin = validateAnyOriginInListInWhitelist(defaults, request);
                Iterable<CorsDefaults> defaultsWithAllowedOrigin = allowsOrigin(defaults, origin);

                Response.ResponseBuilder response = Response.fromResponse(containerResponse.getResponse());
                addAccessControlAllowOrigin(response, origin);
                conditionallyAddAccessControlAllowCredentials(response, origin, defaultsWithAllowedOrigin);
                addAccessControlExposeHeaders(response, getAllowedResponseHeaders(defaultsWithAllowedOrigin, origin));
                containerResponse.setResponse(response.build());
                return containerResponse;
            }
            catch (PreflightFailedException ex)
            {
                log.info("Unable to add CORS headers to response: " + ex.getMessage());
            }
        }
        return containerResponse;
    }

    private void addAccessControlExposeHeaders(Response.ResponseBuilder response, Set<String> allowedHeaders)
    {
        for (String header : allowedHeaders)
        {
            response.header(ACCESS_CONTROL_EXPOSE_HEADERS.value(), header);
        }
    }

    private void addAccessControlAllowHeaders(Response.ResponseBuilder response, Set<String> allowedHeaders)
    {
        for (String header : allowedHeaders)
        {
            response.header(ACCESS_CONTROL_ALLOW_HEADERS.value(), header);
        }
    }

    private void addAccessControlAllowMethods(Response.ResponseBuilder response, String allowMethod)
    {
        response.header(ACCESS_CONTROL_ALLOW_METHODS.value(), allowMethod);
    }

    private void addAccessControlMaxAge(Response.ResponseBuilder response)
    {
        response.header(ACCESS_CONTROL_MAX_AGE.value(), 60 * 60);
    }

    private void addAccessControlAllowOrigin(Response.ResponseBuilder response, String origin)
    {
        response.header(ACCESS_CONTROL_ALLOW_ORIGIN.value(), origin);
    }

    private void conditionallyAddAccessControlAllowCredentials(Response.ResponseBuilder response, String origin, Iterable<CorsDefaults> defaultsWithAllowedOrigin)
    {
        if (anyAllowsCredentials(defaultsWithAllowedOrigin, origin))
        {
            response.header(ACCESS_CONTROL_ALLOW_CREDENTIALS.value(), "true");
        }
    }

    private void validateAccessControlRequestHeaders(Set<String> allowedHeaders, ContainerRequest request) throws PreflightFailedException
    {
        //Note: According to the spec, this should be a case-insensitive comparison
        List<String> requestedHeaders = request.getRequestHeader(ACCESS_CONTROL_REQUEST_HEADERS.value());
        requestedHeaders = requestedHeaders != null ? requestedHeaders : Collections.<String>emptyList();
        if (!allowedHeaders.containsAll(requestedHeaders))
        {
            List<String> unexpectedHeaders = newArrayList(requestedHeaders);
            unexpectedHeaders.removeAll(allowedHeaders);

            throw new PreflightFailedException("Unexpected headers in CORS request: " + unexpectedHeaders);
        }
    }

    private void validateAccessControlRequestMethod(String allowMethod, ContainerRequest request) throws PreflightFailedException
    {
        String requestedMethod = request.getHeaderValue(ACCESS_CONTROL_REQUEST_METHOD.value());
        if (!allowMethod.equals(requestedMethod))
        {
            throw new PreflightFailedException("Invalid method: " + requestedMethod);
        }
    }

    private String validateAnyOriginInListInWhitelist(Iterable<CorsDefaults> defaults, ContainerRequest request) throws PreflightFailedException
    {
        String originRaw = extractOrigin(request);
        String[] originList = originRaw.split(" ");
        for (String origin : originList)
        {
            validateOriginAsUri(origin);
            if (! Iterables.isEmpty(allowsOrigin(defaults, origin)))
            {
                return origin;
            }
        }
        throw new PreflightFailedException("Origins '" + originRaw + "' not in whitelist");
    }

    private String validateSingleOriginInWhitelist(Iterable<CorsDefaults> defaults, ContainerRequest request) throws PreflightFailedException
    {
        String origin = extractOrigin(request);
        validateOriginAsUri(origin);

        if (Iterables.isEmpty(allowsOrigin(defaults, origin)))
        {
            throw new PreflightFailedException("Origin '" + origin + "' not in whitelist");
        }
        return origin;
    }

    private void validateOriginAsUri(String origin) throws PreflightFailedException
    {
        try
        {
            URI.create(origin);
        }
        catch (IllegalArgumentException ex)
        {
            throw new PreflightFailedException("Origin '" + origin + "' is not a valid URI");
        }
    }

    public static String extractOrigin(ContainerRequest request)
    {
        return request.getHeaderValue(ORIGIN.value());
    }

    public ContainerRequestFilter getRequestFilter()
    {
        return this;
    }

    public ContainerResponseFilter getResponseFilter()
    {
        return this;
    }

    /**
     * Thrown if the preflight or simple cross-origin check process fails
     */
    private static class PreflightFailedException extends Exception
    {
        private PreflightFailedException(String message)
        {
            super(message);
        }
    }

    private static Iterable<CorsDefaults> allowsOrigin(Iterable<CorsDefaults> delegates, final String uri)
    {
        return Iterables.filter(delegates, new Predicate<CorsDefaults>()
        {
            public boolean apply(CorsDefaults delegate)
            {
                return delegate.allowsOrigin(uri);
            }
        });
    }

    private static boolean anyAllowsCredentials(Iterable<CorsDefaults> delegatesWhichAllowOrigin, final String uri)
    {
        for (CorsDefaults defs : delegatesWhichAllowOrigin)
        {
            if (defs.allowsCredentials(uri))
            {
                return true;
            }
        }
        return false;
    }


    private static Set<String> getAllowedRequestHeaders(Iterable<CorsDefaults> delegatesWhichAllowOrigin, String uri)
    {
        Set<String> result = newHashSet();
        for (CorsDefaults defs : delegatesWhichAllowOrigin)
        {
            result.addAll(defs.getAllowedRequestHeaders(uri));
        }
        return result;
    }

    private static Set<String> getAllowedResponseHeaders(Iterable<CorsDefaults> delegatesWithAllowedOrigin, String uri)
    {
        Set<String> result = newHashSet();
        for (CorsDefaults defs : delegatesWithAllowedOrigin)
        {
            result.addAll(defs.getAllowedResponseHeaders(uri));
        }
        return result;
    }

}
