package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.util.BatchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CachingDirectoryTest
{
    private CachingDirectory directory;

    @Mock
    InternalDirectoryUtils internalDirectoryUtils;

    @Mock
    UserDao userDao;

    @Before
    public void setUp()
    {
        directory = new CachingDirectory(internalDirectoryUtils, null, null, userDao, null, null);
    }

    @Test
    public void testAddUser_noPopulation() throws Exception
    {
        final UserTemplate user = new UserTemplate("bsmith", -1L);

        // Ensure that the user was not replaced with different user
        when(userDao.add(same(user), any(PasswordCredential.class))).thenReturn(user);

        final User addedUser = directory.addUser(user, null);

        assertNull(addedUser.getDisplayName());
        assertNull(addedUser.getFirstName());
        assertNull(addedUser.getLastName());
    }

    @Test
    public void testAddAllUsers_noPopulation()
    {
        final PasswordCredential credential = new PasswordCredential("password");
        final UserTemplateWithCredentialAndAttributes user = new UserTemplateWithCredentialAndAttributes("bsmith", -1L, credential);
        Set<UserTemplateWithCredentialAndAttributes> users = Collections.singleton(user);

        // Ensure that users were not replaced with different users
        when(userDao.addAll(same(users))).thenReturn(new BatchResult<User>(0));

        final Collection<User> failedUsers = directory.addAllUsers(users).getFailedEntities();

        assertEquals(0, failedUsers.size());
    }

    @Test
    public void testUpdateUser_noPopulation() throws Exception
    {
        final UserTemplate user = new UserTemplate("bsmith", -1L);

        // Ensure that the user was not replaced with different user
        when(userDao.update(same(user))).thenReturn(user);

        final User updatedUser = directory.updateUser(user);

        assertNull(updatedUser.getDisplayName());
        assertNull(updatedUser.getFirstName());
        assertNull(updatedUser.getLastName());
    }
}
