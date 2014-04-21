package com.atlassian.crowd.plugin.rest.filter;

import com.atlassian.sal.api.ApplicationProperties;
import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter which adds the REST service version to the response header.
 */
public class RestServiceVersionFilter implements Filter
{
    private static final String EMBEDDED_CROWD_VERSION_NAME = "X-Embedded-Crowd-Version";

    private final ApplicationProperties applicationProperties;
    private final Map<String, String> httpHeaders;
    private String applicationVersion;

    public RestServiceVersionFilter(final ApplicationProperties applicationProperties)
    {
        Validate.notNull(applicationProperties);
        this.applicationProperties = applicationProperties;
        this.httpHeaders = new HashMap<String, String>();
    }

    @SuppressWarnings("unchecked")
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        applicationVersion = String.format("%s/%s", applicationProperties.getDisplayName(), applicationProperties.getVersion());
        for (Enumeration<String> names = filterConfig.getInitParameterNames(); names.hasMoreElements();)
        {
            String headerName = names.nextElement();
            httpHeaders.put(headerName, filterConfig.getInitParameter(headerName));
        }
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader(EMBEDDED_CROWD_VERSION_NAME, applicationVersion);
        for (Map.Entry<String, String> entry : httpHeaders.entrySet())
        {
            response.setHeader(entry.getKey(), entry.getValue());
        }
        chain.doFilter(servletRequest, response);
    }

    public void destroy()
    {
    }
}
