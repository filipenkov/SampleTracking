package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.util;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * Implementation for the legacy Bamboo server id generator. This should only be used by the upgrade tasks.
 */
public class LegacyBambooServerIdGeneratorImpl implements LegacyBambooServerIdGenerator
{
    public static final String CFG_ROOT = "bamboo.config";
    public static final String CFG_SERVER_NEXT_ID = CFG_ROOT + ".server.nextId";

    private final PropertySet propertySet;

    public LegacyBambooServerIdGeneratorImpl(final LegacyBambooPropertyManager propertyManager)
    {
        this.propertySet = propertyManager.getPropertySet();
    }

    public int next()
    {
        int nextId = 1;
        if (propertySet.exists(CFG_SERVER_NEXT_ID))
        {
            nextId = propertySet.getInt(CFG_SERVER_NEXT_ID);
        }
        propertySet.setInt(CFG_SERVER_NEXT_ID, nextId + 1);
        return nextId;
    }

}
