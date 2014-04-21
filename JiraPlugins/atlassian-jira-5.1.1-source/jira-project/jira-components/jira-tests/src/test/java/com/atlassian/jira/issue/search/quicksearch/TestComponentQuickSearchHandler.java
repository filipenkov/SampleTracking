package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.project.component.MutableProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.project.component.MockProjectComponentManager;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import org.ofbiz.core.entity.GenericValue;

import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TestComponentQuickSearchHandler extends LegacyJiraMockTestCase
{
    public void testComponentQuickSearchHandlerModifySearchResultSingleProject()
    {
        //set up the test environment (1 project to many components)
        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(987)));

        final ProjectComponent component1 = getProjectComponent(new Long(101), "c1 three", "c1 description", "lead", 1, project.getLong("id"));
        final ProjectComponent component2 = getProjectComponent(new Long(102), "three c2 two", "c2 description", "lead", 2, project.getLong("id"));
        final ProjectComponent component3 = getProjectComponent(new Long(103), "three two one c3", "c3 description", "lead", 3, project.getLong("id"));
        final List availableComponents = EasyList.build(component1, component2, component3);

        //map all the components to a single project
        final Map projectToComponents = EasyMap.build(project, availableComponents);

        runCommonTestCases(projectToComponents, component1, component2, component3);
    }

    public void testComponentQuickSearchHandlerModifySearchResultMultipleProjects()
    {
        //set up the test environment (2 projects to varying components)
        final GenericValue project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(987)));
        final GenericValue project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(988)));

        final ProjectComponent proj1comp1 = getProjectComponent(new Long(101), "c1 three", "c1 description", "lead", 1, project1.getLong("id"));
        final ProjectComponent proj2comp1 = getProjectComponent(new Long(102), "three c2 two", "c2 description", "lead", 2, project2.getLong("id"));
        final ProjectComponent proj2comp2 = getProjectComponent(new Long(103), "three two one c3", "c3 description", "lead", 3,
            project2.getLong("id"));

        //map the 3 components to 2 different projects
        final Map projectToComponents = EasyMap.build(project1, EasyList.build(proj1comp1), project2, EasyList.build(proj2comp1, proj2comp2));

        runCommonTestCases(projectToComponents, proj1comp1, proj2comp1, proj2comp2);
    }

    private void runCommonTestCases(final Map projectToComponents, final ProjectComponent proj1comp1, final ProjectComponent proj2comp1, final ProjectComponent proj2comp2)
    {
        //search for no components (search input should not be changed)
        List expectedComponentIds = null;
        _testComponentQuickSearchHandlerModifyResults("pre mid suf", EasyList.build("pre", "mid", "suf"), projectToComponents, expectedComponentIds);

        //search for nothing (ie no components)
        expectedComponentIds = null;
        _testComponentQuickSearchHandlerModifyResults("", EasyList.build(), projectToComponents, expectedComponentIds);

        //search for c1 directly
        expectedComponentIds = EasyList.build(proj1comp1.getId().toString());
        _testComponentQuickSearchHandlerModifyResults("c:c1", EasyList.build(), projectToComponents, expectedComponentIds);

        //search for c2 directly with some white space
        expectedComponentIds = EasyList.build(proj2comp1.getId().toString());
        _testComponentQuickSearchHandlerModifyResults(" c:c2 ", EasyList.build(), projectToComponents, expectedComponentIds);

        //search for c3 directly with extra words
        expectedComponentIds = EasyList.build(proj2comp2.getId().toString());
        _testComponentQuickSearchHandlerModifyResults("hello v:two world c:c3 def", EasyList.build("hello", "v:two", "world", "def"),
            projectToComponents, expectedComponentIds);

        //search for c3 using another unique keyword in c3 and with extra words at start
        expectedComponentIds = EasyList.build(proj2comp2.getId().toString());
        _testComponentQuickSearchHandlerModifyResults("prefix c:one", EasyList.build("prefix"), projectToComponents, expectedComponentIds);

        //search for 2 components with the common whole word "two"
        expectedComponentIds = EasyList.build(proj2comp1.getId().toString(), proj2comp2.getId().toString());
        _testComponentQuickSearchHandlerModifyResults("c:two suffix", EasyList.build("suffix"), projectToComponents, expectedComponentIds);

        //search for 3 components with the common whole word "three"
        expectedComponentIds = EasyList.build(proj1comp1.getId().toString(), proj2comp1.getId().toString(), proj2comp2.getId().toString());
        _testComponentQuickSearchHandlerModifyResults("pre c:three suf", EasyList.build("pre", "suf"), projectToComponents, expectedComponentIds);
    }

    private void _testComponentQuickSearchHandlerModifyResults(final String searchInput, final List untouchedWords, final Map projectToComponents, final Collection expectedComponentIds)
    {
        final QuickSearchResult quickSearchResult = new ModifiableQuickSearchResult(searchInput);

        final Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);

        final AtomicInteger findAllForProjectCalledCount = new AtomicInteger(0);
        final MockProjectComponentManager mockProjectComponentManager2 = new MockProjectComponentManager()
        {
            //override so that we can assert how many times findAllForProject is called
            @Override
            public Collection findAllForProject(final Long projectId)
            {
                findAllForProjectCalledCount.incrementAndGet();
                for (final Iterator it = projectToComponents.keySet().iterator(); it.hasNext();)
                {
                    final GenericValue project = (GenericValue) it.next();
                    if (projectId.equals(project.getLong("id")))
                    {
                        return (Collection) projectToComponents.get(project);
                    }
                }
                return null;
            }
        };

        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);

        //if single project
        if (projectToComponents.size() == 1)
        {
            final GenericValue project = (GenericValue) projectToComponents.keySet().iterator().next();
            //add the single project to the search result
            quickSearchResult.addSearchParameter("pid", project.getString("id"));
            if (expectedComponentIds != null)
            {
                mockProjectManager.expectAndReturn("getProject", P.args(new IsEqual(project.getLong("id"))), project);
            }
        }
        else
        //else multiple project
        {
            //add each project to the search result
            for (final Iterator iterator = projectToComponents.keySet().iterator(); iterator.hasNext();)
            {
                quickSearchResult.addSearchParameter("pid", ((GenericValue) iterator.next()).getString("id"));
            }
            if (expectedComponentIds != null)
            {
                mockPermissionManager.expectAndReturn("getProjects", P.args(new IsEqual(new Integer(Permissions.BROWSE)), P.IS_NULL),
                    projectToComponents.keySet());
            }
        }

        final ComponentQuickSearchHandler quickSearchHandler = new ComponentQuickSearchHandler(mockProjectComponentManager2,
            (ProjectManager) mockProjectManager.proxy(), (PermissionManager) mockPermissionManager.proxy(), getAuthenticationContext());
        quickSearchHandler.modifySearchResult(quickSearchResult);

        if (expectedComponentIds != null)
        {
            assertEquals(new HashSet(expectedComponentIds), new HashSet(quickSearchResult.getSearchParameters("component")));
        }
        else
        {
            assertNull(quickSearchResult.getSearchParameters("component"));
        }

        //ensure that the other search words have not been removed/lost
        for (final Iterator iterator = untouchedWords.iterator(); iterator.hasNext();)
        {
            final String unhandledWord = (String) iterator.next();
            assertContains(unhandledWord, quickSearchResult.getSearchInput());
        }

        mockProjectManager.verify();
        mockPermissionManager.verify();
        //assert that for each project, the findAllForProject was called
        assertEquals(expectedComponentIds == null ? 0 : projectToComponents.size(), findAllForProjectCalledCount.get());
    }

    //--------------------------------------------------------------------------------------------------- Helper Methods

    private ProjectComponent getProjectComponent(final Long id, final String name, final String description, final String lead, final long assigneeType, final Long projectId)
    {
        return new MutableProjectComponent(id, name, description, lead, assigneeType, projectId);
    }

    protected static void assertContains(final String match, final String container)
    {
        assertTrue(container.indexOf(match) != -1);
    }

    protected JiraAuthenticationContext getAuthenticationContext()
    {
        return new MockSimpleAuthenticationContext(null);
    }
}
