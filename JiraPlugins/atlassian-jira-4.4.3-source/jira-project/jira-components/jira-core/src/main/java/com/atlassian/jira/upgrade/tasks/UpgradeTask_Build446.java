package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.CachingPortletConfigurationStore;
import com.atlassian.jira.portal.OfbizPortletConfigurationStore;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.atlassian.jira.portal.OfbizPortletConfigurationStore.Columns;

/**
 * Removes property sets for bamboo portlets.
 *
 * @since v4.0
 */
public class UpgradeTask_Build446 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build446.class);
    private final JiraPropertySetFactory propertySetFactory;
    private final OfBizDelegator ofBizDelegator;

    public UpgradeTask_Build446(final OfBizDelegator ofBizDelegator, final JiraPropertySetFactory propertySetFactory)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.propertySetFactory = propertySetFactory;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        try
        {
            final Set<Long> portlets = new LinkedHashSet<Long>();
            OfBizListIterator iterator = null;
            try
            {
                //find all the bamboo portlet configs
                iterator = ofBizDelegator.findListIteratorByCondition(OfbizPortletConfigurationStore.TABLE, new EntityExpr(Columns.PORTLETKEY, EntityOperator.IN,
                        CollectionBuilder.newBuilder("com.atlassian.jira.plugin.ext.bamboo:bambooStatus", "com.atlassian.jira.plugin.ext.bamboo:buildGraph").asList()));
                GenericValue portletConfigGV = iterator.next();
                while (portletConfigGV != null)
                {
                    portlets.add(portletConfigGV.getLong(Columns.ID));
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

            for (Long portletId : portlets)
            {
                removePropertySet(portletId);
            }

            flushPortletConfigurationCache();
        }
        catch (RuntimeException e)
        {
            log.error("Error while trying to remove property sets for legacy bamboo portlets. Ignoring since the legacy portlet bridge gadget should be able to handle this stale data. This is cleanup only", e);
        }
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

    private void removePropertySet(final Long portletId)
    {
        final PropertySet livePropertySet = propertySetFactory.buildNoncachingPropertySet(OfbizPortletConfigurationStore.TABLE, portletId);
        @SuppressWarnings ("unchecked")
        final Collection<String> keys = livePropertySet.getKeys();
        for (final String propertyKey : keys)
        {
            livePropertySet.remove(propertyKey);
        }
    }

    @Override
    public String getShortDescription()
    {
        return "Remove propertysets for legacy bamboo portlets.";
    }

    @Override
    public String getBuildNumber()
    {
        return "446";
    }
}