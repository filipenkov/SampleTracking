/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.JiraModuleDescriptorFactory;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.MockUserRoleActor;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.util.Predicate;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.PluginSystemLifecycle;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.atlassian.plugin.loaders.SinglePluginLoader;
import com.atlassian.plugin.manager.DefaultPluginManager;
import com.atlassian.plugin.manager.store.MemoryPluginPersistentStateStore;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.Group;
import com.opensymphony.workflow.loader.ActionDescriptor;
import electric.xml.Document;
import electric.xml.Element;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.List;

public class TestAddComment extends AbstractJellyTestCase
{
    private com.opensymphony.user.User u;
    private com.opensymphony.user.User notLoggedInUser;
    private Group g;
    private ProjectRole projectRole;
    private GenericValue project;
    private CommentManager commentManager;

    public TestAddComment(final String s)
    {
        super(s);
    }

    @Override
    protected void overrideServices()
    {
        try
        {
            final DefaultPluginManager pluginManager = new DefaultPluginManager(new MemoryPluginPersistentStateStore(),
                EasyList.build(new SinglePluginLoader("system-projectroleactors-plugin.xml")), new JiraModuleDescriptorFactory(null),
                new DefaultPluginEventManager());
            pluginManager.init();
            ManagerFactory.addService(PluginAccessor.class, pluginManager);
            ManagerFactory.addService(PluginController.class, pluginManager);
            ManagerFactory.addService(PluginSystemLifecycle.class, pluginManager);
        }
        catch (final PluginParseException e)
        {
            throw new NestableRuntimeException(e);
        }
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        u = UtilsForTests.getTestUser("logged-in-user");
        notLoggedInUser = UtilsForTests.getTestUser("not-logged-in-user");
        g = UtilsForTests.getTestGroup("admin-group");
        notLoggedInUser.addToGroup(g);
        u.addToGroup(g);
        JiraTestUtil.loginUser(u);

        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);
        ManagerFactory.getApplicationProperties().setOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS, true);

        project = UtilsForTests.getTestEntity("Project",
            EasyMap.build("key", "ABC", "name", "A Project", "lead", u.getName(), "counter", new Long(2)));

        // Create a project role and add the user to the role
        final ProjectFactory projectFactory = ComponentManager.getComponentInstanceOfType(ProjectFactory.class);
        final ProjectRoleManager projectRoleManager = ComponentManager.getComponentInstanceOfType(ProjectRoleManager.class);
        final ProjectRole projRole = new ProjectRoleImpl("Test Role", "Test Role Description");
        projectRole = projectRoleManager.createRole(projRole);
        ProjectRoleActors roleActors = projectRoleManager.getProjectRoleActors(projectRole, projectFactory.getProject(project));
        final MockUserRoleActor userRoleActor = new MockUserRoleActor(null, null, u);
        roleActors = (ProjectRoleActors) roleActors.addRoleActor(userRoleActor);
        projectRoleManager.updateProjectRoleActors(roleActors);

        final PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        final GenericValue defaultScheme = permissionSchemeManager.createDefaultScheme();
        SchemeEntity schemeEntity = new SchemeEntity(GroupDropdown.DESC, new Long(Permissions.BROWSE));
        permissionSchemeManager.createSchemeEntity(defaultScheme, schemeEntity);
        schemeEntity = new SchemeEntity(GroupDropdown.DESC, new Long(Permissions.COMMENT_ISSUE));
        permissionSchemeManager.createSchemeEntity(defaultScheme, schemeEntity);
        schemeEntity = new SchemeEntity(GroupDropdown.DESC, new Long(Permissions.CREATE_ISSUE));
        permissionSchemeManager.createSchemeEntity(defaultScheme, schemeEntity);
        permissionSchemeManager.addSchemeToProject(project, defaultScheme);

        final FieldScreenRendererFactory fieldScreenRendererFactory = new FieldScreenRendererFactory()
        {
            @Override
            public FieldScreenRenderer getFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issue, IssueOperation issueOperation, Predicate<? super Field> predicate)
            {
                return null;
            }

            public FieldScreenRenderer getFieldScreenRenderer(final User remoteUser, final Issue issue, final IssueOperation issueOperation, final Predicate<? super Field> predicate)
            {
                return null;
            }

            @Override
            public FieldScreenRenderer getFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issue, IssueOperation issueOperation, boolean onlyShownCustomFields)
            {
                return null;
            }

            public FieldScreenRenderer getFieldScreenRenderer(final User remoteUser, final Issue issue, final IssueOperation issueOperation, final boolean onlyShownCustomFields)
            {
                final Mock fieldScreenRenderer = new Mock(FieldScreenRenderer.class);
                fieldScreenRenderer.expectAndReturn("getFieldScreenRenderLayoutItem", P.ANY_ARGS, null);
                return (FieldScreenRenderer) fieldScreenRenderer.proxy();
            }

            @Override
            public FieldScreenRenderer getFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issue, ActionDescriptor actionDescriptor)
            {
                return null;
            }

            public FieldScreenRenderer getFieldScreenRenderer(final User remoteUser, final Issue issue, final ActionDescriptor actionDescriptor)
            {
                return null;
            }

            public FieldScreenRenderer getFieldScreenRenderer(final Collection issues, final ActionDescriptor actionDescriptor)
            {
                return null;
            }

            public FieldScreenRenderer getFieldScreenRenderer(final List fieldIds, final User remoteUser, final Issue issue, final IssueOperation issueOperation)
            {
                return null;
            }

            public FieldScreenRenderer getFieldScreenRenderer(final Issue issue)
            {
                return null;
            }

            @Override
            public FieldScreenRenderer getFieldScreenRenderer(List<String> fieldIds, com.opensymphony.user.User remoteUser, Issue issue, IssueOperation issueOperation)
            {
                return null;
            }
        };

        ManagerFactory.addService(FieldScreenRendererFactory.class, fieldScreenRendererFactory);

        commentManager = (CommentManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(CommentManager.class);
        UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Bug", "sequence", new Long(1)));
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        ManagerFactory.removeService(FieldScreenRendererFactory.class);
        ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(null);
    }

    public void testAddCommentWithDefaultUser() throws Exception
    {
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "A summary", "project", project.getLong("id"),
            "key", "ABC-1"));
        final Issue issueObject = IssueImpl.getIssueObject(issue);

        final String scriptFilename = "add-comment.test.add-comment.jelly";
        final Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        //Check to see if comment was created.
        final List actions = commentManager.getCommentsForUser(issueObject, u);
        assertFalse(actions.isEmpty());
        assertEquals(1, actions.size());

        final Comment comment = (Comment) actions.iterator().next();
        assertEquals("Issue comment", comment.getBody());
        assertEquals(g.getName(), comment.getGroupLevel());
        assertEquals(comment.getCreated(), comment.getUpdated());
        assertEquals(comment.getAuthor(), comment.getUpdateAuthor());
    }

    public void testAddCommentWithAdminRole() throws Exception
    {
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "A summary", "project", project.getLong("id"),
            "key", "ABC-1"));
        final Issue issueObject = IssueImpl.getIssueObject(issue);

        final String scriptFilename = "add-comment.test.add.role-comment.jelly";
        final Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        //Check to see if comment was created.
        final List actions = commentManager.getCommentsForUser(issueObject, u);
        assertFalse(actions.isEmpty());
        assertEquals(1, actions.size());

        final Comment comment = (Comment) actions.iterator().next();
        assertEquals("Issue comment", comment.getBody());
        assertEquals(projectRole.getName(), comment.getRoleLevel().getName());
        assertEquals(projectRole.getId(), comment.getRoleLevelId());
        assertEquals(comment.getCreated(), comment.getUpdated());
        assertEquals(comment.getAuthor(), comment.getUpdateAuthor());
    }

    public void testAddCommentAsAnotherUser() throws Exception
    {
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "A summary", "project", project.getLong("id"),
            "key", "ABC-1"));
        final Issue issueObject = IssueImpl.getIssueObject(issue);

        // check the logged-in user before
        assertEquals(u.getName(), ComponentAccessor.getJiraAuthenticationContext().getUser().getName());

        final String scriptFilename = "add-comment.test.add.other.user-comment.jelly";
        final Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        //Check to see if comment was created.
        final List actions = commentManager.getCommentsForUser(issueObject, u);
        assertFalse(actions.isEmpty());
        assertEquals(1, actions.size());

        final Comment comment = (Comment) actions.iterator().next();
        assertEquals("Issue comment", comment.getBody());
        assertEquals(g.getName(), comment.getGroupLevel());
        assertEquals(notLoggedInUser.getName(), comment.getAuthor());
        assertEquals(notLoggedInUser.getName(), comment.getUpdateAuthor());
        assertTrue(comment.getCreated().toString().matches("Sun Sep 25 12:12:12 .* 2005"));
        assertTrue(comment.getUpdated().toString().matches("Sun Sep 25 12:12:12 .* 2005"));

        // check the logged-in user after
        assertEquals(u.getName(), ComponentAccessor.getJiraAuthenticationContext().getUser().getName());
    }

    public void testAddCommentIncludeUpdateInformation() throws Exception
    {
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "A summary", "project", project.getLong("id"),
            "key", "ABC-1"));
        final Issue issueObject = IssueImpl.getIssueObject(issue);

        final String scriptFilename = "add-comment.test.add.with.updated.info-comment.jelly";
        final Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        //Check to see if comment was created.
        final List comments = commentManager.getComments(issueObject);
        assertEquals(1, comments.size());

        final Comment comment = (Comment) comments.iterator().next();
        assertEquals("Issue comment", comment.getBody());
        assertEquals("not-logged-in-user", comment.getUpdateAuthor());
        assertTrue(comment.getCreated().toString().matches("Mon Sep 25 12:12:12 .* 2006"));
        assertTrue(comment.getUpdated().toString().matches("Tue Sep 25 12:12:12 .* 2007"));
    }

    public void testAddCommentUpdateDateError() throws Exception
    {
        UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "A summary", "project", project.getLong("id"), "key", "ABC-1"));

        final String scriptFilename = "add-comment.test.add.with.update.date.error-comment.jelly";
        try
        {
            runScript(scriptFilename);
            fail("The update date was earlier than the created date of the comment but we let this pass, aaaaaaa!!!!!");
        }
        catch (final JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("The updated date is earlier than the comment date, this is not allowed.") != -1);
        }
    }

    @Override
    protected String getRelativePath()
    {
        return "tag" + FS + "issue" + FS;
    }
}