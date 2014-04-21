package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.CachingPortletConfigurationStore;
import com.atlassian.jira.portal.OfbizPortletConfigurationStore;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.0
 */
public class UpgradeTask_Build402 extends AbstractUpgradeTask
{
    private static final String CHARTING_PLUGIN_PORTLET_KEY_PREFIX = "com.atlassian.jira.ext.charting";
    private static final String SYSTEM_CHARTS_PORTLET_KEY_PREFIX = "com.atlassian.jira.plugin.system.portlets";

    private static final Set<String> portletKeysToReplace = new HashSet<String>();

    private final OfBizDelegator ofBizDelegator;

    static
    {
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:createdvsresolved");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:singlefieldpie");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:timesince");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:recentlycreated");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:averageage");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:resolutiontime");
    }


    public UpgradeTask_Build402(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        Map<Long, String> portletIdToKey = new LinkedHashMap<Long, String>();
        OfBizListIterator iterator = null;
        try
        {
            //find all the portletConfigs where the portletkey is one of the portletKeys we'd like to replace.
            final EntityCondition portletKeysClause =
                    new EntityExpr(OfbizPortletConfigurationStore.Columns.PORTLETKEY, EntityOperator.IN, portletKeysToReplace);
            iterator = ofBizDelegator.findListIteratorByCondition(OfbizPortletConfigurationStore.TABLE, portletKeysClause);
            GenericValue portletConfigGV = iterator.next();
            while (portletConfigGV != null)
            {
                final String portletKey = portletConfigGV.getString(OfbizPortletConfigurationStore.Columns.PORTLETKEY);
                //our ofbiz condition should already guarantee this, but just in case
                if (portletKey.startsWith(CHARTING_PLUGIN_PORTLET_KEY_PREFIX))
                {
                    portletIdToKey.put(portletConfigGV.getLong(OfbizPortletConfigurationStore.Columns.ID), portletKey);
                }
                portletConfigGV = iterator.next();
            }
        }
        finally
        {
            if (iterator != null)
            {
                iterator.close();
            }
        }

        for (Map.Entry<Long, String> entry : portletIdToKey.entrySet())
        {
            final Long portletId = entry.getKey();
            final String portletKey = entry.getValue();
            String newPortletKey = portletKey.replace(CHARTING_PLUGIN_PORTLET_KEY_PREFIX, SYSTEM_CHARTS_PORTLET_KEY_PREFIX);
            //pie chart is a special case because it was renamed from singlefieldpie to just 'pie'
            if (portletKey.contains("singlefieldpie"))
            {
                newPortletKey = SYSTEM_CHARTS_PORTLET_KEY_PREFIX + ":pie";
            }
            ofBizDelegator.bulkUpdateByPrimaryKey(OfbizPortletConfigurationStore.TABLE,
                    MapBuilder.<String, Object>newBuilder().add(OfbizPortletConfigurationStore.Columns.PORTLETKEY, newPortletKey).toMap(),
                    CollectionBuilder.newBuilder(portletId).asList());
        }
        flushPortletConfigurationCache();
    }

    void flushPortletConfigurationCache()
    {
        //just in case there was a previous upgrade task that populated this cache, we'll flush the portletConfig store.
        //this is a bit ugly, but it seems to be the only way to flush this cache.
        final CachingPortletConfigurationStore store = (CachingPortletConfigurationStore) ComponentManager.getComponentInstanceOfType(PortletConfigurationStore.class);
        if (store != null)
        {
            store.flush();
        }
    }


    @Override
    public String getBuildNumber()
    {
        return "402";
    }

    @Override
    public String getShortDescription()
    {
        return "Charting plugin: Converting charting plugin portlets to system portlets.";
    }
}