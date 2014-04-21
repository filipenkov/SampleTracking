package com.atlassian.crowd.directory.ldap.util;

import com.atlassian.crowd.directory.ldap.LdapTypeConfig;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class is a helper class that contains all configuration and implementation information for LDAP
 * This configuration data is pulled from property files on the classpath in the format
 * ConnectorClazz.properties
 */
public interface LDAPPropertiesHelper
{
    Map<String, String> getImplementations();

    Map<String, Properties> getConfigurationDetails();

   List<LdapTypeConfig> getLdapTypeConfigs();
}
