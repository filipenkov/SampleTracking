package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MapBuilder;
import com.opensymphony.module.propertyset.PropertySet;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import org.ofbiz.core.entity.GenericValue;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TestOfbizPortletConfigurationStore extends LegacyJiraMockTestCase
{

    private PortletConfigurationStore portletConfigurationStore;
    private static final Long PAGE1_ID = new Long(10030);
    private static final Long PAGE2_ID = new Long(10040);
    private static final String PORTLET_KEY = "com.atlassian.jira.plugin.system.portlets:introduction";
    private JiraPropertySetFactory propertySetFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        propertySetFactory = ComponentManager.getComponentInstanceOfType(JiraPropertySetFactory.class);

        portletConfigurationStore = new OfbizPortletConfigurationStore(
                ComponentManager.getComponentInstanceOfType(OfBizDelegator.class),
                propertySetFactory,
                ComponentManager.getComponentInstanceOfType(PortletAccessManager.class));
    }

    public void testPortletConfigurationRetrieval()
    {
        //Test the get with no db entries
        assertEquals(0, portletConfigurationStore.getByPortalPage(new Long(1001)).size());
        assertNull(portletConfigurationStore.getByPortletId(new Long(1001)));

        PortletConfiguration pc1 = portletConfigurationStore.addLegacyPortlet(PAGE1_ID, null, new Integer(0), new Integer(0), URI.create("rest/legacy/" + PORTLET_KEY + ".xml"), Color.color1, Collections.<String,String>emptyMap(), PORTLET_KEY);
        PortletConfiguration pc2 = portletConfigurationStore.addLegacyPortlet(PAGE1_ID, null, new Integer(0), new Integer(1), URI.create("rest/legacy/" + PORTLET_KEY + ".xml"), Color.color1, Collections.<String,String>emptyMap(), PORTLET_KEY);
        PortletConfiguration pc3 = portletConfigurationStore.addLegacyPortlet(PAGE2_ID, null, new Integer(0), new Integer(0), URI.create("rest/legacy/" + PORTLET_KEY + ".xml"), Color.color1, Collections.<String,String>emptyMap(), PORTLET_KEY);

        assertEquals(pc1, portletConfigurationStore.getByPortletId(pc1.getId()));
        assertEquals(pc2, portletConfigurationStore.getByPortletId(pc2.getId()));
        assertEquals(pc3, portletConfigurationStore.getByPortletId(pc3.getId()));


        assertEquals(EasyList.build(pc1, pc2), portletConfigurationStore.getByPortalPage(PAGE1_ID));

        assertEquals(EasyList.build(pc3), portletConfigurationStore.getByPortalPage(PAGE2_ID));

        // Test delete
        portletConfigurationStore.delete(pc1);

        assertNull(portletConfigurationStore.getByPortletId(pc1.getId()));
        assertEquals(EasyList.build(pc2), portletConfigurationStore.getByPortalPage(PAGE1_ID));

        // Test Store changes
        pc3.setColumn(new Integer(1));
        pc3.setDashboardPageId(PAGE1_ID);
        pc3.setRow(new Integer(0));
        portletConfigurationStore.store(pc3);

        assertEquals(EasyList.build(pc2, pc3), portletConfigurationStore.getByPortalPage(PAGE1_ID));
        assertEquals(0, portletConfigurationStore.getByPortalPage(PAGE2_ID).size());
        assertEquals(new Integer(0), portletConfigurationStore.getByPortletId(pc3.getId()).getRow());
        assertEquals(new Integer(1), portletConfigurationStore.getByPortletId(pc3.getId()).getColumn());
        assertEquals(PAGE1_ID, portletConfigurationStore.getByPortletId(pc3.getId()).getDashboardPageId());
    }

    public void testPropertySets() throws ObjectConfigurationException
    {
        //new portlet should have no configuration.
        PortletConfiguration pc1 = portletConfigurationStore.addLegacyPortlet(PAGE1_ID, null, new Integer(0), new Integer(0), URI.create("rest/legacy/" + PORTLET_KEY + ".xml"), Color.color1, Collections.<String,String>emptyMap(), PORTLET_KEY);
        assertTrue(pc1.getProperties().getKeys().isEmpty());

        //these keys should not be saved until a call is made to the manager.
        pc1.getProperties().setString("key1", "value1");
        pc1.getProperties().setString("key2", "value2");
        PortletConfiguration pc1FromDb = portletConfigurationStore.getByPortletId(pc1.getId());
        assertTrue(pc1FromDb.getProperties().getKeys().isEmpty());

        //the new values should have been saved.
        portletConfigurationStore.store(pc1);
        pc1FromDb = portletConfigurationStore.getByPortletId(pc1.getId());
        assertEquals(2, pc1FromDb.getProperties().getKeys().size());
        assertEquals("value1", pc1FromDb.getProperties().getString("key1"));
        assertEquals("value2", pc1FromDb.getProperties().getString("key2"));

        //this value should not be saved until the store is called.
        pc1.getProperties().setString("key3", "value3");

        pc1FromDb = portletConfigurationStore.getByPortletId(pc1.getId());
        assertEquals(2, pc1FromDb.getProperties().getKeys().size());
        assertEquals("value1", pc1FromDb.getProperties().getString("key1"));
        assertEquals("value2", pc1FromDb.getProperties().getString("key2"));
        assertNull(pc1FromDb.getProperties().getString("key3"));

        //the4 new value should now be saved.
        portletConfigurationStore.store(pc1);

        pc1FromDb = portletConfigurationStore.getByPortletId(pc1.getId());
        assertEquals(3, pc1FromDb.getProperties().getKeys().size());
        assertEquals("value1", pc1FromDb.getProperties().getString("key1"));
        assertEquals("value2", pc1FromDb.getProperties().getString("key2"));
        assertEquals("value3", pc1FromDb.getProperties().getString("key3"));

        //the configuration should be deleted.
        portletConfigurationStore.delete(pc1);
        PropertySet ps = propertySetFactory.buildNoncachingPropertySet("PortletConfiguration", pc1.getId());
        assertTrue(ps.getKeys().isEmpty());
    }

    public void testAddGadget()
    {
        final Map<String, String> prefs = MapBuilder.<String, String>newBuilder().add("pref1", "value1").add("pref2", "value2").toMap();
        final URI googleUri = URI.create("http://www.google.com");
        portletConfigurationStore.addGadget(PAGE1_ID, 10025L, 3, 4, googleUri, Color.color3, prefs);

        PortletConfiguration portletConfiguration = portletConfigurationStore.getByPortletId(10025L);
        assertEquals(PAGE1_ID, portletConfiguration.getDashboardPageId());
        assertEquals(new Long(10025), portletConfiguration.getId());
        assertEquals(new Integer(3), portletConfiguration.getColumn());
        assertEquals(new Integer(4), portletConfiguration.getRow());
        assertEquals(googleUri, portletConfiguration.getGadgetURI());
        assertEquals(prefs, portletConfiguration.getUserPrefs());
        assertEquals(Color.color3, portletConfiguration.getColor());
        assertNull(portletConfiguration.getKey());

        final PortletConfiguration configuration = portletConfigurationStore.addGadget(PAGE1_ID, null, 3, 4, googleUri, Color.color5, prefs);
        portletConfiguration = portletConfigurationStore.getByPortletId(configuration.getId());
        assertEquals(PAGE1_ID, portletConfiguration.getDashboardPageId());
        assertEquals(new Integer(3), portletConfiguration.getColumn());
        assertEquals(new Integer(4), portletConfiguration.getRow());
        assertEquals(googleUri, portletConfiguration.getGadgetURI());
        assertEquals(prefs, portletConfiguration.getUserPrefs());
        assertEquals(Color.color5, portletConfiguration.getColor());
        assertNull(portletConfiguration.getKey());
    }

    public void testStore()
    {
        final Map<String, String> prefs = MapBuilder.<String, String>newBuilder().add("pref1", "value1").add("pref2", "value2").toMap();
        final Map<String, String> prefs2 = MapBuilder.<String, String>newBuilder().add("pref3", "value3").add("pref4", "value4").toMap();
        final URI googleUri = URI.create("http://www.google.com");
        final PortletConfiguration portletConfiguration = portletConfigurationStore.addGadget(PAGE1_ID, 10025L, 3, 4, googleUri, Color.color3, prefs);
        assertEquals(PAGE1_ID, portletConfiguration.getDashboardPageId());
        assertEquals(new Long(10025), portletConfiguration.getId());
        assertEquals(new Integer(3), portletConfiguration.getColumn());
        assertEquals(new Integer(4), portletConfiguration.getRow());
        assertEquals(googleUri, portletConfiguration.getGadgetURI());
        assertEquals(prefs, portletConfiguration.getUserPrefs());
        assertEquals(Color.color3, portletConfiguration.getColor());
        assertNull(portletConfiguration.getKey());

        portletConfiguration.setColor(Color.color1);
        portletConfiguration.setColumn(1);
        portletConfiguration.setRow(2);
        portletConfiguration.setDashboardPageId(PAGE2_ID);
        portletConfiguration.setUserPrefs(prefs2);
        portletConfigurationStore.store(portletConfiguration);
        final PortletConfiguration configuration = portletConfigurationStore.getByPortletId(10025L);
        assertEquals(PAGE2_ID, configuration.getDashboardPageId());
        assertEquals(new Long(10025), configuration.getId());
        assertEquals(new Integer(1), configuration.getColumn());
        assertEquals(new Integer(2), configuration.getRow());
        assertEquals(googleUri, configuration.getGadgetURI());
        assertEquals(prefs2, configuration.getUserPrefs());
        assertEquals(Color.color1, configuration.getColor());
        assertNull(configuration.getKey());

        PortletConfiguration portletConfig = new PortletConfigurationImpl(10026L, PAGE2_ID, null, null, 1, 2, null, googleUri, Color.color7, prefs2);
        try
        {
            portletConfigurationStore.store(portletConfig);
            fail("Should have thrown exception trying to store portlet with id that doesn't exist");
        }
        catch (IllegalArgumentException e)
        {
            //yay
        }
    }    

    public void testGetAllPortletConfigurations()
    {
        final Map<String, String> prefs = MapBuilder.<String, String>newBuilder().add("pref1", "value1").toMap();

        portletConfigurationStore.addGadget(PAGE1_ID, 10020L, 3, 4, URI.create("http://www.google.com"), Color.color5, Collections.<String, String>emptyMap());
        portletConfigurationStore.addGadget(PAGE2_ID, null, 1, 1, URI.create("http://www.msn.com"), Color.color2, prefs);

        final AtomicInteger count = new AtomicInteger(0);
        final EnclosedIterable<PortletConfiguration> iterable = portletConfigurationStore.getAllPortletConfigurations();
        iterable.foreach(new Consumer<PortletConfiguration>()
        {
            public void consume(@NotNull final PortletConfiguration pc)
            {
                if (pc.getId() == 10020L)
                {
                    assertEquals(PAGE1_ID, pc.getDashboardPageId());
                    assertEquals(3, pc.getColumn().intValue());
                    assertEquals(4, pc.getRow().intValue());
                    assertEquals(URI.create("http://www.google.com"), pc.getGadgetURI());
                    assertEquals(Color.color5, pc.getColor());
                    assertEquals(Collections.<String, String>emptyMap(), pc.getUserPrefs());
                }
                else
                {
                    assertEquals(PAGE2_ID, pc.getDashboardPageId());
                    assertEquals(1, pc.getColumn().intValue());
                    assertEquals(1, pc.getRow().intValue());
                    assertEquals(URI.create("http://www.msn.com"), pc.getGadgetURI());
                    assertEquals(Color.color2, pc.getColor());
                    assertEquals(prefs, pc.getUserPrefs());
                }
                count.incrementAndGet();
            }
        });
        assertEquals(2, count.get());
    }

    public void testUpdateGadgetColor()
    {
        portletConfigurationStore.addGadget(PAGE1_ID, 10020L, 3, 4, URI.create("http://www.google.com"), Color.color5, Collections.<String, String>emptyMap());

        final PortletConfiguration pc = portletConfigurationStore.getByPortletId(10020L);
        assertEquals(Color.color5, pc.getColor());

        portletConfigurationStore.updateGadgetColor(10020L, Color.color3);

        final PortletConfiguration updatedPc = portletConfigurationStore.getByPortletId(10020L);
        assertEquals(Color.color3, updatedPc.getColor());

        try
        {
            portletConfigurationStore.updateGadgetColor(-999L, Color.color2);
            fail("Should have thrown exception");
        }
        catch (DataAccessException e)
        {
            //yay
        }
    }

    public void testUpdateGadgetPosition()
    {
        portletConfigurationStore.addGadget(PAGE1_ID, 10020L, 3, 4, URI.create("http://www.google.com"), Color.color5, Collections.<String, String>emptyMap());

        final PortletConfiguration pc = portletConfigurationStore.getByPortletId(10020L);
        assertEquals(PAGE1_ID, pc.getDashboardPageId());
        assertEquals(4, pc.getRow().intValue());
        assertEquals(3, pc.getColumn().intValue());

        portletConfigurationStore.updateGadgetPosition(10020L, 0, 1, PAGE1_ID);

        final PortletConfiguration updatedPc = portletConfigurationStore.getByPortletId(10020L);
        assertEquals(PAGE1_ID, updatedPc.getDashboardPageId());
        assertEquals(0, updatedPc.getRow().intValue());
        assertEquals(1, updatedPc.getColumn().intValue());

        try
        {
            portletConfigurationStore.updateGadgetPosition(-999L, 0, 0, PAGE1_ID);
            fail("Should have thrown exception");
        }
        catch (DataAccessException e)
        {
            //yay
        }

        portletConfigurationStore.updateGadgetPosition(10020L, 2, 5, PAGE2_ID);
        final PortletConfiguration movedPc = portletConfigurationStore.getByPortletId(10020L);
        assertEquals(PAGE2_ID, movedPc.getDashboardPageId());
        assertEquals(2, movedPc.getRow().intValue());
        assertEquals(5, movedPc.getColumn().intValue());
        final List<PortletConfiguration> page1Gadgets = portletConfigurationStore.getByPortalPage(PAGE1_ID);
        assertEquals(0, page1Gadgets.size());
        final List<PortletConfiguration> page2Gadgets = portletConfigurationStore.getByPortalPage(PAGE2_ID);
        assertEquals(movedPc, page2Gadgets.get(0));
    }

    //test special case for Oracle (JRA-18125) where empty strings come back as null.
    public void testGetUserPrefs()
    {
        final OfBizDelegator delegator = createMock(OfBizDelegator.class);
        final JiraPropertySetFactory jiraPropertySetFactory = createMock(JiraPropertySetFactory.class);
        final PortletAccessManager portletAccessManager = createMock(PortletAccessManager.class);

        List<GenericValue> results = CollectionBuilder.<GenericValue>newBuilder(
                new MockGenericValue("GadgetUserPreference", MapBuilder.newBuilder().add("portletconfiguration", 10000L).add("userprefkey", "key1").add("userprefvalue", "val1").toMap()),
                new MockGenericValue("GadgetUserPreference", MapBuilder.newBuilder().add("portletconfiguration", 10000L).add("userprefkey", "key2").add("userprefvalue", null).toMap())
        ).asList();

        expect(delegator.findByAnd("GadgetUserPreference", MapBuilder.<String, Object>newBuilder().add("portletconfiguration", 10000L).toMap())).
                andReturn(results);

        replay(delegator, jiraPropertySetFactory, portletAccessManager);
        final OfbizPortletConfigurationStore store = new OfbizPortletConfigurationStore(delegator, jiraPropertySetFactory, portletAccessManager);
        final Map<String, String> prefs = store.getUserPreferences(10000L);

        assertEquals("val1", prefs.get("key1"));
        assertNotNull(prefs.get("key2"));
        assertEquals("", prefs.get("key2"));

        verify(delegator, jiraPropertySetFactory, portletAccessManager);
    }


    private void assertEquals(PortletConfiguration configuration1, PortletConfiguration configuration2)
    {
        assertEquals(configuration1.getColumn(), configuration2.getColumn());
        assertEquals(configuration1.getId(), configuration2.getId());
        assertEquals(configuration1.getDashboardPageId(), configuration2.getDashboardPageId());
        assertEquals(configuration1.getKey(), configuration2.getKey());
        assertEquals(configuration1.getRow(), configuration2.getRow());
    }

    private void assertEquals(List /*<PortletConfiguration>*/ expectedList, List /*<PortletConfiguration>*/ actualList)
    {
        assertEquals("Configuration lists have diferent size.", expectedList.size(), actualList.size());
        for (int i = 0; i < expectedList.size(); i++)
        {
            assertEquals((PortletConfiguration) expectedList.get(i), (PortletConfiguration) actualList.get(i));
        }
    }
}
