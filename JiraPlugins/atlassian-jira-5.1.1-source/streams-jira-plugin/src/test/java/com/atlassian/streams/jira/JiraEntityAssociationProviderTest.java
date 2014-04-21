package com.atlassian.streams.jira;

import java.net.URI;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.streams.spi.StreamsEntityAssociationProvider;
import com.atlassian.streams.testing.AbstractEntityAssociationProviderWithIssuesTest;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.project;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraEntityAssociationProviderTest extends AbstractEntityAssociationProviderWithIssuesTest
{
    @Mock PermissionManager permissionManager;
    @Mock ProjectManager projectManager;
    @Mock IssueManager issueManager;
    @Mock JiraAuthenticationContext authenticationContext;
    @Mock User user;
    @Mock Project project;
    @Mock MutableIssue issue;
    
    @Before
    public void setup()
    {
        when(authenticationContext.getLoggedInUser()).thenReturn(user);
    }

    @Override
    public StreamsEntityAssociationProvider createProvider()
    {
        return new JiraEntityAssociationProvider(applicationProperties, permissionManager, projectManager,
                                                 issueManager, authenticationContext);
    }

    @Override
    protected String getProjectUriPath(String key)
    {
        return "/browse/" + key;
    }
    
    @Override
    protected URI getProjectEntityType()
    {
        return project().iri(); 
    }
    
    @Override
    protected void setProjectExists(String key, boolean exists)
    {
        when(projectManager.getProjectObjByKeyIgnoreCase(key)).thenReturn(exists ? project : null);
    }
    
    @Override
    protected void setProjectViewPermission(String key, boolean permitted)
    {
        when(projectManager.getProjectObjByKeyIgnoreCase(key)).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, user)).thenReturn(permitted);
    }

    @Override
    protected void setProjectEditPermission(String key, boolean permitted)
    {
        setProjectViewPermission(key, permitted);
    }

    @Override
    protected String getIssueUriPath(String key)
    {
        return "/browse/" + key;
    }
    
    @Override
    protected URI getIssueEntityType()
    {
        return issue().iri(); 
    }
    
    @Override
    protected void setIssueExists(String key, boolean exists)
    {
        when(issueManager.getIssueObject(key)).thenReturn(exists ? issue : null);
    }

    @Override
    protected void setIssueViewPermission(String key, boolean permitted)
    {
        when(issueManager.getIssueObject(key)).thenReturn(issue);
        when(permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, user)).thenReturn(permitted);
    }
    
    @Override
    protected void setIssueEditPermission(String key, boolean permitted)
    {
        when(issueManager.getIssueObject(key)).thenReturn(issue);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).thenReturn(permitted);
    }
}
