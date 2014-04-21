package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.FlushablePortletConfigurationStore;
import com.atlassian.jira.portal.OfbizPortletConfigurationStore;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.util.LegacyPortletUpgradeTask;
import com.atlassian.jira.upgrade.util.LegacyPortletUpgradeTaskFactory;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.collect.Sized;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Convert legacy portlets over to gadgets.
 *
 * @since v4.0
 */
public class UpgradeTask_Build438 extends AbstractUpgradeTask
{
    static final String COM_ATLASSIAN_JIRA_PLUGIN_SYSTEM_PORTLETS_TEXT = "com.atlassian.jira.plugin.system.portlets:text";
    static final String COM_ATLASSIAN_JIRA_GADGETS_TEXT_GADGET = "com.atlassian.jira.gadgets:text-gadget";

    private static final Logger log = Logger.getLogger(UpgradeTask_Build438.class);

    private final JiraPropertySetFactory propertySetFactory;
    private final LegacyPortletUpgradeTaskFactory legacyPortletUpgradeTaskFactory;
    private final OfBizDelegator ofBizDelegator;
    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;

    public UpgradeTask_Build438(final OfBizDelegator ofBizDelegator, final JiraPropertySetFactory propertySetFactory,
            final LegacyPortletUpgradeTaskFactory legacyPortletUpgradeTaskFactory, final PluginAccessor pluginAccessor,
            final PluginController pluginController)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.propertySetFactory = propertySetFactory;
        this.legacyPortletUpgradeTaskFactory = legacyPortletUpgradeTaskFactory;
        this.pluginAccessor = pluginAccessor;
        this.pluginController = pluginController;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Map<String, LegacyPortletUpgradeTask> portletToUpgradeTasks = legacyPortletUpgradeTaskFactory.createPortletToUpgradeTaskMapping();

        final Map<Long, String> portletIdToKey = new LinkedHashMap<Long, String>();
        OfBizListIterator iterator = null;
        try
        {
            //find all the portletConfigs where the portletkey is one of the portletKeys we'd like to replace.
            iterator = ofBizDelegator.findListIteratorByCondition(OfbizPortletConfigurationStore.TABLE, null);
            GenericValue portletConfigGV = iterator.next();
            while (portletConfigGV != null)
            {
                final String portletKey = portletConfigGV.getString(OfbizPortletConfigurationStore.Columns.PORTLETKEY);
                //if the key is null or empty, then this portlet has already been converted.  Skip it!
                //we'll also skip it if there's no portlet specific upgrade tasks.
                if (StringUtils.isNotBlank(portletKey) && portletToUpgradeTasks.containsKey(portletKey))
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

        final Context updateContext = Contexts.percentageLogger(new MapSized(portletIdToKey), log, "Converting legacy portlets to gadgets is {0}% complete");
        for (final Map.Entry<Long, String> entry : portletIdToKey.entrySet())
        {
            final Context.Task task = updateContext.start(entry);
            try
            {
                final Long portletId = entry.getKey();
                final String key = entry.getValue();
                final LegacyPortletUpgradeTask legacyPortletUpgradeTask = portletToUpgradeTasks.get(key);
                final URI gadgetUri = legacyPortletUpgradeTask.getGadgetUri();
                ofBizDelegator.bulkUpdateByPrimaryKey(OfbizPortletConfigurationStore.TABLE,
                        MapBuilder.<String, Object>newBuilder().
                                add(OfbizPortletConfigurationStore.Columns.PORTLETKEY, null).
                                add(OfbizPortletConfigurationStore.Columns.GADGET_XML, gadgetUri.toASCIIString()).toMap(),
                        CollectionBuilder.newBuilder(portletId).asList());

                final PropertySet livePropertySet = propertySetFactory.buildNoncachingPropertySet(OfbizPortletConfigurationStore.TABLE, portletId);
                final Map<String, String> userPrefs = legacyPortletUpgradeTask.convertUserPrefs(livePropertySet);

                for (final Map.Entry<String, String> userPref : userPrefs.entrySet())
                {
                    //create corresponding entries in the userprefs table for this gadget.
                    ofBizDelegator.createValue(OfbizPortletConfigurationStore.USER_PREFERENCES_TABLE,
                            MapBuilder.<String, Object>newBuilder().add(OfbizPortletConfigurationStore.UserPreferenceColumns.PORTLETID, portletId).
                                    add(OfbizPortletConfigurationStore.UserPreferenceColumns.KEY, userPref.getKey()).
                                    add(OfbizPortletConfigurationStore.UserPreferenceColumns.VALUE, userPref.getValue()).toMap());
                }
                //finally delete the original propertyset!
                removePropertySet(portletId);
            }
            finally
            {
                task.complete();
            }
        }

        copyPortletPluginStateForGadgets();

        flushPortletConfigurationCache();
    }

    /**
     * If the pre-4.0 "Text Portlet" was explicitly enabled in this instance (it is disabled by default), then we need
     * to explicitly enable the Text Gadget too, so that portlets are migrated correctly.
     */
    void copyPortletPluginStateForGadgets()
    {
        if (pluginAccessor.isPluginModuleEnabled(COM_ATLASSIAN_JIRA_PLUGIN_SYSTEM_PORTLETS_TEXT))
        {
            pluginController.enablePluginModule(COM_ATLASSIAN_JIRA_GADGETS_TEXT_GADGET);
        }
    }

    void flushPortletConfigurationCache()
    {
        //just in case there was a previous upgrade task that populated this cache, we'll flush the portletConfig store.
        //this is a bit ugly, but it seems to be the only way to flush this cache.
        final PortletConfigurationStore component = ComponentManager.getComponent(PortletConfigurationStore.class);
        if (component != null && component instanceof FlushablePortletConfigurationStore)
        {
            final FlushablePortletConfigurationStore store = (FlushablePortletConfigurationStore) component;
            store.flush();
        }
    }

    private void removePropertySet(final Long portletId)
    {
        final PropertySet livePropertySet = propertySetFactory.buildNoncachingPropertySet(OfbizPortletConfigurationStore.TABLE, portletId);
        //JRA-19626: Data may contain duplicate property set keys which can blow up when trying to delete the same key twice.  Need to
        //convert the collection of keys to a Set such that we'll only remove each key once.
        @SuppressWarnings ("unchecked")
        final Set<String> keys = new HashSet<String>(livePropertySet.getKeys());
        for (final String propertyKey : keys)
        {
            livePropertySet.remove(propertyKey);
        }
    }

    @Override
    public String getShortDescription()
    {
        return "Converts Legacy Portlets to Gadgets including user preferences.";
    }

    @Override
    public String getBuildNumber()
    {
        return "438";
    }

    static class MapSized implements Sized
    {
        private final Map map;

        public MapSized(Map map)
        {
            this.map = map;
        }

        public int size()
        {
            return map.size();
        }

        public boolean isEmpty()
        {
            return map.isEmpty();
        }
    }
}