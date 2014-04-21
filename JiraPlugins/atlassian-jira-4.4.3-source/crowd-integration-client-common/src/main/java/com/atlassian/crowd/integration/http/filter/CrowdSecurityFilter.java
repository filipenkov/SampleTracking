package com.atlassian.crowd.integration.http.filter;

import com.atlassian.crowd.integration.http.CrowdHttpAuthenticator;
import com.atlassian.crowd.service.client.ClientProperties;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter manages protecting a <code>web.xml</code> <code>url-pattern</code>. If the requesting user does
 * not have a valid token, they will be redirected to the authentication path specified in the <code>crowd.properties</code>
 * configuration file. Additional values are stored to the user's session such as their originally requested
 * URL should their authentication be found invalid.
 */
public class CrowdSecurityFilter implements Filter
{
    /**
     * Create a static reference to the logger.
     */
    private static final Logger logger = Logger.getLogger(CrowdSecurityFilter.class);

    private static final String BASE_NAME = "com.atlassian.crowd.security";
    private static final String FILTER_RUN = BASE_NAME + ".FILTER_RUN";

    /**
     * The session key stored as a <code>String<code>, is the requested secure url before redirect to the authentication
     * page.
     */
    public static final String ORIGINAL_URL = BASE_NAME + ".ORIGINAL_URL";

    private final CrowdHttpAuthenticator httpAuthenticator;
    private final ClientProperties clientProperties;

    /**
     * Constructs a CrowdSecurityFilter.
     *
     * @param httpAuthenticator CrowdHttpAuthenticator
     * @param clientProperties ClientProperties
     */
    public CrowdSecurityFilter(CrowdHttpAuthenticator httpAuthenticator, ClientProperties clientProperties)
    {
        this.httpAuthenticator = httpAuthenticator;
        this.clientProperties = clientProperties;
    }

    /**
     * Configures the filter.
     *
     * @param filterConfig the {@link javax.servlet.FilterConfig} to use.
     * @throws javax.servlet.ServletException {@link javax.servlet.Filter} related problems.
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    /**
     * Shuts down the filter.
     */
    public void destroy()
    {
    }

    /**
     * Executes the filter.
     *
     * @param servletRequest  the {@link javax.servlet.ServletRequest} to use.
     * @param servletResponse the {@link javax.servlet.ServletResponse} to use.
     * @param filterChain     the {@link javax.servlet.FilterChain} to use.
     * @throws java.io.IOException I/O related problems.
     * @throws ServletException    {@link javax.servlet.Servlet} related problems.
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {

        Date filterStart = new Date();


        boolean isValidated = false;

        try
        {
            Boolean filterRun = (Boolean) servletRequest.getAttribute(FILTER_RUN);

            if (filterRun != null && filterRun.booleanValue())
            {
                filterChain.doFilter(servletRequest, servletResponse);

            }
            else
            {

                // do the rest of the filter chain
                servletRequest.setAttribute(FILTER_RUN, Boolean.TRUE);

                HttpServletRequest request = (HttpServletRequest) servletRequest;
                HttpServletResponse response = (HttpServletResponse) servletResponse;

                isValidated = httpAuthenticator.isAuthenticated(request, response);

                // get the request URL
                StringBuffer originalURL = request.getRequestURL();

                boolean foundParameter = false;
                if (request.getParameterMap().size() > 0)
                {
                    originalURL.append("?");

                    Enumeration params = request.getParameterNames();
                    for (; params.hasMoreElements();)
                    {
                        if (foundParameter == false)
                        {
                            foundParameter = true;
                        }
                        else
                        {
                            originalURL.append("&");
                        }

                        String name = (String) params.nextElement();
                        String values[] = request.getParameterValues(name);

                        for (int i = 0; i < values.length; i++)
                        {
                            originalURL.append(name).append("=").append(values[i]);
                        }
                    }
                }

                if (!isValidated)
                {

                    logger.info("Requesting URL is: " + originalURL);
                    request.getSession().setAttribute(ORIGINAL_URL, originalURL.toString());

                    logger.info("Authentication is not valid, redirecting to: "
                            + clientProperties.getApplicationAuthenticationURL());

                    response.sendRedirect(clientProperties.getApplicationAuthenticationURL());

                }
                else
                {
                    request.removeAttribute(ORIGINAL_URL);

                    filterChain.doFilter(servletRequest, servletResponse);

                    if (servletRequest.getAttribute(FILTER_RUN) != null)
                    {
                        servletRequest.removeAttribute(FILTER_RUN);
                    }
                }
            }

        }
        catch (Exception e)
        {
            logger.fatal(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }
        finally
        {
            if (logger.isDebugEnabled())
            {
                Date now = new Date();
                logger.debug("Filter time to run: " + (now.getTime() - filterStart.getTime()) + " ms");
            }
        }
    }
}
