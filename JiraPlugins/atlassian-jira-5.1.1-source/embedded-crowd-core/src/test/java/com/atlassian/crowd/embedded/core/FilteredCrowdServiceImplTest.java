package com.atlassian.crowd.embedded.core;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupWithAttributes;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.runtime.GroupNotFoundException;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import static org.mockito.Matchers.any;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class FilteredCrowdServiceImplTest
{
    private CrowdService filteredCrowdService;
    private CrowdService crowdService;
    private FilteredGroupsProvider groupProvider;

    @Before
    public void setUp()
    {
        crowdService = mock(CrowdService.class);
        groupProvider = mock(FilteredGroupsProvider.class);
        when(groupProvider.getGroups()).thenReturn(Sets.newHashSet("filtered1", "filtered2","aNotHerFiltered"));
        filteredCrowdService = new FilteredCrowdServiceImpl(crowdService, groupProvider);
    }

    @Test
    public void testGetGroupNormalGroup()
    {
        Group group = mock(Group.class);
        when(crowdService.getGroup("group1")).thenReturn(group);
        assertEquals(group, filteredCrowdService.getGroup("group1"));
    }

    @Test
    public void testGetGroupFilteredGroup()
    {
        assertNull(filteredCrowdService.getGroup("filtered2"));
        verify(crowdService, never()).getGroup(anyString());
    }

    @Test
    public void testGetGroupFilteredGroupCaseInSensitive()
    {
        assertNull(filteredCrowdService.getGroup("ANOTHERFILTERED"));
        verify(crowdService, never()).getGroup(anyString());
    }

    @Test
    public void testGetGroupWithAttributesNormalGroup()
    {
        GroupWithAttributes group = mock(GroupWithAttributes.class);
        when(crowdService.getGroupWithAttributes("group1")).thenReturn(group);
        assertEquals(group, filteredCrowdService.getGroupWithAttributes("group1"));
    }

    @Test
    public void testSearchUserShouldNotFilter()
    {
        List<User> users = Arrays.asList(mock(User.class), mock(User.class), mock(User.class));
        UserQuery query = new UserQuery(User.class, mock(SearchRestriction.class), 0, 100);
        when(crowdService.search(query)).thenReturn(users);

        assertEquals(users, filteredCrowdService.search(query));
    }

    @Test(expected = RuntimeException.class)
    public void testSeachByUnknownQueryTypeMustResultInRuntimeException()
    {
        filteredCrowdService.search(mock(Query.class));
    }

    @Test
    public void testSearchGroupWithGroupReturnTypeShouldFilter()
    {
        Group group1 = mock(Group.class);
        when(group1.getName()).thenReturn("group1");
        Group group2 = mock(Group.class);
        when(group2.getName()).thenReturn("filtered2");
        Group group3 = mock(Group.class);
        when(group3.getName()).thenReturn("group3");

        List<Group> groups = Arrays.asList(group1, group2, group3);
        GroupQuery query = new GroupQuery(Group.class, GroupType.GROUP ,mock(SearchRestriction.class), 0, 100);
        when(crowdService.search(query)).thenReturn(groups);

        assertEquals(Lists.newArrayList(group1, group3), Lists.newArrayList(filteredCrowdService.search(query)));
    }

    @Test
    public void testSearchGroupWithStringReturnTypeShouldFilter()
    {
        List<String> groups = Arrays.asList("group1", "filtered2", "group3");
        GroupQuery query = new GroupQuery(String.class, GroupType.GROUP ,mock(SearchRestriction.class), 0, 100);
        when(crowdService.search(query)).thenReturn(groups);

        assertEquals(Lists.newArrayList("group1", "group3"), Lists.newArrayList(filteredCrowdService.search(query)));
    }

    @Test
    public void testSearchMembershipOnNonFilteredGroupReturningNamesMustFilterResultingGroups()
    {
        List<String> groups = Arrays.asList("filtered1", "group2", "group3");

        MembershipQuery query = QueryBuilder.createMembershipQuery(100, 0, true, EntityDescriptor.group(), String.class, EntityDescriptor.group(), "group5");
        when(crowdService.search(query)).thenReturn(groups);

        assertEquals(Lists.newArrayList("group2", "group3"), Lists.newArrayList(filteredCrowdService.search(query)));
    }

    @Test
    public void testSearchMembershipOnNonFilteredGroupustFilterResultingGroups()
    {
        Group group1 = mock(Group.class);
        when(group1.getName()).thenReturn("group1");
        Group group2 = mock(Group.class);
        when(group2.getName()).thenReturn("group2");
        Group group3 = mock(Group.class);
        when(group3.getName()).thenReturn("filtered1");

        List<Group> groups = Arrays.asList(group1, group2, group3);
        MembershipQuery query = QueryBuilder.createMembershipQuery(100, 0, true, EntityDescriptor.group(), Group.class, EntityDescriptor.group(), "group5");
        when(crowdService.search(query)).thenReturn(groups);

        assertEquals(Lists.newArrayList(group1, group2), Lists.newArrayList(filteredCrowdService.search(query)));
    }

    @Test
    public void testSearchMembershipOnFilteredGroupMustReturnNoResult()
    {
        List<String> groups = Arrays.asList("group1", "group2", "group3");

        MembershipQuery query = QueryBuilder.createMembershipQuery(100, 0, true, EntityDescriptor.group(), String.class, EntityDescriptor.group(), "filtered2");
        when(crowdService.search(query)).thenReturn(groups);

        assertEquals(0, Lists.newArrayList(filteredCrowdService.search(query)).size());
    }

    @Test
    public void testGetGroupWithAttributesFilteredGroup()
    {
        assertNull(filteredCrowdService.getGroupWithAttributes("filtered1"));
        verify(crowdService, never()).getGroupWithAttributes(anyString());
    }

    @Test
    public void testIsUserMemberOfGroupNormalGroupName()
    {
        String username = "filtered1";
        String groupname = "group1";
        when(crowdService.isUserMemberOfGroup(username, groupname)).thenReturn(true);
        assertTrue(filteredCrowdService.isUserMemberOfGroup(username, groupname));
    }

    @Test
    public void testIsUserMemberOfGroupFilteredGroupName()
    {
        String username = "user1";
        String groupname = "filtered1";
        assertFalse(filteredCrowdService.isUserMemberOfGroup(username, groupname));
        verify(crowdService, never()).isUserMemberOfGroup(anyString(), anyString());
    }

    @Test
    public void testIsUserMemberOfGroupNormalGroup()
    {
        User user = mock(User.class);
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("groupA");
        when(crowdService.isUserMemberOfGroup(user, group)).thenReturn(true);
        assertTrue(filteredCrowdService.isUserMemberOfGroup(user, group));
    }

    @Test
    public void testIsUserMemberOfGroupFilteredGroup()
    {
        User user = mock(User.class);
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered1");
        assertFalse(filteredCrowdService.isUserMemberOfGroup(user, group));
        verify(crowdService, never()).isUserMemberOfGroup(any(User.class), any(Group.class));
    }

    @Test
    public void testIsGroupMemberOfGroupFilteredChild()
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("filtered1");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("parentgroup");

        assertFalse(filteredCrowdService.isGroupMemberOfGroup(childGroup, parentGroup));
        verify(crowdService, never()).isGroupMemberOfGroup(any(Group.class), any(Group.class));
    }

    @Test
    public void testIsGroupMemberOfGroupFilteredParent()
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("child");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("filtered2");

        assertFalse(filteredCrowdService.isGroupMemberOfGroup(childGroup, parentGroup));
        verify(crowdService, never()).isGroupMemberOfGroup(any(Group.class), any(Group.class));
    }

    @Test
    public void testIsGroupMemberOfGroupNoFiltered()
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("child");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("parent");

        when(crowdService.isGroupMemberOfGroup(childGroup, parentGroup)).thenReturn(true);
        assertTrue(filteredCrowdService.isGroupMemberOfGroup(childGroup, parentGroup));
    }

    @Test
    public void testIsGroupMemberOfGroupFilteredChildName()
    {
        String childGroup = "filtered1";
        String parentGroup = "parentgroup";

        assertFalse(filteredCrowdService.isGroupMemberOfGroup(childGroup, parentGroup));
        verify(crowdService, never()).isGroupMemberOfGroup(anyString(), anyString());
    }

    @Test
    public void testIsGroupMemberOfGroupFilteredParentName()
    {
        String childGroup = "filtered1";
        String parentGroup = "parentgroup";

        assertFalse(filteredCrowdService.isGroupMemberOfGroup(childGroup, parentGroup));
        verify(crowdService, never()).isGroupMemberOfGroup(anyString(), anyString());
    }

    @Test
    public void testIsGroupMemberOfGroupNoFilteredName()
    {
        String childGroup = "childgroup";
        String parentGroup = "parentgroup";

        when(crowdService.isGroupMemberOfGroup(childGroup, parentGroup)).thenReturn(true);
        assertTrue(filteredCrowdService.isGroupMemberOfGroup(childGroup, parentGroup));
    }

    @Test
    public void testAddGroupNormal() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("group");

        when(crowdService.addGroup(group)).thenReturn(group);
        assertEquals(group, filteredCrowdService.addGroup(group));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testAddGroupFiltered() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered2");
        filteredCrowdService.addGroup(group);
    }

    @Test
    public void testUpdateGroupNormal() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("group");

        when(crowdService.updateGroup(group)).thenReturn(group);
        assertEquals(group, filteredCrowdService.updateGroup(group));
    }

    @Test(expected = GroupNotFoundException.class)
    public void testUpdateGroupFiltered() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered1");
        filteredCrowdService.updateGroup(group);
    }

    @Test
    public void testSetGroupAttributeNormal() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("group");

        filteredCrowdService.setGroupAttribute(group, "attribute1", "value1");

        verify(crowdService, times(1)).setGroupAttribute(group, "attribute1", "value1");
    }

    @Test(expected = GroupNotFoundException.class)
    public void testSetGroupAttributeFiltered() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered2");

        filteredCrowdService.setGroupAttribute(group, "attribute1", "value1");
    }

    @Test
    public void testSetGroupAttributeValueSetNormal() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("group");
        Set<String> values = Sets.newHashSet("value1", "value2");

        filteredCrowdService.setGroupAttribute(group, "attribute1", values);

        verify(crowdService, times(1)).setGroupAttribute(group, "attribute1", values);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testSetGroupAttributeValueSetFiltered() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered2");

        filteredCrowdService.setGroupAttribute(group, "attribute1", Sets.newHashSet("value1", "value2"));
    }

    @Test
    public void testRemoveGroupAttributeNormal() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("group");

        filteredCrowdService.removeGroupAttribute(group, "attribute1");

        verify(crowdService, times(1)).removeGroupAttribute(group, "attribute1");
    }

    @Test(expected = GroupNotFoundException.class)
    public void testRemoveGroupAttributeFiltered() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered2");

        filteredCrowdService.removeGroupAttribute(group, "attribute1");
    }

    @Test
    public void testRemoveAllGroupAttributesNormal() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("group");

        filteredCrowdService.removeAllGroupAttributes(group);

        verify(crowdService, times(1)).removeAllGroupAttributes(group);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testRemoveAllGroupAttributesFiltered() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered1");

        filteredCrowdService.removeAllGroupAttributes(group);
    }

    @Test
    public void testRemoveGroupNormal() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("group");
        when(crowdService.removeGroup(group)).thenReturn(true);

        assertTrue(filteredCrowdService.removeGroup(group));
        verify(crowdService, times(1)).removeGroup(group);
    }

    @Test
    public void testRemoveGroupFiltered() throws Exception
    {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered1");

        assertFalse(filteredCrowdService.removeGroup(group));
    }

    @Test
    public void testAddUserToGroupNormal() throws Exception
    {
        User user = mock(User.class);
        when(user.getName()).thenReturn("user");
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("group");

        filteredCrowdService.addUserToGroup(user, group);
        verify(crowdService, times(1)).addUserToGroup(user, group);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddUserToGroupFiltered() throws Exception
    {
        User user = mock(User.class);
        when(user.getName()).thenReturn("user");
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered1");

        filteredCrowdService.addUserToGroup(user, group);
    }

    @Test
    public void testAddGroupToGroupNormal() throws Exception
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("child");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("parent");

        filteredCrowdService.addGroupToGroup(childGroup, parentGroup);
        verify(crowdService, times(1)).addGroupToGroup(childGroup, parentGroup);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddGroupToGroupFilteredChild() throws Exception
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("filtered1");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("parent");

        filteredCrowdService.addGroupToGroup(childGroup, parentGroup);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testAddGroupToGroupFilteredParent() throws Exception
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("child");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("filtered2");

        filteredCrowdService.addGroupToGroup(childGroup, parentGroup);
    }

    @Test
    public void testRemoveUserFromGroupNormal() throws Exception
    {
        User user = mock(User.class);
        when(user.getName()).thenReturn("user");
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("group");

        filteredCrowdService.addUserToGroup(user, group);
        verify(crowdService, times(1)).addUserToGroup(user, group);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testRemoveUserFromGroupFiltered() throws Exception
    {
        User user = mock(User.class);
        when(user.getName()).thenReturn("user");
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered1");

        filteredCrowdService.removeUserFromGroup(user, group);
    }

    @Test
    public void testRemoveGroupFromGroupNormal() throws Exception
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("child");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("parent");

        filteredCrowdService.removeGroupFromGroup(childGroup, parentGroup);
        verify(crowdService, times(1)).removeGroupFromGroup(childGroup, parentGroup);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testRemoveGroupFromGroupFilteredChild() throws Exception
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("filtered1");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("parent");

        filteredCrowdService.removeGroupFromGroup(childGroup, parentGroup);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testRemoveGroupFromGroupFilteredParent() throws Exception
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("child");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("filtered2");

        filteredCrowdService.removeGroupFromGroup(childGroup, parentGroup);
    }

    @Test
    public void testIsUserDirectGroupMemberNormal() throws Exception
    {
        User user = mock(User.class);
        when(user.getName()).thenReturn("user");
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("group");
        when(crowdService.isUserDirectGroupMember(user, group)).thenReturn(true);

        assertTrue(filteredCrowdService.isUserDirectGroupMember(user, group));
        verify(crowdService, times(1)).isUserDirectGroupMember(user, group);
    }

    @Test
    public void testIsUserDirectGroupMemberFiltered() throws Exception
    {
        User user = mock(User.class);
        when(user.getName()).thenReturn("user");
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("filtered1");

        assertFalse(filteredCrowdService.isUserDirectGroupMember(user, group));
    }

    @Test
    public void testIsGroupDirectGroupMemberNormal() throws Exception
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("child");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("parent");

        when(crowdService.isGroupDirectGroupMember(childGroup, parentGroup)).thenReturn(true);

        filteredCrowdService.isGroupDirectGroupMember(childGroup, parentGroup);

        verify(crowdService, times(1)).isGroupDirectGroupMember(childGroup, parentGroup);
    }

    @Test
    public void testIsGroupDirectGroupMemberFilteredChild() throws Exception
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("filtered1");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("parent");

        assertFalse(filteredCrowdService.isGroupDirectGroupMember(childGroup, parentGroup));
    }

    @Test
    public void testIsGroupDirectGroupMemberFilteredParent() throws Exception
    {
        Group childGroup = mock(Group.class);
        when(childGroup.getName()).thenReturn("child");
        Group parentGroup = mock(Group.class);
        when(parentGroup.getName()).thenReturn("filtered2");

        assertFalse(filteredCrowdService.isGroupDirectGroupMember(childGroup, parentGroup));
    }
}