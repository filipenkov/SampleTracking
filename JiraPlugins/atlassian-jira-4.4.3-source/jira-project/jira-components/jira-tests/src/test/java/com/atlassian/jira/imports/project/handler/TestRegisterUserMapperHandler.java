package com.atlassian.jira.imports.project.handler;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v3.13
 */
public class TestRegisterUserMapperHandler extends ListeningTestCase
{
    /*
     * Simple test that just adds a single User with only mandatory fields.
     */
    @Test
    public void testSimpleExample() throws ParseException
    {
        // Note that at present, we expect password to be ignored.
        final ExternalUser expectedUser = new ExternalUser("wanchor", "William Anchor", "wanchor@example.com");
        expectedUser.setId("10060");
        expectedUser.setPasswordHash("qwerty");

        UserMapper userMapper = new UserMapper(null);

        // Create our RegisterUserMapperHandler under test.
        RegisterUserMapperHandler registerUserMapperHandler = new RegisterUserMapperHandler(userMapper);
        registerUserMapperHandler.startDocument();
        // Send the User entry (Now with Crowd, Full Name and email address are no longer OSProperties :)
        //<User id="10011" directoryId="10000" userName="wilma" lowerUserName="wilma" active="1" createdDate="2010-01-04 09:49:04.932591" updatedDate="2010-01-04 09:49:04.932591" firstName="" lowerFirstName="" lastName="" lowerLastName="" displayName="Wilma Flinstone" lowerDisplayName="wilma flinstone" emailAddress="wilma@example.com" lowerEmailAddress="wilma@example.com" credential="jEUdB0f3hSbXaKrvGQrOull9LQ74qN9hVSjNpi5cicihae2b8IIBATUtHNWwyEBCopdv9Uqm5hn+5rpGVJC0Gg=="/>
        registerUserMapperHandler.handleEntity("User", EasyMap.build("id", "10060", "userName", "wanchor", "displayName", "William Anchor", "firstName", "William", "lastName", "Anchor", "emailAddress", "wanchor@example.com", "credential", "qwerty"));
        registerUserMapperHandler.endDocument();

        assertEquals(1, userMapper.getRegisteredOldIds().size());
        assertEquals(expectedUser, userMapper.getExternalUser("wanchor"));
    }

    /*
     * Sends a number of Users, and includes extra entries that will try to trick the RegisterUserMapperHandler
     */
    @Test
    public void testUserWithProperties() throws ParseException
    {
        final ExternalUser expectedUser = new ExternalUser("wilma", "Wilma Flinstone", "wilma@example.com");
        expectedUser.setId("10011");
        expectedUser.setPasswordHash("qwerty");
        expectedUser.setUserProperty("ice cream", "strawberry");

        UserMapper userMapper = new UserMapper(null);

        // Create our RegisterUserMapperHandler under test.
        RegisterUserMapperHandler registerUserMapperHandler = new RegisterUserMapperHandler(userMapper);
        registerUserMapperHandler.startDocument();
        // Send ExternalEntity Mapping for wilma
        //     <ExternalEntity id="10001" name="wilma" type="com.atlassian.jira.user.OfbizExternalEntityStore"/>
        registerUserMapperHandler.handleEntity("ExternalEntity", EasyMap.build("id", "10001", "name", "wilma", "type", "com.atlassian.jira.user.OfbizExternalEntityStore"));
        // Send Property key "jira.meta.ice cream"
        //     <OSPropertyEntry id="10070" entityName="ExternalEntity" entityId="10001" propertyKey="jira.meta.ice cream" type="5"/>
        registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10070", "entityName", "ExternalEntity", "entityId", "10001", "propertyKey", "jira.meta.ice cream", "type", "5"));
        // Send Property Value for "jira.meta.ice cream"
        //     <OSPropertyString id="10070" value="strawberry"/>
        registerUserMapperHandler.handleEntity("OSPropertyString", EasyMap.build("id", "10070", "value", "strawberry"));
        // Send the User entry (Now with Crowd, Full Name and email address are no longer OSProperties :)
        // <User id="10011" directoryId="1" userName="wilma" lowerUserName="wilma" active="1" createdDate="2010-05-31 12:05:58.0" updatedDate="2010-05-31 12:05:58.0" displayName="Wilma Flinstone" lowerDisplayName="wilma flinstone" emailAddress="wilma@example.com" lowerEmailAddress="wilma@example.com" credential="jEUdB0f3hSbXaKrvGQrOull9LQ74qN9hVSjNpi5cicihae2b8IIBATUtHNWwyEBCopdv9Uqm5hn+5rpGVJC0Gg=="/>
        registerUserMapperHandler.handleEntity("User", EasyMap.build("id", "10011", "userName", "wilma", "displayName", "Wilma Flinstone", "emailAddress", "wilma@example.com", "credential", "qwerty"));

        registerUserMapperHandler.endDocument();

        assertEquals(1, userMapper.getRegisteredOldIds().size());
        assertEquals(expectedUser, userMapper.getExternalUser("wilma"));
    }

