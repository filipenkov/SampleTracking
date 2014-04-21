package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.util;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * Interface for the legacy Bamboo property manager. This should only be used by the upgrade tasks.
 */
public interface LegacyBambooPropertyManager
{
    PropertySet getPropertySet();
}
