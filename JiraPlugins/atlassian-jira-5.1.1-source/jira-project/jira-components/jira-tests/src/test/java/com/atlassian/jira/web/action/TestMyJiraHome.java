package com.atlassian.jira.web.action;

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
public class TestMyJiraHome extends ListeningTestCase
{
    private static final String DEFAULT_HOME = MyJiraHomeLinker.DEFAULT_HOME;

    private JiraAuthenticationContext mockJiraAuthenticationContext = mock(JiraAuthenticationContext.class);
    private User mockUser = mock(User.class);
    private HttpServletResponse mockHttpServletRespone = mock(HttpServletResponse.class);

    private MyJiraHomeLinker myJiraHomeLinker = mock(MyJiraHomeLinker.class);

    private MyJiraHome action = new MyJiraHome(myJiraHomeLinker);

    @Before
    public void setUpMocks()
    {
        final ComponentAccessor.Worker mockWorker = mock(ComponentAccessor.Worker.class);
        ComponentAccessor.initialiseWorker(mockWorker);
        
        when(mockWorker.getComponent(JiraAuthenticationContext.class)).thenReturn(mockJiraAuthenticationContext);

        ServletActionContext.setResponse(mockHttpServletRespone);
    }

    @Test
    public void testExecuteAsAnonymousUser() throws Exception
    {
        expectAnonymousUser();

        action.doExecute();

        verifyRedirect(DEFAULT_HOME);
    }


    @Test
    public void testExecuteAsAuthenticatedUser() throws Exception
    {
        expectAuthenticatedUser();

        action.doExecute();

        verifyRedirect(DEFAULT_HOME);
    }

    private void expectAnonymousUser()
    {
        when(mockJiraAuthenticationContext.getLoggedInUser()).thenReturn(null);
        when(myJiraHomeLinker.getHomeLink(null)).thenReturn(DEFAULT_HOME);
    }

    private void expectAuthenticatedUser()
    {
        when(mockJiraAuthenticationContext.getLoggedInUser()).thenReturn(mockUser);
        when(myJiraHomeLinker.getHomeLink(mockUser)).thenReturn(DEFAULT_HOME);
    }

    private void verifyRedirect(final String expectedUrl) throws IOException
    {
        verify(mockHttpServletRespone).sendRedirect(eq(expectedUrl));
    }

}
