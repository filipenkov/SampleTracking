package com.atlassian.crowd.directory.ldap.mapper.entity;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplateWithAttributes;
import com.atlassian.crowd.model.group.GroupType;
import junit.framework.TestCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.ldap.UncategorizedLdapException;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;


public class LDAPGroupAttributesMapperTest extends TestCase
{
    private LDAPPropertiesMapper mockLdapPropertiesMapper;

    private LDAPGroupAttributesMapper groupMapper;
    private LDAPGroupAttributesMapper legacyRoleMapper;

    private static final long DIRECTORY_ID = 1;


    protected void setUp()
    {
        // mock up properties ldappropertiesMapper
        mockLdapPropertiesMapper = mock(LDAPPropertiesMapper.class);

        when(mockLdapPropertiesMapper.getObjectClassAttribute()).thenReturn("objectClass");

        when(mockLdapPropertiesMapper.getGroupDescriptionAttribute()).thenReturn(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY);
        when(mockLdapPropertiesMapper.getGroupFilter()).thenReturn(LDAPPropertiesMapper.GROUP_OBJECTFILTER_KEY);
        when(mockLdapPropertiesMapper.getGroupMemberAttribute()).thenReturn(LDAPPropertiesMapper.GROUP_USERNAMES_KEY);
        when(mockLdapPropertiesMapper.getGroupNameAttribute()).thenReturn(LDAPPropertiesMapper.GROUP_NAME_KEY);
        when(mockLdapPropertiesMapper.getGroupObjectClass()).thenReturn(LDAPPropertiesMapper.GROUP_OBJECTCLASS_KEY);

        when(mockLdapPropertiesMapper.getRoleDescriptionAttribute()).thenReturn(LDAPPropertiesMapper.ROLE_DESCRIPTION_KEY);
        when(mockLdapPropertiesMapper.getRoleFilter()).thenReturn(LDAPPropertiesMapper.ROLE_OBJECTFILTER_KEY);
        when(mockLdapPropertiesMapper.getRoleMemberAttribute()).thenReturn(LDAPPropertiesMapper.ROLE_USERNAMES_KEY);
        when(mockLdapPropertiesMapper.getRoleNameAttribute()).thenReturn(LDAPPropertiesMapper.ROLE_NAME_KEY);
        when(mockLdapPropertiesMapper.getRoleObjectClass()).thenReturn(LDAPPropertiesMapper.ROLE_OBJECTCLASS_KEY);


        groupMapper = new LDAPGroupAttributesMapper(DIRECTORY_ID, GroupType.GROUP, mockLdapPropertiesMapper);
        legacyRoleMapper = new LDAPGroupAttributesMapper(DIRECTORY_ID, GroupType.LEGACY_ROLE, mockLdapPropertiesMapper);
    }

    protected void tearDown()
    {
        groupMapper = null;
    }

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

    // TEST GROUP STUFF
    public void testMapAttributesFromGroupNullGroup()
    {
        try
        {
            groupMapper.mapAttributesFromGroup(null);

            fail("Should have thrown UncategorizedLdapException on null input");
        }
        catch (UncategorizedLdapException e)
        {
            // expected
        }
    }


    public void testMapAttributesFromGroupWithDescription() throws NamingException
    {
        // create group
        GroupTemplateWithAttributes group = new GroupTemplateWithAttributes("Chocolate", DIRECTORY_ID, GroupType.GROUP);
        group.setDescription("Chocoholics group");

        Attributes attributes = groupMapper.mapAttributesFromGroup(group);

        assertNotNull(attributes);
        assertEquals("Name should be same", group.getName(), getAttribute(LDAPPropertiesMapper.GROUP_NAME_KEY, attributes));
        assertEquals("Description should be same", group.getDescription(), getAttribute(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY, attributes));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(attributes);
    }

