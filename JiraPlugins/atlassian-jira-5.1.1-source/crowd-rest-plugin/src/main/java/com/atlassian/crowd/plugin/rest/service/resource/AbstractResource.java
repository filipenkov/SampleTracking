package com.atlassian.crowd.plugin.rest.service.resource;

import com.atlassian.crowd.plugin.rest.service.util.AuthenticatedApplicationUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

public class AbstractResource
{
    protected static final String START_INDEX_PARAM = "start-index";
    protected static final String MAX_RESULTS_PARAM = "max-results";
    protected static final String DEFAULT_SEARCH_RESULT_SIZE = "1000";

    @Context
    protected UriInfo uriInfo;

    @Context
    protected  HttpServletRequest request;

    protected String getApplicationName()
    {
        String applicationName = AuthenticatedApplicationUtil.getAuthenticatedApplication(request);
        if (applicationName == null)
        {
            throw new IllegalStateException("Application name was not set as an attribute in the HttpSession");
        }
        else
        {
            return applicationName;
        }
    }

    protected URI getBaseUri()
    {
        return uriInfo.getBaseUri();
    }
}
