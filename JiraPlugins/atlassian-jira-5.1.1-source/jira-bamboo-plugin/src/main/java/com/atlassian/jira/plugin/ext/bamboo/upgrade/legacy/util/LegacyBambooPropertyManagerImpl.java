package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.util;

import java.util.HashMap;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * Implementation for the legacy Bamboo property manager. This should only be used by the upgrade tasks.
 */
public class LegacyBambooPropertyManagerImpl implements LegacyBambooPropertyManager
{
    private PropertySet propertySet;

    public LegacyBambooPropertyManagerImpl()
    {
        loadPropertySet();
    }

    /**
     * Get the property set
     */
    public PropertySet getPropertySet()
    {
        return propertySet;
    }

    /**
     * Locate PropertySet using PropertyStore for this sequenceName/sequenceId mapping.
     */
    protected void loadPropertySet()
    {
        HashMap psArgs = new HashMap();
        psArgs.put("delegator.name", "default");
        psArgs.put("entityName", "BambooServerProperties");
        psArgs.put("entityId", new Long(1L));

        propertySet = PropertySetManager.getInstance("ofbiz", psArgs);
    }
}
