package com.atlassian.jira.plugin.componentpanel;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.component.MutableProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.Query;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestBrowseComponentContextImpl extends MockControllerTestCase
{
    private SearchService searchService;

    @Before
    public void setUp() throws Exception
    {
        searchService = mockController.getMock(SearchService.class);
    }

    @Test
    public void testCreateSearchQuery() throws Exception
    {
        final Project proj100 = new MockProject(100L,"HSP");
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        projectManager.getProjectObj(100L);
        mockController.setReturnValue(proj100);
        mockController.replay();

        final MutableProjectComponent component = new MutableProjectComponent(77L, "JQL Components", null, null, 1, 100L);
        BrowseComponentContext ctx = new BrowseComponentContextImpl(searchService, component, null)
        {
            protected ProjectManager getProjectManager()
            {
                return projectManager;
            }
        };
        Query initialQuery = ctx.createQuery();
        assertEquals(JqlQueryBuilder.newBuilder().where().project("HSP").and().component("JQL Components").buildQuery(), initialQuery);
    }

    @Test
    public void testCreateParameters() throws Exception
    {
        final Project proj100 = new MockProject(100L);
        final ProjectComponent component77 = new MutableProjectComponent(77L, null, null, null, 1, 100L);
        final User admin = new MockUser("admin");

        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        projectManager.getProjectObj(100L);
        mockController.setReturnValue(proj100);
        mockController.replay();

        BrowseComponentContext ctx = new BrowseComponentContextImpl(searchService, component77, admin)
        {
            protected ProjectManager getProjectManager()
            {
                return projectManager;
            }
        };

        final Map<String,Object> params = ctx.createParameterMap();
        assertEquals(3, params.size());
        assertEquals(admin, params.get("user"));
        assertEquals(proj100, params.get("project"));
        assertEquals(component77, params.get("component"));
    }
}
