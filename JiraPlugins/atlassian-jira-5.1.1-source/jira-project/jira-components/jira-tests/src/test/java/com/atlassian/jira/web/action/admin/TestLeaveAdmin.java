package com.atlassian.jira.web.action.admin;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeLinker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class TestLeaveAdmin extends ListeningTestCase
{
    private static final String MY_JIRA_HOME = "/my-jira-home";

    private JiraAuthenticationContext mockJiraAuthenticationContext = mock(JiraAuthenticationContext.class);
    private User mockUser = mock(User.class);
    private HttpServletResponse mockHttpServletRespone = mock(HttpServletResponse.class);

    private MyJiraHomeLinker mockMyJiraHomeLinker = mock(MyJiraHomeLinker.class);

    private LeaveAdmin action = new LeaveAdmin(mockMyJiraHomeLinker);

    @Before
    public void setUpMocks()
    {
        final ComponentAccessor.Worker mockWorker = mock(ComponentAccessor.Worker.class);
        ComponentAccessor.initialiseWorker(mockWorker);

        when(mockWorker.getComponent(JiraAuthenticationContext.class)).thenReturn(mockJiraAuthenticationContext);

        ServletActionContext.setResponse(mockHttpServletRespone);
    }

    @Test
    public void testLeaveAdminWhenUserNotLoggedIn() throws Exception
    {
        expectUserIsNotLoggedInAndHomeIsDefault();

        action.doExecute();

        verifyRedirect(MyJiraHomeLinker.DEFAULT_HOME);
    }

    @Test
    public void testLeaveAdminWhenUserLoggedInWithHomeSet() throws Exception
    {
        expectUserIsLoggedInWithHomeSet();

        action.doExecute();

        verifyRedirect(MY_JIRA_HOME);
    }

    @Test
    public void testAvoidRedirectLoopWhenLeavingAdmin() throws Exception
    {
        expectUserIsLoggedInWithAdminSummarySet();

        action.doExecute();

        verifyRedirect(MyJiraHomeLinker.DEFAULT_HOME);
    }

    private void expectUserIsNotLoggedInAndHomeIsDefault()
    {
        when(mockJiraAuthenticationContext.getLoggedInUser()).thenReturn(null);
        when(mockMyJiraHomeLinker.getHomeLink(null)).thenReturn(MyJiraHomeLinker.DEFAULT_HOME);
    }

    private void expectUserIsLoggedInWithHomeSet()
    {
        when(mockJiraAuthenticationContext.getLoggedInUser()).thenReturn(mockUser);
        expectFindMyHomeIs(MY_JIRA_HOME);
    }

    private void expectUserIsLoggedInWithAdminSummarySet()
    {
        when(mockJiraAuthenticationContext.getLoggedInUser()).thenReturn(mockUser);
        expectFindMyHomeIs(LeaveAdmin.ADMIN_SUMMARY_LOCATION);
    }

    private void expectFindMyHomeIs(final String home)
    {
        when(mockMyJiraHomeLinker.getHomeLink(mockUser)).thenReturn(home);
    }

    private void verifyRedirect(final String expectedUrl) throws IOException
    {
        verify(mockHttpServletRespone).sendRedirect(eq(expectedUrl));
    }
}
