package com.atlassian.jira.admin;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.webfragment.DefaultSimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.user.UserAdminHistoryManager;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.opensymphony.user.User;
import org.easymock.classextension.EasyMock;

import java.util.List;

/**
 * @since v4.1
 */
public class TestAdminHistoryLinkFactory extends MockControllerTestCase
{
    private UserAdminHistoryManager historyManager;
    private User user;

    private AdminHistoryLinkFactory linkFactory;
    private PluginAccessor pluginAccessor;
    private DefaultSimpleLinkManager simpleLinkManager;
    private ApplicationProperties applicationProperties;


    @Before
    public void setUp() throws Exception
    {

        historyManager = mockController.getMock(UserAdminHistoryManager.class);
        pluginAccessor = mockController.getMock(PluginAccessor.class);
        simpleLinkManager = mockController.getMock(DefaultSimpleLinkManager.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);

        final MockProviderAccessor mpa = new MockProviderAccessor();
        user = new User("admin", mpa, new MockCrowdService());

        linkFactory = new AdminHistoryLinkFactory(historyManager, simpleLinkManager, pluginAccessor, applicationProperties);
    }

    @After
    public void tearDown() throws Exception
    {
        linkFactory = null;
        user = null;
        historyManager = null;

    }

    @Test
    public void testNullUSerNullHistory()
    {
        EasyMock.expect(historyManager.getAdminPageHistoryWithoutPermissionChecks(null)).andReturn(null);

        EasyMock.expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS)).andReturn("5");

        mockController.replay();

        assertTrue(linkFactory.getLinks(null, null).isEmpty());

        mockController.verify();
    }

    @Test
    public void testNullHistory()
    {
        EasyMock.expect(historyManager.getAdminPageHistoryWithoutPermissionChecks(user)).andReturn(null);

        EasyMock.expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS)).andReturn("5");


        mockController.replay();

        assertTrue(linkFactory.getLinks(user, null).isEmpty());

        mockController.verify();
    }

    @Test
    public void testEmptyHistory()
    {
        EasyMock.expect(historyManager.getAdminPageHistoryWithoutPermissionChecks(user)).andReturn(CollectionBuilder.<UserHistoryItem>newBuilder().asList());

        EasyMock.expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS)).andReturn("5");

        mockController.replay();

        assertTrue(linkFactory.getLinks(user, null).isEmpty());

        mockController.verify();
    }


    @Test
    public void testOneHistory()
    {
        final UserHistoryItem history = mockController.getMock(UserHistoryItem.class);
        final SimpleLink link = new SimpleLinkImpl("admin_link_1", "Admin Link", "Admin Link title", null, "admin-item-link", "/admin/url", null);
        final SimpleLinkSection mockLinkSection = mockController.getMock(SimpleLinkSection.class);

        EasyMock.expect(historyManager.getAdminPageHistoryWithoutPermissionChecks(user))
                .andReturn(CollectionBuilder.newBuilder(history).asList());


        Plugin plugin = mockController.getMock(Plugin.class);

        EasyMock.expect(simpleLinkManager.getSectionsForLocation(EasyMock.eq("system.admin"), EasyMock.eq(user), EasyMock.isA(JiraHelper.class)))
                .andReturn(CollectionBuilder.<SimpleLinkSection>newBuilder(mockLinkSection).asList());
        EasyMock.expect(mockLinkSection.getId())
                .andReturn("admin_section");

        EasyMock.expect(simpleLinkManager.getLinksForSection(EasyMock.eq("system.admin/admin_section"), EasyMock.eq(user), EasyMock.isA(JiraHelper.class)))
                .andReturn(CollectionBuilder.<SimpleLink>newBuilder(link).asList());
        EasyMock.expect(history.getEntityId())
                .andReturn("admin_link_1");
        EasyMock.expect(history.getData())
                .andReturn("/admin/url");

        EasyMock.expect(plugin.getKey())
                .andReturn("");
        EasyMock.expect(pluginAccessor.getPlugin("jira.top.navigation.bar"))
                .andReturn(plugin);
        EasyMock.expect(pluginAccessor.isPluginEnabled(isA(String.class)))
                .andReturn(false);

        EasyMock.expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS)).andReturn("5");

        mockController.replay();

        final List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        final List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testMultipleHistory()
    {
        final UserHistoryItem history = mockController.getMock(UserHistoryItem.class);
        final UserHistoryItem history2 = mockController.getMock(UserHistoryItem.class);
        final SimpleLink link = new SimpleLinkImpl("admin_link_1", "Admin Link", "Admin Link title", null, "admin-item-link", "/admin/url", null);
        final SimpleLink link2 = new SimpleLinkImpl("admin_link_2", "Admin Link 2", "Admin Link title 2", null, "admin-item-link", "/admin/url2", null);
        final SimpleLinkSection mockLinkSection = mockController.getMock(SimpleLinkSection.class);

        EasyMock.expect(historyManager.getAdminPageHistoryWithoutPermissionChecks(user))
                .andReturn(CollectionBuilder.newBuilder(history, history2).asList());


        final Plugin plugin = mockController.getMock(Plugin.class);


        EasyMock.expect(simpleLinkManager.getSectionsForLocation(EasyMock.eq("system.admin"), EasyMock.eq(user), EasyMock.isA(JiraHelper.class)))
                .andReturn(CollectionBuilder.<SimpleLinkSection>newBuilder(mockLinkSection).asList());
        EasyMock.expect(mockLinkSection.getId())
                .andReturn("admin_section");


        EasyMock.expect(simpleLinkManager.getLinksForSection(EasyMock.eq("system.admin/admin_section"), EasyMock.eq(user), EasyMock.isA(JiraHelper.class)))
                .andReturn(CollectionBuilder.<SimpleLink>newBuilder(link, link2).asList());

        EasyMock.expect(history.getEntityId())
                .andReturn("admin_link_1");
        EasyMock.expect(history.getData())
                .andReturn("/admin/url");

        EasyMock.expect(history2.getEntityId())
                .andReturn("admin_link_2");
        EasyMock.expect(history2.getData())
                .andReturn("/admin/url2");


        EasyMock.expect(mockLinkSection.getId())
                .andReturn("admin_section");

        EasyMock.expect(simpleLinkManager.getLinksForSection(EasyMock.eq("system.admin/admin_section"), EasyMock.eq(user), EasyMock.isA(JiraHelper.class)))
                .andReturn(CollectionBuilder.<SimpleLink>newBuilder(link2).asList());

        EasyMock.expect(history2.getEntityId())
                .andReturn("admin_link_2");
        EasyMock.expect(history2.getData())
                .andReturn("/admin/url2");

        EasyMock.expect(plugin.getKey())
                .andReturn("");
        EasyMock.expect(pluginAccessor.getPlugin("jira.top.navigation.bar"))
                .andReturn(plugin);
        EasyMock.expect(pluginAccessor.isPluginEnabled(isA(String.class)))
                .andReturn(false);

        EasyMock.expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS)).andReturn("5");


        mockController.replay();

        final List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        final List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link, link2).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }


}
