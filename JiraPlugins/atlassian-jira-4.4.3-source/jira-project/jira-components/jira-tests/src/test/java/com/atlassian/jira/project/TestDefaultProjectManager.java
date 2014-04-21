/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.test.mock.MockAtlassianServletRequest;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.security.DefaultPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpSession;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpSession;

public class TestDefaultProjectManager extends AbstractUsersIndexingTestCase
{
    private GenericValue project1;
    private GenericValue project2;
    private GenericValue issue1;
    private GenericValue issue2;
    private GenericValue issue3;
    private GenericValue version1;
    private GenericValue version2;
    private GenericValue version3;
    private GenericValue projectCat;
    private GenericValue projectCat2;
    private final Long projectCatID = new Long(30);
    private final Long projectCat2ID = new Long(31);

    // Variables for default assignee tests
    private DefaultProjectManager dpm;
    private User componentLead;
    private User projectLead;
    private GenericValue projectWithDefaultAssigneeLead;
    private GenericValue projectWithDefaultUnassigned;
    private GenericValue componentWithComponentAssignee;
    private GenericValue componentWithComponentUnassigned;
    private GenericValue componentWithProjectLeadAssignee;
    private GenericValue componentWithProjectDefaultAssignee;
    private GenericValue componentWithProjectDefaultUnassigned;
    private FieldVisibilityBean origFieldVisibilityBean;

