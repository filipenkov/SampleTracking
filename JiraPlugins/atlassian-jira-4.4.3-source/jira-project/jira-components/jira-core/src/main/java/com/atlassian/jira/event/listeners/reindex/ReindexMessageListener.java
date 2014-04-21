package com.atlassian.jira.event.listeners.reindex;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.plugins.PluginReindexHelper;
import com.atlassian.jira.web.action.admin.plugins.PluginReindexHelperImpl;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;

/**
 * Adds a re-index notification whenever a plugin module requiring reindex is enabled.
 *
 * @since v4.3
 */
public class ReindexMessageListener implements Startable
{
    private final PluginReindexHelper pluginReindexHelper;
    private final JiraAuthenticationContext authenticationContext;
    private final ReindexMessageManager reindexMessageManager;
    private final PluginAccessor pluginAccessor;
    private final EventPublisher eventPublisher;

    public ReindexMessageListener(JiraAuthenticationContext authenticationContext, PluginAccessor pluginAccessor, ReindexMessageManager reindexMessageManager, EventPublisher eventPublisher) {
        this.pluginAccessor = pluginAccessor;
        this.reindexMessageManager = reindexMessageManager;
        this.eventPublisher = eventPublisher;
        this.pluginReindexHelper = new PluginReindexHelperImpl(this.pluginAccessor);
        this.authenticationContext = authenticationContext;
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public synchronized void pluginModuleEnabled(final PluginModuleEnabledEvent pmEnabledEvent)
    {
        if (pluginReindexHelper.doesEnablingPluginModuleRequireMessage(pmEnabledEvent.getModule().getCompleteKey()))
        {
            reindexMessageManager.pushMessage(authenticationContext.getLoggedInUser(),"admin.notifications.task.plugins");
        }
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
    }
}
