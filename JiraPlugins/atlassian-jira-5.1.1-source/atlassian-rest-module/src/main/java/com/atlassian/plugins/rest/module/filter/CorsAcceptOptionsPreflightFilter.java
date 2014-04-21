package com.atlassian.plugins.rest.module.filter;

import com.atlassian.plugins.rest.common.security.CorsHeaders;
import com.atlassian.plugins.rest.common.security.jersey.CorsResourceFilter;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.util.HashSet;
import java.util.Set;

import static com.atlassian.plugins.rest.common.security.jersey.CorsResourceFilter.extractOrigin;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;

/**
 * This is a filter to force Jersey to handle OPTIONS when part of a preflight cors check.
 *
 * @since 2.6
 */
@Provider
public class CorsAcceptOptionsPreflightFilter implements ContainerRequestFilter
{
    public ContainerRequest filter(final ContainerRequest request)
    {
        if (request.getMethod().equals(HttpMethod.OPTIONS))
        {
            String origin = extractOrigin(request);
            String targetMethod = request.getHeaderValue(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD.value());
            if (targetMethod != null && origin != null)
            {
                request.setMethod(targetMethod);
                request.getProperties().put(CorsResourceFilter.CORS_PREFLIGHT_REQUESTED, "true");
            }
        }
        return request;
    }
}