    public TestDefaultProjectManager(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        origFieldVisibilityBean = ComponentManager.getComponentInstanceOfType(FieldVisibilityBean.class);
        CrowdService crowdService = ComponentManager.getComponentInstanceOfType(CrowdService.class);

        final FieldVisibilityBean visibilityBean = EasyMock.createMock(FieldVisibilityBean.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityBean.class, visibilityBean);

        User u1 = new MockUser("Owen Fellows");
        Group g1 = new MockGroup("Test Group");
        crowdService.addUser(u1, "");
        crowdService.addGroup(g1);

        crowdService.addUserToGroup(u1, g1);
        new MockUser("Watcher 1");

        MockHttpServletRequest request = new MockAtlassianServletRequest();
        ServletActionContext.setRequest(request);

        JiraTestUtil.loginUser(u1);

        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "name", "Project 1", "counter", new Long(100), "id", new Long(100)));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "XYZ", "name", "Project 2", "counter", new Long(101), "id", new Long(101)));

        GenericValue scheme = JiraTestUtil.setupAndAssociateDefaultPermissionScheme(project1);
        ManagerFactory.getPermissionSchemeManager().addSchemeToProject(project2, scheme);
        ManagerFactory.getPermissionManager().addPermission(Permissions.BROWSE, scheme, "Test Group", GroupDropdown.DESC);

        version1 = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(1000), "name", "ver1", "project", project1.getLong("id"), "sequence", new Long(1), "released", "true", "archived", "true"));
        version2 = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(1001), "name", "ver2", "project", project1.getLong("id"), "sequence", new Long(2), "released", "true"));
        version3 = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(1002), "name", "ver3", "project", project1.getLong("id"), "sequence", new Long(3), "archived", "true"));
        UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(1003), "name", "ver3", "project", project1.getLong("id"), "sequence", new Long(4)));

        CoreFactory.getAssociationManager().createAssociation(project1, version1, IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(project1, version2, IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(project1, version3, IssueRelationConstants.VERSION);

        GenericValue component1 = UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(2000), "name", "comp1", "project", project1.getLong("id")));
        GenericValue component2 = UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(2001), "name", "comp2", "project", project1.getLong("id")));
        GenericValue component3 = UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(2002), "name", "comp3", "project", project1.getLong("id")));

        CoreFactory.getAssociationManager().createAssociation(project1, component1, IssueRelationConstants.COMPONENT);
        CoreFactory.getAssociationManager().createAssociation(project1, component2, IssueRelationConstants.COMPONENT);
        CoreFactory.getAssociationManager().createAssociation(project1, component3, IssueRelationConstants.COMPONENT);

        issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-99"));
        issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-100"));
        issue3 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-101"));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-102"));

        CoreFactory.getAssociationManager().createAssociation(issue1, version1, IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue2, version2, IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue3, version3, IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue1, version3, IssueRelationConstants.FIX_VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue2, version2, IssueRelationConstants.FIX_VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue3, version1, IssueRelationConstants.FIX_VERSION);

        projectCat = UtilsForTests.getTestEntity("ProjectCategory", EasyMap.build("id", projectCatID, "name", "foo", "description", "bar"));
        projectCat2 = UtilsForTests.getTestEntity("ProjectCategory", EasyMap.build("id", projectCat2ID, "name", "bib", "description", "la"));

        ManagerFactory.getIndexManager().reIndexAll();
    }

    protected void tearDown() throws Exception
    {
        ManagerFactory.addService(FieldVisibilityBean.class, origFieldVisibilityBean);
        super.tearDown();
    }

    public void testGetProjectObj()
    {
        ProjectManager pm = ComponentAccessor.getProjectManager();

        // non-existing project - ID is null - not a requirement at the moment
        Project project = pm.getProjectObj(null);
        assertNull(project);

        // non-existing project
        project = pm.getProjectObj(new Long(666));
        assertNull(project);

        // existing project
        Long projectId1 = project1.getLong("id");
        project = pm.getProjectObj(projectId1);
        assertNotNull(project);
        assertEquals(projectId1, project.getId());

        Long projectId2 = project2.getLong("id");
        project = pm.getProjectObj(projectId2);
        assertNotNull(project);
        assertEquals(projectId2, project.getId());
    }

    public void testGetNextIdWithDuplicateKey() throws GenericEntityException
    {
        ProjectManager pm = new DefaultProjectManager();

        /**
         * project (ABC) next counter = 101
         * existing issue keys: ABC-99, ABC-100, ABC-101, ABC-102
         * getNextId() should skip over counters 101 and 102 as they are already associated with existing issues
         */
        assertEquals(100, project1.getLong("counter").longValue()); //ensure that the counter starts where se think
        assertEquals(103, pm.getNextId(project1));
        assertEquals(104, pm.getNextId(project1));
        assertEquals(105, pm.getNextId(project1));
        project1 = pm.getProject(project1.getLong("id"));
        assertEquals(105, project1.getLong("counter").longValue()); //ensure that the counter is incremented properly
        assertEquals(project1, pm.getProjectByKey("ABC"));
        assertEquals(project1, pm.getProjectByName("Project 1"));
    }

    public void testUpdateProject() throws GenericEntityException
    {
        ProjectManager pm = ManagerFactory.getProjectManager();
        project1.set("name", "A New Name");
        pm.updateProject(project1);
        assertEquals("A New Name", pm.getProject(project1.getLong("id")).getString("name"));
        assertNotNull(pm.getProjectByName("A New Name"));
        assertNotNull(pm.getProjectByKey("ABC"));
    }

    public void testGetComponents() throws GenericEntityException
    {
        ProjectManager pm = ManagerFactory.getProjectManager();
        assertNotNull(pm.getComponent(new Long(2000)));

        GenericValue comp = pm.getComponent(project1, "comp2");
        assertEquals(new Long(2001), comp.getLong("id"));

        Collection comps = pm.getComponents(project1);
        assertEquals(3, comps.size());
        assertTrue(comps.contains(pm.getComponent(new Long(2000))));
        assertTrue(comps.contains(pm.getComponent(new Long(2001))));
        assertTrue(comps.contains(pm.getComponent(new Long(2002))));
    }

    public void testGetProjects() throws GenericEntityException
    {
        ProjectManager pm = ManagerFactory.getProjectManager();
        List projects = new ArrayList(pm.getProjects());
        assertEquals(2, projects.size());
        assertEquals(projects.get(0), project1);
        assertEquals(projects.get(1), project2);
    }


    public void testOther() throws GenericEntityException
    {
        ProjectManager pm = ManagerFactory.getProjectManager();
        pm.refreshProjectDependencies(project1);
        pm.refresh();
    }

    public void testUpdateProjectCategory() throws GenericEntityException
    {
        ProjectManager pm = ManagerFactory.getProjectManager();
        projectCat.set("name", "A New Name");
        projectCat.set("description", "A New Description");
        pm.updateProjectCategory(projectCat);

        GenericValue retrievedProjectCat = pm.getProjectCategory(projectCatID);
        assertEquals("A New Name", retrievedProjectCat.getString("name"));
        assertEquals("A New Description", retrievedProjectCat.getString("description"));
    }

    public void testGetProjectCategoryFromProject() throws GenericEntityException
    {
        ProjectManager pm = ManagerFactory.getProjectManager();

        // null project id
        GenericValue projectCategory = pm.getProjectCategoryFromProject(null);
        assertNull(projectCategory);

        //valid project id but no association set
        projectCategory = pm.getProjectCategoryFromProject(project1);
        assertNull(projectCategory);

        //valid project id and association exists.. return the projectCategory
        CoreFactory.getAssociationManager().createAssociation(project1, projectCat, ProjectRelationConstants.PROJECT_CATEGORY);
        projectCategory = pm.getProjectCategoryFromProject(project1);
        assertEquals(projectCat, projectCategory);
    }

    public void testGetProjectsFromProjectCategory() throws GenericEntityException
    {
        ProjectManager pm = ManagerFactory.getProjectManager();

        // test null projectCategory id
        Collection projects = pm.getProjectsFromProjectCategory((GenericValue) null);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory id associated with NO projects
        projects = pm.getProjectsFromProjectCategory(projectCat);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory associated with a project
        pm.setProjectCategory(project1, projectCat);
        projects = pm.getProjectsFromProjectCategory(projectCat);
        assertEquals(1, projects.size());
        assertTrue(projects.contains(project1));
    }

    public void testGetProjectObjectsFromProjectCategory() throws GenericEntityException
    {
        ProjectManager pm = ManagerFactory.getProjectManager();

        // test null projectCategory id
        Collection projects = pm.getProjectObjectsFromProjectCategory(null);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory id associated with NO projects
        projects = pm.getProjectObjectsFromProjectCategory(projectCatID);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory associated with a project
        pm.setProjectCategory(project1, projectCat);
        projects = pm.getProjectObjectsFromProjectCategory(projectCatID);
        assertEquals(1, projects.size());
        final Project project = (Project) projects.iterator().next();
        assertEquals(new Long(100L), project.getId());
        assertEquals(projectCat, project.getProjectCategory());
    }

    public void testSetProjectCategory() throws GenericEntityException
    {
        //test null project
        ProjectManager pm = ManagerFactory.getProjectManager();
        try
        {
            pm.setProjectCategory((GenericValue) null, null);
            fail("Should have thrown IllegalArgumentException if null project");
        }
        catch (IllegalArgumentException e)
        {
            //this is what we want to happen
        }

        //test setting up a relation with a project that has no categories
        assertNull(pm.getProjectCategoryFromProject(null));
        pm.setProjectCategory(project1, projectCat);
        assertEquals(projectCat, pm.getProjectCategoryFromProject(project1));
        assertEquals(1, CoreFactory.getAssociationManager().getSinkFromSource(project1, "ProjectCategory", ProjectRelationConstants.PROJECT_CATEGORY, false).size());

        //test setting up a relation with a project that has one category already
        pm.setProjectCategory(project1, projectCat2);
        assertEquals(projectCat2, pm.getProjectCategoryFromProject(project1));
        assertEquals(1, CoreFactory.getAssociationManager().getSinkFromSource(project1, "ProjectCategory", ProjectRelationConstants.PROJECT_CATEGORY, false).size());

        //test setting up a relation with a null category (ie no project category)
        pm.setProjectCategory(project1, null);
        assertEquals(null, pm.getProjectCategoryFromProject(project1));
        assertEquals(0, CoreFactory.getAssociationManager().getSinkFromSource(project1, "ProjectCategory", ProjectRelationConstants.PROJECT_CATEGORY, false).size());
    }

    /**
     *
     */
    private void setupDefaultAssigneeTests()
    {
        componentLead = UtilsForTests.getTestUser("componentLead");
        projectLead = UtilsForTests.getTestUser("projectLead");

        projectWithDefaultAssigneeLead = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "projectWithAssigneeLead", "key", "ABC", "lead", projectLead.getName(), "assigneetype", new Long(ProjectAssigneeTypes.PROJECT_LEAD)));
        componentWithProjectLeadAssignee = UtilsForTests.getTestEntity("Component", EasyMap.build("name", "componentWithProjectLeadAssignee", "project", projectWithDefaultAssigneeLead.getLong("id"), "assigneetype", new Long(ComponentAssigneeTypes.PROJECT_LEAD)));
        componentWithProjectDefaultAssignee = UtilsForTests.getTestEntity("Component", EasyMap.build("name", "componentWithProjectDefaultAssignee", "project", projectWithDefaultAssigneeLead.getLong("id"), "assigneetype", new Long(ComponentAssigneeTypes.PROJECT_DEFAULT)));

        projectWithDefaultUnassigned = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "projectWithDefaultUnassigned", "key", "ABC", "assigneetype", new Long(ProjectAssigneeTypes.UNASSIGNED)));
        componentWithComponentAssignee = UtilsForTests.getTestEntity("Component", EasyMap.build("name", "componentWithComponentAssignee", "project", projectWithDefaultUnassigned.getLong("id"), "lead", componentLead.getName(), "assigneetype", new Long(ComponentAssigneeTypes.COMPONENT_LEAD)));
        componentWithComponentUnassigned = UtilsForTests.getTestEntity("Component", EasyMap.build("name", "componentWithComponentUnassigned", "project", projectWithDefaultUnassigned.getLong("id"), "assigneetype", new Long(ComponentAssigneeTypes.UNASSIGNED)));
        componentWithProjectDefaultUnassigned = UtilsForTests.getTestEntity("Component", EasyMap.build("name", "componentWithProjectDefaultUnassigned", "project", projectWithDefaultUnassigned.getLong("id"), "assigneetype", new Long(ComponentAssigneeTypes.PROJECT_DEFAULT)));

        dpm = new DefaultProjectManager();
    }

    public void testDefaultAssigneeWithNoUnassigned() throws DefaultAssigneeException
    {
        setupDefaultAssigneeTests();

        // Should be false as project lead cannot be assigned issues.
        _testNoDefaultAssignee(projectWithDefaultAssigneeLead, null);

        setupMockPermissionManager(projectLead);

        // Should be true as project lead can be assigned issues.
        _testDefaultAssignee(projectWithDefaultAssigneeLead, null, projectLead);
    }

    public void testDefaultAssigneeWithUnassigned() throws DefaultAssigneeException, GenericEntityException
    {
        setupDefaultAssigneeTests();

        // Should be false as unassigned is turned off and project lead cannot be assigned issues.
        _testNoDefaultAssignee(projectWithDefaultUnassigned, null);

        setupMockPermissionManager(projectLead);

        // Should be false as unassigned is turned off and the lead is null so it fails
        _testNoDefaultAssignee(projectWithDefaultUnassigned, null);

        projectWithDefaultUnassigned.set("lead", projectLead.getName());
        projectWithDefaultUnassigned.store();

        // Should be true as unassigned is turned off and project lead can be assigned issues,
        // so it defaults to project lead.
        assertTrue(dpm.isDefaultAssignee(projectWithDefaultUnassigned, null));

        User defaultAssignee = dpm.getDefaultAssignee(projectWithDefaultUnassigned, null);
        assertEquals(projectLead, defaultAssignee);

        // Turn on unassigned
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);

        // Reset permissions
        PermissionManager permissionManager = new DefaultPermissionManager()
        {
            public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
            {
                return false;
            }
        };
        ManagerFactory.addService(PermissionManager.class, permissionManager);

        // Should be true as unassigned is turned on
        _testDefaultAssignee(projectWithDefaultUnassigned, null, null);
    }

    public void testDefaultAssigneeProjectLead()
    {
        setupDefaultAssigneeTests();

        // Should return false as project lead is unassignable
        _testNoDefaultAssignee(projectWithDefaultAssigneeLead, componentWithProjectLeadAssignee);

        setupMockPermissionManager(projectLead);

        // Should return true as project lead is assignable
        _testDefaultAssignee(projectWithDefaultAssigneeLead, componentWithProjectLeadAssignee, projectLead);
    }

    public void testDefaultAssigneeProjectDefault()
    {
        setupDefaultAssigneeTests();

        // Should return false as project lead is unassignable and component's default assignee is the project default
        _testNoDefaultAssignee(projectWithDefaultAssigneeLead, componentWithProjectDefaultAssignee);

        setupMockPermissionManager(projectLead);

        // Should return true as project lead is assignable and component's default assignee is the project default
        _testDefaultAssignee(projectWithDefaultAssigneeLead, componentWithProjectDefaultAssignee, projectLead);
    }

    public void testDefaultAssigneeComponentLead()
    {
        setupDefaultAssigneeTests();

        // Should return false as components lead is unassignable, unassigned is turned off and project lead is unassignable
        _testNoDefaultAssignee(projectWithDefaultUnassigned, componentWithComponentAssignee);

        setupMockPermissionManager(componentLead);

        // Should return true as component lead is assignable
        _testDefaultAssignee(projectWithDefaultUnassigned, componentWithComponentAssignee, componentLead);
    }

    public void testDeafultAssigneeComponentUnassigned()
    {
        setupDefaultAssigneeTests();

        // Should return false as unassigned is NOT allowed and component's and project's default assignee are set to
        // unassigned
        _testNoDefaultAssignee(projectWithDefaultUnassigned, componentWithComponentUnassigned);

        // Turn on unassigned allowed
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);

        // Should return true as unassigned is turnned ON
        _testDefaultAssignee(projectWithDefaultUnassigned, componentWithComponentUnassigned, null);
    }

    public void testDefaultAssigneeProjectDefaultUnassigned()
    {
        setupDefaultAssigneeTests();

        // Should return false as unassigned is NOT allowed and components default assignee is set to
        // project's default (which is unassigned)
        _testNoDefaultAssignee(projectWithDefaultUnassigned, componentWithProjectDefaultUnassigned);

        // Turn on unassigned allowed
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);

        // Should return true as unassigned is turnned ON!!! yippeeeee (spelled owen's way)
        _testDefaultAssignee(projectWithDefaultUnassigned, componentWithComponentUnassigned, null);
    }

    public void testGetProjectByLead()
    {
        setupDefaultAssigneeTests();

        // Now test that we can retrieve the project with lead
        ProjectManager pm = ManagerFactory.getProjectManager();
        Collection projectsWithLead = pm.getProjectsLeadBy(projectLead);
        assertEquals(1, projectsWithLead.size());
        assertEquals("projectWithAssigneeLead", projectWithDefaultAssigneeLead.getString("name"));
    }

    public void testGetProjectByLeadWhereNoProjectsExistForLead()
    {
        setupDefaultAssigneeTests();
        ProjectManager pm = ManagerFactory.getProjectManager();
        Collection projectsWithoutLead = pm.getProjectsLeadBy(componentLead);
        assertEquals(0, projectsWithoutLead.size());
    }

    private void _testDefaultAssignee(GenericValue project, GenericValue component, User expectedLead)
    {
        assertTrue(dpm.isDefaultAssignee(project, component));
        try
        {
            User defaultAssignee = dpm.getDefaultAssignee(project, component);
            assertEquals(expectedLead, defaultAssignee);
        }
        catch (DefaultAssigneeException e)
        {
            fail("What the!!! Should not throw an exception.");
        }
    }

    private void _testNoDefaultAssignee(GenericValue project, GenericValue component)
    {
        assertFalse(dpm.isDefaultAssignee(project, component));
        try
        {
            dpm.getDefaultAssignee(project, component);
            fail("Exception should have been thrown.");
        }
        catch (DefaultAssigneeException e)
        {
            //shweet
        }
    }

    private void setupMockPermissionManager(final User lead)
    {
        PermissionManager permissionManager = new DefaultPermissionManager()
        {
            public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
            {
                return permissionsId == Permissions.ASSIGNABLE_USER && lead.equals(u);
            }
        };
        ManagerFactory.addService(PermissionManager.class, permissionManager);
    }
}
