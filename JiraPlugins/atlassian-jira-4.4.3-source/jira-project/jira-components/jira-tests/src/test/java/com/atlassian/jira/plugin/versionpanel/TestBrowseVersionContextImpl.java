package com.atlassian.jira.plugin.versionpanel;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.query.Query;
import com.opensymphony.user.User;

import java.util.Map;

public class TestBrowseVersionContextImpl extends MockControllerTestCase
{
    @Test
    public void testCreateSearchQuery() throws Exception
    {
        final Project proj100 = new MockProject(100L,"BLUB");
        mockController.replay();

        final Version version = new MockVersion(444L, "New Version 1")
        {
            public Project getProjectObject()
            {
                return proj100;
            }
        };
        BrowseVersionContext ctx = new BrowseVersionContextImpl(version, null);

        final Query query = ctx.createQuery();

        assertEquals(JqlQueryBuilder.newBuilder().where().project("BLUB").and().fixVersion().eq("New Version 1").buildQuery(), query);
    }

    @Test
    public void testCreateParameters() throws Exception
    {
        final Project proj100 = new MockProject(100L);
        final Version version444 = new MockVersion(444L, "New Version 1")
        {
            public Project getProjectObject()
            {
                return proj100;
            }
        };
        final User admin = new User("admin", new MockProviderAccessor(), new MockCrowdService());

        BrowseVersionContext ctx = new BrowseVersionContextImpl(version444, admin)
        {
            protected ProjectManager getProjectManager()
            {
                return null;
            }
        };

        final Map<String,Object> params = ctx.createParameterMap();
        assertEquals(3, params.size());
        assertEquals(admin, params.get("user"));
        assertEquals(proj100, params.get("project"));
        assertEquals(version444, params.get("version"));
    }
}
