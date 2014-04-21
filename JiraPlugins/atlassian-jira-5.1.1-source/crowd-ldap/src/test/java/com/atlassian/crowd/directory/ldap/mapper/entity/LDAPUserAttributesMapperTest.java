package com.atlassian.crowd.directory.ldap.mapper.entity;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ldap.UncategorizedLdapException;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class LDAPUserAttributesMapperTest
{
    @Mock
    LDAPPropertiesMapper mockLdapPropertiesMapper;
    
    private LDAPUserAttributesMapper mapper;

    private static final long DIRECTORY_ID = 1;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        when(mockLdapPropertiesMapper.getObjectClassAttribute()).thenReturn("objectClass");
        when(mockLdapPropertiesMapper.getUserObjectClass()).thenReturn("we-are-not-testing-this");
        when(mockLdapPropertiesMapper.getUserNameAttribute()).thenReturn(LDAPPropertiesMapper.USER_USERNAME_KEY);
        when(mockLdapPropertiesMapper.getUserFirstNameAttribute()).thenReturn(LDAPPropertiesMapper.USER_FIRSTNAME_KEY);
        when(mockLdapPropertiesMapper.getUserLastNameAttribute()).thenReturn(LDAPPropertiesMapper.USER_LASTNAME_KEY);
        when(mockLdapPropertiesMapper.getUserDisplayNameAttribute()).thenReturn(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY);
        when(mockLdapPropertiesMapper.getUserEmailAttribute()).thenReturn(LDAPPropertiesMapper.USER_EMAIL_KEY);

        mapper = new LDAPUserAttributesMapper(DIRECTORY_ID, mockLdapPropertiesMapper);
    }

    @After
    public void tearDown()
    {
        mapper = null;
    }

    /**
     * @param directoryAttributes
     * @return
     * @throws javax.naming.NamingException If there attribute does not exist
     */
    private String getAttribute(String attributeName, Attributes directoryAttributes) throws NamingException
    {
        Attribute attribute = directoryAttributes.get(attributeName);
        if (attribute == null)
        {
            throw new UncategorizedLdapException(attributeName + " was not found in results");
        }
        try
        {
            return (String) attribute.get(0);
        }
        catch (javax.naming.NamingException e)
        {
            throw new UncategorizedLdapException(e);
        }

    }

    @Test
    public void testMapFromAttributes() throws NamingException
    {
        // create attributes
        Attributes directoryAttributes = new BasicAttributes();
        directoryAttributes.put(LDAPPropertiesMapper.USER_USERNAME_KEY, "bob_dinosaur");
        directoryAttributes.put(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, "Bob");
        directoryAttributes.put(LDAPPropertiesMapper.USER_LASTNAME_KEY, "Dinosaur");
        directoryAttributes.put(LDAPPropertiesMapper.USER_EMAIL_KEY, "bob.dinosaur@example.com");
        directoryAttributes.put(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, "Bob Dinosaur");

        User user = (User) mapper.mapFromAttributes(directoryAttributes);

        assertNotNull(user);
        assertEquals("Usernames should be the same", getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes), user.getName());
        assertEquals("First names should be the same", getAttribute(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, directoryAttributes), user.getFirstName());
        assertEquals("Last names should be the same", getAttribute(LDAPPropertiesMapper.USER_LASTNAME_KEY, directoryAttributes), user.getLastName());
        assertEquals("Email addresses should be the same", getAttribute(LDAPPropertiesMapper.USER_EMAIL_KEY, directoryAttributes), user.getEmailAddress());
        assertEquals("Display name should be the same", getAttribute(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, directoryAttributes), user.getDisplayName());
    }

    @Test
    public void testMapUserFromAttributesNull()
    {
        try
        {
            mapper.mapUserFromAttributes(null);

            fail("Should have thrown UncategorizedLdapException on null input");
        }
        catch (UncategorizedLdapException e)
        {
            // expected
        }
    }

    @Test
    public void testMapUserFromAttributesUsername() throws NamingException
    {
        // create attributes
        Attributes directoryAttributes = new BasicAttributes();
        directoryAttributes.put(LDAPPropertiesMapper.USER_USERNAME_KEY, "bob_dinosaur");

        User user = mapper.mapUserFromAttributes(directoryAttributes);

        assertNotNull(user);
        assertEquals("Usernames should be the same", getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes), user.getName());
        assertEquals("Display name should be username", getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes), user.getDisplayName());
        assertEquals("Last name should be username", getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes), user.getLastName());
    }

    @Test
    public void testMapUserFromAttributesNoUsername() throws NamingException
    {
        // create attributes
        Attributes directoryAttributes = new BasicAttributes();
        directoryAttributes.put(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, "Bob");

        try
        {
            mapper.mapUserFromAttributes(directoryAttributes);

            fail("Should have thrown NamingException due to lack of username attribute");
        }
        catch (org.springframework.ldap.NamingException e)
        {
        }
    }

    @Test
    public void testMapUserFromAttributesUsernameFirstname() throws NamingException
    {
        // create attributes
        Attributes directoryAttributes = new BasicAttributes();
        directoryAttributes.put(LDAPPropertiesMapper.USER_USERNAME_KEY, "bob_dinosaur");
        directoryAttributes.put(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, "Bob");

        User user = mapper.mapUserFromAttributes(directoryAttributes);

        assertNotNull(user);
        assertEquals("Usernames should be the same", getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes), user.getName());
        assertEquals("First names should be the same", getAttribute(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, directoryAttributes), user.getFirstName());
        assertEquals("Display name should be first name", getAttribute(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, directoryAttributes), user.getDisplayName());
        assertEquals("Last name should be first name", getAttribute(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, directoryAttributes), user.getLastName());
    }

    @Test
    public void testMapUserFromAttributesUsernameLastname() throws NamingException
    {
        // create attributes
        Attributes directoryAttributes = new BasicAttributes();
        directoryAttributes.put(LDAPPropertiesMapper.USER_USERNAME_KEY, "bob_dinosaur");
        directoryAttributes.put(LDAPPropertiesMapper.USER_LASTNAME_KEY, "Dinosaur");

        User user = mapper.mapUserFromAttributes(directoryAttributes);

        assertNotNull(user);
        assertEquals("Usernames should be the same", getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes), user.getName());
        assertEquals("Last names should be the same", getAttribute(LDAPPropertiesMapper.USER_LASTNAME_KEY, directoryAttributes), user.getLastName());
        assertEquals("Display name should be last name", getAttribute(LDAPPropertiesMapper.USER_LASTNAME_KEY, directoryAttributes), user.getDisplayName());
    }

    @Test
    public void testMapUserFromAttributesUsernameDisplayName() throws NamingException
    {
        // create attributes
        Attributes directoryAttributes = new BasicAttributes();
        directoryAttributes.put(LDAPPropertiesMapper.USER_USERNAME_KEY, "bob_dinosaur");
        directoryAttributes.put(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, "Dinosaur Dude");

        User user = mapper.mapUserFromAttributes(directoryAttributes);

        assertNotNull(user);
        assertEquals("Usernames should be the same", getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes), user.getName());
        assertEquals("DisplayNames should be the same", getAttribute(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, directoryAttributes), user.getDisplayName());
    }

    @Test
    public void testMapUserFromAttributesUsernameEmail() throws NamingException
    {
        // create attributes
        Attributes directoryAttributes = new BasicAttributes();
        directoryAttributes.put(LDAPPropertiesMapper.USER_USERNAME_KEY, "bob_dinosaur");
        directoryAttributes.put(LDAPPropertiesMapper.USER_EMAIL_KEY, "bob.dinosaur@example.com");

        User user = mapper.mapUserFromAttributes(directoryAttributes);

        assertNotNull(user);
        assertEquals("Usernames should be the same", getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes), user.getName());
        assertEquals("Email addresses should be the same", getAttribute(LDAPPropertiesMapper.USER_EMAIL_KEY, directoryAttributes), user.getEmailAddress());
    }

    @Test
    public void testMapUserFromAttributesUsernameFirstnameLastnameEmailDisplayName() throws NamingException
    {
        // create attributes
        Attributes directoryAttributes = new BasicAttributes();
        directoryAttributes.put(LDAPPropertiesMapper.USER_USERNAME_KEY, "bob_dinosaur");
        directoryAttributes.put(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, "Bob");
        directoryAttributes.put(LDAPPropertiesMapper.USER_LASTNAME_KEY, "Dinosaur");
        directoryAttributes.put(LDAPPropertiesMapper.USER_EMAIL_KEY, "bob.dinosaur@example.com");
        directoryAttributes.put(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, "Bob Dinosaur");

        User user = mapper.mapUserFromAttributes(directoryAttributes);

        assertNotNull(user);
        assertEquals("Usernames should be the same", getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes), user.getName());
        assertEquals("First names should be the same", getAttribute(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, directoryAttributes), user.getFirstName());
        assertEquals("Last names should be the same", getAttribute(LDAPPropertiesMapper.USER_LASTNAME_KEY, directoryAttributes), user.getLastName());
        assertEquals("Email addresses should be the same", getAttribute(LDAPPropertiesMapper.USER_EMAIL_KEY, directoryAttributes), user.getEmailAddress());
        assertEquals("Display name should be the same", getAttribute(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, directoryAttributes), user.getDisplayName());
    }

    @Test
    public void testMapAttributesFromUserNull()
    {
        try
        {
            mapper.mapAttributesFromUser(null);

            fail("Should have thrown UncategorizedLdapException on null input");
        }
        catch (UncategorizedLdapException e)
        {
            // expected
        }
    }

    @Test
    public void testMapAttributesFromUserUsernameFirstname() throws NamingException
    {
        // create user
        UserTemplateWithAttributes user = new UserTemplateWithAttributes("bob_dinosaur", DIRECTORY_ID);
        user.setName("bob_dinosaur");
        user.setFirstName("Bob");

        Attributes directoryAttributes = mapper.mapAttributesFromUser(user);

        assertNotNull(directoryAttributes);
        assertEquals("Usernames should be the same", user.getName(), getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes));
        assertEquals("First names should be the same", user.getFirstName(), getAttribute(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, directoryAttributes));
        assertEquals("Display name should be first name", user.getFirstName(), getAttribute(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, directoryAttributes));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(directoryAttributes);
    }

    @Test
    public void testMapAttributesFromUserFirstNameLastNameFromDisplayName() throws NamingException
    {
        // create user
        UserTemplateWithAttributes user = new UserTemplateWithAttributes("bob_dinosaur", DIRECTORY_ID);
        user.setName("bob_dinosaur");
        user.setDisplayName("Bob Dinosaur");

        Attributes directoryAttributes = mapper.mapAttributesFromUser(user);

        assertNotNull(directoryAttributes);
        assertEquals("Usernames should be the same", user.getName(), getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes));
        assertEquals("First name should be Bob", "Bob", getAttribute(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, directoryAttributes));
        assertEquals("Last name should be Dinosaur", "Dinosaur", getAttribute(LDAPPropertiesMapper.USER_LASTNAME_KEY, directoryAttributes));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(directoryAttributes);
    }

    @Test
    public void testMapAttributesFromUserEmptyFirstNameLastName() throws NamingException
    {
        // create user
        UserTemplateWithAttributes user = new UserTemplateWithAttributes("bob_dinosaur", DIRECTORY_ID);
        user.setName("bob_dinosaur");

        Attributes directoryAttributes = mapper.mapAttributesFromUser(user);

        assertNotNull(directoryAttributes);
        assertEquals("Usernames should be the same", user.getName(), getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes));
        assertNull("First name should not have been saved", directoryAttributes.get(LDAPPropertiesMapper.USER_FIRSTNAME_KEY));
        assertEquals("Last name should be username", user.getName(), getAttribute(LDAPPropertiesMapper.USER_LASTNAME_KEY, directoryAttributes));
        assertEquals("Display name should be username", user.getName(), getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(directoryAttributes);
    }

    @Test
    public void testMapAttributesFromUserUsernameLastname() throws NamingException
    {
        // create user
        UserTemplateWithAttributes user = new UserTemplateWithAttributes("bob_dinosaur", DIRECTORY_ID);
        user.setName("bob_dinosaur");
        user.setLastName("Dinosaur");

        Attributes directoryAttributes = mapper.mapAttributesFromUser(user);

        assertNotNull(directoryAttributes);
        assertEquals("Usernames should be the same", user.getName(), getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes));
        assertEquals("Last names should be the same", user.getLastName(), getAttribute(LDAPPropertiesMapper.USER_LASTNAME_KEY, directoryAttributes));
        assertEquals("Display name should be last name", user.getLastName(), getAttribute(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, directoryAttributes));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(directoryAttributes);
    }

    @Test
    public void testMapAttributesFromUserUsernameDisplayName() throws NamingException
    {
        // create user
        UserTemplateWithAttributes user = new UserTemplateWithAttributes("bob_dinosaur", DIRECTORY_ID);
        user.setName("bob_dinosaur");
        user.setDisplayName("Dino Dude");

        Attributes directoryAttributes = mapper.mapAttributesFromUser(user);

        assertNotNull(directoryAttributes);
        assertEquals("Usernames should be the same", user.getName(), getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes));
        assertEquals("Display names should be the same", user.getDisplayName(), getAttribute(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, directoryAttributes));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(directoryAttributes);
    }

    @Test
    public void testMapAttributesFromUserUsernameEmail() throws NamingException
    {
        // create user
        UserTemplateWithAttributes user = new UserTemplateWithAttributes("bob_dinosaur", DIRECTORY_ID);
        user.setName("bob_dinosaur");
        user.setEmailAddress("bob.dino@example.com");

        Attributes directoryAttributes = mapper.mapAttributesFromUser(user);

        assertNotNull(directoryAttributes);
        assertEquals("Usernames should be the same", user.getName(), getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes));
        assertEquals("Email addresses should be the same", user.getEmailAddress(), getAttribute(LDAPPropertiesMapper.USER_EMAIL_KEY, directoryAttributes));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(directoryAttributes);
    }

    @Test
    public void testMapAttributesFromUserUsernameFirstnameLastnameEmailDisplayName() throws NamingException
    {
        // create user
        UserTemplateWithAttributes user = new UserTemplateWithAttributes("bob_dinosaur", DIRECTORY_ID);
        user.setName("bob_dinosaur");
        user.setFirstName("Bob");
        user.setLastName("Dinosaur");
        user.setEmailAddress("bob.dinosaur@example.com");
        user.setDisplayName("Bob Dinosaur");

        Attributes directoryAttributes = mapper.mapAttributesFromUser(user);

        assertNotNull(directoryAttributes);
        assertEquals("Usernames should be the same", user.getName(), getAttribute(LDAPPropertiesMapper.USER_USERNAME_KEY, directoryAttributes));
        assertEquals("First names should be the same", user.getFirstName(), getAttribute(LDAPPropertiesMapper.USER_FIRSTNAME_KEY, directoryAttributes));
        assertEquals("Last names should be the same", user.getLastName(), getAttribute(LDAPPropertiesMapper.USER_LASTNAME_KEY, directoryAttributes));
        assertEquals("Email addresses should be the same", user.getEmailAddress(), getAttribute(LDAPPropertiesMapper.USER_EMAIL_KEY, directoryAttributes));
        assertEquals("Display names should be the same", user.getDisplayName(), getAttribute(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, directoryAttributes));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(directoryAttributes);
    }

    @Test
    public void testMapAttributesFromEmptyUserValues() throws NamingException
    {
        // create user
        UserTemplateWithAttributes user = new UserTemplateWithAttributes("bob_dinosaur", DIRECTORY_ID);
        user.setName("bob_dinosaur");
        user.setFirstName("");
        user.setLastName("");
        user.setEmailAddress("");
        user.setDisplayName("");

        Attributes directoryAttributes = mapper.mapAttributesFromUser(user);
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(directoryAttributes);
    }

    /**
     * Tests that a null email attribute from LDAP will result in an empty string when the user object is constructed.
     */
    @Test
    public void testMapUserFromAttributesWithNullEmail() throws Exception
    {
        Attributes directoryAttributes = new BasicAttributes();
        directoryAttributes.put(LDAPPropertiesMapper.USER_USERNAME_KEY, "bob_dinosaur");
        directoryAttributes.put(LDAPPropertiesMapper.USER_EMAIL_KEY, null);

        UserTemplateWithAttributes user = mapper.mapUserFromAttributes(directoryAttributes);
        assertEquals("", user.getEmailAddress());
    }

}
