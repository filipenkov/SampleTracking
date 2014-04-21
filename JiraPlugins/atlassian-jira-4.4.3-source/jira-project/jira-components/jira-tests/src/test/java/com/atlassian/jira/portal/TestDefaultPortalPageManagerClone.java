package com.atlassian.jira.portal;

import com.atlassian.gadgets.Vote;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.dashboard.permission.GadgetPermissionManager;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.index.MockSharedEntityIndexer;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.MockControl;
import org.easymock.internal.AlwaysMatcher;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Unit tests for {@link com.atlassian.jira.issue.search.DefaultSearchRequestManager}
 *
 * @since v3.13
 */
public class TestDefaultPortalPageManagerClone extends ListeningTestCase
{
    private User user;
    private MockProviderAccessor mpa;
    private MockControl portalPageStoreCtrl;
    private PortalPageStore portalPageStore;

    private MockControl shareManagerCtrl;
    private ShareManager shareManager;

    private MockControl portletAccessManagerCtrl;
    private PortletAccessManager portletAccessManager;

    private MockControl portletConfigurationManagerCtrl;
    private PortletConfigurationManager portletConfigurationManager;

    private SharePermission perm2;
    private SharePermission perm1;

    private PortalPage portalPage1;
    private PortalPage portalPage2;
    private PortalPage portalPageWithPortlets;

    private MockControl gadgetPermissionManagerCtrl;
    private GadgetPermissionManager gadgetPermissionManager;

    private static final Long DEVIL_ID = new Long(666);
    private static final String DASHBOARD = "dashboard";
    private static final String OWEN = "owen";
    private static URI GADGET_XML;

    @Before
    public void setUp() throws Exception
    {
        GADGET_XML = new URI("http://atlassian.com");

        portalPageStoreCtrl = MockControl.createStrictControl(PortalPageStore.class);
        portalPageStore = (PortalPageStore) portalPageStoreCtrl.getMock();

        shareManagerCtrl = MockControl.createStrictControl(ShareManager.class);
        shareManager = (ShareManager) shareManagerCtrl.getMock();

        portletAccessManagerCtrl = MockControl.createStrictControl(PortletAccessManager.class);
        portletAccessManager = (PortletAccessManager) portletAccessManagerCtrl.getMock();

        portletConfigurationManagerCtrl = MockControl.createStrictControl(PortletConfigurationManager.class);
        portletConfigurationManager = (PortletConfigurationManager) portletConfigurationManagerCtrl.getMock();


        gadgetPermissionManagerCtrl = MockControl.createStrictControl(GadgetPermissionManager.class);
        gadgetPermissionManager = (GadgetPermissionManager) gadgetPermissionManagerCtrl.getMock();

        mpa = new MockProviderAccessor();
        user = new User("admin", mpa, new MockCrowdService());
        perm1 = new SharePermissionImpl(GroupShareType.TYPE, "jira-user", null);
        perm2 = new SharePermissionImpl(GlobalShareType.TYPE, null, null);

        portalPage1 = PortalPage.id(1L).name("one").description("one description").owner(user.getName()).build();
        portalPage2 = PortalPage.id(2L).name("two").description("two description").owner(user.getName()).build();
        portalPageWithPortlets = PortalPage.id(DEVIL_ID).name(DASHBOARD).description(DASHBOARD).owner(OWEN).layout(Layout.AA).version(0L).build();

    }

    @After
    public void tearDown() throws Exception
    {
        gadgetPermissionManagerCtrl = null;
        gadgetPermissionManager = null;
    }

    private PortalPageManager createDefaultPortalPageManager()
    {
        portalPageStoreCtrl.replay();
        shareManagerCtrl.replay();
        portletAccessManagerCtrl.replay();
        portletConfigurationManagerCtrl.replay();
        gadgetPermissionManagerCtrl.replay();

        return new DefaultPortalPageManager(shareManager, portalPageStore, portletAccessManager, portletConfigurationManager, new MockSharedEntityIndexer())
        {
            @Override
            GadgetPermissionManager getGadgetPermissionManager()
            {
                return gadgetPermissionManager;
            }
        };
    }

