package com.atlassian.crowd.directory.ldap.validator;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class GroupRoleDnOverlapValidatorTest extends TestCase
{
    private static final String roleDNempty = "";
    private static final String groupDNempty = "";

    private static final String roleDN1 = "ou=admin";
    private static final String groupDN1 = "ou=administrators";

    private static final String roleDN2 = "ou=admin,ou=internal";
    private static final String groupDN2 = "ou=internal,ou=admin";


    private GroupRoleDnOverlapValidator validator;
    private Directory directory;

    public void setUp()
    {
        validator = new GroupRoleDnOverlapValidator();
        directory = mock(DirectoryImpl.class);
        // Group/Role DN Overlapping can only occur for Connector diretories
        when(directory.getType()).thenReturn(DirectoryType.CONNECTOR);
    }


    public void testGetErrorBothBlankDNRolesEnabled() throws Exception
    {
        when(directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION)).thenReturn(roleDNempty);
        when(directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION)).thenReturn(groupDNempty);
        when(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED)).thenReturn("false");

        // Invalid: Roles enabled - DNs overlap
        String errorMessage = validator.getError(directory);
        assertNotNull(errorMessage);
    }

    public void testGetErrorBothBlankDNRolesDisabled() throws Exception
    {
        when(directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION)).thenReturn(roleDNempty);
        when(directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION)).thenReturn(groupDNempty);
        when(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED)).thenReturn("true");

        // Valid: roles disabled so rolesDN disregarded
        String errorMessage = validator.getError(directory);
        assertNull(errorMessage);
    }

    public void testGetErrorIdenticalDNRolesEnabled() throws Exception
    {
        when(directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION)).thenReturn(groupDN1);
        when(directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION)).thenReturn(groupDN1);
        when(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED)).thenReturn("false");

        // Invalid: Roles enabled - DNs overlap
        String errorMessage = validator.getError(directory);
        assertNotNull(errorMessage);
    }

    public void testGetErrorIdenticalDNRolesDisabled() throws Exception
    {
        when(directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION)).thenReturn(groupDN1);
        when(directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION)).thenReturn(groupDN1);
        when(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED)).thenReturn("true");

        // Valid: roles disabled so rolesDN disregarded
        String errorMessage = validator.getError(directory);
        assertNull(errorMessage);
    }

    public void testGetErrorPartialOverlapRolesEnabled() throws Exception
    {
        when(directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION)).thenReturn(roleDN1);
        when(directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION)).thenReturn(roleDN2);
        when(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED)).thenReturn("false");

        // Invalid: Roles enabled - DNs overlap
        String errorMessage = validator.getError(directory);
        assertNotNull(errorMessage);


        when(directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION)).thenReturn(roleDN2);
        when(directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION)).thenReturn(roleDN1);
        when(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED)).thenReturn("false");

        // Invalid: Roles enabled - DNs overlap
        errorMessage = validator.getError(directory);
        assertNotNull(errorMessage);
    }

    public void testGetErrorPartialOverlapRolesDisabled() throws Exception
    {
        when(directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION)).thenReturn(roleDN1);
        when(directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION)).thenReturn(roleDN2);
        when(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED)).thenReturn("true");

        // Valid: roles disabled so rolesDN disregarded
        String errorMessage = validator.getError(directory);
        assertNull(errorMessage);


        when(directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION)).thenReturn(roleDN2);
        when(directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION)).thenReturn(roleDN1);
        when(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED)).thenReturn("true");

        // Valid: roles disabled so rolesDN disregarded
        errorMessage = validator.getError(directory);
        assertNull(errorMessage);
    }

    public void testGetErrorNoOverlapRolesEnabled() throws Exception
    {
        when(directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION)).thenReturn(roleDN1);
        when(directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION)).thenReturn(groupDN1);
        when(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED)).thenReturn("false");

        // Valid: Roles enabled but no DN overlap
        String errorMessage = validator.getError(directory);
        assertNull(errorMessage);
    }

    public void testGetErrorNoOverlapRolesDisabled() throws Exception
    {
        when(directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION)).thenReturn(roleDN1);
        when(directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION)).thenReturn(groupDN1);
        when(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED)).thenReturn("true");

        // Valid: roles disabled so rolesDN disregarded
        String errorMessage = validator.getError(directory);
        assertNull(errorMessage);
    }
}
