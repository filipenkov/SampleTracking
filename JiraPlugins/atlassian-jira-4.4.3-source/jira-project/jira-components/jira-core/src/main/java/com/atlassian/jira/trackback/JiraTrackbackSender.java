package com.atlassian.jira.trackback;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.trackback.DefaultTrackbackSender;
import com.atlassian.trackback.Trackback;

import java.util.HashMap;
import java.util.Map;

/**
 * This implementation will use the JIRA encoding in the trackback ping content type
 *
 * @since v3.13
 */
public class JiraTrackbackSender extends DefaultTrackbackSender
{
    private final ApplicationProperties applicationProperties;

    public JiraTrackbackSender(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    String getJiraEncoding()
    {
        return applicationProperties.getEncoding();
    }

    public Map buildHttpHeaders(final String pingUrl, final Trackback tb)
    {
        final Map ourMap = new HashMap(super.buildHttpHeaders(pingUrl, tb));

        final String encoding = getJiraEncoding();
        final String contentType = "application/x-www-form-urlencoded; charset=" + encoding;
        ourMap.put("Content-Type", contentType);
        return ourMap;
    }
}
