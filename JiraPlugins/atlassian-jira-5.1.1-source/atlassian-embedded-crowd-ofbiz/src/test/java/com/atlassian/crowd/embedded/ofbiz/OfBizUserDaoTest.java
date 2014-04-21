package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.ofbiz.db.OfBizHelper;
import com.atlassian.crowd.embedded.testing.TestData;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericDelegator;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class OfBizUserDaoTest extends AbstractTransactionalOfBizTestCase
{
    private OfBizUserDao userDao;
    private OfBizDirectoryDao directoryDao;
    private OfBizInternalMembershipDao internalmembershipDao;

    @Before
    public void setUp() throws Exception
    {
        directoryDao = new OfBizDirectoryDao(getGenericDelegator());
        internalmembershipDao = new OfBizInternalMembershipDao(getGenericDelegator(), directoryDao);
        userDao = new OfBizUserDao(getGenericDelegator(), directoryDao, internalmembershipDao);
    }

    @After
    public void tearDown() throws Exception
    {
        userDao = null;
    }

    @Test
    public void testAddAndFindUserByName() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);

        TestData.User.assertEqualsTestUser(createdUser);

        final User retrievedUser = userDao.findByName(TestData.DIRECTORY_ID, TestData.User.NAME);

        TestData.User.assertEqualsTestUser(retrievedUser);
    }

    @Test
    public void testTurkish() throws Exception
    {
        // EMBCWD-735
        // Add a dotted i turkish user
        User user = TestData.User.getUser("turkish", TestData.DIRECTORY_ID, TestData.User.ACTIVE, TestData.User.FIRST_NAME, TestData.User.LAST_NAME, TestData.User.DISPLAY_NAME, TestData.User.EMAIL);
        User createdUser = userDao.add(user, TestData.User.CREDENTIAL);

        // Now add a dotless turk?sh user
        user = TestData.User.getUser("turk\u0131sh", TestData.DIRECTORY_ID, TestData.User.ACTIVE, TestData.User.FIRST_NAME, TestData.User.LAST_NAME, TestData.User.DISPLAY_NAME, TestData.User.EMAIL);
        createdUser = userDao.add(user, TestData.User.CREDENTIAL);

        List<User> allUsers = userDao.search(TestData.DIRECTORY_ID, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertEquals(2, allUsers.size());
    }

    @Test
    public void testAddAndStoreAttributesAndFindUserWithAttributesByName() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);
        TestData.User.assertEqualsTestUser(createdUser);

        userDao.storeAttributes(createdUser, TestData.Attributes.getTestData());

        final UserWithAttributes retrievedUser = userDao.findByNameWithAttributes(TestData.DIRECTORY_ID, TestData.User.NAME);
        TestData.User.assertEqualsTestUser(retrievedUser);
        TestData.Attributes.assertEqualsTestData(retrievedUser);
    }

    @Test
    public void testUpdateUser() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);
        TestData.User.assertEqualsTestUser(createdUser);

        final boolean updatedIsActive = false;
        final String updatedEmail = "updatedEmail@example.com";
        final String updatedFirstName = "updatedFirstName";
        final String updatedLastName = "updatedLastName";
        final String updatedDisplayName = "updatedDisplayName";

        userDao.update(TestData.User.getUser(createdUser.getName(), createdUser.getDirectoryId(), updatedIsActive, updatedFirstName, updatedLastName,
                updatedDisplayName, updatedEmail));

        final User updatedUser = userDao.findByName(TestData.DIRECTORY_ID, TestData.User.NAME);

        assertEquals(TestData.User.NAME, updatedUser.getName());
        assertEquals(updatedIsActive, updatedUser.isActive());
        assertEquals(updatedFirstName, updatedUser.getFirstName());
        assertEquals(updatedLastName, updatedUser.getLastName());
        assertEquals(updatedDisplayName, updatedUser.getDisplayName());
        assertEquals(updatedEmail, updatedUser.getEmailAddress());
    }

    @Test
    public void testUpdateCredentialAndGetCredential() throws Exception
    {
        final PasswordCredential updatedCredential = PasswordCredential.encrypted("I am a secret hash");
        final User user = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);

        userDao.updateCredential(user, updatedCredential, 0);

        assertEquals(updatedCredential, userDao.getCredential(TestData.DIRECTORY_ID, TestData.User.NAME));
    }

    @Test
    public void testRemoveUser() throws Exception
    {
        userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);
        assertNotNull(userDao.findByName(TestData.DIRECTORY_ID, TestData.User.NAME));

        userDao.remove(TestData.User.getTestData());
        try
        {
            userDao.findByName(TestData.DIRECTORY_ID, TestData.User.NAME);
            fail("Should have thrown a user not found exception");
        }
        catch (final UserNotFoundException e)
        {
            assertEquals(TestData.User.NAME, e.getUserName());
        }
    }

    @Test
    public void testRemoveAttribute() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);
        userDao.storeAttributes(createdUser, TestData.Attributes.getTestData());

        TestData.Attributes.assertEqualsTestData(userDao.findByNameWithAttributes(TestData.DIRECTORY_ID, TestData.User.NAME));

        userDao.removeAttribute(createdUser, TestData.Attributes.ATTRIBUTE1);
        final UserWithAttributes userWithLessAttributes = userDao.findByNameWithAttributes(TestData.DIRECTORY_ID, TestData.User.NAME);

        assertNull(userWithLessAttributes.getValue(TestData.Attributes.ATTRIBUTE1));
    }

    @Test
    public void testGetCredentialHistory() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);

        OfBizHelper ofBiz = new OfBizHelper(GenericDelegator.getGenericDelegator("default"));
        ofBiz.createValue(UserCredentialHistoryEntity.ENTITY, UserCredentialHistoryEntity.getData(((OfBizUser) createdUser).getId(), "secret1",
                2));
        ofBiz.createValue(UserCredentialHistoryEntity.ENTITY, UserCredentialHistoryEntity.getData(((OfBizUser) createdUser).getId(), "secret3",
                1));
        ofBiz.createValue(UserCredentialHistoryEntity.ENTITY, UserCredentialHistoryEntity.getData(((OfBizUser) createdUser).getId(), "secret2",
                3));

        final List<PasswordCredential> credentials = userDao.getCredentialHistory(TestData.DIRECTORY_ID, TestData.User.NAME);

        assertEquals(3, credentials.size());

        assertEquals(Arrays.asList(new PasswordCredential("secret3", true), new PasswordCredential("secret1", true), new PasswordCredential(
                "secret2", true)), credentials);
    }
}
