package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.favourites.FavouritesStore;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.easymock.MockControl;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Unit tests for {@link com.atlassian.jira.upgrade.tasks.UpgradeTask_Build325}.
 * 
 * @since v3.13
 */
public class TestUpgradeTask_Build325 extends LegacyJiraMockTestCase
{
    private PortalPageManager portalPageManager;
    private MockControl portalPageManagerControl;
    private FavouritesManager favouritesManager;
    private MockControl favouritesManagerControl;
    private FavouritesStore favouritesStore;
    private MockControl favouritesStoreControl;
    private static final Long ID_1 = new Long(1);
    private static final Long ID_2 = new Long(2);
    private static final Long ID_3 = new Long(3);
    private static final Long ID_SYSTEM_DEFAULT = new Long(666);
    private PortalPage page1;
    private PortalPage page2;
    private PortalPage page3;
    private PortalPage pageSystemDefault;
    private static final String UNKNOWN_USERNAME = "Idontknowthisguy";
    private static final Long ID_DASHBOARD = new Long(345);
    private PortalPage pageCalledDashboard;

    protected void setUp() throws Exception
    {
        super.setUp();

        portalPageManagerControl = MockControl.createStrictControl(PortalPageManager.class);
        portalPageManager = (PortalPageManager) portalPageManagerControl.getMock();

        favouritesManagerControl = MockControl.createStrictControl(FavouritesManager.class);
        favouritesManager = (FavouritesManager) favouritesManagerControl.getMock();

        favouritesStoreControl = MockControl.createStrictControl(FavouritesStore.class);
        favouritesStore = (FavouritesStore) favouritesStoreControl.getMock();

        page1 = PortalPage.id(ID_1).name("pag1").description("page1 desc").owner("admin").build();
        page2 = PortalPage.id(ID_2).name("page2").description("page2 desc").owner("admin").build();
        page3 = PortalPage.id(ID_3).name("page3").description("page3 desc").owner("fred").build();
        pageCalledDashboard = PortalPage.id(ID_DASHBOARD).name("dashboard").description("page1 desc").owner("admin").build();
        pageSystemDefault = PortalPage.id(ID_SYSTEM_DEFAULT).name("system default").description("system default desc").build();     
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        UtilsForTests.cleanOFBiz();
    }

    private void replayMocks()
    {
        portalPageManagerControl.replay();
        favouritesManagerControl.replay();
        favouritesStoreControl.replay();
    }

    private void verifyMocks()
    {
        portalPageManagerControl.verify();
        favouritesManagerControl.verify();
        favouritesStoreControl.verify();
    }

    private UpgradeTask_Build325 buildUpgradeTask()
    {
        return buildUpgradeTask(CoreFactory.getGenericDelegator());
    }


    private UpgradeTask_Build325 buildUpgradeTask(final GenericDelegator genericDelegator)
    {
        replayMocks();
        final UpgradeTask_Build325 upgradeTask_build325 = new UpgradeTask_Build325(genericDelegator, favouritesStore, portalPageManager, new MockI18nBean.MockI18nBeanFactory())
        {
            Locale getUserLocale(final String userName) throws GenericEntityException
            {
                if (userName.equalsIgnoreCase(UNKNOWN_USERNAME))
                {
                    throw new GenericEntityException("User not found");
                }
                return ComponentAccessor.getApplicationProperties().getDefaultLocale();
            }
        };
        // some very simple assertions
        assertEquals("325", upgradeTask_build325.getBuildNumber());
        assertNotNull(upgradeTask_build325.getShortDescription());
        return upgradeTask_build325;
    }

