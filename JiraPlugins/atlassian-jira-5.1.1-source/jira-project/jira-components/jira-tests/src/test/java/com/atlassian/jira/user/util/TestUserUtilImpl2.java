package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.studio.MockStudioHooks;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.MockComponentLocator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * TestUserUtilImpl without being a JiraMockTestCase
 *
 * @since v4.3
 */
public class TestUserUtilImpl2 extends ListeningTestCase
{
    @Test
    public void testGetAllUsersInGroupNamesEmpty() throws Exception
    {
        UserUtilImpl userUtil = new UserUtilImpl(new MockComponentLocator(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks());
        try
        {
            userUtil.getAllUsersInGroupNames(null);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            // good.
        }

        SortedSet<User> users = userUtil.getAllUsersInGroupNames(new ArrayList<String>());
        assertTrue(users.isEmpty());
    }

    @Test
    public void testGetAllUsersInGroupNames() throws Exception
    {
        // Set up users and groups.
        MockCrowdService crowdService = new MockCrowdService();
        crowdService.addUser(new MockUser("ZAC"), "");
        final User userAdam = new MockUser("adam");
        final Group groupAnts = new MockGroup("ants");
        crowdService.addUser(userAdam, "");
        crowdService.addGroup(groupAnts);
        crowdService.addUserToGroup(userAdam, groupAnts);
        final User userBetty = new MockUser("betty");
        final User userBertie = new MockUser("bertie");
        final Group groupBeetles = new MockGroup("beetles");
        crowdService.addUser(userBetty, "");
        crowdService.addUser(userBertie, "");
        crowdService.addGroup(groupBeetles);
        crowdService.addUserToGroup(userBetty, groupBeetles);
        crowdService.addUserToGroup(userBertie, groupBeetles);

        // Create my UserUtilImpl
        UserUtilImpl userUtil = new UserUtilImpl(new MockComponentLocator(), null, null, crowdService, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks());

        // Ants
        Collection<String> groupNames = CollectionBuilder.list("ants");
        SortedSet<User> users = userUtil.getAllUsersInGroupNames(groupNames);
        assertEquals(1, users.size());
        assertTrue(users.contains(userAdam));

        // Beetles
        groupNames = CollectionBuilder.list("beetles");
        users = userUtil.getAllUsersInGroupNames(groupNames);
        assertEquals(2, users.size());
        assertTrue(users.contains(userBertie));
        assertTrue(users.contains(userBetty));

        // Ants and Beetles
        groupNames = CollectionBuilder.list("ants", "beetles");
        users = userUtil.getAllUsersInGroupNames(groupNames);
        assertEquals(3, users.size());
        assertTrue(users.contains(userAdam));
        assertTrue(users.contains(userBertie));
        assertTrue(users.contains(userBetty));
    }

    @Test
    public void testGetAllUsersInGroupsEmpty() throws Exception
    {
        UserUtilImpl userUtil = new UserUtilImpl(new MockComponentLocator(), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks());
        try
        {
            userUtil.getAllUsersInGroups(null);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            // good.
        }

        SortedSet<User> users = userUtil.getAllUsersInGroups(new ArrayList<Group>());
        assertTrue(users.isEmpty());
    }

    @Test
    public void testGetAllUsersInGroups() throws Exception
    {
        // Set up users and groups.
        MockCrowdService crowdService = new MockCrowdService();
        crowdService.addUser(new MockUser("ZAC"), "");
        final User userAdam = new MockUser("adam");
        final Group groupAnts = new MockGroup("ants");
        crowdService.addUser(userAdam, "");
        crowdService.addGroup(groupAnts);
        crowdService.addUserToGroup(userAdam, groupAnts);
        final User userBetty = new MockUser("betty");
        final User userBertie = new MockUser("bertie");
        final Group groupBeetles = new MockGroup("beetles");
        crowdService.addUser(userBetty, "");
        crowdService.addUser(userBertie, "");
        crowdService.addGroup(groupBeetles);
        crowdService.addUserToGroup(userBetty, groupBeetles);
        crowdService.addUserToGroup(userBertie, groupBeetles);

        // Create my UserUtilImpl
        UserUtilImpl userUtil = new UserUtilImpl(new MockComponentLocator(), null, null, crowdService, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks());

        // Ants
        Collection<Group> groups = CollectionBuilder.<Group>list(new MockGroup("ants"));
        SortedSet<User> users = userUtil.getAllUsersInGroups(groups);
        assertEquals(1, users.size());
        assertTrue(users.contains(userAdam));

        // Beetles
        groups = CollectionBuilder.<Group>list(new MockGroup("beetles"));
        users = userUtil.getAllUsersInGroups(groups);
        assertEquals(2, users.size());
        assertTrue(users.contains(userBertie));
        assertTrue(users.contains(userBetty));

        // Ants and Beetles
        groups = CollectionBuilder.<Group>list(new MockGroup("ants"), new MockGroup("beetles"));
        users = userUtil.getAllUsersInGroups(groups);
        assertEquals(3, users.size());
        assertTrue(users.contains(userAdam));
        assertTrue(users.contains(userBertie));
        assertTrue(users.contains(userBetty));
    }
}