    /**
     * Sends a number of Users, and includes extra entries that will try to trick the RegisterUserMapperHandler
     */
    @Test
    public void testComplexExample() throws ParseException
    {
        // Set up the expected Users to be added.
        // Note that at present, we expect password to be ignored.
        // User Willy
        final ExternalUser expectedUser1 = new ExternalUser("wanchor", "William Anchor", "wanchor@example.com");
        expectedUser1.setId("10060");
        expectedUser1.setUserProperty("banana", "green");
        expectedUser1.setUserProperty("cherry", "red");
        expectedUser1.setPasswordHash("qwerty");
        // User Hillary Clinton
        final ExternalUser expectedUser2 = new ExternalUser("hclinton", "Hillary Clinton", "hclinton@example.com");
        expectedUser2.setId("10062");
        expectedUser2.setUserProperty("banana", "yellow");
        expectedUser2.setUserProperty("cherry", "none");
        expectedUser2.setPasswordHash("asdf");

        UserMapper userMapper = new UserMapper(null);

        // Create our RegisterUserMapperHandler under test.
        RegisterUserMapperHandler registerUserMapperHandler = new RegisterUserMapperHandler(userMapper);

        // Send ExternalEntity Mappings for the Users
        //     <ExternalEntity id="10001" name="wilma" type="com.atlassian.jira.user.OfbizExternalEntityStore"/>
        registerUserMapperHandler.handleEntity("ExternalEntity", EasyMap.build("id", "10001", "name", "wanchor", "type", "com.atlassian.jira.user.OfbizExternalEntityStore"));
        registerUserMapperHandler.handleEntity("ExternalEntity", EasyMap.build("id", "10002", "name", "hclinton", "type", "com.atlassian.jira.user.OfbizExternalEntityStore"));
        // <OSPropertyEntry id="10153" entityName="ExternalEntity" entityId="10060" propertyKey="jira.meta.banana" type="5"/>
        registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10153", "entityName", "ExternalEntity", "entityId", "10001", "propertyKey", "jira.meta.banana"));
        registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10154", "entityName", "ExternalEntity", "entityId", "10001", "propertyKey", "jira.meta.cherry"));
        registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10157", "entityName", "ExternalEntity", "entityId", "10002", "propertyKey", "jira.meta.banana"));
        registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10158", "entityName", "ExternalEntity", "entityId", "10002", "propertyKey", "jira.meta.cherry"));

        // Send some other properties that should be ignored by RegisterUserMapperHandler
        registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10161", "entityName", "Rubbish", "entityId", "10001", "propertyKey", "fullName"));
        registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10162", "entityName", "Rubbish", "entityId", "10001", "propertyKey", "email"));

        // Now send the value nodes for above OSPropertyEntrys
        //<OSPropertyString id="10153" value="green"/>
        registerUserMapperHandler.handleEntity("OSPropertyString", EasyMap.build("id", "10153", "value", "green"));
        registerUserMapperHandler.handleEntity("OSPropertyString", EasyMap.build("id", "10154", "value", "red"));
        registerUserMapperHandler.handleEntity("OSPropertyString", EasyMap.build("id", "10157", "value", "yellow"));
        registerUserMapperHandler.handleEntity("OSPropertyString", EasyMap.build("id", "10158", "value", "none"));
        // And the "trick" values
        registerUserMapperHandler.handleEntity("OSPropertyString", EasyMap.build("id", "10161", "value", "Rubbish"));
        registerUserMapperHandler.handleEntity("OSPropertyString", EasyMap.build("id", "10162", "value", "Rubbish"));

        // Finally we send the User entries
        registerUserMapperHandler.handleEntity("User", EasyMap.build("id", "10060", "userName", "wanchor", "displayName", "William Anchor", "firstName", "William", "lastName", "Anchor", "emailAddress", "wanchor@example.com", "credential", "qwerty"));
        registerUserMapperHandler.handleEntity("User", EasyMap.build("id", "10062", "userName", "hclinton", "displayName", "Hillary Clinton", "firstName", "Hillary", "lastName", "Clinton", "emailAddress", "hclinton@example.com", "credential", "asdf"));

        // All done - let the mock do its assertions.
        assertEquals(2, userMapper.getRegisteredOldIds().size());
        assertEquals(expectedUser1, userMapper.getExternalUser("wanchor"));
        assertEquals(expectedUser2, userMapper.getExternalUser("hclinton"));
    }

