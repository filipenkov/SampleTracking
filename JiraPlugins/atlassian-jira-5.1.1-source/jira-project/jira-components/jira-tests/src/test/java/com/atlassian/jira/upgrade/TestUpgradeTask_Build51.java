/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build51;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TestUpgradeTask_Build51 extends MockControllerTestCase
{
    private OfBizDelegator delegator;
    private ApplicationProperties applicationProperties;


    @Before
    public void setUp() throws Exception
    {
        applicationProperties = createMock(ApplicationProperties.class);
        delegator = createMock(OfBizDelegator.class);
    }

    @After
    public void tearDown() throws Exception
    {
        applicationProperties = null;
        delegator = null;
    }

    @Test
    public void testGetBuildNumber()
    {
        replay(delegator, applicationProperties);
        UpgradeTask_Build51 task = new UpgradeTask_Build51(null, null);
        assertEquals("51", task.getBuildNumber());
    }

    @Test
    public void testGetShortDescription()
    {
        replay(delegator, applicationProperties);
        UpgradeTask_Build51 task = new UpgradeTask_Build51(null, null);
        assertEquals("Inserts a default dashboard configuration into the database if it doesn't already exist.", task.getShortDescription());
    }

    @Test
    public void testAlreadyExists() throws Exception
    {


        expect(applicationProperties.getText(APKeys.JIRA_TITLE)).andReturn("Nicks Instance");

        expect(delegator.findByAnd("PortalPage", MapBuilder.build("username", null))).andReturn(CollectionBuilder.<GenericValue>list(new MockGenericValue("PortalPage")));

        replay(delegator, applicationProperties);

        final UpgradeTask_Build51 task = new UpgradeTask_Build51(delegator, applicationProperties);

        task.doUpgrade(false);

    }

    @Test
    public void testDoesntExist() throws Exception
    {


        expect(applicationProperties.getText(APKeys.JIRA_TITLE)).andReturn("Nicks Instance");

        expect(delegator.findByAnd("PortalPage", MapBuilder.build("username", null))).andReturn(null);

        expect(delegator.createValue("PortalPage", MapBuilder.<String, Object>newBuilder("pagename", "dashboard").add("sequence", 0L).toMap()))
                .andReturn(new MockGenericValue("PortalPage", MapBuilder.<String, Object>newBuilder("pagename", "dashboard").add("sequence", 0L).add("id", 10000L).toMap()));

        expect(delegator.createValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:introduction-gadget/gadgets/introduction-gadget.xml").add("columnNumber", 0).add("position", 0).toMap()))
                .andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:introduction-gadget/gadgets/introduction-gadget.xml").add("columnNumber", 0).add("position", 0).add("id", 10000L).toMap()));

        expect(delegator.createValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.streams.streams-jira-plugin:activitystream-gadget/gadgets/activitystream-gadget.xml").add("columnNumber", 0).add("position", 1).toMap()))
                .andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.streams.streams-jira-plugin:activitystream-gadget/gadgets/activitystream-gadget.xml").add("columnNumber", 0).add("position", 1).add("id", 10001L).toMap()));
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10001L).add("userprefvalue", "__all_projects__").add("userprefkey", "keys").toMap()))
                .andReturn(null);
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10001L).add("userprefvalue", "true").add("userprefkey", "isConfigured").toMap()))
                .andReturn(null);
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10001L).add("userprefvalue", "Nicks Instance").add("userprefkey", "title").toMap()))
                .andReturn(null);
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10001L).add("userprefvalue", "5").add("userprefkey", "numofentries").toMap()))
                .andReturn(null);


        expect(delegator.createValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:assigned-to-me-gadget/gadgets/assigned-to-me-gadget.xml").add("columnNumber", 1).add("position", 0).toMap()))
                .andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:assigned-to-me-gadget/gadgets/assigned-to-me-gadget.xml").add("columnNumber", 1).add("position", 0).add("id", 10003L).toMap()));
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10003L).add("userprefvalue", "true").add("userprefkey", "isConfigured").toMap()))
                .andReturn(null);


        expect(delegator.createValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:favourite-filters-gadget/gadgets/favourite-filters-gadget.xml").add("columnNumber", 1).add("position", 1).toMap()))
                .andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:favourite-filters-gadget/gadgets/favourite-filters-gadget.xml").add("columnNumber", 1).add("position", 1).add("id", 10002L).toMap()));
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10002L).add("userprefvalue", "true").add("userprefkey", "isConfigured").toMap()))
                .andReturn(null);


        expect(delegator.createValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml").add("columnNumber", 1).add("position", 2).toMap()))
                .andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml").add("columnNumber", 1).add("position", 2).add("id", 10004L).toMap()));

        replay(delegator, applicationProperties);

        final UpgradeTask_Build51 task = new UpgradeTask_Build51(delegator, applicationProperties);

        task.doUpgrade(false);

    }

    @Test
    public void testEmptyDefault() throws Exception
    {


        expect(applicationProperties.getText(APKeys.JIRA_TITLE)).andReturn("Nicks Instance");

        expect(delegator.findByAnd("PortalPage", MapBuilder.build("username", null))).andReturn(Collections.<GenericValue>emptyList());

        expect(delegator.createValue("PortalPage", MapBuilder.<String, Object>newBuilder("pagename", "dashboard").add("sequence", 0L).toMap()))
                .andReturn(new MockGenericValue("PortalPage", MapBuilder.<String, Object>newBuilder("pagename", "dashboard").add("sequence", 0L).add("id", 10000L).toMap()));

        expect(delegator.createValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:introduction-gadget/gadgets/introduction-gadget.xml").add("columnNumber", 0).add("position", 0).toMap()))
                .andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:introduction-gadget/gadgets/introduction-gadget.xml").add("columnNumber", 0).add("position", 0).add("id", 10000L).toMap()));

        expect(delegator.createValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.streams.streams-jira-plugin:activitystream-gadget/gadgets/activitystream-gadget.xml").add("columnNumber", 0).add("position", 1).toMap()))
                .andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.streams.streams-jira-plugin:activitystream-gadget/gadgets/activitystream-gadget.xml").add("columnNumber", 0).add("position", 1).add("id", 10001L).toMap()));
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10001L).add("userprefvalue", "__all_projects__").add("userprefkey", "keys").toMap()))
                .andReturn(null);
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10001L).add("userprefvalue", "true").add("userprefkey", "isConfigured").toMap()))
                .andReturn(null);
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10001L).add("userprefvalue", "Nicks Instance").add("userprefkey", "title").toMap()))
                .andReturn(null);
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10001L).add("userprefvalue", "5").add("userprefkey", "numofentries").toMap()))
                .andReturn(null);

        expect(delegator.createValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:assigned-to-me-gadget/gadgets/assigned-to-me-gadget.xml").add("columnNumber", 1).add("position", 0).toMap()))
                .andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:assigned-to-me-gadget/gadgets/assigned-to-me-gadget.xml").add("columnNumber", 1).add("position", 0).add("id", 10003L).toMap()));
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10003L).add("userprefvalue", "true").add("userprefkey", "isConfigured").toMap()))
                .andReturn(null);


        expect(delegator.createValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:favourite-filters-gadget/gadgets/favourite-filters-gadget.xml").add("columnNumber", 1).add("position", 1).toMap()))
                .andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:favourite-filters-gadget/gadgets/favourite-filters-gadget.xml").add("columnNumber", 1).add("position", 1).add("id", 10002L).toMap()));
        expect(delegator.createValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder("portletconfiguration", 10002L).add("userprefvalue", "true").add("userprefkey", "isConfigured").toMap()))
                .andReturn(null);


        expect(delegator.createValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml").add("columnNumber", 1).add("position", 2).toMap()))
                .andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.<String, Object>newBuilder("portalpage", 10000L).add("gadgetXml", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml").add("columnNumber", 1).add("position", 2).add("id", 10004L).toMap()));

        replay(delegator, applicationProperties);

        final UpgradeTask_Build51 task = new UpgradeTask_Build51(delegator, applicationProperties);

        task.doUpgrade(false);

    }

}
