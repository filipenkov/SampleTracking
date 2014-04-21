package com.atlassian.jira.rest.v2.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.BadRequestWebException;
import com.atlassian.jira.rest.NotAuthorisedWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.UrlValidator;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST resource for changing the JIRA base URL.
 *
 * @since v5.0.3
 */
@Path ("settings")
@AnonymousAllowed
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
public class SettingsResource
{
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final GlobalPermissionManager globalPermissionManager;
    private final ContextI18n i18n;

    public SettingsResource(ApplicationProperties applicationProperties, JiraAuthenticationContext jiraAuthenticationContext, GlobalPermissionManager globalPermissionManager, ContextI18n i18n)
    {
        this.applicationProperties = applicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.globalPermissionManager = globalPermissionManager;
        this.i18n = i18n;
    }

    /**
     * Sets the base URL that is configured for this JIRA instance.
     *
     * @param baseURL a String containing the base URL that will be set for this JIRA instance
     *
     * @request.representation.mediaType application/json
     *
     * @request.representation.doc
     *      A string containing the base URL that will be set for this JIRA instance.
     *
     * @request.representation.example
     *      http://jira.atlassian.com/
     *
     * @response.representation.400.doc
     *      Returned if the specified base URL is not valid.
     *
     */
    @PUT
    @Path ("baseUrl")
    public void setBaseURL(String baseURL)
    {
        if (!isSysAdmin(jiraAuthenticationContext.getLoggedInUser()))
        {
            throw new NotAuthorisedWebException(ErrorCollection.of(i18n.getText("rest.settings.baseurl.permission.denied")));
        }

        if (!UrlValidator.isValid(baseURL))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("rest.settings.baseurl.invalid")));
        }

        applicationProperties.setString(APKeys.JIRA_BASEURL, baseURL);
    }

    private boolean isSysAdmin(User currentUser)
    {
        return currentUser != null && globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser);
    }
}
