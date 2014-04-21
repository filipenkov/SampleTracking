package com.atlassian.jira.issue.fields;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.jql.context.ClauseContext;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.ProjectIssueTypeContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.bean.FieldVisibilityBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit test for {@link com.atlassian.jira.issue.fields.FieldContextGenerator}.
 *
 * @since v4.0
 */
public class TestFieldContextGenerator extends MockControllerTestCase
{
    private static final IssueType BUG = new MockIssueType("bug", "bug");
    private static final IssueType FEATURE = new MockIssueType("feature", "feature");
    private static final IssueType IMPROVEMENT = new MockIssueType("improvement", "improvement");
    private static final IssueType WART = new MockIssueType("wart", "wart");
    private static final IssueType BOOK_REQUEST = new MockIssueType("bookrequest", "bookrequest");
    private List<Project> projects;
    private MockProject project1;
    private MockProject project2;
    private MockProject project3;


    @Before
    public void setUp() throws Exception
    {
        project1 = new MockProject();
        project1.setId(1L);
        project2 = new MockProject();
        project2.setId(2L);
        project3 = new MockProject();
        project3.setId(3L);
        projects = Arrays.<Project>asList(project1, project2, project3);

    }

    @Test
    public void testMultipleProjectsAllVisible() throws Exception
    {
        final FieldVisibilityBean visibilityBean = mockController.getMock(FieldVisibilityBean.class);
        visibilityBean.isFieldHiddenInAllSchemes(project1.getId(), "test");
        mockController.setReturnValue(false);
        visibilityBean.isFieldHiddenInAllSchemes(project2.getId(), "test");
        mockController.setReturnValue(false);
        visibilityBean.isFieldHiddenInAllSchemes(project3.getId(), "test");
        mockController.setReturnValue(false);

        mockController.replay();

        final FieldContextGenerator contextGenerator = new FieldContextGenerator(visibilityBean);
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        final ClauseContext clauseContext = contextGenerator.generateClauseContext(projects, "test");
        assertEquals(expectedContext, clauseContext);

        mockController.verify();
    }

    @Test
    public void testMultipleProjectsOneNotVisible() throws Exception
    {
        final FieldVisibilityBean visibilityBean = mockController.getMock(FieldVisibilityBean.class);
        visibilityBean.isFieldHiddenInAllSchemes(project1.getId(), "test");
        mockController.setReturnValue(true);
        visibilityBean.isFieldHiddenInAllSchemes(project2.getId(), "test");
        mockController.setReturnValue(false);
        visibilityBean.isFieldHiddenInAllSchemes(project3.getId(), "test");
        mockController.setReturnValue(false);

        mockController.replay();

        final FieldContextGenerator contextGenerator = new FieldContextGenerator(visibilityBean);
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        final ClauseContext clauseContext = contextGenerator.generateClauseContext(projects, "test");
        assertEquals(expectedContext, clauseContext);

        mockController.verify();
    }

    @Test
    public void testMultipleProjectsAllNotVisible() throws Exception
    {
        final FieldVisibilityBean visibilityBean = mockController.getMock(FieldVisibilityBean.class);
        visibilityBean.isFieldHiddenInAllSchemes(project1.getId(), "test");
        mockController.setReturnValue(true);
        visibilityBean.isFieldHiddenInAllSchemes(project2.getId(), "test");
        mockController.setReturnValue(true);
        visibilityBean.isFieldHiddenInAllSchemes(project3.getId(), "test");
        mockController.setReturnValue(true);

        mockController.replay();

        final FieldContextGenerator contextGenerator = new FieldContextGenerator(visibilityBean);
        final ClauseContext expectedContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        final ClauseContext clauseContext = contextGenerator.generateClauseContext(projects, "test");
        assertEquals(expectedContext, clauseContext);

        mockController.verify();
    }

}
