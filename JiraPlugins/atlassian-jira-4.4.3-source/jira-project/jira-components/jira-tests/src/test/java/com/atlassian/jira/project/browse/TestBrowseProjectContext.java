package com.atlassian.jira.project.browse;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.query.Query;
import com.opensymphony.user.User;

import java.util.Map;

public class TestBrowseProjectContext extends MockControllerTestCase
{
    @Test
    public void testCreateSearchQuery() throws Exception
    {
        mockController.replay();

        BrowseProjectContext ctx = new BrowseProjectContext(null, new MockProject(100L, "STVO"));
        Query initialQuery = ctx.createQuery();
        assertEquals(JqlQueryBuilder.newBuilder().where().project("STVO").buildQuery(), initialQuery);
    }

    @Test
    public void testCreateParameters() throws Exception
    {
        final Project proj100 = new MockProject(100L, "JQL");
        final User admin = new User("admin", new MockProviderAccessor(), new MockCrowdService());
        BrowseProjectContext ctx = new BrowseProjectContext(admin, proj100);

        final Map<String,Object> params = ctx.createParameterMap();
        assertEquals(2, params.size());
        assertEquals(admin, params.get("user"));
        assertEquals(proj100, params.get("project"));
    }
}
