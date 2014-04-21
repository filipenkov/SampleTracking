package com.atlassian.jira.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestProjectHistoryLinkFactory extends MockControllerTestCase
{
    private VelocityRequestContext requestContext;
    private VelocityRequestContextFactory requestContextFactory;
    private UserProjectHistoryManager historyManager;
    private I18nHelper.BeanFactory i18nFactory;
    private I18nHelper i18n;
    private User user;

    private ProjectHistoryLinkFactory linkFactory;
    private SimpleLinkFactoryModuleDescriptor mockModuleDescriptor;

    @Before
    public void setUp() throws Exception
    {

        requestContext = mockController.getMock(VelocityRequestContext.class);
        requestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);
        i18nFactory = mockController.getMock(I18nHelper.BeanFactory.class);
        i18n = mockController.getMock(I18nHelper.class);
        historyManager = mockController.getMock(UserProjectHistoryManager.class);
        mockModuleDescriptor = mockController.getMock(SimpleLinkFactoryModuleDescriptor.class);

        user = new MockUser("admin");

        linkFactory = new ProjectHistoryLinkFactory(requestContextFactory, historyManager, i18nFactory);

        mockModuleDescriptor.getSection();
        mockController.setDefaultReturnValue("something");

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
        historyManager = null;

    }

    @Test
    public void testNullUserNullHistory()
    {
        historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, null);
        mockController.setReturnValue(null);

        mockController.replay();
        
        assertTrue(linkFactory.getLinks(null, null).isEmpty());

        mockController.verify();
    }

    @Test
    public void testNullHistory()
    {
        historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user);
        mockController.setReturnValue(null);

        mockController.replay();

        assertTrue(linkFactory.getLinks(user, null).isEmpty());

        mockController.verify();
    }

    @Test
    public void testEmptyHistory()
    {
        historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();

        assertTrue(linkFactory.getLinks(user, null).isEmpty());

        mockController.verify();
    }


    @Test
    public void testOneHistoryNullUserNullCurrent()
    {
        Project project = mockController.getMock(Project.class);
        SimpleLink link = new SimpleLinkImpl("proj_lnk_1", "Project One (ONE)", "Browse Project One", "/secure/projectavatar?pid=1&avatarId=11&size=small", null, "/browse/ONE", null);

                historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(project).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        historyManager.getCurrentProject(Permissions.BROWSE, (User) null);
        mockController.setReturnValue(null);

        i18nFactory.getInstance((User) null);
        mockController.setReturnValue(i18n);

        project.getId();
        mockController.setReturnValue(1L);
        project.getName();
        mockController.setReturnValue("Project One");
        project.getKey();
        mockController.setReturnValue("ONE");
        GenericValue gv = mockController.getMock(GenericValue.class);
        project.getGenericValue();
        mockController.setReturnValue(gv);
        gv.getLong("avatar");
        mockController.setReturnValue(1L);
        Avatar avatar = mockController.getMock(Avatar.class);
        project.getAvatar();
        mockController.setReturnValue(avatar);
        avatar.getId();
        mockController.setReturnValue(11L);

        i18n.getText("tooltip.browseproject.specified", "Project One");
        mockController.setReturnValue("Browse Project One");

        mockController.replay();

        linkFactory.init(mockModuleDescriptor);
        List<SimpleLink> returnList = linkFactory.getLinks(null, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testOneHistoryNullCurrent()
    {
        Project project = mockController.getMock(Project.class);
        SimpleLink link = new SimpleLinkImpl("proj_lnk_1", "Project One (ONE)", "Browse Project One", "/jira/secure/projectavatar?pid=1&avatarId=11&size=small", null, "/jira/browse/ONE", null);

                historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(project).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        historyManager.getCurrentProject(Permissions.BROWSE, user);
        mockController.setReturnValue(null);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        project.getId();
        mockController.setReturnValue(1L);
        project.getName();
        mockController.setReturnValue("Project One");
        project.getKey();
        mockController.setReturnValue("ONE");
        GenericValue gv = mockController.getMock(GenericValue.class);
        project.getGenericValue();
        mockController.setReturnValue(gv);
        gv.getLong("avatar");
        mockController.setReturnValue(1L);
        Avatar avatar = mockController.getMock(Avatar.class);
        project.getAvatar();
        mockController.setReturnValue(avatar);
        avatar.getId();
        mockController.setReturnValue(11L);

        i18n.getText("tooltip.browseproject.specified", "Project One");
        mockController.setReturnValue("Browse Project One");

        mockController.replay();

        linkFactory.init(mockModuleDescriptor);
        List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testOneHistoryDiffCurrent()
    {
        Project project = mockController.getMock(Project.class);
        Project curProject = new MockProject(22L);
        SimpleLink link = new SimpleLinkImpl("proj_lnk_1", "Project One (ONE)", "Browse Project One", "/jira/secure/projectavatar?pid=1&avatarId=11&size=small", null, "/jira/browse/ONE", null);

        historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(project).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        historyManager.getCurrentProject(Permissions.BROWSE, user);
        mockController.setReturnValue(curProject);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        project.getId();
        mockController.setReturnValue(1L);
        project.getName();
        mockController.setReturnValue("Project One");
        project.getKey();
        mockController.setReturnValue("ONE");
        GenericValue gv = mockController.getMock(GenericValue.class);
        project.getGenericValue();
        mockController.setReturnValue(gv);
        gv.getLong("avatar");
        mockController.setReturnValue(1L);
        Avatar avatar = mockController.getMock(Avatar.class);
        project.getAvatar();
        mockController.setReturnValue(avatar);
        avatar.getId();
        mockController.setReturnValue(11L);

        i18n.getText("tooltip.browseproject.specified", "Project One");
        mockController.setReturnValue("Browse Project One");

        mockController.replay();

        linkFactory.init(mockModuleDescriptor);
        List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testOneHistorySameCurrent()
    {
        Project project = mockController.getMock(Project.class);
        SimpleLink link = new SimpleLinkImpl("proj_lnk_1", "Project One (ONE)", "Browse Project One", "/jira/secure/projectavatar?pid=1&avatarId=11&size=small", null, "/jira/browse/ONE", null);

        historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(project).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        historyManager.getCurrentProject(Permissions.BROWSE, user);
        mockController.setReturnValue(project);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        List<SimpleLink> expectedList = CollectionBuilder.<SimpleLink>newBuilder().asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }


}
