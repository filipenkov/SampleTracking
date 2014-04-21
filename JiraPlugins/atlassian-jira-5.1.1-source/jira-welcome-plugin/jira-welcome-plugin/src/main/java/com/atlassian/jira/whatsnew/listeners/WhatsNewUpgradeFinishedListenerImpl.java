package com.atlassian.jira.whatsnew.listeners;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.extension.JiraStartedEvent;
import com.atlassian.plugin.PluginController;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.regex.Pattern;

public class WhatsNewUpgradeFinishedListenerImpl implements WhatsNewUpgradeFinishedListener, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(WhatsNewUpgradeFinishedListenerImpl.class);

    private static Pattern PRODUCTION_VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+(\\.\\d+|-rc\\d+)?(_\\d+)?$");
    private static final String WHATSNEW_SHOW_WHATS_NEW_FLAG = "com.atlassian.jira.whatsnew.jira-whatsnew-plugin:show-whats-new-flag";
    static final String WHATSNEW_ENABLE_PROPERTY = "atlassian.dev.jira.whatsnew.show";

    private PluginController pluginController;
    private ApplicationProperties applicationProperties;
    private EventPublisher eventPublisher;

    public WhatsNewUpgradeFinishedListenerImpl(PluginController pluginController, ApplicationProperties applicationProperties, EventPublisher eventPublisher)
    {
        this.pluginController = pluginController;
        this.applicationProperties = applicationProperties;
        this.eventPublisher = eventPublisher;

        eventPublisher.register(this);
    }

    @EventListener
    public void enableAfterUpgrade(final JiraStartedEvent event)
    {
        /*
         * Don't show the dialog for development builds
         * e.g. not for 3.5-m4, 3.5-beta1, 3.5-SNAPSHOT, but for 3.5-rc2 or 3.5
         */
        String versionNumber = applicationProperties.getVersion();
        if (shouldEnableWhatsNew(versionNumber))
        {
            log.info("Enabling show-whats-new-flag for all users");
            pluginController.enablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
        }
        else
        {
            log.info("Disabling show-whats-new-flag for all users");
            pluginController.disablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
        }
    }

    private boolean shouldEnableWhatsNew(String versionNumber)
    {
        // basically the feature is disabled in dev mode unless overridden by the system property
        return (System.getProperty(WHATSNEW_ENABLE_PROPERTY) == null &&
                    (PRODUCTION_VERSION_PATTERN.matcher(versionNumber).find() && !JiraSystemProperties.isDevMode())) ||
                Boolean.getBoolean(WHATSNEW_ENABLE_PROPERTY);
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
