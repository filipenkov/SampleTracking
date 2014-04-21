package com.atlassian.streams.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.streams.api.common.Option;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProjectEntityResolverTest
{
    private static final String EXISTING_PROJECT_KEY = "existing-project-key";
    private static final String NONEXISTENT_PROJECT_KEY = "nonexistent-project-key";

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private ProjectService projectService;

    @Test
    public void testNotExistingProject()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("The project doesn't exist");
        when(projectService.getProjectByKey(any(User.class), anyString())).thenReturn(
                new ProjectService.GetProjectResult(errorCollection));

        Option<Object> option = new ProjectEntityResolver(jiraAuthenticationContext, projectService).apply(NONEXISTENT_PROJECT_KEY);
        assertFalse("The returned value is " + option + " while it should be not defined", option.isDefined());
    }

    @Test
    public void testExistingProjectWithNotAuthorizedUser()
    {
        User mockedUser = mock(User.class);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("The user does not have access to the project");

        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(mockedUser);
        when(projectService.getProjectByKey(eq(mockedUser), eq(EXISTING_PROJECT_KEY))).thenReturn(
                new ProjectService.GetProjectResult(errorCollection));

        Option<Object> option = new ProjectEntityResolver(jiraAuthenticationContext, projectService).apply(EXISTING_PROJECT_KEY);
        assertFalse("The returned value is " + option + " while it should be not defined", option.isDefined());
    }

    @Test
    public void testExistingProjectWithAuthorizedUser()
    {
        User mockedUser = mock(User.class);
        Project mockedProject = mock(Project.class);

        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(mockedUser);
        when(projectService.getProjectByKey(eq(mockedUser), eq(EXISTING_PROJECT_KEY))).thenReturn(
                new ProjectService.GetProjectResult(new SimpleErrorCollection(), mockedProject));

        Option<Object> option = new ProjectEntityResolver(jiraAuthenticationContext, projectService).apply(EXISTING_PROJECT_KEY);
        assertTrue("The returned value is not defined while it should be a project instance", option.isDefined());
    }
}