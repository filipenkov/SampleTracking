package com.atlassian.jira.startup;

/**
 * JIRA used to have context-listeners that did things on startup. These have been converted from ServletContextListener
 * to JiraLauncher. Instead of contextInitialized and contextDestroyed they now implement start() and stop(). This allows
 * us to decouple tenant creation from context creation and gives us further flexibility about when exactly bits of the
 * start up occur.
 * @since v4.3
 */
public interface JiraLauncher
{
    /**
     * Called during JIRA "startup". In the multitenant world startup is more complicated and broken into multiple chunks.
     * There is the stuff that happens when the servlet container first comes up, there is stuff that happens when
     * the System Tenant is being created (also during servlet context creation), and there is stuff when further tenants
     * are created (dynamically, at some point often well after startup).
     *
     * The logic for ordering all of this will be handled by the DefaultJiraLauncher.
     * @see DefaultJiraLauncher
     */
    void start();

    /**
     * Called when JIRA is shutting down. Just like startup this can mean either per-tenant shutdown or servlet-wide
     * shutdown. The logic of what exactly happens when is encapsulated in the DefaultJiraLauncher.
     *
     * @see DefaultJiraLauncher
     */
    void stop();
}
