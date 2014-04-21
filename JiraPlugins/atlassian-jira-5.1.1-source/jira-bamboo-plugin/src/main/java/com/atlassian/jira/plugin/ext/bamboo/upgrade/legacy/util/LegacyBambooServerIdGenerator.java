package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.util;

/**
 * Interface for the legacy Bamboo server id generator. This should only be used by the upgrade tasks.
 */
public interface LegacyBambooServerIdGenerator
{
    int next();
}
