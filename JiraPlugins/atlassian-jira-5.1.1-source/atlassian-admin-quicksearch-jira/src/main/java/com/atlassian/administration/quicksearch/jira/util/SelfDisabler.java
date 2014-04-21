package com.atlassian.administration.quicksearch.jira.util;

import com.atlassian.administration.quicksearch.util.VersionVerifier;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Disables this plugin in JIRA below 5.1, as it runs it's own bundled admin quicksearch.
 *
 * @since 1.0
 */
public class SelfDisabler implements InitializingBean
{
    private static final Logger log = LoggerFactory.getLogger(SelfDisabler.class);

    private static final String PLUGIN_KEY = "com.atlassian.administration.atlassian-admin-quicksearch-jira";

    private final PluginController pluginController;
    private final VersionVerifier versionVerifier;
    private final EventPublisher eventPublisher;

    public SelfDisabler(EventPublisher eventPublisher, PluginController pluginController, VersionVerifier versionVerifier)
    {
        this.eventPublisher = eventPublisher;
        this.pluginController = pluginController;
        this.versionVerifier = versionVerifier;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (isVersionWithBundledQuicksearch())
        {
            eventPublisher.register(this);
        }
    }

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent pluginEnabledEvent)
    {

        if (pluginEnabledEvent.getPlugin().getKey().equals(PLUGIN_KEY))
        {
            log.warn("Disabling the '%s' plugin, it should not run in JIRA 4.4 to 5.0");
            pluginController.disablePlugin(PLUGIN_KEY);
        }
    }

    private boolean isVersionWithBundledQuicksearch()
    {
        // [4.4, 5.0] are the versions where admin quicksearch was bundled
        return versionVerifier.isGreaterThanOrEqualTo(4,4) && versionVerifier.isLessThanOrEqualTo(5,0);
    }

}
