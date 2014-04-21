package com.atlassian.crowd.directory;

import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.embedded.spi.MembershipDao;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.PasswordEncoderException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.TimestampedUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.password.encoder.PasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.util.BatchResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractInternalDirectoryTest
{
    @Mock
    private InternalDirectoryUtils internalDirectoryUtils;

    @Mock
    private PasswordEncoderFactory passwordEncoderFactory;

    @Mock
    private DirectoryDao directoryDao;

    @Mock
    private UserDao userDao;

    @Mock
    private GroupDao groupDao;

    @Mock
    private MembershipDao membershipDao;

    private AbstractInternalDirectory directory;

    @Before
    public void initInternalDirectory()
    {
        this.directory = new AbstractInternalDirectory(internalDirectoryUtils,
                passwordEncoderFactory, directoryDao, userDao, groupDao, membershipDao)
        {
            @Override
            public BatchResult<String> addAllUsersToGroup(Set<String> userNames, String groupName)
                    throws GroupNotFoundException
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public BatchResult<User> addAllUsers(Set<UserTemplateWithCredentialAndAttributes> users)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public BatchResult<Group> addAllGroups(Set<GroupTemplate> groups)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public User addUser(UserTemplate user, PasswordCredential credential) throws InvalidCredentialException,
                    InvalidUserException, UserAlreadyExistsException, OperationFailedException
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Group addLocalGroup(GroupTemplate group) throws InvalidGroupException, OperationFailedException
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test
    public void passwordCanBeSetWhenNoHistoryIsStored() throws UserNotFoundException, InvalidCredentialException
    {
        directory.setAttributes(ImmutableMap.<String, String>of());

        when(passwordEncoderFactory.getInternalEncoder(null)).thenReturn(new DummyEncoder());

        PasswordCredential cred = new PasswordCredential("password");

        TimestampedUser u = mock(TimestampedUser.class);

        when(userDao.findByName(0, "user")).thenReturn(u);

        directory.updateUserCredential("user", cred);

        PasswordCredential expected = new PasswordCredential("1:password", true);

        verify(userDao).updateCredential(u, expected, 0);
    }

    @Test(expected = InvalidCredentialException.class)
    public void passwordSetFailsWhenInHistory() throws UserNotFoundException, InvalidCredentialException
    {
        directory.setAttributes(ImmutableMap.<String, String>of(AbstractInternalDirectory.ATTRIBUTE_PASSWORD_HISTORY_COUNT, "1"));

        when(passwordEncoderFactory.getInternalEncoder(null)).thenReturn(new IdentityEncoder());

        PasswordCredential cred = new PasswordCredential("password");

        TimestampedUser u = mock(TimestampedUser.class);

        when(userDao.findByName(0, "user")).thenReturn(u);
        when(userDao.getCredentialHistory(0, "user")).thenReturn(ImmutableList.of(new PasswordCredential("password", true)));

        directory.updateUserCredential("user", cred);

        PasswordCredential expected = new PasswordCredential("password", true);

        verify(userDao).updateCredential(u, expected, 1);
    }

    @Test(expected = InvalidCredentialException.class)
    public void passwordSetFailsWhenAlreadyCurrentPassword() throws UserNotFoundException, InvalidCredentialException
    {
        directory.setAttributes(ImmutableMap.<String, String>of(AbstractInternalDirectory.ATTRIBUTE_PASSWORD_HISTORY_COUNT, "1"));

        when(passwordEncoderFactory.getInternalEncoder(null)).thenReturn(new IdentityEncoder());

        PasswordCredential cred = new PasswordCredential("password");

        TimestampedUser u = mock(TimestampedUser.class);

        when(userDao.findByName(0, "user")).thenReturn(u);
        when(userDao.getCredential(0, "user")).thenReturn(new PasswordCredential("password", true));

        directory.updateUserCredential("user", cred);

        PasswordCredential expected = new PasswordCredential("password", true);

        verify(userDao).updateCredential(u, expected, 1);
    }

    @Test(expected = InvalidCredentialException.class)
    public void passwordSetFailsWhenSamePasswordInHistoryDifferentHash() throws UserNotFoundException, InvalidCredentialException
    {
        directory.setAttributes(ImmutableMap.<String, String>of(AbstractInternalDirectory.ATTRIBUTE_PASSWORD_HISTORY_COUNT, "1"));

        when(passwordEncoderFactory.getInternalEncoder(null)).thenReturn(new DummyEncoder());

        PasswordCredential cred = new PasswordCredential("password");

        TimestampedUser u = mock(TimestampedUser.class);

        when(userDao.findByName(0, "user")).thenReturn(u);
        when(userDao.getCredentialHistory(0, "user")).thenReturn(ImmutableList.of(new PasswordCredential("2:password", true)));

        directory.updateUserCredential("user", cred);
    }

    @Test(expected = InvalidCredentialException.class)
    public void passwordSetFailsWhenSameAsCurrentPasswordDifferentHash() throws UserNotFoundException, InvalidCredentialException
    {
        directory.setAttributes(ImmutableMap.<String, String>of(AbstractInternalDirectory.ATTRIBUTE_PASSWORD_HISTORY_COUNT, "1"));

        when(passwordEncoderFactory.getInternalEncoder(null)).thenReturn(new DummyEncoder());

        PasswordCredential cred = new PasswordCredential("password");

        TimestampedUser u = mock(TimestampedUser.class);

        when(userDao.findByName(0, "user")).thenReturn(u);
        when(userDao.getCredential(0, "user")).thenReturn(new PasswordCredential("2:password", true));

        directory.updateUserCredential("user", cred);
    }

    @Test
    public void sanityCheckDummyEncoder()
    {
        PasswordEncoder pe = new DummyEncoder();

        assertEquals("1:pass", pe.encodePassword("pass", null));
        assertTrue(pe.isPasswordValid("1:pass", "pass", null));
        assertTrue(pe.isPasswordValid(":pass", "pass", null));
        assertFalse(pe.isPasswordValid("1:pass2", "pass", null));
    }

    @Test
    public void historyMatchDescriptionsAreCorrectForDifferentHistoryLengths()
    {
        assertEquals("the current password", directory.historyMatchDescription(1));
        assertEquals("either the current password or the previous password", directory.historyMatchDescription(2));
        assertEquals("either the current password or one of the previous 2 passwords", directory.historyMatchDescription(3));
        assertEquals("either the current password or one of the previous 999 passwords", directory.historyMatchDescription(1000));
    }

    static class IdentityEncoder implements PasswordEncoder
    {
        @Override
        public String encodePassword(String rawPass, Object salt) throws PasswordEncoderException
        {
            return rawPass;
        }

        @Override
        public String getKey()
        {
            return null;
        }

        @Override
        public boolean isPasswordValid(String encPass, String rawPass, Object salt)
        {
            return encPass.equals(rawPass);
        }
    }

    static class DummyEncoder implements PasswordEncoder
    {
        private int counter;

        @Override
        public String encodePassword(String rawPass, Object salt) throws PasswordEncoderException
        {
            return (++counter) + ":" + rawPass;
        }

        @Override
        public String getKey()
        {
            return null;
        }

        @Override
        public boolean isPasswordValid(String encPass, String rawPass, Object salt)
        {
            int i = encPass.indexOf(':');
            return (i >= 0 && rawPass.equals(encPass.substring(i + 1)));
        }
    }
}
