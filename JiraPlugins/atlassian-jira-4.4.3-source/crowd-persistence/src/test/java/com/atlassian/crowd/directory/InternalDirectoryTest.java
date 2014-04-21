package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.embedded.spi.MembershipDao;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.InternalGroup;
import com.atlassian.crowd.model.user.*;
import com.atlassian.crowd.password.encoder.LdapShaPasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.crowd.util.I18nHelper;
import com.atlassian.crowd.util.PasswordHelper;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * InternalDirectory Tester.
 */
public class InternalDirectoryTest
{
    private LdapShaPasswordEncoder ldapShaPasswordEncoder;
    private PasswordCredential encryptedCredential;
    private InternalDirectory internalDirectory;
    private PasswordCredential unencryptedCredential;
    private UserTemplate adminUser;
    private UserTemplate testUser;
    private GroupTemplate mainGroup;
    private GroupTemplate childGroup;
    private InternalUserWithAttributes internalUserWithAttributes;
    private PasswordEncoderFactory passwordEncoderFactory;
    private UserDao userDao;
    private GroupDao groupDao;
    private MembershipDao membershipDao;
    private PasswordHelper passwordHelper;
    private DirectoryDao directoryDao;
    private DirectoryImpl directoryConfiguration;


    private static final String ENCRYPTION_ALGORITHM = "atlassian-security";
    private static final String PASSWORD_1 = "mypassword1";
    private static final String ADMIN_USERNAME = "admin";
    private static final String PASSWORD_2 = "mypassword2";

    private static final String TEST_USERNAME = "test";

    private static final String MAIN_GROUP = "main group";
    private static final String MAIN_GROUP_DESCRIPTION = "Top of the world";

    private static final String CHILD_GROUP = "child group";
    private static final String CHILD_GROUP_DESCRIPTION = "Somewhere near the bottom";
    private static final long DIRECTORY_ID = 1;

