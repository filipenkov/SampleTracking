package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Condition to determine if trackback receiving is enabled.
 *
 * @since v4.4
 */
public class IsTrackBackReceivingEnabled implements Condition
{
    private final ApplicationProperties applicationProperties;

    public IsTrackBackReceivingEnabled(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {

        return applicationProperties.getOption(APKeys.JIRA_OPTION_TRACKBACK_RECEIVE);
    }
}
