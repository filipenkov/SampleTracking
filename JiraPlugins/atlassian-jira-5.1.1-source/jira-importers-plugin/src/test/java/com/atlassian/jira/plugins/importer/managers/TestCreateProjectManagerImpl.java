package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugins.importer.external.ExternalException;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestCreateProjectManagerImpl {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock private JiraAuthenticationContext authenticationContext;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private ProjectService projectService;
    @Mock private PermissionSchemeManager permissionSchemeManager;
    @Mock private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    @Mock private WorkflowSchemeManager workflowSchemeManager;
    @Mock private CreateProjectHandlerProvider createProjectHandlerProvider;
    @Mock private GlobalPermissionManager globalPermissionManager;

    private CreateProjectManagerImpl manager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        manager = new CreateProjectManagerImpl(authenticationContext, applicationProperties, projectService, permissionSchemeManager,
               issueTypeScreenSchemeManager, workflowSchemeManager, createProjectHandlerProvider, globalPermissionManager);
    }

    @Test
    public void managerThrowsAnExceptionWhenCanCreateProjectReturnsFalse() throws ExternalException {
        thrown.expect(ExternalException.class);
        thrown.expectMessage("User bob is not allowed to create projects.");

        CreateProjectHandler projectHandler = mock(CreateProjectHandler.class);
        User user = mock(User.class);
        when(user.getName()).thenReturn("bob");

        when(projectHandler.canCreateProjects(user)).thenReturn(false);

        when(createProjectHandlerProvider.getHandler()).thenReturn(projectHandler);

        manager.createProject(user, new ExternalProject(), ConsoleImportLogger.INSTANCE);
    }

}
