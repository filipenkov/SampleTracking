package com.atlassian.crowd.directory.ldap.cache;

import java.util.Arrays;
import java.util.List;

import com.atlassian.crowd.directory.DirectoryMembershipsIterable;
import com.atlassian.crowd.directory.MockRemoteDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupTemplateWithAttributes;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteDirectoryCacheRefresherTest
{
    MockDirectoryCache mockDirectoryCache;
    
    @Mock
    RemoteDirectory remoteDirectory;
    
    LDAPUserWithAttributes userAdam, userBob;
    LDAPGroupWithAttributes lemmings, sheep;
    LDAPGroupWithAttributes role1;

    @Before
    public void setUp() throws Exception
    {
        mockDirectoryCache = new MockDirectoryCache();
        
        /* Need to ensure DMI is constructed after mocking is complete */
        when(remoteDirectory.getMemberships()).thenAnswer(
                new Answer<Iterable<Membership>>()
                {
                    @Override
                    public Iterable<Membership> answer(InvocationOnMock invocation) throws Throwable
                    {
                        return new DirectoryMembershipsIterable(remoteDirectory);
                    }
                });
        
        userAdam = createUser("adam");
        userBob = createUser("bob");
        lemmings = createGroup("lemmings");
        sheep = createGroup("sheep");
        role1 = createRole("role1");
    }

    @Test
    public void testCannotSyncChanges() throws Exception
    {
        RemoteDirectoryCacheRefresher cacheRefresher = new RemoteDirectoryCacheRefresher(null);
        assertFalse(cacheRefresher.synchroniseChanges(null));
    }

    @Test
    @SuppressWarnings ({ "unchecked" })
    public void testFullRefresh() throws Exception
    {
        final List<LDAPUserWithAttributes> userList = Arrays.asList(userAdam, userBob);
        when(remoteDirectory.searchUsers(any(EntityQuery.class))).thenReturn(userList);
        final List<LDAPGroupWithAttributes> groupList = Arrays.asList(lemmings, sheep);
        when(remoteDirectory.searchGroups(any(EntityQuery.class))).thenReturn(groupList);

        // Create the class under test
        RemoteDirectoryCacheRefresher cacheRefresher = new RemoteDirectoryCacheRefresher(remoteDirectory);
        // Can't do a delta
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));
        // Do the Cache refresh
        cacheRefresher.synchroniseAll(mockDirectoryCache);
        // Still can't sync changes
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));

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
    public void testRefreshWithRoles() throws Exception
    {
        remoteDirectory = new MockRemoteDirectory();
        
        remoteDirectory.addGroup(new GroupTemplate(lemmings));
        remoteDirectory.addGroup(new GroupTemplate(sheep));
        remoteDirectory.addGroup(new GroupTemplate(role1));

        // Create the class under test
        RemoteDirectoryCacheRefresher cacheRefresher = new RemoteDirectoryCacheRefresher(remoteDirectory);
        // Can't do a delta
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));
        // Do the Cache refresh
        cacheRefresher.synchroniseAll(mockDirectoryCache);
        // Still can't sync changes
        assertFalse(cacheRefresher.synchroniseChanges(mockDirectoryCache));

        // Expectations:
        // Groups
        assertEquals(2, mockDirectoryCache.getGroups().size());
        assertNotNull(mockDirectoryCache.getGroup("lemmings"));
        assertNotNull(mockDirectoryCache.getGroup("sheep"));
        assertEquals(1, mockDirectoryCache.getRoles().size());
        assertNotNull(mockDirectoryCache.getRole("role1"));
    }

    private LDAPUserWithAttributes createUser(final String username)
    {
        final UserTemplateWithAttributes user = new UserTemplateWithAttributes(username, 1);
        return new LDAPUserWithAttributes("dn="+username, user);
    }

    private LDAPGroupWithAttributes createGroup(final String groupname)
    {
        return createGroup(groupname, GroupType.GROUP);
    }

    private LDAPGroupWithAttributes createRole(final String groupname)
    {
        return createGroup(groupname, GroupType.LEGACY_ROLE);
    }

    private LDAPGroupWithAttributes createGroup(final String groupname, GroupType groupType)
    {
        final GroupTemplateWithAttributes group = new GroupTemplateWithAttributes(groupname, 1, groupType);
        return new LDAPGroupWithAttributes("dn="+groupname, group);
    }
}