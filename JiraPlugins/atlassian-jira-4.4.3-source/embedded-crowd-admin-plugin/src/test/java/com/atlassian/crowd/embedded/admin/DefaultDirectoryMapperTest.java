package com.atlassian.crowd.embedded.admin;

import com.atlassian.crowd.directory.DelegatedAuthenticationDirectory;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.embedded.admin.delegatingldap.DelegatingLdapDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.ldap.LdapDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.util.MapBuilder;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PermissionOption;
import com.atlassian.crowd.embedded.impl.ImmutableDirectory;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DefaultDirectoryMapperTest
{
    private DirectoryMapper directoryMapper;
    private LdapDirectoryConfiguration configuration;
    private ImmutableDirectory.Builder builder;

    @Before
    public void setUp() throws Exception
    {
        directoryMapper = new DefaultDirectoryMapper();
        configuration = new LdapDirectoryConfiguration();
        builder = ImmutableDirectory.newBuilder();
    }

    @Test
    public void testPagedResultsIsMappedToDirectory() throws Exception
    {
        configuration.setLdapPagedresults(false);
        Directory nonPagedDirectory = directoryMapper.buildLdapDirectory(configuration);
        assertEquals("false", nonPagedDirectory.getAttributes().get(LDAPPropertiesMapper.LDAP_PAGEDRESULTS_KEY));

        configuration.setLdapPagedresults(true);
        Directory pagedDirectory = directoryMapper.buildLdapDirectory(configuration);
        assertEquals("true", pagedDirectory.getAttributes().get(LDAPPropertiesMapper.LDAP_PAGEDRESULTS_KEY));
    }

    @Test
    public void testPagedResultsIsMappedToConfiguration() throws Exception
    {
        builder.setAttributes(MapBuilder.build(LDAPPropertiesMapper.LDAP_PAGEDRESULTS_KEY, "false"));
        Directory nonPagedDirectory = builder.toDirectory();
        LdapDirectoryConfiguration nonPagedConfiguration = directoryMapper.toLdapConfiguration(nonPagedDirectory);
        assertFalse(nonPagedConfiguration.isLdapPagedresults());

        builder.setAttributes(MapBuilder.build(LDAPPropertiesMapper.LDAP_PAGEDRESULTS_KEY, "true"));
        Directory pagedDirectory = builder.toDirectory();
        LdapDirectoryConfiguration pagedConfiguration = directoryMapper.toLdapConfiguration(pagedDirectory);
        assertTrue("Should be paged, but isn't", pagedConfiguration.isLdapPagedresults());
    }
    
    @Test
    public void testLocalGroupsIsMappedToDirectory() throws Exception
    {
        configuration.setLdapPermissionOption(PermissionOption.READ_ONLY);
        Directory nonLocalGroupsDirectory = directoryMapper.buildLdapDirectory(configuration);
        assertEquals("false", nonLocalGroupsDirectory.getAttributes().get(LDAPPropertiesMapper.LOCAL_GROUPS));

        configuration.setLdapPermissionOption(PermissionOption.READ_ONLY_LOCAL_GROUPS);
        Directory localGroupsDirectory = directoryMapper.buildLdapDirectory(configuration);
        assertEquals("true", localGroupsDirectory.getAttributes().get(LDAPPropertiesMapper.LOCAL_GROUPS));
    }
    
    @Test
    public void testLocalGroupsIsMappedToConfiguration() throws Exception
    {
        builder.setAttributes(MapBuilder.build(LDAPPropertiesMapper.LOCAL_GROUPS, "false"));
        Directory noLocalGroupsDirectory = builder.toDirectory();
        LdapDirectoryConfiguration noLocalGroupsConfiguration = directoryMapper.toLdapConfiguration(noLocalGroupsDirectory);
        assertEquals(PermissionOption.READ_ONLY, noLocalGroupsConfiguration.getLdapPermissionOption());

        builder.setAttributes(MapBuilder.build(LDAPPropertiesMapper.LOCAL_GROUPS, "true"));
        Directory localGroupsDirectory = builder.toDirectory();
        LdapDirectoryConfiguration localGroupsConfiguration = directoryMapper.toLdapConfiguration(localGroupsDirectory);
        assertEquals(PermissionOption.READ_ONLY_LOCAL_GROUPS, localGroupsConfiguration.getLdapPermissionOption());
    }

    @Test
    public void testNestedGroupsIsMappedToDirectory() throws Exception
    {
        configuration.setNestedGroupsEnabled(true);
        assertFalse(configuration.isLdapNestedgroupsDisabled());
        Directory nonNestedDirectory = directoryMapper.buildLdapDirectory(configuration);
        assertEquals("false", nonNestedDirectory.getAttributes().get(LDAPPropertiesMapper.LDAP_NESTED_GROUPS_DISABLED));

        configuration.setNestedGroupsEnabled(true);
        assertFalse(configuration.isLdapNestedgroupsDisabled());
        Directory nestedDirectory = directoryMapper.buildLdapDirectory(configuration);
        assertEquals("false", nestedDirectory.getAttributes().get(LDAPPropertiesMapper.LDAP_NESTED_GROUPS_DISABLED));
    }

    @Test
    public void testNestedGroupsIsMappedToConfiguration() throws Exception
    {
        builder.setAttributes(MapBuilder.build(LDAPPropertiesMapper.LDAP_NESTED_GROUPS_DISABLED, "true"));
        Directory nonNestedDirectory = builder.toDirectory();
        LdapDirectoryConfiguration nonNestedConfiguration = directoryMapper.toLdapConfiguration(nonNestedDirectory);
        assertFalse("should have nested groups turned off", nonNestedConfiguration.isNestedGroupsEnabled());

        builder.setAttributes(MapBuilder.build(LDAPPropertiesMapper.LDAP_NESTED_GROUPS_DISABLED, "false"));
        Directory nestedDirectory = builder.toDirectory();
        LdapDirectoryConfiguration nestedConfiguration = directoryMapper.toLdapConfiguration(nestedDirectory);
        assertTrue("should have nested groups turned on", nestedConfiguration.isNestedGroupsEnabled());
    }

    @Test
    public void testAutoAddGroupsAreIncludedOnlyForReadOnlyLocalGroups()
    {
        LdapDirectoryConfiguration cfg = new LdapDirectoryConfiguration();
        cfg.setLdapAutoAddGroups("test-group");

        cfg.setLdapPermissionOption(PermissionOption.READ_ONLY_LOCAL_GROUPS);
        assertEquals("test-group", directoryMapper.buildLdapDirectory(cfg).getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));

        cfg.setLdapPermissionOption(PermissionOption.READ_ONLY);
        assertEquals("", directoryMapper.buildLdapDirectory(cfg).getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));

        cfg.setLdapPermissionOption(PermissionOption.READ_WRITE);
        assertEquals("", directoryMapper.buildLdapDirectory(cfg).getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));
    }
    
    @Test
    public void autoAddGroupsAreUsedByBuildDelegatingLdapDirectory()
    {
        DelegatingLdapDirectoryConfiguration cfg = new DelegatingLdapDirectoryConfiguration();
        cfg.setCreateUserOnAuth(true);
        cfg.setLdapAutoAddGroups("test-groups");
        
        Directory dir = directoryMapper.buildDelegatingLdapDirectory(cfg);
        
        assertEquals("test-groups", dir.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));
    }
    
    @Test
    public void autoAddGroupsAreUsedByToDelegatingLdapDirectoryConfiguration()
    {
        builder.setAttributes(Collections.singletonMap(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS, "test-groups"));
        Directory dir = builder.toDirectory();
        
        assertEquals("test-groups", dir.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));

        DelegatingLdapDirectoryConfiguration cfg = directoryMapper.toDelegatingLdapConfiguration(dir);
        
        assertEquals("test-groups", cfg.getLdapAutoAddGroups());
    }
    
    @Test
    public void autoAddGroupsAreUsedByBuildDelegatingLdapDirectoryWithMultipleGroups()
    {
        DelegatingLdapDirectoryConfiguration cfg = new DelegatingLdapDirectoryConfiguration();
        cfg.setCreateUserOnAuth(true);
        cfg.setLdapAutoAddGroups("test-group1, test-group2, test-group1");
        
        Directory dir = directoryMapper.buildDelegatingLdapDirectory(cfg);
        
        assertEquals("test-group1|test-group2", dir.getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));
    }

    @Test
    public void testAutoAddGroupsAreIncludedOnlyWhenCreateUserOnAuth()
    {
        DelegatingLdapDirectoryConfiguration cfg = new DelegatingLdapDirectoryConfiguration();
        cfg.setLdapAutoAddGroups("test-group");

        cfg.setCreateUserOnAuth(true);
        assertEquals("test-group", directoryMapper.buildDelegatingLdapDirectory(cfg).getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));

        cfg.setCreateUserOnAuth(false);
        assertEquals("", directoryMapper.buildDelegatingLdapDirectory(cfg).getValue(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));
    }

    @Test
    public void testUpdateOnAuthIsEnabledIfCreateOnAuthIsEnabled() throws Exception
    {
        DelegatingLdapDirectoryConfiguration cfg = new DelegatingLdapDirectoryConfiguration();
        cfg.setCreateUserOnAuth(true);

        Directory directory = directoryMapper.buildDelegatingLdapDirectory(cfg);
        assertEquals("true", directory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH));
        assertEquals("true", directory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_UPDATE_USER_ON_AUTH));
    }

    @Test
    public void testUpdateOnAuthIsNotEnabledIfCreateOnAuthIsNotEnabled() throws Exception
    {
        DelegatingLdapDirectoryConfiguration cfg = new DelegatingLdapDirectoryConfiguration();
        cfg.setCreateUserOnAuth(false);

        Directory directory = directoryMapper.buildDelegatingLdapDirectory(cfg);
        assertEquals("false", directory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_CREATE_USER_ON_AUTH));
        assertEquals("false", directory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_UPDATE_USER_ON_AUTH));
    }
}
