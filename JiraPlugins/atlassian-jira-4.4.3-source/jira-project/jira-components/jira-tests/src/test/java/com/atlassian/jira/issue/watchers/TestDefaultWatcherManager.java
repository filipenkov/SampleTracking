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
import com.atlassian.jira.user.MockUser;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.mockobjects.dynamic.Mock;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Locale;

public class TestDefaultWatcherManager extends AbstractUsersTestCase
{
    DefaultWatcherManager watcherManager;
    Mock cacheManager;
    private User bob;
    private GenericValue issue;
    private GenericValue project;

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
        issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "key", "JRA-52", "project", new Long(1), "watches", new Long(0)));
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
        issue = null;
        project = null;
        cacheManager = null;
        watcherManager = null;

    }

    public void testAddUserToWatchList()
    {
        assertTrue(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).isEmpty());

        watcherManager.startWatching(bob, issue);

        assertFalse(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).isEmpty());
        assertTrue(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).contains(bob));
    }

    public void testRemoveUserFromWatchList()
    {
        assertTrue(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).isEmpty());

        watcherManager.startWatching(bob, issue);

        assertFalse(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).isEmpty());
        assertTrue(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).contains(bob));

        watcherManager.stopWatching(bob, issue);

        assertTrue(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).isEmpty());
        assertFalse(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).contains(bob));
    }

    public void testRemoveAllWatchesForUser()
    {
        assertTrue(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).isEmpty());

        watcherManager.startWatching(bob, issue);
        GenericValue issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(3), "key", "JRA-53", "project", new Long(1), "watches", new Long(0)));
        watcherManager.startWatching(bob, issue2);
        assertTrue(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).contains(bob));
        assertTrue(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue2).contains(bob));

        watcherManager.removeAllWatchesForUser(bob);
        assertFalse(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue).contains(bob));
        assertFalse(watcherManager.getCurrentWatchList(Locale.ENGLISH, issue2).contains(bob));
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
