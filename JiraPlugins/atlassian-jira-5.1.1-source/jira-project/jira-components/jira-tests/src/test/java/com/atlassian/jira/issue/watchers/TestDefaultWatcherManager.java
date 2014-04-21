/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.watchers;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.issue.index.MemoryIndexManager;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.MockUser;
import com.mockobjects.dynamic.Mock;
import org.ofbiz.core.entity.GenericValue;

import java.util.Locale;

public class TestDefaultWatcherManager extends AbstractUsersTestCase
{
    WatcherManager watcherManager;
    Mock cacheManager;
    private User bob;
    private GenericValue issueGV;
    private GenericValue project;
    private MockIssue issue;

    public TestDefaultWatcherManager(final String s)
    {
        super(s);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        CrowdService crowdService = ComponentManager.getComponentInstanceOfType(CrowdService.class);
        bob = new MockUser("bob");
        crowdService.addUser(bob, "password");
        issueGV = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "key", "JRA-52", "project", new Long(1), "watches", new Long(0)));
        issue = new MockIssue();
        issue.setGenericValue(issueGV);
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "JRA"));

        JiraTestUtil.loginUser(bob);

        watcherManager = new DefaultWatcherManager(getUserAssociationStore(), null, new MemoryIndexManager(), null);
    }

    private UserAssociationStore getUserAssociationStore()
    {
        return ComponentManager.getComponent(UserAssociationStore.class);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        bob = null;
        issueGV = null;
        project = null;
        cacheManager = null;
        watcherManager = null;

    }

    public void testAddUserToWatchList()
    {
        assertTrue(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).isEmpty());

        watcherManager.startWatching(bob, issueGV);

        assertFalse(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).isEmpty());
        assertTrue(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).contains(bob));
    }

    public void testRemoveUserFromWatchList()
    {
        assertTrue(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).isEmpty());

        watcherManager.startWatching(bob, issueGV);

        assertFalse(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).isEmpty());
        assertTrue(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).contains(bob));

        watcherManager.stopWatching(bob, issueGV);

        assertTrue(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).isEmpty());
        assertFalse(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).contains(bob));
    }

    public void testRemoveAllWatchesForUser()
    {
        assertTrue(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).isEmpty());

        watcherManager.startWatching(bob, issueGV);
        GenericValue issue2GV = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(3), "key", "JRA-53", "project", new Long(1), "watches", new Long(0)));
        MockIssue issue2 = new MockIssue();
        issue2.setGenericValue(issue2GV);
        watcherManager.startWatching(bob, issue2GV);
        assertTrue(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).contains(bob));
        assertTrue(watcherManager.getCurrentWatchList(issue2, Locale.ENGLISH).contains(bob));

        watcherManager.removeAllWatchesForUser(bob);
        assertFalse(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH).contains(bob));
        assertFalse(watcherManager.getCurrentWatchList(issue2, Locale.ENGLISH).contains(bob));
    }

    public void testRemoveAllWatchesForUserNullParam()
    {
        final WatcherManager wm = new DefaultWatcherManager(null, null, new MemoryIndexManager(), null);
        try
        {
            wm.removeAllWatchesForUser(null);
            fail("IllegalArgumentEx should have been thrown");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }
    }
}