    private void verifyMocks()
    {
        portalPageStoreCtrl.verify();
        shareManagerCtrl.verify();
        portletAccessManagerCtrl.verify();
        portletConfigurationManagerCtrl.verify();
        gadgetPermissionManagerCtrl.verify();
    }

    @Test
    public void test_createWithClone_NullPortalPage() throws Exception
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        try
        {
            portalPageManager.createBasedOnClone(user, null, portalPage2);
            fail("Should not accept null search portalPage.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void test_createWithClone_NullPortalPageClone() throws Exception
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        try
        {
            portalPageManager.createBasedOnClone(null, portalPage2, null);
            fail("Should not accept null search portalPage clone.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void test_createWithClone_NoPerms() throws Exception
    {
        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(Collections.emptyList());

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharedEntity.SharePermissions.PRIVATE).build();

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(SharedEntity.SharePermissions.PRIVATE);

        portletAccessManager.canUserSeePortlet(user, "portlet0");
        portletAccessManagerCtrl.setDefaultMatcher(new AlwaysMatcher());
        portletAccessManagerCtrl.setDefaultReturnValue(true);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
        assertNotNull(portalPage);
        assertEquals(portalPage1, portalPage);
        assertTrue(portalPage.getPermissions().isPrivate());

        verifyMocks();
    }

    @Test
    public void test_createWithClone_hasPerms() throws Exception
    {
        final HashSet permSet1 = new HashSet();
        permSet1.add(perm1);
        permSet1.add(perm2);
        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(permSet1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();

        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(Collections.emptyList());
        
        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permissions);

        portletAccessManager.canUserSeePortlet(user, "portlet0");
        portletAccessManagerCtrl.setDefaultMatcher(new AlwaysMatcher());
        portletAccessManagerCtrl.setDefaultReturnValue(true);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
        assertNotNull(portalPage);
        assertEquals(portalPage1, portalPage);
        assertEquals(permissions, portalPage.getPermissions());

        verifyMocks();

    }


    @Test
    public void test_createWithClone_gadgets_hasPerms() throws Exception
    {

        PortletConfiguration gadget = makePC(DEVIL_ID, 123L, null, 1, 1, null, GADGET_XML);
        PortletConfiguration copyOfGadget = makePC(1L, 123L, null, 1, 1, null, GADGET_XML);

        final HashSet permSet1 = new HashSet();
        permSet1.add(perm1);
        permSet1.add(perm2);
        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(permSet1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();

        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(CollectionBuilder.newBuilder(gadget).asList());

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permissions);

        gadgetPermissionManager.extractModuleKey(GADGET_XML.toASCIIString());
        gadgetPermissionManagerCtrl.setReturnValue("module-key");

        gadgetPermissionManager.voteOn("module-key", (com.atlassian.crowd.embedded.api.User) user);
        gadgetPermissionManagerCtrl.setReturnValue(Vote.ALLOW);

        portletConfigurationManager.addGadget(1L, 1, 1, GADGET_XML, Color.color1,  new HashMap<String, String>());
        portletConfigurationManagerCtrl.setReturnValue(copyOfGadget);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
    }

    @Test
    public void test_createWithClone_gadgets_noPerms() throws Exception
    {

        PortletConfiguration gadget = makePC(DEVIL_ID, 123L, null, 1, 1, null, GADGET_XML);

        final HashSet permSet1 = new HashSet();
        permSet1.add(perm1);
        permSet1.add(perm2);
        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(permSet1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();

        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(CollectionBuilder.newBuilder(gadget).asList());

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permissions);

        gadgetPermissionManager.extractModuleKey(GADGET_XML.toASCIIString());
        gadgetPermissionManagerCtrl.setReturnValue("module-key");

        gadgetPermissionManager.voteOn("module-key", (com.atlassian.crowd.embedded.api.User) user);
        gadgetPermissionManagerCtrl.setReturnValue(Vote.DENY);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
    }

    @Test
    public void test_createWithClone_gadgets_externalGadget() throws Exception
    {

        PortletConfiguration gadget = makePC(DEVIL_ID, 123L, null, 1, 1, null, GADGET_XML);
        PortletConfiguration copyOfGadget = makePC(1L, 123L, null, 1, 1, null, GADGET_XML);

        final HashSet permSet1 = new HashSet();
        permSet1.add(perm1);
        permSet1.add(perm2);
        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(permSet1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();

        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(CollectionBuilder.newBuilder(gadget).asList());

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permissions);

        gadgetPermissionManager.extractModuleKey(GADGET_XML.toASCIIString());
        gadgetPermissionManagerCtrl.setReturnValue(null);

        portletConfigurationManager.addGadget(1L, 1, 1, GADGET_XML, Color.color1,  new HashMap<String, String>());
        portletConfigurationManagerCtrl.setReturnValue(copyOfGadget);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
    }

    @Test
    public void test_createWithClone_CopyPropertySets() throws Exception
    {
        final HashSet permSet1 = new HashSet();
        permSet1.add(perm1);
        permSet1.add(perm2);

        final MockPropertySet mockPropertySetSrc = makeFilledPS();
        final MockPropertySet mockPropertySetTarget = new MockPropertySet();
        final PortletConfiguration expectedPC = makePC(123L, portalPage1.getId().longValue(), "portlet0", 0, 0, mockPropertySetSrc, null);
        final PortletConfiguration createdPC = makePC(567L, portalPage1.getId().longValue(), "portlet0", 0, 0, mockPropertySetTarget, null);

        portalPageWithPortlets = PortalPage.id(DEVIL_ID).name("Name").description("Description").owner("Owner").build();

        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(CollectionBuilder.newBuilder(expectedPC).asList());

        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(permSet1);
        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permissions);

        portletAccessManager.canUserSeePortlet((com.atlassian.crowd.embedded.api.User) user, "portlet0");
        portletAccessManagerCtrl.setDefaultMatcher(new AlwaysMatcher());
        portletAccessManagerCtrl.setDefaultReturnValue(true);

        portletConfigurationManager.addLegacyPortlet(null, null, null, null);
        portletConfigurationManagerCtrl.setDefaultMatcher(new AlwaysMatcher());
        portletConfigurationManagerCtrl.setReturnValue(createdPC);

        portletConfigurationManager.store(null);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
        assertNotNull(portalPage);
        assertSame(portalPage1.getId(), portalPage.getId());
        assertEquals(permissions, portalPage.getPermissions());

        final Map map = mockPropertySetSrc.getMap();
        assertEquals(map.size(), mockPropertySetTarget.getMap().size());
        for (final Iterator iterator = mockPropertySetSrc.getMap().entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final Object targetVal = mockPropertySetTarget.getMap().get(entry.getKey());
            assertNotNull(targetVal);
            assertEquals(String.valueOf(entry.getValue()), String.valueOf(targetVal));

        }
        verifyMocks();
    }

    MockPropertySet makeFilledPS()
    {
        final MockPropertySet mockPropertySet = new MockPropertySet();
        final Map map = mockPropertySet.getMap();
        map.put("string", "val1");
        mockPropertySet.getMap().put("stringbuffer", new StringBuffer("sb"));
        mockPropertySet.getMap().put("long", new Long(1));
        return mockPropertySet;
    }

    PortletConfiguration makePC(final Long id, final Long portalPageId, final String portletKey, final Integer suggestedCol, final Integer suggestRow, final PropertySet propertySet, final URI gadgetXml)
    {
        return new MockPortletConfiguration(id, suggestedCol, suggestRow, portalPageId, portletKey, propertySet, gadgetXml);
    }
}