    @Before
    public void setUp() throws Exception
    {
        ldapShaPasswordEncoder = new LdapShaPasswordEncoder();

        adminUser = new UserTemplate(ADMIN_USERNAME, DIRECTORY_ID);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("Istrator");
        adminUser.setEmailAddress("admin@test.com");
        adminUser.setDisplayName("Mr. Admin");
        adminUser.setActive(true);

        testUser = new UserTemplate(TEST_USERNAME, DIRECTORY_ID);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmailAddress("test@test.com");
        testUser.setDisplayName("Super Tester");
        testUser.setActive(true);


        mainGroup = new GroupTemplate(MAIN_GROUP, DIRECTORY_ID, GroupType.GROUP);
        mainGroup.setDescription(MAIN_GROUP_DESCRIPTION);

        childGroup = new GroupTemplate(CHILD_GROUP, DIRECTORY_ID, GroupType.GROUP);
        childGroup.setDescription(CHILD_GROUP_DESCRIPTION);

        unencryptedCredential = new PasswordCredential(PASSWORD_1);
        encryptedCredential = new PasswordCredential(ldapShaPasswordEncoder.encodePassword(PASSWORD_1, null), true);


        InternalEntityTemplate template = new InternalEntityTemplate();
        template.setId(DIRECTORY_ID);
        template.setName("Test Directory");
        directoryConfiguration = new DirectoryImpl(template);
        directoryConfiguration.setType(DirectoryType.INTERNAL);
        directoryConfiguration.setImplementationClass(InternalDirectory.class.getCanonicalName());
        directoryConfiguration.setAttribute(InternalDirectory.ATTRIBUTE_USER_ENCRYPTION_METHOD, ENCRYPTION_ALGORITHM);

        userDao = mock(UserDao.class);
        groupDao = mock(GroupDao.class);
        membershipDao = mock(MembershipDao.class);
        directoryDao = mock(DirectoryDao.class);
        passwordEncoderFactory = mock(PasswordEncoderFactory.class);
        passwordHelper = mock(PasswordHelper.class);
        I18nHelper i18nHelper = mock(I18nHelper.class);
        when(i18nHelper.getText(anyString())).thenReturn("Test Class Call");
        when(i18nHelper.getText(anyString(), anyObject())).thenReturn("Test Class Call");
        when(i18nHelper.getText(anyString(), anyString(), anyString())).thenReturn("Test Class Call");

        InternalDirectoryUtilsImpl internalDirectoryUtils = new InternalDirectoryUtilsImpl(passwordHelper);


        internalDirectory = new InternalDirectory(internalDirectoryUtils, passwordEncoderFactory, directoryDao, userDao, groupDao, membershipDao);
        internalDirectory.setAttributes(Collections.<String, String>emptyMap());
        internalDirectory.setDirectoryId(DIRECTORY_ID);
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test the happy path
     */
    @Test
    public void testAddUser() throws Exception
    {
        when(passwordEncoderFactory.getInternalEncoder(anyString())).thenReturn(ldapShaPasswordEncoder);
        when(userDao.add(adminUser, encryptedCredential)).thenReturn(new InternalUser(adminUser, directoryConfiguration, encryptedCredential));

        final User user = internalDirectory.addUser(adminUser, unencryptedCredential);

        assertEquals(ADMIN_USERNAME, user.getName());

        // Verify that the store attributes method was also called.
        verify(userDao, atMost(1)).storeAttributes(eq(adminUser), anyMap());
    }

    @Test
    public void testAddPrincipalWithInvalidCredential() throws Exception
    {
        when(passwordEncoderFactory.getInternalEncoder(anyString())).thenReturn(ldapShaPasswordEncoder);

        try
        {
            internalDirectory.addUser(adminUser, new PasswordCredential(""));
            fail("Should not be able to add a user with a blank credential");
        } catch (InvalidCredentialException e)
        {
            // We should be here
        }

        internalDirectory.setAttributes(Collections.singletonMap(InternalDirectory.ATTRIBUTE_PASSWORD_REGEX, "[a-zA-Z]"));

        try
        {
            internalDirectory.addUser(adminUser, new PasswordCredential("123"));
            fail("Should not be able to add a user with a numercial credential");
        } catch (InvalidCredentialException e)
        {
            // We should be here
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPrincipalMustHaveName() throws Exception
    {
        when(passwordEncoderFactory.getInternalEncoder(anyString())).thenReturn(ldapShaPasswordEncoder);
        when(userDao.add(null, unencryptedCredential)).thenThrow(new IllegalArgumentException());

        internalDirectory.addUser(null, unencryptedCredential);

        fail("We should of thrown an IllegalArgumentException here");
    }

    @Test
    public void testAuthentication() throws Exception
    {
        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), new HashMap<String, Set<String>>());
        when(userDao.findByNameWithAttributes(internalDirectory.getDirectoryId(), ADMIN_USERNAME)).thenReturn(internalUserWithAttributes);
        when(userDao.getCredential(internalDirectory.getDirectoryId(), ADMIN_USERNAME)).thenReturn(encryptedCredential);

        when(passwordEncoderFactory.getInternalEncoder(anyString())).thenReturn(ldapShaPasswordEncoder);

        // Call authenticate
        User user = internalDirectory.authenticate(ADMIN_USERNAME, unencryptedCredential);

        // Assert stuff on the principal
        Map<String, Set<String>> attribs = new HashMap<String, Set<String>>();
        attribs.put(UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton("0"));
        attribs.put(UserConstants.REQUIRES_PASSWORD_CHANGE, Collections.singleton("false"));

        verify(userDao, times(1)).storeAttributes(eq(internalUserWithAttributes), argThat(attributesMatch(attribs)));
    }

    @Test
    public void testAuthenticationWithInActiveUser() throws Exception
    {
        adminUser.setActive(false);
        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), new HashMap<String, Set<String>>());
        when(userDao.findByNameWithAttributes(internalDirectory.getDirectoryId(), ADMIN_USERNAME)).thenReturn(internalUserWithAttributes);

        // Call authenticate
        try
        {
            internalDirectory.authenticate(ADMIN_USERNAME, unencryptedCredential);
            fail("This user account is inactive");
        } catch (InactiveAccountException e)
        {
            // Yay!
        }

