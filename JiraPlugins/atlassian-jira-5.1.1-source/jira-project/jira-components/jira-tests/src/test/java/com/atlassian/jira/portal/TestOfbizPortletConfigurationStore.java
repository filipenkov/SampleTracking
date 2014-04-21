package com.atlassian.jira.portal;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import org.ofbiz.core.entity.GenericValue;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

public class TestOfbizPortletConfigurationStore extends LegacyJiraMockTestCase
{
    private PortletConfigurationStore portletConfigurationStore;
    private static final Long PAGE1_ID = 10030L;
    private static final Long PAGE2_ID = 10040L;
    private JiraPropertySetFactory propertySetFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        propertySetFactory = ComponentManager.getComponentInstanceOfType(JiraPropertySetFactory.class);

        portletConfigurationStore = new OfbizPortletConfigurationStore(
                ComponentManager.getComponentInstanceOfType(OfBizDelegator.class),
                propertySetFactory);
    }

    public void testAddGadget()
    {
        final Map<String, String> prefs = ImmutableMap.of("pref1", "value1", "pref2", "value2");
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

        final PortletConfiguration configuration = portletConfigurationStore.addGadget(PAGE1_ID, null, 3, 4, googleUri, Color.color5, prefs);
        portletConfiguration = portletConfigurationStore.getByPortletId(configuration.getId());
        assertEquals(PAGE1_ID, portletConfiguration.getDashboardPageId());
        assertEquals(new Integer(3), portletConfiguration.getColumn());
        assertEquals(new Integer(4), portletConfiguration.getRow());
        assertEquals(googleUri, portletConfiguration.getGadgetURI());
        assertEquals(prefs, portletConfiguration.getUserPrefs());
        assertEquals(Color.color5, portletConfiguration.getColor());
    }

    public void testStore()
    {
        final Map<String, String> prefs = ImmutableMap.of("pref1", "value1", "pref2", "value2");
        final Map<String, String> prefs2 = ImmutableMap.of("pref3", "value3", "pref4", "value4");
        final URI googleUri = URI.create("http://www.google.com");
        final PortletConfiguration portletConfiguration = portletConfigurationStore.addGadget(PAGE1_ID, 10025L, 3, 4, googleUri, Color.color3, prefs);
        assertEquals(PAGE1_ID, portletConfiguration.getDashboardPageId());
        assertEquals(new Long(10025), portletConfiguration.getId());
        assertEquals(new Integer(3), portletConfiguration.getColumn());
        assertEquals(new Integer(4), portletConfiguration.getRow());
        assertEquals(googleUri, portletConfiguration.getGadgetURI());
        assertEquals(prefs, portletConfiguration.getUserPrefs());
        assertEquals(Color.color3, portletConfiguration.getColor());

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

        PortletConfiguration portletConfig = new PortletConfigurationImpl(10026L, PAGE2_ID,1, 2, googleUri, Color.color7, prefs2);
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
        final Map<String, String> prefs = ImmutableMap.of("pref1", "value1");

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

        List<GenericValue> results = ImmutableList.<GenericValue>of(
                new MockGenericValue("GadgetUserPreference", ImmutableMap.of("portletconfiguration", 10000L, "userprefkey", "key1", "userprefvalue", "val1")),
                new MockGenericValue("GadgetUserPreference", MapBuilder.<String, Object>newBuilder().add("portletconfiguration", 10000L).add("userprefkey", "key2").add("userprefvalue", null).toMap())
        );

        expect(delegator.findByAnd("GadgetUserPreference", ImmutableMap.of("portletconfiguration", 10000L))).
                andReturn(results);

        replay(delegator, jiraPropertySetFactory);
        final OfbizPortletConfigurationStore store = new OfbizPortletConfigurationStore(delegator, jiraPropertySetFactory);
        final Map<String, String> prefs = store.getUserPreferences(10000L);

        assertEquals("val1", prefs.get("key1"));
        assertNotNull(prefs.get("key2"));
        assertEquals("", prefs.get("key2"));

        verify(delegator, jiraPropertySetFactory);
    }


    private void assertEquals(PortletConfiguration configuration1, PortletConfiguration configuration2)
    {
        assertEquals(configuration1.getColumn(), configuration2.getColumn());
        assertEquals(configuration1.getId(), configuration2.getId());
        assertEquals(configuration1.getDashboardPageId(), configuration2.getDashboardPageId());
        assertEquals(configuration1.getRow(), configuration2.getRow());
    }
}
