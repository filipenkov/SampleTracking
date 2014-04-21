package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.manager;

import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.LegacyBambooServer;

/**
 * Interface for the legacy Bamboo server manager. This should only be used by the upgrade tasks.
 */
public interface LegacyBambooServerManager
{
    /**
     *
     * @return Whether any Bamboo servers have been defined
     */
    public boolean hasServers();

    /**
     *
     * @return Collection of Bamboo server definitions for the Jira instance
     */
    public Iterable<LegacyBambooServer> getServers();

    /**
     * Test if server passed as an argument is default "catch-all" server.
     *
     * @param server Bamboo server definition to be tested
     * @return True if Bamboo server definition represents default "catch-all" server
     */
    boolean isDefaultServer(LegacyBambooServer server);
}
