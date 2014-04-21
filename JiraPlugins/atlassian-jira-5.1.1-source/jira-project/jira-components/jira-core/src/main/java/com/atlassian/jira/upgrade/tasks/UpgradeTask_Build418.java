package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.FlushablePortletConfigurationStore;
import com.atlassian.jira.portal.OfbizPortletConfigurationStore;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts legacy portlet keys (prior to JIRA 3.0) over to the new (legacy) format using plugin keys
 *
 * @since v4.0
 */
public class UpgradeTask_Build418 extends AbstractUpgradeTask
{
    private static final String SYSTEM_PORTLET_PACKAGE = "com.atlassian.jira.plugin.system.portlets";
    private final OfBizDelegator ofBizDelegator;
    private static final String[] LEGACY_KEYS =
            new String[] {
                    "PROJECTS",
                    "PROJECTTABLE",
                    "PROJECT",
                    "PROJECTSTATS",
                    "SAVEDFILTERS",
                    "ASSIGNEDTOME",
                    "INPROGRESS",
                    "SEARCHREQUEST",
                    "INTRODUCTION",
                    "USERISSUES",
                    "ADMIN" };

    public UpgradeTask_Build418(final OfBizDelegator ofBizDelegator)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        final List<String> legacyKeys = Arrays.asList(LEGACY_KEYS);

        final Map<Long, String> portletIdToKey = new HashMap<Long, String>();
        //get all the portletconfigurations that are using one of the old keys.
        final OfBizListIterator iter = ofBizDelegator.findListIteratorByCondition(OfbizPortletConfigurationStore.TABLE,
                new EntityExpr(OfbizPortletConfigurationStore.Columns.PORTLETKEY, EntityOperator.IN, legacyKeys));
        try
        {
            GenericValue portletConfigurationGV = iter.next();
            while (portletConfigurationGV != null)
            {
                final String portletKey = portletConfigurationGV.getString(OfbizPortletConfigurationStore.Columns.PORTLETKEY);
                final Long portletId = portletConfigurationGV.getLong(OfbizPortletConfigurationStore.Columns.ID);
                portletIdToKey.put(portletId, portletKey);
                portletConfigurationGV = iter.next();
            }
        }
        finally
        {
            iter.close();
        }

        //then go through and update them to use the new keys!
        for (Map.Entry<Long, String> portletEntry : portletIdToKey.entrySet())
        {
            final String newPortletKey = SYSTEM_PORTLET_PACKAGE + ":" + portletEntry.getValue().toLowerCase();
            ofBizDelegator.bulkUpdateByPrimaryKey(OfbizPortletConfigurationStore.TABLE,
                    MapBuilder.singletonMap(OfbizPortletConfigurationStore.Columns.PORTLETKEY, newPortletKey),
                    CollectionBuilder.newBuilder(portletEntry.getKey()).asList());
        }

        //just in case any other upgrade tasks already populated the PortletConfigurationStore cache we clear it here.
        getFlushablePortletConfigurationStore().flush();
    }

    FlushablePortletConfigurationStore getFlushablePortletConfigurationStore()
    {
        return (FlushablePortletConfigurationStore) ComponentManager.getComponent(PortletConfigurationStore.class);
    }

    @Override
    public String getShortDescription()
    {
        return "Converting legacy portlet keys (e.g. INTRODUCTION) to the new format (e.g. com.atlassian.jira.plugin.system.portlets:introduction).";
    }

    @Override
    public String getBuildNumber()
    {
        return "418";
    }
}