        verify(userDao, times(1)).findByNameWithAttributes(internalDirectory.getDirectoryId(), ADMIN_USERNAME);
        verify(userDao, never()).storeAttributes(eq(adminUser), anyMap());
    }

    @Test
    public void testAuthenticationPrincipalRequiresPasswordChange() throws Exception
    {
        when(passwordEncoderFactory.getInternalEncoder(anyString())).thenReturn(ldapShaPasswordEncoder);
        when(userDao.getCredential(internalDirectory.getDirectoryId(), ADMIN_USERNAME)).thenReturn(encryptedCredential);

        // Set the last changed time on the principal 01/01/2000
        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), Collections.singletonMap(UserConstants.PASSWORD_LASTCHANGED, Collections.singleton(Long.toString(949375890937L))));

        // Set the maximum age in days (14 days) for a password on the internal directory
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(InternalDirectory.ATTRIBUTE_PASSWORD_MAX_CHANGE_TIME, "14");

        internalDirectory.setAttributes(attributes);

        when(userDao.findByNameWithAttributes(internalDirectory.getDirectoryId(), internalUserWithAttributes.getName())).thenReturn(internalUserWithAttributes);

        // Call authenticate
        try
        {
            internalDirectory.authenticate(internalUserWithAttributes.getName(), unencryptedCredential);
            fail("Authentication should have failed");
        } catch (ExpiredCredentialException e)
        {
            // validate the REQUIRES_PASSWORD_CHANGE value has been set to 'true'
            Map<String, Set<String>> userAttributes = Maps.newHashMap();
            userAttributes.put(UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton("0"));
            userAttributes.put(UserConstants.REQUIRES_PASSWORD_CHANGE, Collections.singleton("true"));
            verify(userDao, times(1)).storeAttributes(eq(internalUserWithAttributes), argThat(attributesMatch(userAttributes)));
        }
    }

    @Test
    public void testAuthenticationWithBadPassword() throws Exception
    {
        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), Collections.<String, Set<String>>emptyMap());

        when(passwordEncoderFactory.getInternalEncoder(anyString())).thenReturn(ldapShaPasswordEncoder);
        when(userDao.findByNameWithAttributes(internalDirectory.getDirectoryId(), internalUserWithAttributes.getName())).thenReturn(internalUserWithAttributes);
        when(userDao.getCredential(internalDirectory.getDirectoryId(), ADMIN_USERNAME)).thenReturn(encryptedCredential);

        // Call authenticate
        try
        {
            internalDirectory.authenticate(internalUserWithAttributes.getName(), new PasswordCredential("badpassword"));
            fail("Authentication should of failed");

        } catch (InvalidAuthenticationException e)
        {
            // Success!
        }

        Map<String, Set<String>> userAttributes = Maps.newHashMap();
        userAttributes.put(UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton("1"));

        verify(userDao, times(1)).storeAttributes(eq(internalUserWithAttributes), argThat(attributesMatch(userAttributes)));
    }

    @Test
    public void testAuthenticationWithMaxPasswordAttempts() throws Exception
    {
        // set up the principal with max password attempts
        internalDirectory.setAttributes(Collections.singletonMap(InternalDirectory.ATTRIBUTE_PASSWORD_MAX_ATTEMPTS, "1"));

        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), Collections.<String, Set<String>>singletonMap(UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton("1")));

        when(userDao.findByNameWithAttributes(internalDirectory.getDirectoryId(), internalUserWithAttributes.getName())).thenReturn(internalUserWithAttributes);

        // Call authenticate
        try
        {
            internalDirectory.authenticate(internalUserWithAttributes.getName(), unencryptedCredential);
            fail();
        } catch (InvalidAuthenticationException e)
        {
            // success!
            assertEquals("Maximum allowed invalid password attempts has been reached", e.getMessage());
        }

    }

    @Test
    public void testAuthenticationWithUnknownPrincipal() throws Exception
    {
        try
        {
            // Remove the mock since we will not be using it here.
            when(userDao.findByNameWithAttributes(internalDirectory.getDirectoryId(), "unknown-user")).thenThrow(new UserNotFoundException("unknown-user"));

            internalDirectory.authenticate("unknown-user", unencryptedCredential);

            fail("We should of thrown an Invalid principal exception");

        } catch (UserNotFoundException e)
        {
            // We should be here
        }
    }

    @Test
    public void testUpdateUserCredentials() throws Exception
    {
        when(passwordEncoderFactory.getInternalEncoder(anyString())).thenReturn(ldapShaPasswordEncoder);

        // we will try and find the user
        InternalUser internalUser = new InternalUser(adminUser, directoryConfiguration, encryptedCredential);

        when(userDao.findByName(internalDirectory.getDirectoryId(), internalUser.getName())).thenReturn(internalUser);

        internalDirectory.updateUserCredential(internalUser.getName(), new PasswordCredential("newpassword"));

        Map<String, Set<String>> userAttributes = new HashMap<String, Set<String>>();
        userAttributes.put(UserConstants.REQUIRES_PASSWORD_CHANGE, Collections.singleton(Boolean.FALSE.toString()));
        userAttributes.put(UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton(Long.toString(0)));

        verify(userDao).storeAttributes(eq(internalUser), argThat(attributesMatch(userAttributes)));
    }

    @Test
    public void testUpdatePrincipalCredentialsWithOldPassword() throws Exception
    {
        when(passwordEncoderFactory.getInternalEncoder(anyString())).thenReturn(ldapShaPasswordEncoder);
        internalDirectory.setAttributes(Collections.singletonMap(InternalDirectory.ATTRIBUTE_PASSWORD_HISTORY_COUNT, "1"));

        InternalUser internalUser = new InternalUser(adminUser, directoryConfiguration, encryptedCredential);

        when(userDao.getCredentialHistory(internalDirectory.getDirectoryId(), internalUser.getName())).thenReturn(Arrays.asList(encryptedCredential));

        internalUser.getCredentialRecords().add(new InternalUserCredentialRecord(internalUser, encryptedCredential.getCredential()));

        when(userDao.findByName(internalDirectory.getDirectoryId(), internalUser.getName())).thenReturn(internalUser);

        try
        {
            internalDirectory.updateUserCredential(internalUser.getName(), new PasswordCredential(PASSWORD_1));
            fail("Should of thrown InvalidCredentialException since password is already used");
        } catch (InvalidCredentialException e)
        {
            // Success!
        }
    }

    @Test
    public void testRequiresExistingPasswordChangeFlagReturnsTrue()
    {
        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), Collections.<String, Set<String>>singletonMap(UserConstants.REQUIRES_PASSWORD_CHANGE, Collections.singleton(Boolean.TRUE.toString())));

        // this should return true right away because the requires password change is set to true on the principal
        assertTrue(internalDirectory.requiresPasswordChange(internalUserWithAttributes));
    }

    @Test
    public void testRequiresExistingPasswordChangeFlagReturnsFalse()
    {
        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), Collections.<String, Set<String>>singletonMap(UserConstants.REQUIRES_PASSWORD_CHANGE, Collections.singleton(Boolean.FALSE.toString())));

        // this should return true right away because the requires password change is set to true on the principal
        assertFalse(internalDirectory.requiresPasswordChange(internalUserWithAttributes));
    }

    @Test
    public void testCurrentUserInvalidPasswordAttemptsWithNullAttribute()
    {
        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), Collections.<String, Set<String>>singletonMap(UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton(String.class.cast(null))));

        long attempts = internalDirectory.currentPrincipalInvalidPasswordAttempts(internalUserWithAttributes);

        assertEquals("Method should return 0 if no attempts are found", 0L, attempts);

    }

    @Test
    public void testCurrentUserInvalidPasswordAttemptsWithEmptyAttributes()
    {

        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), Collections.<String, Set<String>>emptyMap());

        long attempts = internalDirectory.currentPrincipalInvalidPasswordAttempts(internalUserWithAttributes);

        assertEquals("Method should return 0 if no attempts are found", 0L, attempts);
    }

    @Test
    public void testCurrentPrincipalInvalidPasswordAttemptsWithNullAttibuteList()
    {

        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), Collections.<String, Set<String>>singletonMap(UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.<String>emptySet()));

        long attempts = internalDirectory.currentPrincipalInvalidPasswordAttempts(internalUserWithAttributes);

        assertEquals("Method should return 0 if no attempts are found", 0L, attempts);
    }

    @Test
    public void testCurrentPrincipalInvalidPasswordAttemptsWithCorrectAttribute()
    {

        internalUserWithAttributes = new InternalUserWithAttributes(new InternalUser(adminUser, directoryConfiguration, encryptedCredential), Collections.<String, Set<String>>singletonMap(UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton("5")));

        long attempts = internalDirectory.currentPrincipalInvalidPasswordAttempts(internalUserWithAttributes);

        assertEquals("Method should return 5", 5L, attempts);
    }


    @Test
    public void testAddGroup() throws Exception
    {
        when(groupDao.add(mainGroup)).thenReturn(new InternalGroup(mainGroup, directoryConfiguration));

        final Group group = internalDirectory.addGroup(mainGroup);

        assertEquals(MAIN_GROUP, group.getName());

        // Verify that the store attributes method was also called.
        verify(groupDao, times(1)).add(mainGroup);
    }

    @Test
    public void testAddGroupToGroup() throws Exception
    {
        // mock the find
        when(groupDao.findByName(DIRECTORY_ID, MAIN_GROUP)).thenReturn(new InternalGroup(mainGroup, directoryConfiguration));
        when(groupDao.findByName(DIRECTORY_ID, CHILD_GROUP)).thenReturn(new InternalGroup(childGroup, directoryConfiguration));

        internalDirectory.addGroupToGroup(childGroup.getName(), mainGroup.getName());

        // Verify the add membership was called
        verify(membershipDao, times(1)).addGroupToGroup(DIRECTORY_ID, CHILD_GROUP, MAIN_GROUP);

    }

    @Test
    public void testAddGroupToGroupWrongGroupType() throws Exception
    {
        // Change childGroup to be of type LEGACY_ROLE
        childGroup.setType(GroupType.LEGACY_ROLE);

        // mock the find
        when(groupDao.findByName(DIRECTORY_ID, MAIN_GROUP)).thenReturn(new InternalGroup(mainGroup, directoryConfiguration));
        when(groupDao.findByName(DIRECTORY_ID, CHILD_GROUP)).thenReturn(new InternalGroup(childGroup, directoryConfiguration));

        try
        {
            internalDirectory.addGroupToGroup(mainGroup.getName(), childGroup.getName());
            fail("InvalidMembershipException expected");
        } catch (InvalidMembershipException e)
        {
            // Expected
        }

        // Verify the add membership was NOT called
        verify(membershipDao, never()).addGroupToGroup(DIRECTORY_ID, CHILD_GROUP, MAIN_GROUP);
    }


    @Test
    public void testRemoveUserFromGroup() throws Exception
    {
        // pretend user is already in group
        when(membershipDao.isUserDirectMember(DIRECTORY_ID, ADMIN_USERNAME, MAIN_GROUP)).thenReturn(true);
        // mock the find
        when(userDao.findByName(DIRECTORY_ID, ADMIN_USERNAME)).thenReturn(new InternalUser(adminUser, directoryConfiguration, encryptedCredential));
        when(groupDao.findByName(DIRECTORY_ID, MAIN_GROUP)).thenReturn(new InternalGroup(mainGroup, directoryConfiguration));

        internalDirectory.removeUserFromGroup(ADMIN_USERNAME, MAIN_GROUP);

        // removal of membership should be successful
        verify(membershipDao, times(1)).removeUserFromGroup(DIRECTORY_ID, ADMIN_USERNAME, MAIN_GROUP);
    }

    @Test
    public void testRemoveUserFromGroupNotMembers() throws Exception
    {
        // user is not in group
        when(membershipDao.isUserDirectMember(DIRECTORY_ID, ADMIN_USERNAME, MAIN_GROUP)).thenReturn(false);
        // mock the find
        when(userDao.findByName(DIRECTORY_ID, ADMIN_USERNAME)).thenReturn(new InternalUser(adminUser, directoryConfiguration, encryptedCredential));
        when(groupDao.findByName(DIRECTORY_ID, MAIN_GROUP)).thenReturn(new InternalGroup(mainGroup, directoryConfiguration));

        try
        {
            internalDirectory.removeUserFromGroup(ADMIN_USERNAME, MAIN_GROUP);
            fail("MembershipNotFoundException expected");
        } catch (MembershipNotFoundException e)
        {
            // expected
        }

        // not a member, so should not call removeUserFromGroup
        verify(membershipDao, never()).removeUserFromGroup(DIRECTORY_ID, ADMIN_USERNAME, MAIN_GROUP);
    }

    @Test
    public void testRemoveUserFromGroupNoUser() throws Exception
    {
        // pretend user is already in group
        when(membershipDao.isUserDirectMember(DIRECTORY_ID, ADMIN_USERNAME, MAIN_GROUP)).thenReturn(true);
        // mock the find
        when(userDao.findByName(DIRECTORY_ID, ADMIN_USERNAME)).thenThrow(new UserNotFoundException(ADMIN_USERNAME));
        when(groupDao.findByName(DIRECTORY_ID, MAIN_GROUP)).thenReturn(new InternalGroup(mainGroup, directoryConfiguration));

        try
        {
            internalDirectory.removeUserFromGroup(ADMIN_USERNAME, MAIN_GROUP);
            fail("UserNotFoundException expected");
        } catch (UserNotFoundException e)
        {
            // expected
        }

        // user does not exist,  so should not call removeUserFromGroup
        verify(membershipDao, never()).removeUserFromGroup(DIRECTORY_ID, ADMIN_USERNAME, MAIN_GROUP);
    }

    @Test
    public void testRemoveUserFromGroupNoGroup() throws Exception
    {
        // pretend user is already in group
        when(membershipDao.isUserDirectMember(DIRECTORY_ID, ADMIN_USERNAME, MAIN_GROUP)).thenReturn(true);
        // mock the find
        when(userDao.findByName(DIRECTORY_ID, ADMIN_USERNAME)).thenReturn(new InternalUser(adminUser, directoryConfiguration, encryptedCredential));
        when(groupDao.findByName(DIRECTORY_ID, MAIN_GROUP)).thenThrow(new GroupNotFoundException(MAIN_GROUP));

        try
        {
            internalDirectory.removeUserFromGroup(ADMIN_USERNAME, MAIN_GROUP);
            fail("GroupNotFoundException expected");
        } catch (GroupNotFoundException e)
        {
            // expected
        }

        // group does not exist, so should not call removeUserFromGroup
        verify(membershipDao, never()).removeUserFromGroup(DIRECTORY_ID, ADMIN_USERNAME, MAIN_GROUP);
    }

    @Test
    public void testRemoveGroupFromGroup() throws Exception
    {
        when(membershipDao.isGroupDirectMember(DIRECTORY_ID, CHILD_GROUP, MAIN_GROUP)).thenReturn(true);
        when(groupDao.findByName(DIRECTORY_ID, MAIN_GROUP)).thenReturn(new InternalGroup(mainGroup, directoryConfiguration));
        when(groupDao.findByName(DIRECTORY_ID, CHILD_GROUP)).thenReturn(new InternalGroup(childGroup, directoryConfiguration));

        internalDirectory.removeGroupFromGroup(CHILD_GROUP, MAIN_GROUP);

        // membership removal should be called
        verify(membershipDao, times(1)).removeGroupFromGroup(DIRECTORY_ID, CHILD_GROUP, MAIN_GROUP);

    }

    @Test
    public void testRemoveGroupFromGroupNotMembers() throws Exception
    {
        when(membershipDao.isGroupDirectMember(DIRECTORY_ID, CHILD_GROUP, MAIN_GROUP)).thenReturn(false);
        when(groupDao.findByName(DIRECTORY_ID, MAIN_GROUP)).thenReturn(new InternalGroup(mainGroup, directoryConfiguration));
        when(groupDao.findByName(DIRECTORY_ID, CHILD_GROUP)).thenReturn(new InternalGroup(childGroup, directoryConfiguration));

        try
        {
            internalDirectory.removeGroupFromGroup(CHILD_GROUP, MAIN_GROUP);
            fail("MembershipNotFoundException expected");
        } catch (MembershipNotFoundException e)
        {
            // expected
        }
        // membership removal should NOT be called
        verify(membershipDao, never()).removeGroupFromGroup(DIRECTORY_ID, CHILD_GROUP, MAIN_GROUP);

    }


    @Test
    public void testRemoveGroupFromGroupIncorrectGroupType() throws Exception
    {
        // Change child to GroupType.LEGACY_ROLE
        childGroup.setType(GroupType.LEGACY_ROLE);

        when(membershipDao.isGroupDirectMember(DIRECTORY_ID, CHILD_GROUP, MAIN_GROUP)).thenReturn(true);
        when(groupDao.findByName(DIRECTORY_ID, MAIN_GROUP)).thenReturn(new InternalGroup(mainGroup, directoryConfiguration));
        when(groupDao.findByName(DIRECTORY_ID, CHILD_GROUP)).thenReturn(new InternalGroup(childGroup, directoryConfiguration));

        try
        {
            internalDirectory.removeGroupFromGroup(CHILD_GROUP, MAIN_GROUP);
            fail("InvalidMembershipException expected");
        } catch (InvalidMembershipException e)
        {
            // expected
        }
        // membership removal should NOT be called
        verify(membershipDao, never()).removeGroupFromGroup(DIRECTORY_ID, CHILD_GROUP, MAIN_GROUP);

    }


    @Test
    public void testAddAllUsers()
    {

        Set<UserTemplateWithCredentialAndAttributes> usersToAdd = new HashSet<UserTemplateWithCredentialAndAttributes>();
        usersToAdd.add(new UserTemplateWithCredentialAndAttributes(adminUser, encryptedCredential));
        usersToAdd.add(new UserTemplateWithCredentialAndAttributes(testUser, encryptedCredential));

        when(userDao.addAll((Set<UserTemplateWithCredentialAndAttributes>) anyObject())).thenReturn(new BatchResult<User>(0));

        Collection<User> failedUsers = internalDirectory.addAllUsers(usersToAdd).getFailedEntities();

        // should add all users with no failed users
        verify(userDao, times(1)).addAll(usersToAdd);
        assertEquals(0, failedUsers.size());
    }

    @Test
    public void testAddAllUsersNull()
    {

        try
        {
            internalDirectory.addAllUsers(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            // expected
        }

        verify(userDao, never()).addAll((Set<UserTemplateWithCredentialAndAttributes>) anyObject());
    }

    @Test
    public void testAddAllUsersInvalidUser()
    {
        // make testUser invalid - have upper case in name
        testUser.setName("Test User");
        testUser.setDirectoryId(200L);

        Set<UserTemplateWithCredentialAndAttributes> usersToAdd = new HashSet<UserTemplateWithCredentialAndAttributes>();
        usersToAdd.add(new UserTemplateWithCredentialAndAttributes(adminUser, encryptedCredential));
        usersToAdd.add(new UserTemplateWithCredentialAndAttributes(testUser, encryptedCredential));

        when(userDao.addAll((Set<UserTemplateWithCredentialAndAttributes>) anyObject())).thenReturn(new BatchResult<User>(0));


        Collection<User> failedUsers = internalDirectory.addAllUsers(usersToAdd).getFailedEntities();

        // should add users
        verify(userDao, times(1)).addAll((Set<UserTemplateWithCredentialAndAttributes>) anyObject());
        assertEquals(1, failedUsers.size());
        assertTrue(failedUsers.contains(testUser));

    }

    @Test
    public void testAddAllUsersIllegalArgument()
    {
        Set<UserTemplateWithCredentialAndAttributes> usersToAdd = new HashSet<UserTemplateWithCredentialAndAttributes>();
        usersToAdd.add(new UserTemplateWithCredentialAndAttributes(adminUser, encryptedCredential));
        usersToAdd.add(null);

        when(userDao.addAll((Set<UserTemplateWithCredentialAndAttributes>) anyObject())).thenReturn(new BatchResult<User>(0));

        try
        {
            internalDirectory.addAllUsers(usersToAdd);
            fail("IllegalArgumentException expected - trying to add null user");
        } catch (IllegalArgumentException e)
        {
            // expected
        }

        verify(userDao, never()).addAll((Set<UserTemplateWithCredentialAndAttributes>) anyObject());

    }

    @Test
    public void testAddAllUsersInvalidCredential()
    {
        // set password regex so credentials for testUser is invalid
        internalDirectory.setAttributes(Collections.singletonMap(InternalDirectory.ATTRIBUTE_PASSWORD_REGEX, "abc"));

        Set<UserTemplateWithCredentialAndAttributes> usersToAdd = new HashSet<UserTemplateWithCredentialAndAttributes>();
        usersToAdd.add(new UserTemplateWithCredentialAndAttributes(adminUser, encryptedCredential));
        usersToAdd.add(new UserTemplateWithCredentialAndAttributes(testUser, encryptedCredential));

        when(userDao.addAll((Set<UserTemplateWithCredentialAndAttributes>) anyObject())).thenReturn(new BatchResult<User>(0));

        Collection<User> failedUsers = internalDirectory.addAllUsers(usersToAdd).getFailedEntities();

        // try to add all users - but none of them will match the regex "abc" so fail both!
        verify(userDao, times(1)).addAll((Set<UserTemplateWithCredentialAndAttributes>) anyObject());
        assertEquals(2, failedUsers.size());
        assertTrue(failedUsers.contains(testUser));
        assertTrue(failedUsers.contains(adminUser));
    }


    @Test
    public void testAddAllGroups() throws Exception
    {
        Set<GroupTemplate> groupsToAdd = new HashSet<GroupTemplate>();
        groupsToAdd.add(new GroupTemplate(mainGroup));
        groupsToAdd.add(new GroupTemplate(childGroup));

        when(groupDao.addAll((Set<? extends Group>) anyObject())).thenReturn(new BatchResult<Group>(0));

        Collection<Group> failedGroups = internalDirectory.addAllGroups(groupsToAdd).getFailedEntities();

        // should add groups
        verify(groupDao, times(1)).addAll((Set<GroupTemplate>) anyObject());
        assertEquals(0, failedGroups.size());
    }

    @Test
    public void testAddAllGroupsNull() throws Exception
    {

        try
        {
            internalDirectory.addAllGroups(null);
            fail("IllegalArgumentException expected - trying to add null group");
        } catch (IllegalArgumentException e)
        {
            // expected
        }

        verify(groupDao, never()).addAll((Set<GroupTemplate>) anyObject());
    }

    @Test
    public void testAddAllGroupsInvalidGroup() throws Exception
    {
        // make childGroup invalid - have upper case in name
        childGroup.setName("Child Group");
        childGroup.setDirectoryId(200L);

        Set<GroupTemplate> groupsToAdd = new HashSet<GroupTemplate>();
        groupsToAdd.add(new GroupTemplate(mainGroup));
        groupsToAdd.add(new GroupTemplate(childGroup));

        when(groupDao.addAll((Set<GroupTemplate>) anyObject())).thenReturn(new BatchResult<Group>(0));

        Collection<Group> failedGroups = internalDirectory.addAllGroups(groupsToAdd).getFailedEntities();

        // should add groups
        verify(groupDao, times(1)).addAll((Set<GroupTemplate>) anyObject());
        assertEquals(1, failedGroups.size());
        assertTrue(failedGroups.contains(childGroup));

    }

    @Test
    public void testAddAllGroupsIllegalArgument() throws Exception
    {
        Set<GroupTemplate> groupsToAdd = new HashSet<GroupTemplate>();
        groupsToAdd.add(new GroupTemplate(mainGroup));
        groupsToAdd.add(null);

        when(groupDao.addAll((Set<GroupTemplate>) anyObject())).thenReturn(new BatchResult<Group>(0));

        try
        {
            Collection<Group> failedGroups = internalDirectory.addAllGroups(groupsToAdd).getFailedEntities();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            // expected
        }

        verify(groupDao, never()).addAll((Set<GroupTemplate>) anyObject());
    }

    @Test
    public void testRenameUser() throws Exception
    {
        InternalUser internalUser = new InternalUser(testUser, directoryConfiguration, encryptedCredential);
        when(userDao.findByName(DIRECTORY_ID, TEST_USERNAME)).thenReturn(internalUser);
        when(userDao.findByName(DIRECTORY_ID, "newname")).thenThrow(new UserNotFoundException("newname"));

        internalDirectory.renameUser(TEST_USERNAME, "newname");

        verify(userDao, times(1)).rename(internalUser, "newname");
    }

    @Test
    public void testRenameUserInvalid() throws Exception
    {
        when(userDao.findByName(DIRECTORY_ID, TEST_USERNAME)).thenReturn(new InternalUser(testUser, directoryConfiguration, encryptedCredential));
        when(userDao.findByName(DIRECTORY_ID, ADMIN_USERNAME)).thenReturn(new InternalUser(adminUser, directoryConfiguration, encryptedCredential));

        try
        {
            internalDirectory.renameUser(TEST_USERNAME, ADMIN_USERNAME);
            fail("InvalidUserException expected");
        } catch (UserAlreadyExistsException e)
        {
            // expected
        }

        verify(userDao, never()).rename(testUser, ADMIN_USERNAME);
    }

    @Test
    public void testRenameGroup() throws Exception
    {
        InternalGroup internalGroup = new InternalGroup(childGroup, directoryConfiguration);
        when(groupDao.findByName(DIRECTORY_ID, CHILD_GROUP)).thenReturn(internalGroup);
        when(groupDao.findByName(DIRECTORY_ID, "newname")).thenThrow(new GroupNotFoundException("newname"));

        internalDirectory.renameGroup(CHILD_GROUP, "newname");

        verify(groupDao, times(1)).rename(internalGroup, "newname");
    }

    @Test
    public void testRenameGroupInvalid() throws Exception
    {
        when(groupDao.findByName(DIRECTORY_ID, CHILD_GROUP)).thenReturn(new InternalGroup(childGroup, directoryConfiguration));
        when(groupDao.findByName(DIRECTORY_ID, MAIN_GROUP)).thenReturn(new InternalGroup(mainGroup, directoryConfiguration));

        try
        {
            internalDirectory.renameGroup(CHILD_GROUP, MAIN_GROUP);
            fail("InvalidGroupException expected");
        } catch (InvalidGroupException e)
        {
            // expected
        }

        verify(groupDao, never()).rename(childGroup, MAIN_GROUP);
    }


    private CheckDefinedAttributes attributesMatch(Map<String, Set<String>> attributes)
    {
        return new CheckDefinedAttributes(attributes);
    }


    private static class CheckDefinedAttributes extends ArgumentMatcher<Map<String, Set<String>>>
    {
        private final Map<String, Set<String>> attributes;

        public CheckDefinedAttributes(final Map<String, Set<String>> attributes)
        {
            this.attributes = attributes;
        }

        public boolean matches(Object attributeMap)
        {
            return ((Map) attributeMap).entrySet().containsAll(attributes.entrySet());
        }
    }

}
