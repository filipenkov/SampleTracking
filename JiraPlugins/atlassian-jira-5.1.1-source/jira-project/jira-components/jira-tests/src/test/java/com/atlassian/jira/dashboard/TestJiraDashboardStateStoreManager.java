package com.atlassian.jira.dashboard;

import com.atlassian.gadgets.dashboard.spi.DashboardStateStoreException;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.DashboardState;
import static com.atlassian.gadgets.dashboard.DashboardState.dashboard;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.jira.portal.MockPropertySet;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.portal.PortalPageStore;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationImpl;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestJiraDashboardStateStoreManager extends MockControllerTestCase
{
    @Test
    public void testRetrieveNullId()
    {
        final JiraDashboardStateStoreManager stateStore = mockController.instantiate(JiraDashboardStateStoreManager.class);
        try
        {
            stateStore.retrieve(null);
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            //yay
        }
    }

    @Test
    public void testRetrieveDashboardNotFound()
    {
        final PortalPageStore mockPortalPageStore = mockController.getMock(PortalPageStore.class);
        mockPortalPageStore.getPortalPage(10020L);
        mockController.setReturnValue(null);

        final JiraDashboardStateStoreManager stateStore = mockController.instantiate(JiraDashboardStateStoreManager.class);
        try
        {
            stateStore.retrieve(DashboardId.valueOf(Long.toString(10020)));
            fail("Should have thrown an DashboardNotFoundException");
        }
        catch (DashboardNotFoundException e)
        {
            //yay
        }

    }

    @Test
    public void testRetrieveSuccess()
    {
        final PortalPageStore mockPortalPageStore = mockController.getMock(PortalPageStore.class);
        mockPortalPageStore.getPortalPage(10020L);
        mockController.setReturnValue(PortalPage.id(10020L).name("Test Dashboard").description("").owner("admin").favouriteCount(0L).layout(Layout.AA).version(0L).build());

        final Map<String, String> prefs = MapBuilder.<String, String>newBuilder().add("pref1", "value1").add("pref2", "value2").toMap();
        final PortalPageManager mockPortalPageManager = mockController.getMock(PortalPageManager.class);
        expect(mockPortalPageManager.getPortletConfigurations(10020L)).andReturn(getSortedPortletConfigurationMocks(prefs));

        final VelocityRequestContext mockVelocityRequestContext = mockController.getMock(VelocityRequestContext.class);
        mockVelocityRequestContext.getCanonicalBaseUrl();
        mockController.setDefaultReturnValue("http://jira.atlassian.com");

        final VelocityRequestContextFactory mockVelocityRequestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);
        mockVelocityRequestContextFactory.getJiraVelocityRequestContext();
        mockController.setDefaultReturnValue(mockVelocityRequestContext);

        final JiraDashboardStateStoreManager stateStore = mockController.instantiate(JiraDashboardStateStoreManager.class);
        final DashboardState state = stateStore.retrieve(DashboardId.valueOf(Long.toString(10020)));

        assertEquals(DashboardId.valueOf(Long.toString(10020)), state.getId());
        assertEquals("Test Dashboard", state.getTitle());
        assertEquals(Layout.AA, state.getLayout());
        final Iterable<GadgetState> firstColumn = state.getGadgetsInColumn(DashboardState.ColumnIndex.ZERO);
        final Iterator<GadgetState> firstColumnIterator = firstColumn.iterator();
        final GadgetState firstRow = firstColumnIterator.next();
        final GadgetState secondRow = firstColumnIterator.next();
        assertFalse(firstColumnIterator.hasNext());
        final Iterable<GadgetState> secondColumn = state.getGadgetsInColumn(DashboardState.ColumnIndex.ONE);
        final Iterator<GadgetState> secondColumnIterator = secondColumn.iterator();
        final GadgetState secondColfirstRow = secondColumnIterator.next();
        assertFalse(secondColumnIterator.hasNext());

        assertEquals(GadgetId.valueOf(Long.toString(10011)), firstRow.getId());
        assertEquals(Color.color1, firstRow.getColor());
        assertEquals(URI.create("http://www.google.com/"), firstRow.getGadgetSpecUri());
        assertEquals(Collections.<String, String>emptyMap(), firstRow.getUserPrefs());

        assertEquals(GadgetId.valueOf(Long.toString(10012)), secondRow.getId());
        assertEquals(Color.color2, secondRow.getColor());
        assertEquals(URI.create("/gadgets/sample.xml"), secondRow.getGadgetSpecUri());
        assertEquals(prefs, secondRow.getUserPrefs());

        assertEquals(GadgetId.valueOf(Long.toString(10231)), secondColfirstRow.getId());
        assertEquals(Color.color5, secondColfirstRow.getColor());
        assertEquals(URI.create("http://www.msn.com/"), secondColfirstRow.getGadgetSpecUri());
        assertEquals(Collections.<String, String>emptyMap(), secondColfirstRow.getUserPrefs());
    }

    @Test
    public void testStoreWithNullState()
    {
        final JiraDashboardStateStoreManager stateStore = mockController.instantiate(JiraDashboardStateStoreManager.class);
        try
        {
            stateStore.update(null, Collections.<DashboardChange>emptyList());
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            //yay
        }
        try
        {
            DashboardState state = dashboard(DashboardId.valueOf(Long.toString(10020))).title("My Dashboard").layout(Layout.AA).build();
            stateStore.update(state, null);
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            //yay
        }
    }

    @Test
    public void testStoreDashboardDoesntExist()
    {
        final PortalPageStore mockPortalPageStore = mockController.getMock(PortalPageStore.class);
        expect(mockPortalPageStore.updatePortalPageOptimisticLock(10020L, 0L)).andReturn(false);

        final JiraDashboardStateStoreManager stateStore = mockController.instantiate(JiraDashboardStateStoreManager.class);
        try
        {
            final DashboardState state = dashboard(DashboardId.valueOf(Long.toString(10020))).title("Test Dashboard").build();
            stateStore.update(state, Collections.<DashboardChange>emptyList());
            fail("Should have thrown an DashboardStateStore");
        }
        catch (DashboardStateStoreException e)
        {
            //yay
        }

    }

    @Test
    public void testStoreDashboardWithPortalPageUpdate()
    {
        final AtomicBoolean retrieveCalled = new AtomicBoolean(false);
        final PortalPageStore mockPortalPageStore = mockController.getMock(PortalPageStore.class);
        expect(mockPortalPageStore.updatePortalPageOptimisticLock(10020L, 0L)).andReturn(true);
        expect(mockPortalPageStore.getPortalPage(10020L)).andReturn(PortalPage.id(10020L).name("Test Dashboard").description("").owner("admin").favouriteCount(0L).layout(Layout.AA).version(0L).build());

        final PortalPage newPortalPage = PortalPage.id(10020L).name("My Dashboard").description("").owner("admin").favouriteCount(0L).layout(Layout.AAA).version(0L).build();
        expect(mockPortalPageStore.update(eqPortalPage(newPortalPage))).andReturn(newPortalPage);

        final Map<String, String> prefs = MapBuilder.<String, String>newBuilder().add("pref1", "value1").add("pref2", "value2").toMap();        
        final PortletConfigurationStore mockPortletConfigurationStore = mockController.getMock(PortletConfigurationStore.class);
        expect(mockPortletConfigurationStore.getByPortalPage(10020L)).andReturn(getPortletConfigurationMocks(prefs));

        mockPortletConfigurationStore.store(eqPortletConfiguration(new PortletConfigurationImpl(10011L, 10020L, 1, 0, URI.create("http://www.google.com/"), Color.color6, Collections.<String, String>emptyMap())));
        expect(mockPortletConfigurationStore.addGadget(10020L, 10019L, 1, 1, URI.create("http://www.newgadget.com/"), Color.color2, Collections.<String, String>emptyMap())).andReturn(null);
        //don't really care what this returns for the purpose of this test.
        mockPortletConfigurationStore.store(eqPortletConfiguration(new PortletConfigurationImpl(10231L, 10020L, 2, 0, URI.create("http://www.msn.com/"), Color.color5, Collections.<String, String>emptyMap())));
        mockPortletConfigurationStore.delete(eqPortletConfiguration(new PortletConfigurationImpl(10012L, 10020L, 0, 1, URI.create("/gadgets/sample.xml"), Color.color2, prefs)));

        mockController.replay();

        //Define the new dashboard.
        GadgetState gadget1 = GadgetState.gadget(GadgetId.valueOf(Long.toString(10011))).specUri(URI.create("http://www.google.com/")).color(Color.color6).build();
        GadgetState gadget3 = GadgetState.gadget(GadgetId.valueOf(Long.toString(10019))).specUri(URI.create("http://www.newgadget.com/")).color(Color.color2).build();
        GadgetState gadget4 = GadgetState.gadget(GadgetId.valueOf(Long.toString(10231))).specUri(URI.create("http://www.msn.com/")).color(Color.color5).build();

        final List<GadgetState> col1 = Collections.emptyList();
        final List<GadgetState> col2 = CollectionBuilder.newBuilder(gadget1, gadget3).asList();
        final List<GadgetState> col3 = CollectionBuilder.newBuilder(gadget4).asList();
        final List<List<GadgetState>> columns = CollectionBuilder.newBuilder(col1, col2, col3).asList();
        final DashboardState state = dashboard(DashboardId.valueOf(Long.toString(10020))).title("My Dashboard").layout(Layout.AAA).columns(columns).build();

        final JiraDashboardStateStoreManager stateStore = new JiraDashboardStateStoreManager(mockPortalPageStore, mockPortletConfigurationStore, null)
        {
            @Override
            public DashboardState retrieve(final DashboardId dashboardId)
                    throws DashboardNotFoundException, DashboardStateStoreException
            {
                retrieveCalled.set(true);
                return state;
            }
        };

        final DashboardState newState = stateStore.update(state, Collections.<DashboardChange>emptyList());

        assertEquals(state, newState);
        assertTrue(retrieveCalled.get());
    }

    @Test
    public void testFindGagdetByIdDoesntExist()
    {
        final PortletConfigurationStore mockPortletConfigurationStore = mockController.getMock(PortletConfigurationStore.class);
        expect(mockPortletConfigurationStore.getByPortletId(1L)).andReturn(null);

        final JiraDashboardStateStoreManager stateStore = mockController.instantiate(JiraDashboardStateStoreManager.class);
        try
        {
            stateStore.findDashboardWithGadget(GadgetId.valueOf("1"));
            fail("Should have thrown exception.");
        }
        catch (DashboardStateStoreException e)
        {
            //yay
        }
    }

    @Test
    public void testFindGagdetByIdDashboardDoesntExist()
    {
        final PortletConfigurationStore mockPortletConfigurationStore = mockController.getMock(PortletConfigurationStore.class);
        final PortletConfigurationImpl pc = new PortletConfigurationImpl(10027L, 10020L, 0, 0, null, Color.color8, Collections.<String, String>emptyMap());
        expect(mockPortletConfigurationStore.getByPortletId(1L)).andReturn(pc);
        final PortalPageStore mockPortalPageStore = mockController.getMock(PortalPageStore.class);
        expect(mockPortalPageStore.getPortalPage(10020L)).andReturn(null);

        final JiraDashboardStateStoreManager stateStore = mockController.instantiate(JiraDashboardStateStoreManager.class);
        try
        {
            stateStore.findDashboardWithGadget(GadgetId.valueOf("1"));
            fail("Should have thrown exception.");
        }
        catch (DashboardNotFoundException e)
        {
            //yay
        }
    }

    @Test
    public void testFindGagdetByIdSuccess()
    {
        final PortletConfigurationStore mockPortletConfigurationStore = mockController.getMock(PortletConfigurationStore.class);
        final PortletConfigurationImpl pc = new PortletConfigurationImpl(10027L, 10020L, 0, 0, null, Color.color8, Collections.<String, String>emptyMap());
        expect(mockPortletConfigurationStore.getByPortletId(1L)).andReturn(pc);

        final DashboardState state = dashboard(DashboardId.valueOf(Long.toString(10020))).title("My Dashboard").layout(Layout.AAA).build();
        mockController.replay();
        final JiraDashboardStateStoreManager stateStore = new JiraDashboardStateStoreManager(null, mockPortletConfigurationStore, null)
        {
            @Override
            public DashboardState retrieve(final DashboardId dashboardId)
                    throws DashboardNotFoundException, DashboardStateStoreException
            {
                return state;
            }
        };
        final DashboardState retrievedState = stateStore.findDashboardWithGadget(GadgetId.valueOf("1"));
        assertEquals(state, retrievedState);
    }

    @Test
    public void testRemoveNullDashboardId()
    {
        final JiraDashboardStateStoreManager stateStore = mockController.instantiate(JiraDashboardStateStoreManager.class);
        try
        {
            stateStore.remove(null);
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            //yay
        }
    }

    @Test
    public void testRemoveDashboard()
    {
        final PortalPageManager mockPortalPageManager = mockController.getMock(PortalPageManager.class);
        mockPortalPageManager.delete(10020L);

        final JiraDashboardStateStoreManager stateStore = mockController.instantiate(JiraDashboardStateStoreManager.class);

        stateStore.remove(DashboardId.valueOf(Long.toString(10020)));

        //really just asserting that the right method is called with the right param.
    }

    public List<PortletConfiguration> getPortletConfigurationMocks(final Map<String, String> prefs)
    {
        final PortletConfiguration pc1Gadget = new PortletConfigurationImpl(10011L, 10020L, 0, 0, URI.create("http://www.google.com/"), Color.color1, Collections.<String, String>emptyMap());
        final PortletConfiguration pc2Gadget = new PortletConfigurationImpl(10012L, 10020L, 0, 1, URI.create("/gadgets/sample.xml"), Color.color2, prefs);
        final PortletConfiguration pc4Gadget = new PortletConfigurationImpl(10231L, 10020L, 1, 1, URI.create("http://www.msn.com/"), Color.color5, Collections.<String, String>emptyMap());

        return CollectionBuilder.newBuilder(pc1Gadget, pc2Gadget, pc4Gadget).asList();
    }

    public List<List<PortletConfiguration>> getSortedPortletConfigurationMocks(final Map<String, String> prefs)
    {
        final List<List<PortletConfiguration>> ret = new ArrayList<List<PortletConfiguration>>();
        ret.add(new ArrayList<PortletConfiguration>());
        ret.add(new ArrayList<PortletConfiguration>());
        final List<PortletConfiguration> pcs = getPortletConfigurationMocks(prefs);
        for (PortletConfiguration pc : pcs)
        {
            ret.get(pc.getColumn()).add(pc);
        }

        return ret;
    }

    public static PortalPage eqPortalPage(PortalPage in)
    {
        EasyMock.reportMatcher(new PortalPageMatcher(in));
        return in;
    }

    public static PortletConfiguration eqPortletConfiguration(PortletConfiguration in)
    {
        EasyMock.reportMatcher(new PortletConfigurationMatcher(in));
        return in;
    }


    private static class PortalPageMatcher implements IArgumentMatcher
    {
        private final PortalPage expected;

        private PortalPageMatcher(PortalPage expected)
        {
            this.expected = expected;
        }

        public void appendTo(final StringBuffer buffer)
        {
            buffer.append("eqPortalPage(").
                    append(expected);
        }

        public boolean matches(final Object o)
        {
            if (o instanceof PortalPage)
            {
                PortalPage other = (PortalPage) o;
                return other.getId().equals(expected.getId()) &&
                        other.getName().equals(expected.getName()) &&
                        other.getLayout().equals(expected.getLayout()) &&
                        other.getOwnerUserName().equals(expected.getOwnerUserName());
            }
            return false;
        }
    }

    private static class PortletConfigurationMatcher implements IArgumentMatcher
    {
        private final PortletConfiguration expected;

        public PortletConfigurationMatcher(final PortletConfiguration expected)
        {
            this.expected = expected;
        }

        public void appendTo(final StringBuffer buffer)
        {
            buffer.append("eqPortletConfiguration(").
                    append(expected);
        }

        public boolean matches(final Object o)
        {
            if (o instanceof PortletConfiguration)
            {
                PortletConfiguration other = (PortletConfiguration) o;
                final boolean res = other.getId().equals(expected.getId()) &&
                        other.getColor().equals(expected.getColor()) &&
                        other.getColumn().equals(expected.getColumn()) &&
                        other.getRow().equals(expected.getRow()) &&
                        other.getUserPrefs().equals(expected.getUserPrefs()) &&
                        other.getDashboardPageId().equals(expected.getDashboardPageId());
                boolean gadgetUriEqual = other.getGadgetURI() != null ? other.getGadgetURI().equals(expected.getGadgetURI()) : expected.getGadgetURI() == null;

                return res && gadgetUriEqual;
            }
            return false;
        }
    }
}
