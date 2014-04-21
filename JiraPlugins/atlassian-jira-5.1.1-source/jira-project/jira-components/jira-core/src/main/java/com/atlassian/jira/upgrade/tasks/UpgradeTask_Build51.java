/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.portal.PortletConfigurationException;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

public class UpgradeTask_Build51 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build51.class);


    private final OfBizDelegator delegator;
    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build51(OfBizDelegator delegator, ApplicationProperties applicationProperties)
    {
        super(false);
        this.delegator = delegator;
        this.applicationProperties = applicationProperties;
    }

    public String getBuildNumber()
    {
        return "51";
    }

    public String getShortDescription()
    {
        return "Inserts a default dashboard configuration into the database if it doesn't already exist.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        final String instanceName = applicationProperties.getText(APKeys.JIRA_TITLE);

        final SystemDashGadget[] leftGadgetIds = new SystemDashGadget[] { new SystemDashGadget("rest/gadgets/1.0/g/com.atlassian.jira.gadgets:introduction-gadget/gadgets/introduction-gadget.xml", null),
                                                                                   new SystemDashGadget("rest/gadgets/1.0/g/com.atlassian.streams.streams-jira-plugin:activitystream-gadget/gadgets/activitystream-gadget.xml", MapBuilder.newBuilder("keys", "__all_projects__").add("isConfigured", "true").add("title", instanceName).add("numofentries", "5").toListOrderedMap())};
        final SystemDashGadget[] rightGadgetIds = new SystemDashGadget[] {new SystemDashGadget("rest/gadgets/1.0/g/com.atlassian.jira.gadgets:assigned-to-me-gadget/gadgets/assigned-to-me-gadget.xml", MapBuilder.build("isConfigured", "true")),
                                                                 new SystemDashGadget("rest/gadgets/1.0/g/com.atlassian.jira.gadgets:favourite-filters-gadget/gadgets/favourite-filters-gadget.xml", MapBuilder.build("isConfigured", "true")),
                                                                 new SystemDashGadget("rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml", null) };
        final SystemDashGadget[][] gadgets = new SystemDashGadget[][] { leftGadgetIds, rightGadgetIds };

        try
        {
            final List<GenericValue> defaultDashboard = delegator.findByAnd("PortalPage", MapBuilder.build("username", null));

            if (defaultDashboard == null || defaultDashboard.size() == 0)
            {

                final GenericValue systemDashboard = delegator.createValue("PortalPage", MapBuilder.<String, Object>newBuilder("pagename", "dashboard").add("sequence", 0L).toMap());
                for (int i = 0; i < gadgets.length; i++)
                {
                    SystemDashGadget[] singleColGadgets = gadgets[i];
                    for (int j = 0; j < singleColGadgets.length; j++)
                    {
                        final String gadgetXml = singleColGadgets[j].getXml();
                        final GenericValue gadget = delegator.createValue("PortletConfiguration",
                                MapBuilder.<String, Object>newBuilder("portalpage", systemDashboard.getLong("id"))
                                    .add("gadgetXml", gadgetXml)
                                    .add("columnNumber", i)
                                    .add("position", j).toMap());
                        final Map<String, String> params = singleColGadgets[j].getParams();
                        if (params != null)
                        {
                            for (Map.Entry<String, String> param : params.entrySet())
                            {
                                delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("userprefkey", param.getKey())
                                        .add("userprefvalue", param.getValue()).add("portletconfiguration", gadget.getLong("id")).toMap());
                            }
                        }
                    }
                }
            }
            else
            {
                log.info("Not adding default dashboard as one is already detected");
            }
        }
        catch (Exception e)
        {
            addError(getI18nBean().getText("admin.errors.upgrade.error.in.build.51"));
            throw new PortletConfigurationException(e);
        }
    }

    static class SystemDashGadget
    {
        private String xml;
        private Map<String, String> params;

        SystemDashGadget(String xml, Map<String, String> params)
        {
            this.xml = xml;
            this.params = params;
        }

        public String getXml()
        {
            return xml;
        }

        public Map<String, String> getParams()
        {
            return params;
        }
    }
}
