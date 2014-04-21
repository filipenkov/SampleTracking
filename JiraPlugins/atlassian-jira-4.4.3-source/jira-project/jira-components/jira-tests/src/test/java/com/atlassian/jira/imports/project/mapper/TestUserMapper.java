package com.atlassian.jira.imports.project.mapper;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v3.13
 */
public class TestUserMapper extends ListeningTestCase
{
    @Test
    public void testRegisterExternalUser() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duder", "Duder McMan", "duder@test.com", "duder");

        UserMapper userMapper = new UserMapper(null)
        {
            public boolean userExists(final String userName)
            {
                return true;
            }
        };
        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        assertEquals(2, userMapper.getRegisteredOldIds().size());
        assertEquals(externalUser1, userMapper.getExternalUser("dude"));
        assertEquals(externalUser2, userMapper.getExternalUser("duder"));
    }

    @Test
    public void testRegisterNullExternalUser() throws Exception
    {
        UserMapper userMapper = new UserMapper(null);

        try
        {
            userMapper.registerOldValue(null);
            fail("should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }

        assertEquals(0, userMapper.getRegisteredOldIds().size());
    }

    @Test
    public void testFlagAsMandatorySimple() throws Exception
    {
        UserMapper userMapper = new UserMapper(null);

        userMapper.flagUserAsMandatory("dude");
        userMapper.flagUserAsMandatory("duder");
        assertEquals(2, userMapper.getRequiredOldIds().size());
        assertTrue(userMapper.getRequiredOldIds().contains("dude"));
        assertTrue(userMapper.getRequiredOldIds().contains("duder"));
    }

    @Test
    public void testFlagAsMandatoryOverridesInUse() throws Exception
    {
        UserMapper userMapper = new UserMapper(null)
        {
            public boolean userExists(final String userName)
            {
                return false;
            }
        };

        userMapper.flagUserAsInUse("dude");
        assertEquals(1, userMapper.getUnmappedUsersInUse().size());
        userMapper.flagUserAsMandatory("dude");
        // This should remove "dude" from In Use
        assertEquals(0, userMapper.getUnmappedUsersInUse().size());
        assertEquals(1, userMapper.getRequiredOldIds().size());
        assertTrue(userMapper.getRequiredOldIds().contains("dude"));
    }

    @Test
    public void testGetUnmappedMandatoryUsers() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duder", "Duder McMan", "duder@test.com", "duder");

        UserMapper userMapper = new UserMapper(null)
        {
            public boolean userExists(final String userName)
            {
                return userName.equals("dude");
            }
        };
                
        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        userMapper.flagUserAsMandatory("dude");
        userMapper.flagUserAsMandatory("duder");

        assertEquals(1, userMapper.getUnmappedMandatoryUsers().size());
        assertEquals(externalUser2, userMapper.getUnmappedMandatoryUsers().iterator().next());
    }

    @Test
    public void testFlagAsInUseSimple()
    {
        UserMapper userMapper = new UserMapper(null)
        {
            public boolean userExists(final String userName)
            {
                return "fred".equals(userName);
            }
        };

        userMapper.flagUserAsInUse("dude");
        userMapper.flagUserAsInUse("duder");
        userMapper.flagUserAsInUse("fred");
        userMapper.flagUserAsInUse("barney");
        userMapper.registerOldValue(new ExternalUser("barney", "", ""));
        assertEquals(3, userMapper.getUnmappedUsersInUse().size());
        assertTrue(userMapper.getUnmappedUsersInUse().contains(new ExternalUser("dude", "", "")));
        assertTrue(userMapper.getUnmappedUsersInUse().contains(new ExternalUser("duder", "", "")));
        assertTrue(userMapper.getUnmappedUsersInUse().contains(new ExternalUser("barney", "", "")));
    }

    @Test
    public void testFlagAsInUseButAlsoMandatory()
    {
        UserMapper userMapper = new UserMapper(null)
        {
            public boolean userExists(final String userName)
            {
                return false;
            }
        };

        // Dude is required
        userMapper.flagUserAsMandatory("dude");
        // Now if dude is flagged as in use, this should be ignored.
        userMapper.flagUserAsInUse("dude");
        userMapper.flagUserAsInUse("duder");
        assertEquals(1, userMapper.getUnmappedUsersInUse().size());
        assertTrue(userMapper.getUnmappedUsersInUse().contains(new ExternalUser("duder", "", "")));
    }

    @Test
    public void testGetUnmappedRequiredUsersNoExternalUser() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duder", "Duder McMan", "duder@test.com", "duder");

        UserMapper userMapper = new UserMapper(null)
        {
            public boolean userExists(final String userName)
            {
                return "dude".equals(userName) || "duder".equals(userName);
            }
        };

        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        userMapper.flagUserAsMandatory("dude");
        userMapper.flagUserAsMandatory("duder");
        userMapper.flagUserAsMandatory("duderest");

        assertEquals(1, userMapper.getUnmappedMandatoryUsers().size());
        assertEquals(new ExternalUser("duderest", "", ""), userMapper.getUnmappedMandatoryUsers().iterator().next());
    }

    @Test
    public void testGetUnmappedMandatoryUsersWithNoRegisteredOldValue() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duder", "Duder McMan", "duder@test.com", "duder");

        UserMapper userMapper = new UserMapper(null)
        {
            public boolean userExists(final String userName)
            {
                return "dude".equals(userName) || "barney".equals(userName);
            }
        };

        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        userMapper.flagUserAsMandatory("dude");
        userMapper.flagUserAsMandatory("duder");
        userMapper.flagUserAsMandatory("fred");
        userMapper.flagUserAsMandatory("barney");

        assertEquals(1, userMapper.getUnmappedMandatoryUsersWithNoRegisteredOldValue().size());
        assertEquals(new ExternalUser("fred", "", ""), userMapper.getUnmappedMandatoryUsersWithNoRegisteredOldValue().iterator().next());
    }

    @Test
    public void testGetUnmappedUsersInUseWithNoRegisteredOldValue() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duder", "Duder McMan", "duder@test.com", "duder");

        UserMapper userMapper = new UserMapper(null)
        {
            public boolean userExists(final String userName)
            {
                return "dude".equals(userName) || "barney".equals(userName);
            }
        };

        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        userMapper.flagUserAsInUse("dude");
        userMapper.flagUserAsInUse("duder");
        userMapper.flagUserAsInUse("fred");
        userMapper.flagUserAsInUse("barney");

        assertEquals(1, userMapper.getUnmappedUsersInUseWithNoRegisteredOldValue().size());
        assertEquals(new ExternalUser("fred", "", ""), userMapper.getUnmappedUsersInUseWithNoRegisteredOldValue().iterator().next());
    }

    @Test
    public void testGetUsersToAutoCreate() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duder", "Duder McMan", "duder@test.com", "duder");

        UserMapper userMapper = new UserMapper(null)
        {
            public boolean userExists(final String userName)
            {
                return "betty".equals(userName) || "wilma".equals(userName);
            }
        };

        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        userMapper.flagUserAsInUse("dude");
        userMapper.flagUserAsMandatory("duder");
        userMapper.flagUserAsInUse("fred");
        userMapper.flagUserAsMandatory("barney");
        userMapper.flagUserAsInUse("wilma");
        userMapper.flagUserAsMandatory("betty");

        assertEquals(2, userMapper.getUsersToAutoCreate().size());
        assertTrue(userMapper.getUsersToAutoCreate().contains(externalUser1));
        assertTrue(userMapper.getUsersToAutoCreate().contains(externalUser2));
    }
}
