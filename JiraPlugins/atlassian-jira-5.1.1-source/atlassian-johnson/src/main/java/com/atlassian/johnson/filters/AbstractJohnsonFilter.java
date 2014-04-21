package com.atlassian.johnson.filters;

import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.config.JohnsonConfig;
import com.atlassian.johnson.event.RequestEventCheck;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.setup.SetupConfig;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Collection;

/**
 * Base class for handling error cases where the application is unavailable to handle normal requests.
 */
public abstract class AbstractJohnsonFilter implements Filter
{
    protected static final String TEXT_XML_UTF8_CONTENT_TYPE = "text/xml;charset=utf-8";

    protected FilterConfig filterConfig = null;
    protected JohnsonConfig config;

    public void init(FilterConfig filterConfig)
    {
        this.filterConfig = filterConfig;
        config = JohnsonConfig.getInstance();
    }

    /**
     * This filter checks to see if there are any application consistency errors before any pages are accessed. If there are errors then a redirect to the errors page is made
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        String alreadyFilteredKey = this.getClass().getName() + "_already_filtered";
        if (servletRequest.getAttribute(alreadyFilteredKey) != null)
        {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        else
        {
            servletRequest.setAttribute(alreadyFilteredKey, Boolean.TRUE);
        }

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        //get the URI of this request
        String servletPath = getServletPath(req);

        //Get the container for this context and run all of the configured request event checks
        JohnsonEventContainer appEventContainer = getContainerAndRunEventChecks(req);

        SetupConfig setup = config.getSetupConfig();

        //if there are application consistency events then redirect to the errors page
        if (appEventContainer.hasEvents() && !ignoreURI(servletPath))
        {
            handleError(appEventContainer, req, resp);
        }
        //if application is not setup then send to the Setup Page
        else if (!ignoreURI(servletPath) && !setup.isSetup() && !setup.isSetupPage(servletPath))
        {
            handleNotSetup(req, resp);
        }
        else
        {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    /**
     * Handles the given request for error cases when there is a Johnson {@link com.atlassian.johnson.event.Event} which
     * stops normal application functioning.
     * @param appEventContainer the JohnsonEventContainer that contains the events.
     * @param servletRequest the request being directed to the error.
     * @param servletResponse the response.
     * @throws IOException when the error cannot be handled.
     */
    protected abstract void handleError(JohnsonEventContainer appEventContainer, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException;

    /**
     * Handles the given request for cases when the application is not yet setup which
     * stops normal application functioning.
     * @param servletRequest the request being directed to the error.
     * @param servletResponse the response.
     * @throws IOException when the error cannot be handled.
     */
    protected abstract void handleNotSetup(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException;

    protected boolean ignoreURI(String uri)
    {
        return uri.equalsIgnoreCase(config.getErrorPath()) || config.isIgnoredPath(uri);
    }

    /**
     * Retrieves the current request servlet path. Deals with differences between servlet specs (2.2 vs 2.3+)
     *
     * Taken from the Webwork RequestUtils class
     *
     * @param request the request
     * @return the servlet path
     */
    protected static String getServletPath(HttpServletRequest request)
    {
        String servletPath = request.getServletPath();

        if (null != servletPath && !"".equals(servletPath))
        {
            return servletPath;
        }

        String requestUri = request.getRequestURI();
        int startIndex = request.getContextPath().equals("") ? 0 : request.getContextPath().length();
        int endIndex = request.getPathInfo() == null ? requestUri.length() : requestUri.lastIndexOf(request.getPathInfo());

        if (startIndex > endIndex)
        { // this should not happen
            endIndex = startIndex;
        }

        return requestUri.substring(startIndex, endIndex);
    }

    protected JohnsonEventContainer getContainerAndRunEventChecks(HttpServletRequest req)
    {
        //Get the container for this context
        JohnsonEventContainer appEventContainer = JohnsonEventContainer.get(filterConfig.getServletContext());
        // run all of the configured request event checks
        for (Iterator iterator = config.getRequestEventChecks().iterator(); iterator.hasNext();)
        {
            RequestEventCheck requestEventCheck = (RequestEventCheck) iterator.next();
            requestEventCheck.check(appEventContainer, req);
        }
        return appEventContainer;
    }

    protected String getStringForEvents(Collection events)
    {
        StringBuffer message = new StringBuffer();
        int i = 1;
        for (Iterator iterator = events.iterator(); iterator.hasNext(); i++)
        {
            Event event = (Event) iterator.next();
            message.append(event.getDesc());
            if(i < events.size())
            {
                message.append("\n");
            }
        }
        return message.toString();
    }

    public void destroy()
    {
    }
}
