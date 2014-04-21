package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestUserIndexInfoResolver extends MockControllerTestCase
{
    @Test
    public void testGetIdsFromNameStringUserResolverReturnsEmpty() throws Exception
    {
        NameResolver<User> userResolver = mockController.getMock(NameResolver.class);
        final List<String> returnList = CollectionBuilder.newBuilder("name").asList();
        final List<String> resolverList = Collections.emptyList();
        userResolver.getIdsFromName("name");
        mockController.setReturnValue(resolverList);
        mockController.replay();

        UserIndexInfoResolver resolver = new UserIndexInfoResolver(userResolver);
        assertEquals(returnList, resolver.getIndexedValues("name"));
        mockController.verify();
    }
                                      
    @Test
    public void testGetIdsFromNameLongUserResolverReturnsEmpty() throws Exception
    {
        NameResolver<User> userResolver = mockController.getMock(NameResolver.class);
        final List<String> returnList = CollectionBuilder.newBuilder("10").asList();
        final List<String> resolverList = Collections.emptyList();
        userResolver.getIdsFromName("10");
        mockController.setReturnValue(resolverList);
        mockController.replay();

        UserIndexInfoResolver resolver = new UserIndexInfoResolver(userResolver);
        assertEquals(returnList, resolver.getIndexedValues(10L));
        mockController.verify();
    }

    @Test
    public void testGetIdsFromNameStringUserResolverReturnsList() throws Exception
    {
        NameResolver<User> userResolver = mockController.getMock(NameResolver.class);
        final List<String> returnList = CollectionBuilder.newBuilder("name").asList();
        final List<String> resolverList = CollectionBuilder.newBuilder("otherName").asList();
        final List<String> fieldValueList = CollectionBuilder.newBuilder("othername").asList();
        userResolver.getIdsFromName("name");
        mockController.setReturnValue(resolverList);
        mockController.replay();

        UserIndexInfoResolver resolver = new UserIndexInfoResolver(userResolver);
        assertEquals(fieldValueList, resolver.getIndexedValues("name"));
        mockController.verify();
    }
                                      
    @Test
    public void testGetIdsFromNameLongUserResolverReturnsList() throws Exception
    {
        NameResolver<User> userResolver = mockController.getMock(NameResolver.class);
        final List<String> returnList = CollectionBuilder.newBuilder("10").asList();
        final List<String> resolverList = CollectionBuilder.newBuilder("otherName").asList();
        final List<String> fieldValueList = CollectionBuilder.newBuilder("othername").asList();
        userResolver.getIdsFromName("10");
        mockController.setReturnValue(resolverList);
        mockController.replay();

        UserIndexInfoResolver resolver = new UserIndexInfoResolver(userResolver);
        assertEquals(fieldValueList, resolver.getIndexedValues(10L));
        mockController.verify();
    }
}
