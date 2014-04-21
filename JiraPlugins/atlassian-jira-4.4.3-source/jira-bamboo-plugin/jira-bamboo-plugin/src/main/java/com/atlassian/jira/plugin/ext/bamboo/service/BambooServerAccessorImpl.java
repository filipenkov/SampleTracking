package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.net.Request.MethodType;
import com.atlassian.sal.api.net.ResponseException;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class BambooServerAccessorImpl implements BambooServerAccessor
{
    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final BambooContentRewriter bambooContentRewriter;
    private final BambooApplicationLinkManager bambooApplicationLinkManager;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public BambooServerAccessorImpl(BambooContentRewriter bambooContentRewriter, BambooApplicationLinkManager bambooApplicationLinkManager)
    {
        this.bambooContentRewriter = bambooContentRewriter;
        this.bambooApplicationLinkManager = bambooApplicationLinkManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    public String getHtmlFromAction(String bambooAction, Project project, Map extraParams) throws IOException, ResponseException, CredentialsRequiredException
    {
        MultiMap params = new MultiValueMap();
        ApplicationLink applicationLink = bambooApplicationLinkManager.getApplicationLink(project.getKey());

        params.put("projectKey", project.getKey());

        if (extraParams != null)
        {
            params.putAll(extraParams);
        }

        return getHtmlFromUrl(applicationLink, bambooAction, params);
    }

    public String getHtmlFromAction(String bambooAction, Project project, Iterable<String> issueKeys, Map extraParams) throws IOException, ResponseException, CredentialsRequiredException
    {
        MultiMap params = new MultiValueMap();
        ApplicationLink applicationLink = bambooApplicationLinkManager.getApplicationLink(project.getKey());

        if (applicationLink == null)
        {
            return "No Bamboo application link configured.";
        }
        
        for (String issueKey : issueKeys)
        {
            params.put("jiraIssueKey", issueKey);
        }

        if (extraParams != null)
        {
            params.putAll(extraParams);
        }

        return getHtmlFromUrl(applicationLink, bambooAction, params);
    }

    // ------------------------------------------------------------------------------------------------- Private Methods

    private String getHtmlFromUrl(ApplicationLink applicationLink, String bambooAction, MultiMap params) throws IOException, CredentialsRequiredException, ResponseException
    {
        final ApplicationLinkRequest request = applicationLink.createAuthenticatedRequestFactory().createRequest(MethodType.POST, bambooAction);

        request.addRequestParameters("enableJavascript", "false", "maxBuilds", "25");

        for (final Object o : params.entrySet())
        {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            Collection<String> values = (Collection<String>) entry.getValue();
            for (final String value : values)
            {
                request.addRequestParameters(key, value);
            }
        }

        String responseHtml = request.execute();
        return bambooContentRewriter.rewriteHtml(responseHtml, applicationLink.getDisplayUrl().toASCIIString());
    }

    private I18nHelper getI18nHelper()
    {
        return ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper();
    }

    private String getText(String i18nKey)
    {
        return getI18nHelper().getText(i18nKey);
    }
}
