package com.atlassian.jira.portal;

import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import mock.user.MockOSUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestFavouriteDashboardLinkFactory extends MockControllerTestCase
{
    private VelocityRequestContext requestContext;
    private VelocityRequestContextFactory requestContextFactory;
    private PortalPageService portalPageService;
    private UserHistoryManager userHistoryManager;
    private I18nHelper.BeanFactory i18nFactory;
    private I18nHelper i18n;
    private com.opensymphony.user.User user;

    private FavouriteDashboardLinkFactory linkFactory;


    @Before
    public void setUp() throws Exception
    {

        requestContext = mockController.getMock(VelocityRequestContext.class);
        requestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);
        i18nFactory = mockController.getMock(I18nHelper.BeanFactory.class);
        i18n = mockController.getMock(I18nHelper.class);
        portalPageService = mockController.getMock(PortalPageService.class);
        userHistoryManager = mockController.getMock(UserHistoryManager.class);

        user = new MockOSUser("admin");

        linkFactory = new FavouriteDashboardLinkFactory(portalPageService, requestContextFactory, i18nFactory, userHistoryManager);
    }

    @After
    public void tearDown() throws Exception
    {
        requestContext = null;
        requestContextFactory = null;
        linkFactory = null;
        user = null;
        i18nFactory = null;
        i18n = null;
        portalPageService = null;
        userHistoryManager = null;

    }


    @Test
    public void testNullUserNullDashboards()
    {
        SimpleLink link = new SimpleLinkImpl("dash_lnk_system", "View System Dashboard", "View System Dashboard Title", null, null, "/secure/Dashboard.jspa", null);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        portalPageService.getFavouritePortalPages(null);
        mockController.setReturnValue(null);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18nFactory.getInstance((com.opensymphony.user.User) null);
        mockController.setReturnValue(i18n);

        i18n.getText("menu.dashboard.view.system");
        mockController.setReturnValue("View System Dashboard");

        i18n.getText("menu.dashboard.view.system.title");
        mockController.setReturnValue("View System Dashboard Title");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(null, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testNullUserEmptyDashboards()
    {
        SimpleLink link = new SimpleLinkImpl("dash_lnk_system", "View System Dashboard", "View System Dashboard Title", null, null, "/secure/Dashboard.jspa", null);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        portalPageService.getFavouritePortalPages(null);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18nFactory.getInstance((com.opensymphony.user.User) null);
        mockController.setReturnValue(i18n);

        i18n.getText("menu.dashboard.view.system");
        mockController.setReturnValue("View System Dashboard");

        i18n.getText("menu.dashboard.view.system.title");
        mockController.setReturnValue("View System Dashboard Title");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(null, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testNullDashboards()
    {
        SimpleLink link = new SimpleLinkImpl("dash_lnk_system", "View System Dashboard", "View System Dashboard Title", null, null, "/jira/secure/Dashboard.jspa", null);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(null);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        i18n.getText("menu.dashboard.view.system");
        mockController.setReturnValue("View System Dashboard");

        i18n.getText("menu.dashboard.view.system.title");
        mockController.setReturnValue("View System Dashboard Title");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testEmptyDashboards()
    {
        SimpleLink link = new SimpleLinkImpl("dash_lnk_system", "View System Dashboard", "View System Dashboard Title", null, null, "/jira/secure/Dashboard.jspa", null);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        i18n.getText("menu.dashboard.view.system");
        mockController.setReturnValue("View System Dashboard");

        i18n.getText("menu.dashboard.view.system.title");
        mockController.setReturnValue("View System Dashboard Title");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testNoSessionNullUser()
    {
        SimpleLink link = new SimpleLinkImpl("dash_lnk_1", "Portal Page 1", "Portal Page 1 - Portal Description 1", null, null, "/secure/Dashboard.jspa?selectPageId=1", null);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("Portal Page 1").description("Portal Description 1").owner("admin").favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18nFactory.getInstance((com.opensymphony.user.User) null);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, null);
        mockController.setReturnValue(Collections.<String>emptyList());

        i18n.getText("menu.dashboard.title", "Portal Page 1", "Portal Description 1");
        mockController.setReturnValue("Portal Page 1 - Portal Description 1");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(null, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testDiffInSession()
    {
        SimpleLink link = new SimpleLinkImpl("dash_lnk_1", "Portal Page 1", "Portal Page 1 - Portal Description 1", null, null, "/jira/secure/Dashboard.jspa?selectPageId=1", null);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("Portal Page 1").description("Portal Description 1").owner("admin").favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "2")));

        i18n.getText("menu.dashboard.title", "Portal Page 1", "Portal Description 1");
        mockController.setReturnValue("Portal Page 1 - Portal Description 1");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }


    @Test
    public void testSameInSession()
    {
        SimpleLink link = new SimpleLinkImpl("dash_lnk_1", "Portal Page 1", "Portal Page 1 - Portal Description 1", null, null, "/jira/secure/Dashboard.jspa?selectPageId=1", null);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("Portal Page 1").description("Portal Description 1").owner("admin").favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "1")));

        i18n.getText("menu.dashboard.title", "Portal Page 1", "Portal Description 1");
        mockController.setReturnValue("Portal Page 1 - Portal Description 1");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testSameInSessionWithMulti()
    {
        SimpleLink link = new SimpleLinkImpl("dash_lnk_1", "Portal Page 1", "Portal Page 1 - Portal Description 1", null, "bolded", "/jira/secure/Dashboard.jspa?selectPageId=1", null);
        SimpleLink link2 = new SimpleLinkImpl("dash_lnk_2", "Portal Page 2", "Portal Page 2", null, null, "/jira/secure/Dashboard.jspa?selectPageId=2", null);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("Portal Page 1").description("Portal Description 1").owner("admin").favouriteCount(0L).version(0L).build();
        PortalPage page2 = PortalPage.id(2L).name("Portal Page 2").owner("admin").favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page, page2).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "1"), new UserHistoryItem(UserHistoryItem.DASHBOARD, "2")));

        i18n.getText("menu.dashboard.title", "Portal Page 1", "Portal Description 1");
        mockController.setReturnValue("Portal Page 1 - Portal Description 1");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link, link2).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }


    @Test
    public void testLongLabel()
    {
        SimpleLink link = new SimpleLinkImpl("dash_lnk_1", "123456789012345678901234567890", "123456789012345678901234567890 - Portal Description 1", null, null, "/jira/secure/Dashboard.jspa?selectPageId=1", null);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("123456789012345678901234567890").description("Portal Description 1").owner("admin").favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "1")));

        i18n.getText("menu.dashboard.title", "123456789012345678901234567890", "Portal Description 1");
        mockController.setReturnValue("123456789012345678901234567890 - Portal Description 1");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testTooLongLabel()
    {
        SimpleLink link = new SimpleLinkImpl("dash_lnk_1", "123456789012345678901234567890...", "12345678901234567890123456789012345678901234567890 - Portal Description 1", null, null, "/jira/secure/Dashboard.jspa?selectPageId=1", null);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("12345678901234567890123456789012345678901234567890").description("Portal Description 1").owner("admin").favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "1")));

        i18n.getText("menu.dashboard.title", "12345678901234567890123456789012345678901234567890", "Portal Description 1");
        mockController.setReturnValue("12345678901234567890123456789012345678901234567890 - Portal Description 1");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

}