    @Test
    public void testBadDataOSPropertyEntry()
    {
        RegisterUserMapperHandler registerUserMapperHandler = new RegisterUserMapperHandler(null);
        try
        {
            // <OSPropertyEntry id="10153" entityName="ExternalEntity" entityId="10060" propertyKey="jira.meta.Favourite Colour" type="5"/>
            // Missing ID
            registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("entityName", "ExternalEntity", "entityId", "10060", "propertyKey", "jira.meta.banana"));
            fail("ParseException expected.");
        }
        catch (ParseException e)
        {
            // Expected ParseException
            assertEquals("Missing 'id' field for OSPropertyEntry.", e.getMessage());
        }
        try
        {
            // <OSPropertyEntry id="10153" entityName="ExternalEntity" entityId="10060" propertyKey="jira.meta.Favourite Colour" type="5"/>
            // Missing property key
            registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10153", "entityName", "ExternalEntity", "entityId", "10060", "key", "jira.meta.banana"));
            fail("ParseException expected.");
        }
        catch (ParseException e)
        {
            // Expected ParseException
            assertEquals("Missing propertyKey from OSPropertyEntry id = 10153", e.getMessage());
        }
        try
        {
            // <OSPropertyEntry id="10153" entityName="ExternalEntity" entityId="10060" propertyKey="jira.meta.Favourite Colour" type="5"/>
            // Missing entityId
            registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10153", "entityName", "ExternalEntity", "entityIdXXX", "10060", "propertyKey", "jira.meta.banana"));
            fail("ParseException expected.");
        }
        catch (ParseException e)
        {
            // Expected ParseException
            assertEquals("Missing entityId from OSPropertyEntry id = 10153", e.getMessage());
        }

    }

    @Test
    public void testBadDataUser()
    {
        final UserMapper userMapper = new UserMapper(null);
        RegisterUserMapperHandler registerUserMapperHandler = new RegisterUserMapperHandler(userMapper);

        try
        {
            //<User directoryId="10000" userName="wilma" lowerUserName="hclinton" credential="querty"/>
            registerUserMapperHandler.handleEntity("User", EasyMap.build("userName", "hclinton", "credential", "qwerty"));
            // Missing id
            fail("ParseException expected.");
        }
        catch (ParseException e)
        {
            assertEquals("Missing 'id' field for User entry.", e.getMessage());
        }
        try
        {
            //<User directoryId="10000" id="10060" lowerUserName="hclinton" credential="querty"/>
            registerUserMapperHandler.handleEntity("User", EasyMap.build("id", "10060", "credential", "qwerty", "displayName", "William Anchor", "emailAddress", "wanchor@example.com"));
            // Missing name
            assertEquals(0, userMapper.getRegisteredOldIds().size());
        }
        catch (ParseException e)
        {
            fail("We shouldn't care if the user name is missing");
        }
        try
        {
            //<User directoryId="10000" id="10060" userName="wsailor" />
            registerUserMapperHandler.handleEntity("User", EasyMap.build("id", "10060", "userName", "wsailor"));
            // Missing password - we don't care
            assertEquals(1, userMapper.getRegisteredOldIds().size());
        }
        catch (ParseException e)
        {
            fail("We shouldn't care if password is missing.");
        }
    }

    @Test
    public void testIgnorePluginCustomProperties() throws Exception
    {
        // Set up the expected Users to be added.
        // Note that at present, we expect password to be ignored.
        // User Willy
        final ExternalUser expectedUser1 = new ExternalUser("wilma", "Wilma Flinstone", "wilma@example.com");
        expectedUser1.setId("10011");
        expectedUser1.setUserProperty("banana", "green");
        expectedUser1.setPasswordHash("qwerty");

        UserMapper userMapper = new UserMapper(null);

        // Create our RegisterUserMapperHandler under test.
        RegisterUserMapperHandler registerUserMapperHandler = new RegisterUserMapperHandler(userMapper);
        registerUserMapperHandler.startDocument();
        // Send ExternalEntity Mapping for wilma
        //     <ExternalEntity id="10001" name="wilma" type="com.atlassian.jira.user.OfbizExternalEntityStore"/>
        registerUserMapperHandler.handleEntity("ExternalEntity", EasyMap.build("id", "10001", "name", "wilma", "type", "com.atlassian.jira.user.OfbizExternalEntityStore"));
        // Send Property keys
        registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10070", "entityName", "ExternalEntity", "entityId", "10001", "propertyKey", "jira.meta.banana", "type", "5"));
        registerUserMapperHandler.handleEntity("OSPropertyEntry", EasyMap.build("id", "10071", "entityName", "ExternalEntity", "entityId", "10001", "propertyKey", "cherrypopping", "type", "5"));
        // Send Property Values
        registerUserMapperHandler.handleEntity("OSPropertyString", EasyMap.build("id", "10070", "value", "green"));
        registerUserMapperHandler.handleEntity("OSPropertyString", EasyMap.build("id", "10070", "value", "red"));
        // Send the User entry (Now with Crowd, Full Name and email address are no longer OSProperties :)
        // <User id="10011" directoryId="1" userName="wilma" lowerUserName="wilma" active="1" createdDate="2010-05-31 12:05:58.0" updatedDate="2010-05-31 12:05:58.0" displayName="Wilma Flinstone" lowerDisplayName="wilma flinstone" emailAddress="wilma@example.com" lowerEmailAddress="wilma@example.com" credential="jEUdB0f3hSbXaKrvGQrOull9LQ74qN9hVSjNpi5cicihae2b8IIBATUtHNWwyEBCopdv9Uqm5hn+5rpGVJC0Gg=="/>
        registerUserMapperHandler.handleEntity("User", EasyMap.build("id", "10011", "userName", "wilma", "displayName", "Wilma Flinstone", "emailAddress", "wilma@example.com", "credential", "qwerty"));

        registerUserMapperHandler.endDocument();

        // All done - let the mock do its assertions.
        assertEquals(1, userMapper.getRegisteredOldIds().size());
        assertEquals(expectedUser1, userMapper.getExternalUser("wilma"));
    }
}
