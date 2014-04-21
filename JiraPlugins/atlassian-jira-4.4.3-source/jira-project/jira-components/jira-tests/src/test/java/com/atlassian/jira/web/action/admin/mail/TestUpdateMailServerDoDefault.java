package com.atlassian.jira.web.action.admin.mail;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.MockedComponentManagerTestCase;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import webwork.action.ActionContext;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link UpdateMailServer#doDefault()}, without using heavy-weight legacy test base classes.
 *
 * @since v4.3
 */
public class TestUpdateMailServerDoDefault extends MockedComponentManagerTestCase
{
    private MailServerManager mockManager = EasyMock.createNiceMock(MailServerManager.class);
    private PermissionManager mockPermissionManager = EasyMock.createNiceMock(PermissionManager.class);
    private JiraAuthenticationContext mockJiraAuthenticationContext = EasyMock.createNiceMock(JiraAuthenticationContext.class);
    private User currentUser = new MockUser("testuser");

    @Before
    public void stubMockServerManager()
    {
        MailFactory.setServerManager(mockManager);
    }

    @Before
    public void stubMocks()
    {
        // TODO the call to ComponentManager.getComponent should be eliminated from MailServerActionSupport in favour of DI
        // we wouldnt need this call here then
        addMock(WebResourceManager.class, createNiceMock(WebResourceManager.class));
        addMock(PermissionManager.class, mockPermissionManager);
        addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext);
    }

    @After
    public void reset()
    {
        MailFactory.setServerManager(null);
        ActionContext.setContext(null);
    }

    @Test
    public void doDefaultShouldSetAllPropertiesForPopServer() throws Exception
    {
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andStubReturn(currentUser);
        expect(mockManager.getMailServer(1L)).andReturn(new PopMailServerImpl(
                1L, "Test server", "Test desc", MailProtocol.POP, "pop.test.com", "110", "testuser", "testpassword")).anyTimes();
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)).andReturn(true).anyTimes();
        replay(mockManager, mockPermissionManager, mockJiraAuthenticationContext);
        UpdateMailServer tested = new UpdateMailServer();
        tested.setId(1L);
        assertEquals("input", tested.doDefault());
        assertEquals("Test server", tested.getName());
        assertEquals("Test desc", tested.getDescription());
        assertEquals("pop3", tested.getProtocol());
        assertEquals("pop.test.com", tested.getServerName());
        assertEquals("110", tested.getPort());
        assertEquals("testuser", tested.getUsername());
        assertEquals("testpassword", tested.getPassword());
    }
}
