package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collection;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestIssueTypeSearchContextVisibilityChecker extends MockControllerTestCase
{
    private IssueTypeSchemeManager issueTypeSchemeManager;
    private ProjectManager projectManager;
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        issueTypeSchemeManager = mockController.getMock(IssueTypeSchemeManager.class);
        projectManager = mockController.getMock(ProjectManager.class);
        searchContext = mockController.getMock(SearchContext.class);
    }

    @Test
    public void testAllProjects() throws Exception
    {
        searchContext.isForAnyProjects();
        mockController.setReturnValue(true);

        mockController.replay();
        final IssueTypeSearchContextVisibilityChecker checker = new IssueTypeSearchContextVisibilityChecker(projectManager, issueTypeSchemeManager);
        final Collection<String> inIds = CollectionBuilder.newBuilder("1", "2").asCollection();
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, inIds);
        final Set<String> expetedResult = CollectionBuilder.newBuilder("1", "2").asHashSet();
        assertEquals(expetedResult, result);
        mockController.verify();
    }

    @Test
    public void testOneIdNotGood() throws Exception
    {
        final Project project1 = new MockProject(3L);
        final Project project2 = new MockProject(4L);

        final IssueType type1 = new MockIssueType("1", "1");
        final IssueType type2 = new MockIssueType("2", "2");
        final IssueType type4 = new MockIssueType("4", "4");

        searchContext.isForAnyProjects();
        mockController.setReturnValue(false);

        searchContext.getProjectIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder(3L, 4L).asList());

        projectManager.getProjectObj(3L);
        mockController.setReturnValue(project1);

        projectManager.getProjectObj(4L);
        mockController.setReturnValue(project2);

        issueTypeSchemeManager.getIssueTypesForProject(project1);
        mockController.setReturnValue(CollectionBuilder.newBuilder(type1, type4).asList());

        issueTypeSchemeManager.getIssueTypesForProject(project2);
        mockController.setReturnValue(CollectionBuilder.newBuilder(type2).asList());
        
        mockController.replay();
        final IssueTypeSearchContextVisibilityChecker checker = new IssueTypeSearchContextVisibilityChecker(projectManager, issueTypeSchemeManager);
        final Collection<String> inIds = CollectionBuilder.newBuilder("1", "2", "3").asCollection();
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, inIds);
        final Set<String> expetedResult = CollectionBuilder.newBuilder("1", "2").asHashSet();
        assertEquals(expetedResult, result);
        mockController.verify();
    }
}
