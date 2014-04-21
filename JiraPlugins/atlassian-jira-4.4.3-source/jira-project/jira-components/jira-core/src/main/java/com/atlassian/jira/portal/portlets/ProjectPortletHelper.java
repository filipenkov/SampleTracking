package com.atlassian.jira.portal.portlets;

import com.atlassian.core.util.WebRequestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class that provides extra functionality for project-related portlets.
 * <p/>
 * This class is used by {@link com.atlassian.jira.portal.portlets.ProjectPortlet}
 * and {@link com.atlassian.jira.portal.portlets.ProjectsPortlet} classes.
 *
 * @see com.atlassian.jira.portal.portlets.ProjectPortlet
 * @see com.atlassian.jira.portal.portlets.ProjectsPortlet
 * @since v3.13
 */
public class ProjectPortletHelper
{
    /**
     * Uses {@link WebRequestUtils} to check if the browser is good.
     *
     * @param request HTTP request we are processing
     * @return true for good browser, false otherwise
     */
    static Boolean isGoodBrowser(final HttpServletRequest request)
    {
        return Boolean.valueOf(WebRequestUtils.isGoodBrowser(request));
    }
}
