package com.atlassian.crowd.embedded.admin;

import com.atlassian.crowd.embedded.admin.crowd.CrowdDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.delegatingldap.DelegatingLdapDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.internal.InternalDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.jirajdbc.JiraJdbcDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.ldap.LdapDirectoryConfiguration;
import com.atlassian.crowd.embedded.api.Directory;

/**
 * Converts from the UI configuration beans to or from an Embedded Crowd Directory object.
 */
public interface DirectoryMapper
{
    Directory buildLdapDirectory(LdapDirectoryConfiguration configuration);
    Directory buildDelegatingLdapDirectory(DelegatingLdapDirectoryConfiguration configuration);
    Directory buildCrowdDirectory(CrowdDirectoryConfiguration configuration);
    Directory buildInternalDirectory(InternalDirectoryConfiguration command);

    CrowdDirectoryConfiguration toCrowdConfiguration(Directory directory);
    LdapDirectoryConfiguration toLdapConfiguration(Directory directory);
    DelegatingLdapDirectoryConfiguration toDelegatingLdapConfiguration(Directory directory);
    InternalDirectoryConfiguration toInternalConfiguration(Directory directory);

    Directory buildJiraJdbcDirectory(JiraJdbcDirectoryConfiguration configuration);
    JiraJdbcDirectoryConfiguration toJiraJdbcConfiguration(Directory directory);
}
