package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestUserResolver extends MockControllerTestCase
{
    private static final String USER_NAME = "username";
    private UserPickerSearchService userPickerSearchService;

    @Before
    public void setUp() throws Exception
    {
        userPickerSearchService = mockController.getMock(UserPickerSearchService.class);
    }

    @Test
    public void testGetIdsFromUpperCaseNameUserExists() throws Exception
    {
        MockUserResolver userResolver = new MockUserResolver("USERNAME", USER_NAME, Collections.<String>emptyList());
        final List<String> result = userResolver.getIdsFromName("USERNAME");
        assertEquals(Collections.singletonList(USER_NAME), result);
    }

    @Test
    public void testGetIdsFromNameUserExists() throws Exception
    {
        MockUserResolver userResolver = new MockUserResolver(USER_NAME, USER_NAME, Collections.<String>emptyList());
        final List<String> result = userResolver.getIdsFromName(USER_NAME);
        assertEquals(Collections.singletonList(USER_NAME), result);
    }

    @Test
    public void testGetIdsFromNameUserDoesntExist() throws Exception
    {
        MockUserResolver userResolver = new MockUserResolver(USER_NAME, null, Collections.<String>emptyList());
        final List<String> result = userResolver.getIdsFromName(USER_NAME);
        assertEquals(Collections.<String>emptyList(), result);
    }

    @Test
    public void testGetIdsFromNameUserExistsAsFullName() throws Exception
    {
        final List<String> users = CollectionBuilder.newBuilder(USER_NAME).asList();
        MockUserResolver userResolver = new MockUserResolver(USER_NAME, null, users);
        final List<String> result = userResolver.getIdsFromName(USER_NAME);
        assertEquals(users, result);
    }

    @Test
    public void testNameExistsItDoes() throws Exception
    {
        MockUserResolver userResolver = new MockUserResolver(USER_NAME, USER_NAME, Collections.<String>emptyList());
        assertTrue(userResolver.nameExists(USER_NAME));
    }

    @Test
    public void testNameExistsItDoesnt() throws Exception
    {
        MockUserResolver userResolver = new MockUserResolver(USER_NAME, null, Collections.<String>emptyList());
        assertFalse(userResolver.nameExists(USER_NAME));
    }

    @Test
    public void testNameExistsAsFullName() throws Exception
    {
        MockUserResolver userResolver = new MockUserResolver(USER_NAME, null, CollectionBuilder.newBuilder("user").asList());
        assertTrue(userResolver.nameExists(USER_NAME));
    }

    @Test
    public void testIdExistsItDoes() throws Exception
    {
        MockUserResolver userResolver = new MockUserResolver("12", "12", Collections.<String>emptyList());
        assertTrue(userResolver.idExists(12L));
    }
    
    @Test
    public void testIdExistsItDoesnt() throws Exception
    {
        MockUserResolver userResolver = new MockUserResolver("12", null, Collections.<String>emptyList());
        assertFalse(userResolver.idExists(12L));
    }

    @Test
    public void testIdExistsAsFullName() throws Exception
    {
        MockUserResolver userResolver = new MockUserResolver("10", null, CollectionBuilder.newBuilder("user").asList());
        assertTrue(userResolver.idExists(10L));
    }

    @Test
    public void testPickEmailOrFullNameMatchesOnlyFullNames() throws Exception
    {
        final List<String> nameMatches = CollectionBuilder.<String>newBuilder("joe bloggs").asList();
        final List<String> emailMatches = CollectionBuilder.<String>newBuilder().asList();

        mockController.replay();
        final UserResolverImpl userResolver = new UserResolverImpl(userPickerSearchService);
        final List<String> result = userResolver.pickEmailOrFullNameMatches("joe bloggs", nameMatches, emailMatches);
        assertEquals(nameMatches, result);
        mockController.verify();
    }

    @Test
    public void testPickEmailOrFullNameMatchesOnlyEmails() throws Exception
    {
        final List<String> nameMatches = CollectionBuilder.<String>newBuilder().asList();
        final List<String> emailMatches = CollectionBuilder.<String>newBuilder("joebloggs@example.com").asList();

        mockController.replay();
        final UserResolverImpl userResolver = new UserResolverImpl(userPickerSearchService);
        final List<String> result = userResolver.pickEmailOrFullNameMatches("joe bloggs", nameMatches, emailMatches);
        assertEquals(emailMatches, result);
        mockController.verify();
    }
    
    @Test
    public void testPickEmailOrFullNameMatchesEmailsAndNamesButNameNotEmail() throws Exception
    {
        final List<String> nameMatches = CollectionBuilder.<String>newBuilder("joe bloggs").asList();
        final List<String> emailMatches = CollectionBuilder.<String>newBuilder("joebloggs@example.com").asList();

        mockController.replay();
        final UserResolverImpl userResolver = new UserResolverImpl(userPickerSearchService);
        final List<String> result = userResolver.pickEmailOrFullNameMatches("joe bloggs", nameMatches, emailMatches);
        assertEquals(nameMatches, result);
        mockController.verify();
    }

    @Test
    public void testPickEmailOrFullNameMatchesEmailsAndNamesButNameEmail() throws Exception
    {
        final List<String> nameMatches = CollectionBuilder.<String>newBuilder("joe bloggs").asList();
        final List<String> emailMatches = CollectionBuilder.<String>newBuilder("joebloggs@example.com").asList();

        mockController.replay();
        final UserResolverImpl userResolver = new UserResolverImpl(userPickerSearchService);
        final List<String> result = userResolver.pickEmailOrFullNameMatches("joebloggs@example.com", nameMatches, emailMatches);
        assertEquals(emailMatches, result);
        mockController.verify();
    }

    class MockUserResolver extends UserResolverImpl
    {
        private final String expected;
        private final String user;
        private final List<String> fullNameUsers;

        public MockUserResolver(String expected, String userNameUser, final List<String> fullNameUsers)
        {

            super(mockController.getMock(UserPickerSearchService.class));
            this.fullNameUsers = fullNameUsers;
            mockController.replay();
            this.expected = expected;
            this.user = userNameUser;
        }

        @Override
        String getUserNameFromUserName(final String name)
        {
            assertEquals(expected, name);
            return user;
        }

        @Override
        List<String> getUserNameFromFullNameOrEmail(final String name)
        {
            assertEquals(expected, name);
            return fullNameUsers;
        }
    }
}
