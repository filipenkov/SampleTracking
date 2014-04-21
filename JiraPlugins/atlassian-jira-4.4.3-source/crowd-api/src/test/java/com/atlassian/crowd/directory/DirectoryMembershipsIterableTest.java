package com.atlassian.crowd.directory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;

import com.google.common.collect.Iterators;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DirectoryMembershipsIterableTest
{
    @Test(expected = NullPointerException.class)
    public void nullExpressionFails()
    {
        new DirectoryMembershipsIterable(null, null);
    }
    
    @Test
    public void emptyGroupNamesGivesEmptyResult()
    {
        RemoteDirectory remoteDirectory = mock(RemoteDirectory.class);
        Iterable<Membership> m = new DirectoryMembershipsIterable(remoteDirectory, Collections.<String>emptyList());
        
        assertFalse(m.iterator().hasNext());
    }
    
    @Test
    public void exceptionsWrappedAsRuntime() throws Exception
    {
        RemoteDirectory remoteDirectory = mock(RemoteDirectory.class);
        when(remoteDirectory.searchGroupRelationships(Mockito.<MembershipQuery<?>>any())).thenThrow(new OperationFailedException());
        
        Iterator<Membership> i = new DirectoryMembershipsIterable(remoteDirectory, Collections.singleton("")).iterator();
        
        assertTrue(i.hasNext());
        
        try
        {
            i.next();
            fail();
        }
        catch (Membership.MembershipIterationException e)
        {
            assertEquals(OperationFailedException.class, e.getCause().getClass());
        }
    }
    
    @Test
    public void getsGroupNamesWhenNotSupplied() throws Exception
    {
        List<Group> groups = Arrays.<Group>asList(new GroupTemplate("group1"), new GroupTemplate("group2"));
        
        RemoteDirectory remoteDirectory = mock(RemoteDirectory.class);
        when(remoteDirectory.searchGroups(QueryBuilder
                .queryFor(Group.class, EntityDescriptor.group(GroupType.GROUP))
                .returningAtMost(EntityQuery.ALL_RESULTS))).thenReturn(groups);

        Iterator<Membership> i = new DirectoryMembershipsIterable(remoteDirectory).iterator();

        List<Membership> memberships = new ArrayList<Membership>();
        Iterators.addAll(memberships, i);
        
        assertEquals(2, memberships.size());
        assertEquals("group1", memberships.get(0).getGroupName());
        assertEquals("group2", memberships.get(1).getGroupName());
    }
}
