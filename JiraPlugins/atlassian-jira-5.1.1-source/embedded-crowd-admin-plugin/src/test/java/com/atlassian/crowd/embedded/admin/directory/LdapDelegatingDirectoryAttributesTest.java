package com.atlassian.crowd.embedded.admin.directory;

import com.atlassian.crowd.directory.DelegatedAuthenticationDirectory;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class LdapDelegatingDirectoryAttributesTest
{

    @Test
    public void testToAttributesMap() throws Exception
    {
        LdapDelegatingDirectoryAttributes ldapDelegatingDirectoryAttributes = new LdapDelegatingDirectoryAttributes();
        ldapDelegatingDirectoryAttributes.setCreateUserOnAuth(true);
        ldapDelegatingDirectoryAttributes.setDelegatedToClass("class");
        ldapDelegatingDirectoryAttributes.setLdapAutoAddGroups("groups");
        ldapDelegatingDirectoryAttributes.setLdapBasedn("dn");
        ldapDelegatingDirectoryAttributes.setLdapGroupDn("group-dn");
        ldapDelegatingDirectoryAttributes.setLdapGroupDescription("desc");
        ldapDelegatingDirectoryAttributes.setLdapGroupFilter("group-filter");
        ldapDelegatingDirectoryAttributes.setLdapGroupName("group-name");
        ldapDelegatingDirectoryAttributes.setLdapGroupObjectclass("group-obj-class");
        ldapDelegatingDirectoryAttributes.setLdapGroupUsernames("usernames");
        ldapDelegatingDirectoryAttributes.setLdapPassword("password");
        ldapDelegatingDirectoryAttributes.setLdapUrl("url");
        ldapDelegatingDirectoryAttributes.setLdapUserDisplayname("display-name");
        ldapDelegatingDirectoryAttributes.setLdapUserdn("userdn");
        ldapDelegatingDirectoryAttributes.setLdapUserDn("userDn");
        ldapDelegatingDirectoryAttributes.setLdapUserEmail("email");
        ldapDelegatingDirectoryAttributes.setLdapUserFilter("user-filter");
        ldapDelegatingDirectoryAttributes.setLdapUserFirstname("firstname");
        ldapDelegatingDirectoryAttributes.setLdapUserGroup("user-group");
        ldapDelegatingDirectoryAttributes.setLdapUserLastname("lastname");
        ldapDelegatingDirectoryAttributes.setLdapUsermembershipUse(true);
        ldapDelegatingDirectoryAttributes.setLdapUsermembershipUseForGroups(true);
        ldapDelegatingDirectoryAttributes.setLdapUserObjectclass("user-obj-class");
        ldapDelegatingDirectoryAttributes.setLdapUserUsername("username");
        ldapDelegatingDirectoryAttributes.setLdapUserUsernameRdn("rdn");
        ldapDelegatingDirectoryAttributes.setSynchroniseGroupMemberships(true);
        ldapDelegatingDirectoryAttributes.setUpdateUserOnAuth(true);
        ldapDelegatingDirectoryAttributes.setLdapPagedresults(true);
        ldapDelegatingDirectoryAttributes.setLdapPagedresultsSize("5");
        ldapDelegatingDirectoryAttributes.setLdapReferral(true);

        Map<String, String> map = ldapDelegatingDirectoryAttributes.toAttributesMap();

        assertEquals(map.size(), 30);
        assertEquals(Boolean.valueOf(map.get(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH)), true);
        assertEquals(map.get(DelegatedAuthenticationDirectory.ATTRIBUTE_LDAP_DIRECTORY_CLASS), "class");
        assertEquals(map.get(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS), "groups");
        assertEquals(map.get(LDAPPropertiesMapper.LDAP_BASEDN_KEY), "dn");
        assertEquals(map.get(LDAPPropertiesMapper.GROUP_DN_ADDITION), "group-dn");
        assertEquals(map.get(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY), "desc");
        assertEquals(map.get(LDAPPropertiesMapper.GROUP_OBJECTFILTER_KEY), "group-filter");
        assertEquals(map.get(LDAPPropertiesMapper.GROUP_NAME_KEY), "group-name");
        assertEquals(map.get(LDAPPropertiesMapper.GROUP_OBJECTCLASS_KEY), "group-obj-class");
        assertEquals(map.get(LDAPPropertiesMapper.GROUP_USERNAMES_KEY), "usernames");
        assertEquals(map.get(LDAPPropertiesMapper.LDAP_PASSWORD_KEY), "password");
        assertEquals(map.get(LDAPPropertiesMapper.LDAP_URL_KEY), "url");
        assertEquals(map.get(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY), "display-name");
        assertEquals(map.get(LDAPPropertiesMapper.LDAP_USERDN_KEY), "userdn");
        assertEquals(map.get(LDAPPropertiesMapper.USER_DN_ADDITION), "userDn");
        assertEquals(map.get(LDAPPropertiesMapper.USER_EMAIL_KEY), "email");
        assertEquals(map.get(LDAPPropertiesMapper.USER_OBJECTFILTER_KEY), "user-filter");
        assertEquals(map.get(LDAPPropertiesMapper.USER_FIRSTNAME_KEY), "firstname");
        assertEquals(map.get(LDAPPropertiesMapper.USER_GROUP_KEY), "user-group");
        assertEquals(map.get(LDAPPropertiesMapper.USER_LASTNAME_KEY), "lastname");
        assertEquals(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE)), true);
        assertEquals(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE_FOR_GROUP_MEMBERSHIP)), true);
        assertEquals(map.get(LDAPPropertiesMapper.USER_OBJECTCLASS_KEY), "user-obj-class");
        assertEquals(map.get(LDAPPropertiesMapper.USER_USERNAME_KEY), "username");
        assertEquals(map.get(LDAPPropertiesMapper.USER_USERNAME_RDN_KEY), "rdn");
        assertEquals(Boolean.valueOf(map.get(DelegatedAuthenticationDirectory.ATTRIBUTE_KEY_IMPORT_GROUPS)), true);
        assertEquals(Boolean.valueOf(map.get(DelegatedAuthenticationDirectory.ATTRIBUTE_UPDATE_USER_ON_AUTH)), true);
        assertEquals(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_PAGEDRESULTS_KEY)), true);
        assertEquals(map.get(LDAPPropertiesMapper.LDAP_PAGEDRESULTS_SIZE), "5");
        assertEquals(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_REFERRAL_KEY)), true);
    }
    
    @Test
    public void testToAttributesMapIncludesAutoAddGroups() throws Exception
    {
        LdapDelegatingDirectoryAttributes attrs = new LdapDelegatingDirectoryAttributes();

        attrs.setLdapAutoAddGroups("test-group");

        Map<String,String> map;
        attrs.setLdapAutoAddGroups("test-group");
        map = attrs.toAttributesMap();
        assertEquals("test-group", map.get(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));

        attrs.setLdapAutoAddGroups("test-group1|test-group2|test-group1");
        map = attrs.toAttributesMap();
        assertEquals("No conversion or normalising is performed",
                "test-group1|test-group2|test-group1", map.get(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));
    }

    @Test
    public void testFromAttributesMapIncludesAutoAddGroups() throws Exception
    {
        Map<String, String> m;
        LdapDelegatingDirectoryAttributes attrs;

        m = Collections.singletonMap(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS, "test-group");
        attrs = LdapDelegatingDirectoryAttributes.fromAttributesMap(m);
        assertEquals("test-group", attrs.getLdapAutoAddGroups());

        m = Collections.singletonMap(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS, "test-group1|test-group2|test-group1");
        attrs = LdapDelegatingDirectoryAttributes.fromAttributesMap(m);
        assertEquals("No conversion or normalising is performed",
                "test-group1|test-group2|test-group1", attrs.getLdapAutoAddGroups());
    }
}
