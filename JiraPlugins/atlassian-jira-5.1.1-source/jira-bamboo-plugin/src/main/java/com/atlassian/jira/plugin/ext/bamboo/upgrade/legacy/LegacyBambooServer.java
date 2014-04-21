package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy;

import java.util.Set;

/**
 * Interface for a legacy Bamboo server configured in JIRA. This should only be used by the upgrade tasks.
 * Some methods supported in the past have been removed as we don't need them for the upgrade task.
 */
public interface LegacyBambooServer
{
    int getId();
    String getName();
    String getDescription();
    String getHost();
    String getUsername();
    String getPassword();
    Set<String> getAssociatedProjectKeys();
}
