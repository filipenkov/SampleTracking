package com.atlassian.jira.config.properties;

import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;


import java.util.HashMap;
import java.util.Map;

/**
 * Static methods to get property sets
 *
 * @since v4.4
 */
public class PropertySetUtils
{
    private final static String SEQUENCE = "jira.properties";
    private final static long ID = 1;

    private PropertySetUtils() {}

    public static PropertySet createDatabaseBackedPropertySet(OfBizConnectionFactory ofBizConnectionFactory)
    {
        final Map<String, Object> ofbizArgs = new HashMap<String, Object>();
        ofbizArgs.put("delegator.name", ofBizConnectionFactory.getDelegatorName());
        ofbizArgs.put("entityName", SEQUENCE);
        ofbizArgs.put("entityId", ID);

        final PropertySet ofbizPs = PropertySetManager.getInstance("ofbiz", ofbizArgs);
        final Map<String, Object> args = new HashMap<String, Object>();
        args.put("PropertySet", ofbizPs);
        args.put("bulkload", Boolean.TRUE);
        return PropertySetManager.getInstance("cached", args);
    }
}
