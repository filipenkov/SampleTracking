package com.atlassian.crowd.embedded.core;

import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestriction;
import com.atlassian.crowd.search.query.entity.restriction.PropertyUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests for {@link CrowdServiceImpl} doing admin tasks.
 * These originally lived in a separate class - CrowdAdminServiceImpl.
 */
public class CrowdAdminServiceImplTest
{
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationFactory applicationFactory;
    @Mock
    private DirectoryInstanceLoader directoryInstanceLoader;
    @Mock
    private Application application;
    private CrowdService crowdService;

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);
        crowdService = new CrowdServiceImpl(applicationFactory, applicationService, directoryInstanceLoader);

        when(applicationFactory.getApplication()).thenReturn(application);
    }

    @After
    public void tearDown() throws Exception
    {
        applicationService = null;
        applicationFactory = null;
        directoryInstanceLoader = null;
        application = null;
        crowdService = null;
    }

    /**
     * Tests that {@link CrowdServiceImpl#searchUsersAllowingDuplicateNames(com.atlassian.crowd.embedded.api.Query)}
     * performs correctly. In particular that it handles the conversion from {@link com.atlassian.crowd.model.user.User}
     * to {@link com.atlassian.crowd.embedded.api.User} in the query and return type properly. 
     */
    @Test
    public void testSearchUsersAllowingDuplicateNames() throws Exception
    {
        final String USER1_NAME = "user1";
        final String USER2_NAME = "user2";
        List<String> expectedUsernames = Arrays.asList(USER1_NAME, USER2_NAME);
        com.atlassian.crowd.model.user.User user1 = new UserTemplate(USER1_NAME, 1);
        com.atlassian.crowd.model.user.User user2 = new UserTemplate(USER2_NAME, 1);

        when(applicationService.searchUsersAllowingDuplicateNames(eq(application), any(EntityQuery.class))).thenReturn(Arrays.asList(user1, user2));
        Query<User> query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(PropertyUtils.ofTypeString("key")).exactlyMatching("value")).returningAtMost(10);

        Iterable<User> users = crowdService.searchUsersAllowingDuplicateNames(query);
        
        verify(applicationService).searchUsersAllowingDuplicateNames(eq(application), any(EntityQuery.class));
        assertNotNull(users);

        List<String> usernames = new ArrayList<String>();
        for (User user : users)
        {
            usernames.add(user.getName());    
        }

        assertEquals(expectedUsernames.size(), usernames.size());
        assertTrue(usernames.containsAll(expectedUsernames));
    }

    /**
     * Tests that {@link CrowdServiceImpl#searchUsersAllowingDuplicateNames(com.atlassian.crowd.embedded.api.Query)}
     * throws an <code>IllegalArgumentException</code> when the query is not a {@link UserQuery}. 
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSearchUsersAllowingDuplicateNames_NotUserQuery() throws Exception
    {
        Query<User> fakeUserQuery = new Query<User>()
        {
            public int getStartIndex()
            {
                return 0;
            }

            public int getMaxResults()
            {
                return 0;
            }

            public Class<User> getReturnType()
            {
                return null;
            }

            public SearchRestriction getSearchRestriction()
            {
                return new NullRestriction() {};
            }
        };

        crowdService.searchUsersAllowingDuplicateNames(fakeUserQuery);
    }

    /**
     * Tests that {@link CrowdServiceImpl#removeUser(com.atlassian.crowd.embedded.api.User)} returns <tt>true</tt>
     * when the user is successfully removed.
     */
    @Test
    public void testRemoveUser() throws Exception
    {
        final String TEST_USERNAME = "username";
        com.atlassian.crowd.model.user.User user1 = new UserTemplate(TEST_USERNAME, 1);
        assertTrue(crowdService.removeUser(user1));
    }

    /**
     * Tests that {@link CrowdServiceImpl#removeUser(com.atlassian.crowd.embedded.api.User)} returns <tt>false</tt>
     * when the user does not exist.
     */
    @Test
    public void testRemoveUser_UserDoesNotExist() throws Exception
    {
        final String TEST_USERNAME = "username";
        com.atlassian.crowd.model.user.User user1 = new UserTemplate(TEST_USERNAME, 1);
        doThrow(new com.atlassian.crowd.exception.UserNotFoundException(TEST_USERNAME)).when(applicationService).removeUser(application, TEST_USERNAME);
        assertFalse(crowdService.removeUser(user1));
    }

    /**
     * Tests that {@link CrowdServiceImpl#removeGroup(com.atlassian.crowd.embedded.api.Group)} returns <tt>true</tt>
     * when the group is successfully removed.
     */
    @Test
    public void testRemoveGroup() throws Exception
    {
        final String TEST_GROUP_NAME = "groupname";
        Group group = new ImmutableGroup(TEST_GROUP_NAME);
        assertTrue(crowdService.removeGroup(group));
    }

    /**
     * Tests that {@link CrowdServiceImpl#removeGroup(com.atlassian.crowd.embedded.api.Group)} returns <tt>false</tt>
     * when the group does not exist.
     */
    @Test
    public void testRemoveGroup_GroupDoesNotExist() throws Exception
    {
        final String TEST_GROUP_NAME = "groupname";
        Group group = new ImmutableGroup(TEST_GROUP_NAME);
        doThrow(new com.atlassian.crowd.exception.GroupNotFoundException(TEST_GROUP_NAME)).when(applicationService).removeGroup(application, TEST_GROUP_NAME);
        assertFalse(crowdService.removeGroup(group));
    }
}
