package com.atlassian.crowd.directory.ldap.cache;

import com.atlassian.crowd.directory.MicrosoftActiveDirectory;
import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.model.Tombstone;
import com.atlassian.crowd.model.group.GroupTemplateWithAttributes;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsnChangedCacheRefresherTest
{
    MockDirectoryCache mockDirectoryCache;
    
    @Mock
    MicrosoftActiveDirectory activeDirectory;
    
    LDAPUserWithAttributes userAdam, userBob, userCath;
    LDAPGroupWithAttributes lemmings, sheep, fish;

    @Before
    public void setUp() throws Exception
    {
        mockDirectoryCache = new MockDirectoryCache();
        when(activeDirectory.getMemberships()).thenCallRealMethod();
        
        userAdam = createUser("adam", "X01");
        userBob = createUser("bob", "X02");
        userCath = createUser("cath", "X03");
        lemmings = createGroup("lemmings", "X08");
        sheep = createGroup("sheep", "X09");
        fish = createGroup("fish", "X10");
    }

    @Test
    public void testCannotSyncChangesUntilFullRefresh() throws Exception
    {
        UsnChangedCacheRefresher cacheRefresher = new UsnChangedCacheRefresher(null);
        assertFalse(cacheRefresher.synchroniseChanges(null));
    }

    @Test
    public void testFullRefresh() throws Exception
    {
        final List<LDAPUserWithAttributes> userList = Arrays.asList(userAdam, userBob);
        when(activeDirectory.searchUsers(any(EntityQuery.class))).thenReturn(userList);
        final List<LDAPGroupWithAttributes> groupList = Arrays.asList(lemmings, sheep);
        when(activeDirectory.searchGroups(any(EntityQuery.class))).thenReturn(groupList);

        // Create the class under test
        UsnChangedCacheRefresher cacheRefresher = new UsnChangedCacheRefresher(activeDirectory);
        // Can't do a delta until the first full refresh
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));
        // Do the Cache refresh
        cacheRefresher.synchroniseAll(mockDirectoryCache);

        // Expectations - Users
        assertEquals(2, mockDirectoryCache.getUsers().size());
        assertTrue(mockDirectoryCache.getUsers().contains(userAdam));
        assertTrue(mockDirectoryCache.getUsers().contains(userBob));
        // Groups
        assertEquals(2, mockDirectoryCache.getGroups().size());
        assertTrue(mockDirectoryCache.getGroups().contains(lemmings));
        assertTrue(mockDirectoryCache.getGroups().contains(sheep));
    }

    @Test
    public void testCantSynchroniseChangesWithRolesEnabled() throws Exception
    {
        // these methods get called on sync all
        when(activeDirectory.searchUsers(any(EntityQuery.class))).thenReturn(Arrays.asList(userAdam, userBob));
        when(activeDirectory.searchGroups(any(EntityQuery.class))).thenReturn(Arrays.asList(lemmings, sheep));
        when(activeDirectory.fetchHighestCommittedUSN()).thenReturn(111L);
        // make sure roles are enabled
        when(activeDirectory.isRolesDisabled()).thenReturn(false);
        // make sure incremental sync is enabled
        when(activeDirectory.getValue(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)).thenReturn("true");

        // Create the class under test
        UsnChangedCacheRefresher cacheRefresher = new UsnChangedCacheRefresher(activeDirectory);
        // Can't do a delta until the first full refresh
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));
        // Do the Cache refresh
        cacheRefresher.synchroniseAll(mockDirectoryCache);

        // Expectations - Users
        assertEquals(2, mockDirectoryCache.getUsers().size());
        assertTrue(mockDirectoryCache.getUsers().contains(userAdam));
        assertTrue(mockDirectoryCache.getUsers().contains(userBob));
        // Groups
        assertEquals(2, mockDirectoryCache.getGroups().size());
        assertTrue(mockDirectoryCache.getGroups().contains(lemmings));
        assertTrue(mockDirectoryCache.getGroups().contains(sheep));

        // Now try to refresh just changes (roles are enabled - so not allowed)
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));
    }

    @Test
    public void testCantSynchroniseChangesWithIncrementalSyncDisabled() throws Exception
    {
        // these methods get called on sync all
        when(activeDirectory.searchUsers(any(EntityQuery.class))).thenReturn(Arrays.asList(userAdam, userBob));
        when(activeDirectory.searchGroups(any(EntityQuery.class))).thenReturn(Arrays.asList(lemmings, sheep));
        when(activeDirectory.fetchHighestCommittedUSN()).thenReturn(111L);
        // make sure roles are disabled
        when(activeDirectory.isRolesDisabled()).thenReturn(true);
        // make sure incremental sync is disabled
        when(activeDirectory.getValue(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)).thenReturn("false");

        // Create the class under test
        UsnChangedCacheRefresher cacheRefresher = new UsnChangedCacheRefresher(activeDirectory);
        // Can't do a delta until the first full refresh
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));
        // Do the Cache refresh
        cacheRefresher.synchroniseAll(mockDirectoryCache);

        // Expectations - Users
        assertEquals(2, mockDirectoryCache.getUsers().size());
        assertTrue(mockDirectoryCache.getUsers().contains(userAdam));
        assertTrue(mockDirectoryCache.getUsers().contains(userBob));
        // Groups
        assertEquals(2, mockDirectoryCache.getGroups().size());
        assertTrue(mockDirectoryCache.getGroups().contains(lemmings));
        assertTrue(mockDirectoryCache.getGroups().contains(sheep));

        // Now try to refresh just changes (incremental sync is disabled - so not allowed)
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));
    }

    @Test
    public void testSynchroniseChanges() throws Exception
    {
        // these methods get called on sync all
        when(activeDirectory.searchUsers(any(EntityQuery.class))).thenReturn(Arrays.asList(userAdam, userBob));
        when(activeDirectory.searchGroups(any(EntityQuery.class))).thenReturn(Arrays.asList(lemmings, sheep));
        when(activeDirectory.fetchHighestCommittedUSN()).thenReturn(111L);
        // make sure roles are disabled
        when(activeDirectory.isRolesDisabled()).thenReturn(true);
        // make sure incremental sync is enabled
        when(activeDirectory.getValue(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)).thenReturn("true");
        // these get called on sync changes
        when(activeDirectory.findAddedOrUpdatedUsersSince(111L)).thenReturn(Arrays.asList(userCath));
        when(activeDirectory.findUserTombstonesSince(111L)).thenReturn(Arrays.asList(new Tombstone("X02", "111")));
        when(activeDirectory.findAddedOrUpdatedGroupsSince(111L)).thenReturn(Arrays.asList(fish));
        when(activeDirectory.findGroupTombstonesSince(111L)).thenReturn(Arrays.asList(new Tombstone("X09", "111")));

        // Create the class under test
        UsnChangedCacheRefresher cacheRefresher = new UsnChangedCacheRefresher(activeDirectory);
        // Can't do a delta until the first full refresh
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));
        // Do the Cache refresh
        cacheRefresher.synchroniseAll(mockDirectoryCache);

        // Expectations - Users
        assertEquals(2, mockDirectoryCache.getUsers().size());
        assertTrue(mockDirectoryCache.getUsers().contains(userAdam));
        assertTrue(mockDirectoryCache.getUsers().contains(userBob));
        // Groups
        assertEquals(2, mockDirectoryCache.getGroups().size());
        assertTrue(mockDirectoryCache.getGroups().contains(lemmings));
        assertTrue(mockDirectoryCache.getGroups().contains(sheep));

        // Now refresh just changes
        assertTrue(cacheRefresher.synchroniseChanges(mockDirectoryCache));

        // Expectations - Users
        assertEquals(2, mockDirectoryCache.getUsers().size());
        assertTrue(mockDirectoryCache.getUsers().contains(userAdam));
        assertTrue(mockDirectoryCache.getUsers().contains(userCath));
        // Groups
        assertEquals(2, mockDirectoryCache.getGroups().size());
        assertTrue(mockDirectoryCache.getGroups().contains(lemmings));
        assertTrue(mockDirectoryCache.getGroups().contains(fish));
    }

    @Test
    public void testSynchroniseChangesWithTombstonesOutsideOfScopeIgnored() throws Exception
    {
        // these methods get called on sync all
        when(activeDirectory.searchUsers(any(EntityQuery.class))).thenReturn(Arrays.asList(userAdam, userBob));
        when(activeDirectory.searchGroups(any(EntityQuery.class))).thenReturn(Arrays.asList(lemmings, sheep));
        when(activeDirectory.fetchHighestCommittedUSN()).thenReturn(111L);
        // make sure roles are disabled
        when(activeDirectory.isRolesDisabled()).thenReturn(true);
        // make sure incremental sync is enabled
        when(activeDirectory.getValue(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)).thenReturn("true");
        // these get called on sync changes
        when(activeDirectory.findAddedOrUpdatedUsersSince(111L)).thenReturn(Arrays.asList(userCath));
        when(activeDirectory.findUserTombstonesSince(111L)).thenReturn(Arrays.asList(new Tombstone("X02", "111"), new Tombstone("out-of-scope-1", "112")));
        when(activeDirectory.findAddedOrUpdatedGroupsSince(111L)).thenReturn(Arrays.asList(fish));
        when(activeDirectory.findGroupTombstonesSince(111L)).thenReturn(Arrays.asList(new Tombstone("X09", "111"), new Tombstone("out-of-scope-1", "113")));

        // Create the class under test
        UsnChangedCacheRefresher cacheRefresher = new UsnChangedCacheRefresher(activeDirectory);
        // Can't do a delta until the first full refresh
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));
        // Do the Cache refresh
        cacheRefresher.synchroniseAll(mockDirectoryCache);

        // Expectations - Users
        assertEquals(2, mockDirectoryCache.getUsers().size());
        assertTrue(mockDirectoryCache.getUsers().contains(userAdam));
        assertTrue(mockDirectoryCache.getUsers().contains(userBob));
        // Groups
        assertEquals(2, mockDirectoryCache.getGroups().size());
        assertTrue(mockDirectoryCache.getGroups().contains(lemmings));
        assertTrue(mockDirectoryCache.getGroups().contains(sheep));

        // Now refresh just changes
        assertTrue(cacheRefresher.synchroniseChanges(mockDirectoryCache));

        // Expectations - Users
        assertEquals(2, mockDirectoryCache.getUsers().size());
        assertTrue(mockDirectoryCache.getUsers().contains(userAdam));
        assertTrue(mockDirectoryCache.getUsers().contains(userCath));
        // Groups
        assertEquals(2, mockDirectoryCache.getGroups().size());
        assertTrue(mockDirectoryCache.getGroups().contains(lemmings));
        assertTrue(mockDirectoryCache.getGroups().contains(fish));
    }

    private LDAPUserWithAttributes createUser(final String username, final String objectGUID)
    {
        final UserTemplateWithAttributes user = new UserTemplateWithAttributes(username, 1);
        user.setAttribute("objectGUID", objectGUID);
        return new LDAPUserWithAttributes("dn="+username, user);
    }

    private LDAPGroupWithAttributes createGroup(final String groupname, final String objectGUID)
    {
        final GroupTemplateWithAttributes group = new GroupTemplateWithAttributes(groupname, 1, GroupType.GROUP);
        group.setAttribute("objectGUID", objectGUID);
        return new LDAPGroupWithAttributes("dn="+groupname, group);
    }
}
