package com.atlassian.jira.trackback;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.trackback.Trackback;
import com.atlassian.trackback.TrackbackException;
import com.atlassian.trackback.TrackbackStore;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.servlet.http.HttpServletRequest;

/**
 * The lifecycle of this is outside of the normal PICO one so it makes Multitenancy a little bit harder. We handle
 * that by making all access to the ComponentManager lazy. That ensure that we will always be in a servlet context
 * (and thus have a tenant) when we access ComponentManager.
 */
public class OfbizTrackbackStore implements TrackbackStore
{
    public void storeTrackback(final Trackback tb, final HttpServletRequest request) throws TrackbackException
    {
        if (!getApplicationProperties().getOption(APKeys.JIRA_OPTION_TRACKBACK_RECEIVE))
        {
            throw new TrackbackException("Trackback receive disabled");
        }

        //remove leading slash
        String issueKey = request.getPathInfo().substring(1);
        try
        {
            GenericValue aboutIssue = getIssueManager().getIssue(issueKey);
            if (aboutIssue == null)
                throw new TrackbackException("Could not find issue '" + issueKey + "'");
            getTrackbackManager().storeTrackback(tb, aboutIssue);
        }
        catch (GenericEntityException e)
        {
            throw new TrackbackException(tb, e);
        }
    }

    protected IssueManager getIssueManager()
    {
        return ComponentManager.getComponent(IssueManager.class);
    }

    protected ApplicationProperties getApplicationProperties()
    {
        return ComponentManager.getComponent(ApplicationProperties.class);
    }

    protected TrackbackManager getTrackbackManager()
    {
        return ComponentManager.getComponent(TrackbackManager.class);
    }
}