    public void testSuccesfullUpgrade() throws Exception
    {
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_1, "username", "admin", "sequence", new Long(1)));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_2, "username", "admin", "sequence", new Long(2)));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_3, "username", "fred", "sequence", new Long(0)));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_DASHBOARD, "username", "admin", "sequence", new Long(0)));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_SYSTEM_DEFAULT));

        // this is our expected list of pages back
        final List expectedList = EasyList.build(pageCalledDashboard, page1, page2, page3);
        for (final Iterator iterator = expectedList.iterator(); iterator.hasNext();)
        {
            final PortalPage portalPage = (PortalPage) iterator.next();

            final String ownerName = portalPage.getOwnerUserName();
            final Long id = portalPage.getId();

            portalPageManager.getPortalPageById(id);
            portalPageManagerControl.setReturnValue(portalPage);

            if (id.equals(ID_DASHBOARD))
            {
                PortalPage updatedPortalPage = PortalPage.portalPage(pageCalledDashboard).name("Dashboard").build();
                portalPageManager.update(updatedPortalPage);
                portalPageManagerControl.setReturnValue(portalPage);
            }
            favouritesStore.addFavourite(ownerName, portalPage);
            favouritesStoreControl.setReturnValue(Boolean.TRUE);
            portalPageManager.adjustFavouriteCount(portalPage, 1);
        }

        // then it updates the system default.
        portalPageManager.getSystemDefaultPortalPage();
        portalPageManagerControl.setReturnValue(pageSystemDefault);

        final PortalPage updatedSystemPage = PortalPage.portalPage(pageSystemDefault).name("System Dashboard").permissions(SharedEntity.SharePermissions.GLOBAL).build();
        portalPageManager.update(updatedSystemPage);
        portalPageManagerControl.setReturnValue(updatedSystemPage);

        portalPageManager.adjustFavouriteCount(updatedSystemPage, 0);

        final UpgradeTask_Build325 task_build325 = buildUpgradeTask();
        task_build325.doUpgrade(false);

        //verifying the mocks makes sure that the portal pages got updated correctly
        verifyMocks();
    }

    public void testCantFindPage() throws Exception
    {
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_1, "username", "admin"));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_2, "username", "admin"));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_3, "username", "fred"));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_SYSTEM_DEFAULT));

        // this is our expected list of pages back
        final List expectedList = EasyList.build(page1, page2, page3);
        for (final Iterator iterator = expectedList.iterator(); iterator.hasNext();)
        {
            final PortalPage portalPage = (PortalPage) iterator.next();

            final String ownerName = portalPage.getOwnerUserName();
            final Long id = portalPage.getId();

            portalPageManager.getPortalPageById(id);
            if (id.longValue() == 1)
            {
                portalPageManagerControl.setReturnValue(null);
            }
            else
            {
                portalPageManagerControl.setReturnValue(portalPage);
                favouritesStore.addFavourite(ownerName, portalPage);
                favouritesStoreControl.setReturnValue(Boolean.TRUE);
                portalPageManager.adjustFavouriteCount(portalPage, 1);
            }
        }

        // then it updates the
        portalPageManager.getSystemDefaultPortalPage();
        portalPageManagerControl.setReturnValue(pageSystemDefault);

        PortalPage updatedSystem = PortalPage.portalPage(pageSystemDefault).name("System Dashboard").permissions(SharedEntity.SharePermissions.GLOBAL).build();
        portalPageManager.update(updatedSystem);
        portalPageManagerControl.setReturnValue(updatedSystem);

        portalPageManager.adjustFavouriteCount(updatedSystem, 0);

        final UpgradeTask_Build325 task_build325 = buildUpgradeTask();
        task_build325.doUpgrade(false);

        //verifying the mocks makes sure that the portal pages got updated correctly
        verifyMocks();
    }

    public void testExceptionAtStartStopsUpgrade() throws Exception
    {
        final GenericDelegator exceptionThrowingDelgator = new GenericDelegator()
        {
            public List findByCondition(final String s, final EntityCondition entityCondition, final Collection collection, final List list) throws GenericEntityException
            {
                throw new GenericEntityException("expected");
            }
        };
        final UpgradeTask_Build325 task_build325 = buildUpgradeTask(exceptionThrowingDelgator);
        try
        {
            task_build325.doUpgrade(false);
            fail("Should have thrown a DataAccessException");
        }
        catch (final DataAccessException daee)
        {
            // expected
        }

        verifyMocks();
    }

    public void testDataAccessExceptionDuringPageUpdate() throws Exception
    {
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_1, "username", "admin"));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_2, "username", "admin"));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_3, "username", "fred"));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_SYSTEM_DEFAULT));

        // this is our expected list of pages back
        final List expectedList = EasyList.build(page1, page2, page3);
        for (final Iterator iterator = expectedList.iterator(); iterator.hasNext();)
        {
            final PortalPage portalPage = (PortalPage) iterator.next();

            final String ownerName = portalPage.getOwnerUserName();
            final Long id = portalPage.getId();

            portalPageManager.getPortalPageById(id);
            portalPageManagerControl.setReturnValue(portalPage);
            favouritesStore.addFavourite(ownerName, portalPage);
            favouritesStoreControl.setReturnValue(Boolean.TRUE);
            portalPageManager.adjustFavouriteCount(portalPage, 1);
            if (id.longValue() == 1)
            {
                favouritesManagerControl.setThrowable(new DataAccessException("expected"));
            }
        }

        // then it updates the
        portalPageManager.getSystemDefaultPortalPage();
        portalPageManagerControl.setReturnValue(pageSystemDefault);

        PortalPage updatedSystem = PortalPage.portalPage(pageSystemDefault).name("System Dashboard").permissions(SharedEntity.SharePermissions.GLOBAL).build();
        portalPageManager.update(updatedSystem);
        portalPageManagerControl.setReturnValue(updatedSystem);

        portalPageManager.adjustFavouriteCount(updatedSystem, 0);

        final UpgradeTask_Build325 task_build325 = buildUpgradeTask();
        task_build325.doUpgrade(false);

        //verifying the mocks makes sure that the portal pages got updated correctly
        verifyMocks();
    }

    public void testCantFindUserAssociatedWithPage() throws Exception
    {
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_1, "username", UNKNOWN_USERNAME));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_2, "username", "admin"));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_3, "username", "fred"));
        EntityUtils.createValue("PortalPage", EasyMap.build("id", ID_SYSTEM_DEFAULT));

        // this is our expected list of pages back
        final List expectedList = EasyList.build(page2, page3);
        for (final Iterator iterator = expectedList.iterator(); iterator.hasNext();)
        {
            final PortalPage portalPage = (PortalPage) iterator.next();

            final String ownerName = portalPage.getOwnerUserName();
            final Long id = portalPage.getId();

            portalPageManager.getPortalPageById(id);
            portalPageManagerControl.setReturnValue(portalPage);
            favouritesStore.addFavourite(ownerName, portalPage);
            favouritesStoreControl.setReturnValue(Boolean.TRUE);
            portalPageManager.adjustFavouriteCount(portalPage, 1);
        }

        // then it updates the
        portalPageManager.getSystemDefaultPortalPage();
        portalPageManagerControl.setReturnValue(pageSystemDefault);

        PortalPage updatedSystem = PortalPage.portalPage(pageSystemDefault).name("System Dashboard").permissions(SharedEntity.SharePermissions.GLOBAL).build();
        portalPageManager.update(updatedSystem);
        portalPageManagerControl.setReturnValue(updatedSystem);

        portalPageManager.adjustFavouriteCount(updatedSystem, 0);

        final UpgradeTask_Build325 task_build325 = buildUpgradeTask();
        task_build325.doUpgrade(false);

        //verifying the mocks makes sure that the portal pages got updated correctly
        verifyMocks();

    }

    public void testExceptionInsideUpdateSystemDefaultPermissions() throws Exception
    {
        portalPageManager.getSystemDefaultPortalPage();
        portalPageManagerControl.setThrowable(new DataAccessException("expected"));

        final UpgradeTask_Build325 task_build325 = buildUpgradeTask();
        task_build325.doUpgrade(false);

        verifyMocks();
    }
}
