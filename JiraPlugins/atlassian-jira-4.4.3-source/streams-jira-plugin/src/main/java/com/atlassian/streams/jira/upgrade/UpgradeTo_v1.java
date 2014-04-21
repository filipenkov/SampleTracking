package com.atlassian.streams.jira.upgrade;

import java.util.Collection;
import java.util.Map;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.portal.OfbizPortletConfigurationStore;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationImpl;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import com.opensymphony.module.propertyset.PropertySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeTo_v1 implements PluginUpgradeTask
{
    private static final Logger log = LoggerFactory.getLogger(UpgradeTo_v1.class);

    private final PortletConfigurationStore portletConfigurationStore;
    private final JiraPropertySetFactory propertySetFactory;
    private final ProjectManager projectManager;

    public UpgradeTo_v1(final JiraPropertySetFactory propertySetFactory, ProjectManager projectManager)
    {
        this.projectManager = projectManager;
        this.portletConfigurationStore = ComponentManager.getComponentInstanceOfType(PortletConfigurationStore.class);
        this.propertySetFactory = propertySetFactory;
    }

    public Collection<Message> doUpgrade() throws Exception
    {
        final ActivityStreamPortletUpgradeTask portletUpgrade = new ActivityStreamPortletUpgradeTask(projectManager);

        // First get all the portletConfigurations in the database.
        final EnclosedIterable<PortletConfiguration> iterable = portletConfigurationStore
            .getAllPortletConfigurations();
        iterable.foreach(new Consumer<PortletConfiguration>()
        {
            public void consume(@NotNull final PortletConfiguration pc)
            {
                // for each portletconfiguration, check if it's key matches the portlet key we want to upgrade
                if (portletUpgrade.canUpgrade(pc.getKey()))
                {
                    log.info("Upgrading portletconfig with id '" + pc.getId() + "'");
                    // first lets convert the preferences for this portlet to
                    // the new prefs format used for gadgets.
                    final Map<String, String> prefs;
                    try
                    {
                        prefs = portletUpgrade.convertUserPrefs(pc.getProperties());
                    }
                    catch (final ObjectConfigurationException e)
                    {
                        throw new RuntimeException(e);
                    }


                    // then create essentially a copy of the old portletConfig.
                    // This new copy no longer needs to have
                    // the portletKey and propertySet set to any values. It
                    // however does require the GadgetUri and user prefs to be
                    // set.
                    final PortletConfiguration newConfig = new PortletConfigurationImpl(pc.getId(), pc
                        .getDashboardPageId(), null, null, pc.getColumn(), pc.getRow(), null, portletUpgrade
                        .getGadgetUri(), Color.color1, prefs);
                    // Now lets store this new config back to the database.
                    portletConfigurationStore.store(newConfig);
                    // clear out the old properties for this portlet
                    removePropertySet(pc);
                }
            }
        });

        return null;
    }

    private void removePropertySet(final PortletConfiguration pc)
    {
        final PropertySet livePropertySet = propertySetFactory.buildNoncachingPropertySet(
            OfbizPortletConfigurationStore.TABLE, pc.getId());
        @SuppressWarnings("unchecked")
        final Collection<String> keys = livePropertySet.getKeys();
        for (final String propertyKey : keys)
        {
            livePropertySet.remove(propertyKey);
        }
    }

    public String getPluginKey()
    {
        return "com.atlassian.streams.streams-jira-plugin";
    }

    public int getBuildNumber()
    {
        return 1;
    }

    public String getShortDescription()
    {
        return "Converts stream portlets to stream gadget.";
    }

}