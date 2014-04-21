package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.MockPropertySet;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.upgrade.util.AbstractLegacyPortletUpgradeTask;
import com.atlassian.jira.upgrade.util.LegacyPortletUpgradeTask;
import com.atlassian.jira.upgrade.util.LegacyPortletUpgradeTaskFactory;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import org.easymock.EasyMock;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestUpgradeTask_Build438 extends MockControllerTestCase
{
    private PluginAccessor pluginAccessor;
    private PluginController pluginController;

    @Before
    public void setUp() throws Exception
    {
        pluginAccessor = getMock(PluginAccessor.class);
        pluginController = getMock(PluginController.class);
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        final Date date = new Date();
        //should really only be able to deal with strings but you never know with propertysets.
        final Map properties = MapBuilder.newBuilder().
                add("aLong", 100L).
                add("aString", "Cows").
                add("aBool", Boolean.valueOf(true)).
                add("aDate", date).
                add("aMultValue", "value1_*|*_value2_*|*_val|ue3_*|*_value4").
                add("nullValue", null).toLinkedHashMap();

        final Map<String, String> expectedUserPrefs = MapBuilder.<String, String>newBuilder().
                add("aLong", "100").
                add("aString", "Cows").
                add("aBool", "true").
                add("aDate", date.toString()).
                //'|' in a value should be % encoded.
                        add("aMultValue", "value1|value2|val%7Cue3|value4").
                add("nullValue", null).toMap();

        MockPropertySet ps = new MockPropertySet(properties);

        //this one should be converted
        MockGenericValue mockPortletConfigGV = new MockGenericValue("PortletConfiguration",
                MapBuilder.<String, Object>newBuilder().
                        add("id", 10000L).
                        add("portalpage", 10020L).
                        add("portletId", "INPROGRESS").
                        add("columnNumber", 0).
                        add("position", 0).toMap());
        //this is already converted so it should be left untouched
        MockGenericValue mockPortletConfigGV1 = new MockGenericValue("PortletConfiguration",
                MapBuilder.<String, Object>newBuilder().
                        add("id", 10001L).
                        add("portalpage", 10021L).
                        add("portletId", null).
                        add("columnNumber", 0).
                        add("position", 0).
                        add("gadgetXml", URI.create("http://www.google.com/").toASCIIString()).
                        add("color", Color.color1).toMap());
        //this one wont be matched by any upgrade task so it should be left untouched as well
        MockGenericValue mockPortletConfigGV2 = new MockGenericValue("PortletConfiguration",
                MapBuilder.<String, Object>newBuilder().
                        add("id", 10002L).
                        add("portalpage", 10021L).
                        add("portletId", "PROJECTS").
                        add("columnNumber", 0).
                        add("position", 0).toMap());
        final OfBizListIterator mockIterator = mockController.getMock(OfBizListIterator.class);
        mockIterator.next();
        mockController.setReturnValue(mockPortletConfigGV);
        mockIterator.next();
        mockController.setReturnValue(mockPortletConfigGV1);
        mockIterator.next();
        mockController.setReturnValue(mockPortletConfigGV2);
        mockIterator.next();
        mockController.setReturnValue(null);
        mockIterator.close();

        final OfBizDelegator mockOfBizDelegator = mockController.getMock(OfBizDelegator.class);
        mockOfBizDelegator.findListIteratorByCondition("PortletConfiguration", null);
        mockController.setReturnValue(mockIterator);

        mockOfBizDelegator.bulkUpdateByPrimaryKey("PortletConfiguration",
                MapBuilder.<String, Object>newBuilder().add("portletId", null).
                        add("gadgetXml", "/plugins/servlet/gadgets/g/com.atlassian.jira.gadgets/gadgets/InProgress.xml").toMap(),
                CollectionBuilder.newBuilder(10000L).asList());
        mockController.setReturnValue(1);

        final JiraPropertySetFactory mockJiraPropertySetFactory = mockController.getMock(JiraPropertySetFactory.class);
        mockJiraPropertySetFactory.buildNoncachingPropertySet("PortletConfiguration", 10000L);
        mockController.setReturnValue(ps, 2);

        mockOfBizDelegator.createValue("GadgetUserPreference",
                MapBuilder.<String, Object>newBuilder().add("userprefkey", "aLong").
                        add("portletconfiguration", 10000L).
                        add("userprefvalue", "100").toMap());
        mockController.setReturnValue(null);
        mockOfBizDelegator.createValue("GadgetUserPreference",
                MapBuilder.<String, Object>newBuilder().add("userprefkey", "aString").
                        add("portletconfiguration", 10000L).
                        add("userprefvalue", "Cows").toMap());
        mockController.setReturnValue(null);
        mockOfBizDelegator.createValue("GadgetUserPreference",
                MapBuilder.<String, Object>newBuilder().add("userprefkey", "aBool").
                        add("portletconfiguration", 10000L).
                        add("userprefvalue", "true").toMap());
        mockController.setReturnValue(null);
        mockOfBizDelegator.createValue("GadgetUserPreference",
                MapBuilder.<String, Object>newBuilder().add("userprefkey", "aDate").
                        add("portletconfiguration", 10000L).
                        add("userprefvalue", date.toString()).toMap());
        mockController.setReturnValue(null);
        mockOfBizDelegator.createValue("GadgetUserPreference",
                MapBuilder.<String, Object>newBuilder().add("userprefkey", "aMultValue").
                        add("portletconfiguration", 10000L).
                        add("userprefvalue", "value1|value2|val%7Cue3|value4").toMap());
        mockController.setReturnValue(null);
        mockOfBizDelegator.createValue("GadgetUserPreference",
                MapBuilder.<String, Object>newBuilder().add("userprefkey", "nullValue").
                        add("portletconfiguration", 10000L).
                        add("userprefvalue", null).toMap());
        mockController.setReturnValue(null);

        final LegacyPortletUpgradeTaskFactory mockLegacyPortletUpgradeTaskFactory = mockController.getMock(LegacyPortletUpgradeTaskFactory.class);
        mockLegacyPortletUpgradeTaskFactory.createPortletToUpgradeTaskMapping();
        final LegacyPortletUpgradeTask mockLegacyPortletUpgradeTask = new MockLegacyPortletUpgradeTask();
        mockController.setReturnValue(MapBuilder.newBuilder().add("INPROGRESS", mockLegacyPortletUpgradeTask).toMap());

        EasyMock.expect(pluginAccessor.isPluginModuleEnabled(UpgradeTask_Build438.COM_ATLASSIAN_JIRA_PLUGIN_SYSTEM_PORTLETS_TEXT))
                .andReturn(true);
        pluginController.enablePluginModule(UpgradeTask_Build438.COM_ATLASSIAN_JIRA_GADGETS_TEXT_GADGET);
        EasyMock.expectLastCall();

        final AtomicBoolean flushedCache = new AtomicBoolean(false);

        mockController.replay();
        final UpgradeTask_Build438 upgradeTask = new UpgradeTask_Build438(mockOfBizDelegator, mockJiraPropertySetFactory, mockLegacyPortletUpgradeTaskFactory, pluginAccessor, pluginController)
        {
            @Override
            void flushPortletConfigurationCache()
            {
                flushedCache.set(true);
            }
        };
        upgradeTask.doUpgrade(false);
        //all old properties should have been removed!
        assertTrue(ps.getKeys().isEmpty());
        assertTrue(flushedCache.get());
    }

    @Test
    public void testCopyPortletPluginStateForGadgets() throws Exception
    {
        EasyMock.expect(pluginAccessor.isPluginModuleEnabled(UpgradeTask_Build438.COM_ATLASSIAN_JIRA_PLUGIN_SYSTEM_PORTLETS_TEXT))
                .andReturn(false);

        final UpgradeTask_Build438 upgradeTask = new UpgradeTask_Build438(null, null, null, pluginAccessor, pluginController);

        replay();
        upgradeTask.copyPortletPluginStateForGadgets();
    }

    @Test
    public void testInfo()
    {
        replay();
        
        UpgradeTask_Build438 upgradeTask = new UpgradeTask_Build438(null, null, null, pluginAccessor, pluginController);
        final String buildNumber = upgradeTask.getBuildNumber();
        assertEquals("438", buildNumber);
        final String description = upgradeTask.getShortDescription();
        assertEquals("Converts Legacy Portlets to Gadgets including user preferences.", description);
    }

    private static class MockLegacyPortletUpgradeTask extends AbstractLegacyPortletUpgradeTask
    {
        public String getPortletKey()
        {
            return "INPROGRESS";
        }

        public URI getGadgetUri()
        {
            return URI.create("/plugins/servlet/gadgets/g/com.atlassian.jira.gadgets/gadgets/InProgress.xml");
        }
    }
}
