package com.atlassian.core.ofbiz.util;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class OFBizPropertyUtils
{
    public static PropertySet getPropertySet(GenericValue gv)
    {
        HashMap ofbizArgs = new HashMap();
        ofbizArgs.put("delegator.name", gv.delegatorName);
        ofbizArgs.put("entityName", gv.entityName);
        ofbizArgs.put("entityId", gv.getLong("id"));
        return PropertySetManager.getInstance("ofbiz", ofbizArgs, OFBizPropertyUtils.class.getClassLoader());
    }

    public static void removePropertySet(GenericValue gv)
    {
        PropertySet ps = getPropertySet(gv);
        Collection keys = ps.getKeys();
        for (Iterator iterator = keys.iterator(); iterator.hasNext();)
        {
            String key = (String) iterator.next();
            ps.remove(key);
        }
    }
}
