package com.atlassian.crowd.embedded.admin;

import com.atlassian.crowd.directory.DelegatedAuthenticationDirectory;
import com.atlassian.crowd.directory.InternalDirectory;
import com.atlassian.crowd.directory.RemoteCrowdDirectory;
import com.atlassian.crowd.embedded.admin.crowd.CrowdDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.crowd.CrowdPermissionOption;
import com.atlassian.crowd.embedded.admin.delegatingldap.DelegatingLdapDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.directory.CrowdDirectoryAttributes;
import com.atlassian.crowd.embedded.admin.directory.LdapDelegatingDirectoryAttributes;
import com.atlassian.crowd.embedded.admin.directory.LdapDirectoryAttributes;
import com.atlassian.crowd.embedded.admin.internal.InternalDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.jirajdbc.JiraJdbcDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.ldap.LdapDirectoryConfiguration;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PermissionOption;
import com.atlassian.crowd.embedded.impl.ImmutableDirectory;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class DefaultDirectoryMapper implements DirectoryMapper
{
    public Directory buildCrowdDirectory(CrowdDirectoryConfiguration configuration)
    {
        ImmutableDirectory.Builder builder = createBuilder();
        builder.setAllowedOperations(configuration.getCrowdPermissionOption().getAllowedOperations());
        builder.setActive(configuration.isActive());
        builder.setId(configuration.getDirectoryId());
        builder.setImplementationClass(RemoteCrowdDirectory.class.getName());
        builder.setName(configuration.getName());
        builder.setType(DirectoryType.CROWD);

        CrowdDirectoryAttributes attributes = new CrowdDirectoryAttributes();
        BeanUtils.copyProperties(configuration, attributes); // ignores attributes that don't exist in the target - perfect!
        // Convert polling interval from minutes to seconds to store in crowd
        attributes.setCrowdServerSynchroniseIntervalInSeconds(Long.toString(configuration.getCrowdServerSynchroniseIntervalInMin() * 60));
        builder.setAttributes(attributes.toAttributesMap());

        return builder.toDirectory();
    }

    public Directory buildLdapDirectory(LdapDirectoryConfiguration configuration)
    {
        ImmutableDirectory.Builder builder = createBuilder();
        builder.setActive(configuration.isActive());
        builder.setAllowedOperations(configuration.getLdapPermissionOption().getAllowedOperations());
        builder.setEncryptionType(configuration.getLdapUserEncryption());
        builder.setId(configuration.getDirectoryId());
        builder.setImplementationClass(configuration.getType());
        builder.setName(configuration.getName());
        builder.setType(DirectoryType.CONNECTOR);

        LdapDirectoryAttributes attributes = new LdapDirectoryAttributes();
        BeanUtils.copyProperties(configuration, attributes); // ignores attributes that don't exist in the target - perfect!
        attributes.setIncrementalSyncEnabled(configuration.isCrowdSyncIncrementalEnabled());
        if (configuration.getLdapPermissionOption() == PermissionOption.READ_ONLY_LOCAL_GROUPS) {
            attributes.setLdapAutoAddGroups(commaWhitespaceSeparatedGroupsToPipeSeparatedGroups(configuration.getLdapAutoAddGroups()));
        }
        else
        {
            attributes.setLdapAutoAddGroups("");
        }
        builder.setAttributes(attributes.toAttributesMap());

        return builder.toDirectory();
    }

    public Directory buildDelegatingLdapDirectory(DelegatingLdapDirectoryConfiguration configuration)
    {
        ImmutableDirectory.Builder builder = createBuilder();
        builder.setAllowedOperations(EnumSet.allOf(OperationType.class));
        builder.setActive(configuration.isActive());
        builder.setId(configuration.getDirectoryId());
        builder.setImplementationClass(DelegatedAuthenticationDirectory.class.getName());
        builder.setName(configuration.getName());
        builder.setType(DirectoryType.DELEGATING);

        LdapDelegatingDirectoryAttributes attributes = new LdapDelegatingDirectoryAttributes();
        BeanUtils.copyProperties(configuration, attributes); // ignores attributes that don't exist in the target - perfect!
        attributes.setDelegatedToClass(configuration.getType());
        if (configuration.isCreateUserOnAuth())
        {
            attributes.setUpdateUserOnAuth(true); // create-on-auth implies update-on-auth
            attributes.setLdapAutoAddGroups(commaWhitespaceSeparatedGroupsToPipeSeparatedGroups(configuration.getLdapAutoAddGroups()));
        }
        else
        {
            attributes.setLdapAutoAddGroups("");
        }
        builder.setAttributes(attributes.toAttributesMap());

        return builder.toDirectory();
    }

    public Directory buildInternalDirectory(InternalDirectoryConfiguration configuration) {
        ImmutableDirectory.Builder builder = createBuilder();
        builder.setAllowedOperations(EnumSet.allOf(OperationType.class));
        builder.setActive(configuration.isActive());
        builder.setId(configuration.getDirectoryId());
        builder.setImplementationClass(InternalDirectory.class.getName());
        builder.setName(configuration.getName());
        builder.setType(DirectoryType.INTERNAL);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(InternalDirectory.ATTRIBUTE_USER_ENCRYPTION_METHOD, PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER);
        map.put(DirectoryImpl.ATTRIBUTE_KEY_USE_NESTED_GROUPS, Boolean.valueOf(configuration.isNestedGroupsEnabled()).toString());
        builder.setAttributes(map);

        return builder.toDirectory();
    }

    public Directory buildJiraJdbcDirectory(JiraJdbcDirectoryConfiguration configuration)
    {
        ImmutableDirectory.Builder builder = createBuilder();
        builder.setAllowedOperations(EnumSet.of(OperationType.UPDATE_USER));
        builder.setActive(configuration.isActive());
        builder.setId(configuration.getDirectoryId());
        builder.setImplementationClass(JiraJdbcDirectoryConfiguration.DIRECTORY_CLASS);
        builder.setName(configuration.getName());
        builder.setType(DirectoryType.CUSTOM);

        builder.setAttributes(Collections.singletonMap(JiraJdbcDirectoryConfiguration.JNDI_NAME_ATTRIBUTE_KEY, configuration.getDatasourceJndiName()));

        return builder.toDirectory();
    }

    private ImmutableDirectory.Builder createBuilder()
    {
        ImmutableDirectory.Builder builder = ImmutableDirectory.newBuilder();
        Date now = new Date();
        builder.setCreatedDate(now);
        builder.setUpdatedDate(now);
        return builder;
    }

    public CrowdDirectoryConfiguration toCrowdConfiguration(Directory directory)
    {
        CrowdDirectoryConfiguration configuration = new CrowdDirectoryConfiguration();
        configuration.setCrowdPermissionOption(CrowdPermissionOption.fromAllowedOperations(directory.getAllowedOperations()));
        configuration.setDirectoryId(directory.getId() != null ? directory.getId() : 0);
        configuration.setActive(directory.isActive());
        configuration.setName(directory.getName());

        CrowdDirectoryAttributes attributes = CrowdDirectoryAttributes.fromAttributesMap(directory.getAttributes());
        BeanUtils.copyProperties(attributes, configuration);
        // Convert polling interval to minutes to display to user
        configuration.setCrowdServerSynchroniseIntervalInMin(NumberUtils.toLong(attributes.getCrowdServerSynchroniseIntervalInSeconds()) / 60);

        return configuration;
    }

    public LdapDirectoryConfiguration toLdapConfiguration(Directory directory)
    {
        LdapDirectoryConfiguration configuration = new LdapDirectoryConfiguration();
        configuration.setLdapPermissionOption(PermissionOption.fromAllowedOperations(directory.getAllowedOperations()));
        configuration.setActive(directory.isActive());
        configuration.setLdapUserEncryption(directory.getEncryptionType());
        configuration.setDirectoryId(directory.getId() != null ? directory.getId() : 0);
        configuration.setType(directory.getImplementationClass());
        configuration.setName(directory.getName());

        LdapDirectoryAttributes attributes = LdapDirectoryAttributes.fromAttributesMap(directory.getAttributes());
        BeanUtils.copyProperties(attributes, configuration);
        configuration.setCrowdSyncIncrementalEnabled(attributes.isIncrementalSyncEnabled());

        return configuration;
    }

    public DelegatingLdapDirectoryConfiguration toDelegatingLdapConfiguration(final Directory directory)
    {
        DelegatingLdapDirectoryConfiguration configuration = new DelegatingLdapDirectoryConfiguration();
        configuration.setDirectoryId(directory.getId() != null ? directory.getId() : 0);
        configuration.setActive(directory.isActive());
        configuration.setName(directory.getName());

        LdapDelegatingDirectoryAttributes attributes = LdapDelegatingDirectoryAttributes.fromAttributesMap(directory.getAttributes());
        BeanUtils.copyProperties(attributes, configuration);
        configuration.setType(attributes.getDelegatedToClass());
        configuration.setLdapAutoAddGroups(pipeSeparatedGroupsToCommaSeparatedGroups(attributes.getLdapAutoAddGroups()));

        return configuration;
    }

    public JiraJdbcDirectoryConfiguration toJiraJdbcConfiguration(Directory directory)
    {
        JiraJdbcDirectoryConfiguration configuration = new JiraJdbcDirectoryConfiguration();
        configuration.setDirectoryId(directory.getId() != null ? directory.getId() : 0);
        configuration.setActive(directory.isActive());
        configuration.setName(directory.getName());
        configuration.setDatasourceJndiName(directory.getAttributes().get(JiraJdbcDirectoryConfiguration.JNDI_NAME_ATTRIBUTE_KEY));
        return configuration;
    }

    public InternalDirectoryConfiguration toInternalConfiguration(Directory directory) {
        InternalDirectoryConfiguration configuration = new InternalDirectoryConfiguration();
        configuration.setDirectoryId(directory.getId() != null ? directory.getId() : 0);
        configuration.setActive(directory.isActive());
        configuration.setName(directory.getName());

        Map<String,String> attributes = directory.getAttributes();
        String useNestedGroups = attributes.get(DirectoryImpl.ATTRIBUTE_KEY_USE_NESTED_GROUPS);
        if (useNestedGroups != null)
        {
            configuration.setNestedGroupsEnabled(Boolean.valueOf(useNestedGroups));
        }
        else
        {
            configuration.setNestedGroupsEnabled(false);
        }

        return configuration;
    }
    
    public static String pipeSeparatedGroupsToCommaSeparatedGroups(String pipeSeparated)
    {
        return StringUtils.replaceChars(pipeSeparated, DirectoryImpl.AUTO_ADD_GROUPS_SEPARATOR, ',');
    }
    
    public static String commaWhitespaceSeparatedGroupsToPipeSeparatedGroups(String commaSeparated)
    {
        if (commaSeparated == null)
        {
            return "";
        }

        final String[] untrimmedGroups = StringUtils.split(commaSeparated, ',');

        // Remove duplicates and trim group names
        final Set<String> uniqueGroups = new LinkedHashSet<String>(untrimmedGroups.length);
        for (String untrimmedGroup : untrimmedGroups)
        {
            uniqueGroups.add(untrimmedGroup.trim());
        }

        return StringUtils.join(uniqueGroups, DirectoryImpl.AUTO_ADD_GROUPS_SEPARATOR);
    }
}
