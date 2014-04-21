package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.user.MockUser;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public class TestProjectAwareQuickSearchHandlerImpl extends LegacyJiraMockTestCase
{
    public void testProjectAwareQuickSearchHandlerWithSingleProjectInContext()
    {
        GenericValue expectedProject = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "Project", "id", new Long(1)));

        //valid pid in search result
        QuickSearchResult searchResult = new ModifiableQuickSearchResult("searchInput");
        searchResult.addSearchParameter("pid", expectedProject.getString("id"));
        _testProjectAwareHandlerWithSingleProjectInContext(searchResult, EasyList.build(expectedProject));

        //non-existant project
        searchResult = new ModifiableQuickSearchResult("searchInput");
        searchResult.addSearchParameter("pid", "1337"); //non existant
        _testProjectAwareHandlerWithSingleProjectInContext(searchResult, EasyList.build((Object) null));

        try
        {
            searchResult = new ModifiableQuickSearchResult("searchInput");
            searchResult.addSearchParameter("pid", "invalid");
            _testProjectAwareHandlerWithSingleProjectInContext(searchResult, null);
            fail("Expected NumberFormatException thrown");
        }
        catch (NumberFormatException e)
        {
        }

        try
        {
            searchResult = new ModifiableQuickSearchResult("searchInput");
            searchResult.addSearchParameter("pid", "");//invalid
            _testProjectAwareHandlerWithSingleProjectInContext(searchResult, null);
            fail("Expected NumberFormatException thrown");
        }
        catch (NumberFormatException e)
        {
        }

        try
        {
            searchResult = new ModifiableQuickSearchResult("searchInput");
            searchResult.addSearchParameter("pid", null);
            _testProjectAwareHandlerWithSingleProjectInContext(searchResult, EasyList.build(expectedProject));
            fail("Expected NumberFormatException thrown");
        }
        catch (NumberFormatException e)
        {
        }
    }

    public void testProjectAwareQuickSearchHandlerWithMultipleProjectsInContext() throws GenericEntityException
    {
        GenericValue expectedProject1 = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "Project1", "id", new Long(1)));
        GenericValue expectedProject2 = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "Project2", "id", new Long(2)));
        GenericValue expectedProject3 = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "Project3", "id", new Long(3)));

        User user = new MockUser("Test User");

        //2 valid projects in search but expect 3 browsable projects returned
        QuickSearchResult searchResult = new ModifiableQuickSearchResult("searchInput");
        searchResult.addSearchParameter("pid", expectedProject1.getString("id"));
        searchResult.addSearchParameter("pid", expectedProject2.getString("id"));
        _testProjectAwareHandlerWithNoOrMultipleProjectInContext(searchResult, user, EasyList.build(expectedProject1, expectedProject2, expectedProject3));

        //3 valid projects in search but only expect 2 browsable projects returned
        searchResult = new ModifiableQuickSearchResult("searchInput");
        searchResult.addSearchParameter("pid", expectedProject1.getString("id"));
        searchResult.addSearchParameter("pid", expectedProject2.getString("id"));
        searchResult.addSearchParameter("pid", expectedProject3.getString("id"));
        _testProjectAwareHandlerWithNoOrMultipleProjectInContext(searchResult, user, EasyList.build(expectedProject1, expectedProject2));

        //the following tests no longer throw number format exceptions as there are multiple project entries and are ignored 

        searchResult = new ModifiableQuickSearchResult("searchInput");
        searchResult.addSearchParameter("pid", "13371"); //non existant
        searchResult.addSearchParameter("pid", "13372"); //non existant
        _testProjectAwareHandlerWithNoOrMultipleProjectInContext(searchResult, user, EasyList.build(expectedProject1));

        searchResult = new ModifiableQuickSearchResult("searchInput");
        searchResult.addSearchParameter("pid", "invalid1");
        searchResult.addSearchParameter("pid", "invalid2");
        _testProjectAwareHandlerWithNoOrMultipleProjectInContext(searchResult, user, EasyList.build(expectedProject1));

        searchResult = new ModifiableQuickSearchResult("searchInput");
        searchResult.addSearchParameter("pid", "");//invalid
        searchResult.addSearchParameter("pid", "");//invalid
        _testProjectAwareHandlerWithNoOrMultipleProjectInContext(searchResult, user, EasyList.build(expectedProject1));

        searchResult = new ModifiableQuickSearchResult("searchInput");
        searchResult.addSearchParameter("pid", null);
        searchResult.addSearchParameter("pid", null);
        _testProjectAwareHandlerWithNoOrMultipleProjectInContext(searchResult, user, EasyList.build(expectedProject1));
    }

    public void testProjectAwareQuickSearchHandlerWithNoProjectsInContext() throws GenericEntityException
    {
        GenericValue expectedProject = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "Project1", "id", new Long(1)));
        User user =  new MockUser("Test User 2");

        //No project in the context
        QuickSearchResult searchResult = new ModifiableQuickSearchResult("searchInput");
        _testProjectAwareHandlerWithNoOrMultipleProjectInContext(searchResult, user, EasyList.build(expectedProject));
    }

    //--------------------------------------------------------------------------------------------------- Helper Methods

    private void _testProjectAwareHandlerWithSingleProjectInContext(QuickSearchResult searchResult, List expectedProjects)
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        Mock mockAuthenticationContext = new Mock(JiraAuthenticationContext.class);
        mockPermissionManager.setStrict(true);
        mockAuthenticationContext.setStrict(true);

        ProjectAwareQuickSearchHandler projectAwareHandler = new ProjectAwareQuickSearchHandlerImpl(ManagerFactory.getProjectManager(), (PermissionManager) mockPermissionManager.proxy(), (JiraAuthenticationContext) mockAuthenticationContext.proxy());

        List projects = projectAwareHandler.getProjects(searchResult);
        assertEquals(expectedProjects.size(), projects.size());
        assertEquals(expectedProjects, projects);

        //verify that the permission and authentication context are not used
        mockPermissionManager.verify();
        mockAuthenticationContext.verify();
    }

    private void _testProjectAwareHandlerWithNoOrMultipleProjectInContext(QuickSearchResult searchResult, User user, List expectedProjects) throws GenericEntityException
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        Mock mockAuthenticationContext = new Mock(JiraAuthenticationContext.class);

        mockProjectManager.setStrict(true);
        mockPermissionManager.setStrict(true);
        mockAuthenticationContext.setStrict(true);

        mockAuthenticationContext.expectAndReturn("getLoggedInUser", user);
        mockPermissionManager.expectAndReturn("getProjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(user)), expectedProjects);

        ProjectAwareQuickSearchHandler projectAwareHandler = new ProjectAwareQuickSearchHandlerImpl((ProjectManager) mockProjectManager.proxy(), (PermissionManager) mockPermissionManager.proxy(), (JiraAuthenticationContext) mockAuthenticationContext.proxy());

        List projects = projectAwareHandler.getProjects(searchResult);

        assertEquals(expectedProjects.size(), projects.size());
        assertEquals(expectedProjects, projects);

        mockProjectManager.verify();
        mockPermissionManager.verify();
        mockAuthenticationContext.verify();
    }
}
