package com.atlassian.jira.plugin.viewissue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.trackback.TrackbackManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.trackback.Trackback;

import java.util.Collection;
import java.util.Map;

/**
 * Context Provider for the Trackback block
 *
 * @since v4.4
 */
public class TrackBackContextProvider implements CacheableContextProvider
{
    private static final String CONTEXT_TRACKBACKS = "trackbacks";
    private static final String CONTEXT_ISSUE = "issue";
    private static final String CONTEXT_HAS_TRACKBACKS = "hasTrackbacks";
    private static final String CONTEXT_BASEURL = "baseurl";
    private final VelocityRequestContextFactory requestContextFactory;

    private final TrackbackManager trackbackManager;

    public TrackBackContextProvider(TrackbackManager trackbackManager, VelocityRequestContextFactory requestContextFactory)
    {
        this.trackbackManager = trackbackManager;
        this.requestContextFactory = requestContextFactory;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get(CONTEXT_ISSUE);
        final Collection<Trackback> trackbacks = trackbackManager.getTrackbacksForIssue(issue.getGenericValue());

        paramsBuilder.add(CONTEXT_TRACKBACKS, trackbacks);
        paramsBuilder.add(CONTEXT_HAS_TRACKBACKS, !trackbacks.isEmpty());

        final VelocityRequestContext jiraVelocityRequestContext = requestContextFactory.getJiraVelocityRequestContext();

        paramsBuilder.add(CONTEXT_BASEURL, jiraVelocityRequestContext.getBaseUrl());

        return paramsBuilder.toMap();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }
}