    public void testMapAttributesFromGroupWithEmptyDescription() throws NamingException
    {
        // create group
        GroupTemplateWithAttributes group = new GroupTemplateWithAttributes("Chocolate", DIRECTORY_ID, GroupType.GROUP);
        group.setDescription("");

        Attributes attributes = groupMapper.mapAttributesFromGroup(group);

        assertNotNull(attributes);
        assertNull(attributes.get(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(attributes);
    }

    public void testMapAttributesFromGroupNoDescription() throws NamingException
    {
        // create group
        GroupTemplateWithAttributes group = new GroupTemplateWithAttributes("Chocolate", DIRECTORY_ID, GroupType.GROUP);

        Attributes attributes = groupMapper.mapAttributesFromGroup(group);

        assertNotNull(attributes);
        assertEquals("Name should be same", group.getName(), getAttribute(LDAPPropertiesMapper.GROUP_NAME_KEY, attributes));
        assertNull(attributes.get(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(attributes);
    }

    public void testMapGroupFromAttributesNullAttributes()
    {
        try
        {
            groupMapper.mapGroupFromAttributes(null);

            fail("Should have thrown UncategorizedLdapException on null input");
        }
        catch (UncategorizedLdapException e)
        {
            // expected
        }
    }


    public void testMapFromAttributesGroup() throws NamingException
    {
        Attributes attributes = new BasicAttributes();
        attributes.put(LDAPPropertiesMapper.GROUP_NAME_KEY, "Chocolate");
        attributes.put(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY, "Chocoholics Anonymous");

        Group group = (Group) groupMapper.mapFromAttributes(attributes);

        assertNotNull(group);
        assertEquals("Name should be same", getAttribute(LDAPPropertiesMapper.GROUP_NAME_KEY, attributes), group.getName());
        assertEquals("Description should be same", getAttribute(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY, attributes), group.getDescription());
        assertTrue(group.isActive()); // LDAP directories are active
        assertEquals(GroupType.GROUP, group.getType());
    }

    public void testMapGroupFromAttributesWithDescription() throws NamingException
    {
        Attributes attributes = new BasicAttributes();
        attributes.put(LDAPPropertiesMapper.GROUP_NAME_KEY, "Chocolate");
        attributes.put(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY, "Chocoholics Anonymous");

        Group group = groupMapper.mapGroupFromAttributes(attributes);

        assertNotNull(group);
        assertEquals("Name should be same", getAttribute(LDAPPropertiesMapper.GROUP_NAME_KEY, attributes), group.getName());
        assertEquals("Description should be same", getAttribute(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY, attributes), group.getDescription());
        assertTrue(group.isActive()); // LDAP directories are active
        assertEquals(GroupType.GROUP, group.getType());
    }

    public void testMapGroupFromAttributesNoDescription() throws NamingException
    {
        Attributes attributes = new BasicAttributes();
        attributes.put(LDAPPropertiesMapper.GROUP_NAME_KEY, "Chocolate");

        Group group = groupMapper.mapGroupFromAttributes(attributes);

        assertNotNull(group);
        assertEquals("Name should be same", getAttribute(LDAPPropertiesMapper.GROUP_NAME_KEY, attributes), group.getName());
        assertNull(group.getDescription());
        assertTrue(group.isActive()); // LDAP directories are active
        assertEquals(GroupType.GROUP, group.getType());
    }

    public void testMapGroupFromAttributesNoName() throws NamingException
    {
        Attributes attributes = new BasicAttributes();
        attributes.put(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY, "Chocoholics Anonymous");

        try
        {
            groupMapper.mapGroupFromAttributes(attributes);
            fail("UncategorizedLdapException expected");
        }
        catch (UncategorizedLdapException e)
        {
            // expected
        }
    }


    // TEST LEGACY ROLE STUFF
    public void testMapAttributesFromRoleNullRole()
    {
        try
        {
            legacyRoleMapper.mapAttributesFromGroup(null);

            fail("Should have thrown UncategorizedLdapException on null input");
        }
        catch (UncategorizedLdapException e)
        {
            // expected
        }
    }


    public void testMapAttributesFromRoleWithDescription() throws NamingException
    {
        // create group
        GroupTemplateWithAttributes group = new GroupTemplateWithAttributes("Chocolate", DIRECTORY_ID, GroupType.LEGACY_ROLE);
        group.setDescription("Chocoholics group");

        Attributes attributes = legacyRoleMapper.mapAttributesFromGroup(group);

        assertNotNull(attributes);
        assertEquals("Name should be same", group.getName(), getAttribute(LDAPPropertiesMapper.ROLE_NAME_KEY, attributes));
        assertEquals("Description should be same", group.getDescription(), getAttribute(LDAPPropertiesMapper.ROLE_DESCRIPTION_KEY, attributes));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(attributes);
    }

    public void testMapAttributesFromRoleNoDescription() throws NamingException
    {
        // create group
        GroupTemplateWithAttributes group = new GroupTemplateWithAttributes("Chocolate", DIRECTORY_ID, GroupType.LEGACY_ROLE);

        Attributes attributes = legacyRoleMapper.mapAttributesFromGroup(group);

        assertNotNull(attributes);
        assertEquals("Name should be same", group.getName(), getAttribute(LDAPPropertiesMapper.ROLE_NAME_KEY, attributes));
        assertNull(attributes.get(LDAPPropertiesMapper.ROLE_DESCRIPTION_KEY));
        AttributesMapperTestUtils.assertAttributesValuesNeverEmpty(attributes);
    }

    public void testMapRoleFromAttributesNullAttributes()
    {
        try
        {
            legacyRoleMapper.mapGroupFromAttributes(null);

            fail("Should have thrown UncategorizedLdapException on null input");
        }
        catch (UncategorizedLdapException e)
        {
            // expected
        }
    }

    public void testMapFromAttributesRole() throws NamingException
    {
        Attributes attributes = new BasicAttributes();
        attributes.put(LDAPPropertiesMapper.ROLE_NAME_KEY, "Chocolate");
        attributes.put(LDAPPropertiesMapper.ROLE_DESCRIPTION_KEY, "Chocoholics Anonymous");

        Group group = (Group) legacyRoleMapper.mapFromAttributes(attributes);

        assertNotNull(group);
        assertEquals("Name should be same", getAttribute(LDAPPropertiesMapper.ROLE_NAME_KEY, attributes), group.getName());
        assertEquals("Description should be same", getAttribute(LDAPPropertiesMapper.ROLE_DESCRIPTION_KEY, attributes), group.getDescription());
        assertTrue(group.isActive()); // LDAP directories are active
        assertEquals(GroupType.LEGACY_ROLE, group.getType());
    }


    public void testMapRoleFromAttributesWithDescription() throws NamingException
    {
        Attributes attributes = new BasicAttributes();
        attributes.put(LDAPPropertiesMapper.ROLE_NAME_KEY, "Chocolate");
        attributes.put(LDAPPropertiesMapper.ROLE_DESCRIPTION_KEY, "Chocoholics Anonymous");

        Group group = legacyRoleMapper.mapGroupFromAttributes(attributes);

        assertNotNull(group);
        assertEquals("Name should be same", getAttribute(LDAPPropertiesMapper.ROLE_NAME_KEY, attributes), group.getName());
        assertEquals("Description should be same", getAttribute(LDAPPropertiesMapper.ROLE_DESCRIPTION_KEY, attributes), group.getDescription());
        assertTrue(group.isActive()); // LDAP directories are active
        assertEquals(GroupType.LEGACY_ROLE, group.getType());
    }

    public void testMapRoleFromAttributesNoDescription() throws NamingException
    {
        Attributes attributes = new BasicAttributes();
        attributes.put(LDAPPropertiesMapper.ROLE_NAME_KEY, "Chocolate");

        Group group = legacyRoleMapper.mapGroupFromAttributes(attributes);

        assertNotNull(group);
        assertEquals("Name should be same", getAttribute(LDAPPropertiesMapper.ROLE_NAME_KEY, attributes), group.getName());
        assertNull(group.getDescription());
        assertTrue(group.isActive()); // LDAP directories are active
        assertEquals(GroupType.LEGACY_ROLE, group.getType());
    }

    public void testMapRoleFromAttributesNoName() throws NamingException
    {
        Attributes attributes = new BasicAttributes();
        attributes.put(LDAPPropertiesMapper.ROLE_DESCRIPTION_KEY, "Chocoholics Anonymous");

        try
        {
            legacyRoleMapper.mapGroupFromAttributes(attributes);
            fail("UncategorizedLdapException expected");
        }
        catch (UncategorizedLdapException e)
        {
            // expected
        }
    }
}