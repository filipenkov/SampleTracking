/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.trackback;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.TextAnalyzer;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraEntityUtils;
import com.atlassian.trackback.Trackback;
import com.atlassian.trackback.TrackbackHelper;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.io.IOException;

/**
 * Sends trackback pings to URLs mentioned in JIRA text.
 */
public class TrackbackTextAnalyzer implements TextAnalyzer
{
    private static final Logger log = Logger.getLogger(TrackbackTextAnalyzer.class);

    private final TrackbackHelper helper;
    private final ApplicationProperties props;
    private final PermissionManager permissionManager;

    public TrackbackTextAnalyzer(final TrackbackHelper helper, ApplicationProperties props, PermissionManager permissionManager)
    {
        this.helper = helper;
        this.props = props;
        this.permissionManager = permissionManager;
    }

    /**
     * Analyze text for URLs and ping their trackbacks.
     *
     * @param issue     Issue in which text was found
     * @param content Plain (non-HTML) text content to search for URLs
     */
    public void analyseContent(GenericValue issue, final String content, GenericValue action)
    {
        //additional check for whether the content is public
        boolean publicContent = true;
        if (action != null)
        {
            if ("comment".equals(action.get("type")) && action.get("level") != null)
                publicContent = false;
        }

        // Dont ping if issue and the content is not visible to anybody in send public issues only mode.
        if (props.getOption(APKeys.JIRA_OPTION_TRACKBACK_SEND_PUBLIC) &&
            !(permissionManager.hasPermission(Permissions.BROWSE, issue, null) && publicContent))
        {
            return;
        }

        String htmlizedContent = TextUtils.plainTextToHtml(content);
        final String key = issue.getString("key");
        Trackback ping = new Trackback();
        ping.setBlogName("JIRA: " + JiraEntityUtils.getProject(issue).getString("name"));
        ping.setTitle("[" + key + "] " + issue.getString("summary"));
        ping.setUrl(props.getString(APKeys.JIRA_BASEURL) + "/browse/" + key);
        ping.setExcerpt(content);
        try
        {
            helper.pingTrackbacksInContent(htmlizedContent, ping);
        }
        catch (IOException e)
        {
            log.error("Error pinging trackbacks for content in issue " + key + ": ", e);
        }
    }

    public void analyseContent(GenericValue issue, final String content)
    {
        analyseContent(issue, content, null);
    }

    public void analyseContent(Issue issue, final String content, GenericValue action)
    {
        analyseContent(issue.getGenericValue(), content, action);
    }

    public void analyseContent(Issue issue, final String content)
    {
        analyseContent(issue, content, null);
    }
}