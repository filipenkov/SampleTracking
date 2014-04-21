package com.atlassian.jira.security.groups;

import com.atlassian.crowd.embedded.api.User;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.local.ListeningTestCase;

public class TestDefaultGroupManager extends ListeningTestCase
{
    @Test
    public void testIsUserInGroup() throws Exception
    {
        final MockCrowdService mockCrowdService = new MockCrowdService();
        DefaultGroupManager groupManager = new DefaultGroupManager(mockCrowdService);
        assertFalse(groupManager.isUserInGroup(new MockUser("fred"), new MockGroup("dudes")));

        mockCrowdService.addUserToGroup(new MockUser("fred"), new MockGroup("dudes"));
        assertTrue(groupManager.isUserInGroup(new MockUser("fred"), new MockGroup("dudes")));
    }

    @Test
    public void testIsUserInGroupHandlesNulls() throws Exception
    {
        // Need to handle null user and null group in order to maintain behaviour from OSUser.
        DefaultGroupManager groupManager = new DefaultGroupManager(null);
        assertFalse(groupManager.isUserInGroup(null, new MockGroup("dudes")));
        assertFalse(groupManager.isUserInGroup(new MockUser("fred"), null));
        assertFalse(groupManager.isUserInGroup((String) null, null));
        assertFalse(groupManager.isUserInGroup((User) null, null));
        assertFalse(groupManager.isUserInGroup((com.opensymphony.user.User) null, null));
    }
}
