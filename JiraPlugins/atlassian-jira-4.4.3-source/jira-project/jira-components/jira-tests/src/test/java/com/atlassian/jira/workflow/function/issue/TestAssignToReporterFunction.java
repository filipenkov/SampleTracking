package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.user.MockCrowdService;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.map.EasyMap;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Map;

public class TestAssignToReporterFunction extends ListeningTestCase
{
    private User user;

    @Before
    public void setUp() throws Exception
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        user = new User("admin", mpa, new MockCrowdService());
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
    }

    @Test
    public void testBrandNewIssueDoesntStore()
    {
        AssignToReporterFunction func = new AssignToReporterFunction();

        Mock mockIssue = createMockIssue(false);
        Issue issue = (Issue) mockIssue.proxy();
        Map transientVars = EasyMap.build("issue", issue);

        func.execute(transientVars, null, null);
        mockIssue.verify();
    }

    @Test
    public void testExistingIssueDoesStore()
    {
        AssignToReporterFunction func = new AssignToReporterFunction();

        Mock mockIssue = createMockIssue(true);
        Issue issue = (Issue) mockIssue.proxy();
        Map transientVars = EasyMap.build("issue", issue);

        func.execute(transientVars, null, null);
        mockIssue.verify();
    }

    private Mock createMockIssue(boolean isCreated)
    {
        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);

        mockIssue.expectAndReturn("getReporter", user);
        mockIssue.expectVoid("setAssignee", P.args(P.eq(user)));
        mockIssue.expectAndReturn("isCreated", (isCreated ? Boolean.TRUE : Boolean.FALSE));

        if (isCreated)
        {
            mockIssue.expectVoid("store", P.ANY_ARGS);
        }
        else
        {
            mockIssue.expectNotCalled("store");
        }
        return mockIssue;
    }
}
