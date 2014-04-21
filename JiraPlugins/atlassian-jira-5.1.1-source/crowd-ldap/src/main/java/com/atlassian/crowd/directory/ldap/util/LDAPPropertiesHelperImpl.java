package com.atlassian.crowd.directory.ldap.util;

import com.atlassian.crowd.directory.ApacheDS;
import com.atlassian.crowd.directory.ApacheDS15;
import com.atlassian.crowd.directory.AppleOpenDirectory;
import com.atlassian.crowd.directory.FedoraDS;
import com.atlassian.crowd.directory.GenericLDAP;
import com.atlassian.crowd.directory.LDAPDirectory;
import com.atlassian.crowd.directory.MicrosoftActiveDirectory;
import com.atlassian.crowd.directory.NovelleDirectory;
import com.atlassian.crowd.directory.OpenDS;
import com.atlassian.crowd.directory.OpenLDAP;
import com.atlassian.crowd.directory.OpenLDAPRfc2307;
import com.atlassian.crowd.directory.Rfc2307;
import com.atlassian.crowd.directory.SunONE;
import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.LdapTypeConfig;

import com.google.common.collect.ImmutableList;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class is a helper class that contains all configuration and implementation information for LDAP
 * This configuration data is pulled from property files on the classpath in the format
 * ConnectorClazz.properties
 */
public class LDAPPropertiesHelperImpl implements LDAPPropertiesHelper
{
    private static final Logger logger = Logger.getLogger(LDAPPropertiesHelperImpl.class);

    // Implementation details
    private Map<String, String> implementations;

    // Configuration details.
    private Map<String, Properties> configurationDetails;

    private List<LdapTypeConfig> ldapTypeConfigs = new ArrayList<LdapTypeConfig>();

    public LDAPPropertiesHelperImpl()
    {
        init();
    }

    private void init()
    {
        implementations = new LinkedHashMap<String, String>();

        // these appear in order in the ui
        implementations.put(MicrosoftActiveDirectory.getStaticDirectoryType(), MicrosoftActiveDirectory.class.getName());

        // maintain alpha order for the ones below:
        implementations.put(ApacheDS.getStaticDirectoryType(),                 ApacheDS.class.getName());
        implementations.put(ApacheDS15.getStaticDirectoryType(),               ApacheDS15.class.getName());
        implementations.put(AppleOpenDirectory.getStaticDirectoryType(),       AppleOpenDirectory.class.getName());
        implementations.put(FedoraDS.getStaticDirectoryType(),                 FedoraDS.class.getName());
        implementations.put(GenericLDAP.getStaticDirectoryType(),              GenericLDAP.class.getName());
        implementations.put(NovelleDirectory.getStaticDirectoryType(),         NovelleDirectory.class.getName());
        implementations.put(OpenDS.getStaticDirectoryType(),                   OpenDS.class.getName());
        implementations.put(OpenLDAP.getStaticDirectoryType(),                 OpenLDAP.class.getName());
        implementations.put(OpenLDAPRfc2307.getStaticDirectoryType(),          OpenLDAPRfc2307.class.getName());
        implementations.put(Rfc2307.getStaticDirectoryType(),                  Rfc2307.class.getName());
        implementations.put(SunONE.getStaticDirectoryType(),                   SunONE.class.getName());

        logger.debug("Added the following LDAP implementations: " + implementations.toString());

        configurationDetails = new HashMap<String, Properties>();

        configurationDetails.put(GenericLDAP.class.getName(), loadDirectoryProperties(GenericLDAP.class));
        configurationDetails.put(OpenLDAP.class.getName(), loadDirectoryProperties(OpenLDAP.class));
        configurationDetails.put(MicrosoftActiveDirectory.class.getName(), loadDirectoryProperties(MicrosoftActiveDirectory.class));
        configurationDetails.put(SunONE.class.getName(), loadDirectoryProperties(SunONE.class));
        configurationDetails.put(ApacheDS.class.getName(), loadDirectoryProperties(ApacheDS.class));
        configurationDetails.put(ApacheDS15.class.getName(), loadDirectoryProperties(ApacheDS15.class));
        configurationDetails.put(NovelleDirectory.class.getName(), loadDirectoryProperties(NovelleDirectory.class));
        configurationDetails.put(Rfc2307.class.getName(), loadDirectoryProperties(Rfc2307.class));
        configurationDetails.put(AppleOpenDirectory.class.getName(), loadDirectoryProperties(AppleOpenDirectory.class));
        configurationDetails.put(OpenDS.class.getName(), loadDirectoryProperties(OpenDS.class));
        configurationDetails.put(FedoraDS.class.getName(), loadDirectoryProperties(FedoraDS.class));
        configurationDetails.put(OpenLDAPRfc2307.class.getName(), loadDirectoryProperties(OpenLDAPRfc2307.class));

        logger.debug("Added the following LDAP configuration details: " + configurationDetails.toString());

        initHiddenFields();
    }

    public static Collection<? extends Class<? extends LDAPDirectory>> DIRECTORIES_WITH_CONFIGURABLE_USER_ENCRYPTION =
            ImmutableList.of(OpenLDAP.class,
                    OpenLDAPRfc2307.class,
                    GenericLDAP.class,
                    Rfc2307.class,
                    ApacheDS.class,
                    ApacheDS15.class);

    private void initHiddenFields()
    {
        // Initialise the data of which classes hide which fields.
        final Map<String, List<String>> classesHidingField = new HashMap<String, List<String>>();

        final List<String> classesWithHiddenEncryption = new ArrayList<String>(implementations.values());
        for (Class<?> c : DIRECTORIES_WITH_CONFIGURABLE_USER_ENCRYPTION)
        {
            classesWithHiddenEncryption.remove(c.getName());
        }
        classesHidingField.put(LDAPPropertiesMapper.LDAP_USER_ENCRYPTION_METHOD, classesWithHiddenEncryption);

        classesHidingField.put(LDAPPropertiesMapper.LDAP_NESTED_GROUPS_DISABLED, Arrays.<String>asList(OpenLDAPRfc2307.class.getName(), Rfc2307.class.getName(), AppleOpenDirectory.class.getName(), FedoraDS.class.getName()));

        final List<String> classesWithHiddenUseUserMembershipAttributeForGroupMembership = new ArrayList<String>(implementations.values());
        classesWithHiddenUseUserMembershipAttributeForGroupMembership.remove(MicrosoftActiveDirectory.class.getName());
        classesHidingField.put(LDAPPropertiesMapper.LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE_FOR_GROUP_MEMBERSHIP, classesWithHiddenUseUserMembershipAttributeForGroupMembership);

        final List<String> classesWithHiddenIncrementalSyncEnabled = new ArrayList<String>(implementations.values());
        classesWithHiddenIncrementalSyncEnabled.remove(MicrosoftActiveDirectory.class.getName());
        classesHidingField.put(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED, classesWithHiddenIncrementalSyncEnabled);

        // Setup the ldap config types used for UI tailoring
        Set<String> hideableFields = classesHidingField.keySet();
        for (Map.Entry<String, String> entry : implementations.entrySet())
        {
            final String className = entry.getValue();
            final String displayName = entry.getKey();
            LdapTypeConfig config = new LdapTypeConfig(className, displayName, configurationDetails.get(className));

            for (String field : hideableFields)
            {
                if (classesHidingField.get(field).contains(className))
                {
                    config.setHiddenField(field);
                }
            }
            ldapTypeConfigs.add(config);
        }

    }

    protected Properties loadDirectoryProperties(Class<?> clazz)
    {
        InputStream stream;
        Properties props = new Properties();

        String fqcn = clazz.getName();
        String key = fqcn.substring(fqcn.lastIndexOf('.') + 1) + ".properties";

        // find the properties file
        stream = clazz.getResourceAsStream("/com/atlassian/crowd/integration/directory/" + key.toLowerCase(Locale.ENGLISH));
        if (stream == null)
        {
            logger.warn("Unable to load properties with key: " + key);
        }
        else
        {
            try
            {
                props.load(stream);
                if (logger.isDebugEnabled())
                {
                    logger.debug("The following properties for key: " + key + "were loaded: " + props);
                }
            }
            catch (IOException e)
            {
                logger.fatal("Failed to load property with key: " + key, e);
            }
            finally
            {
                try
                {
                    stream.close();
                }
                catch (IOException e)
                {
                    logger.warn(e.getMessage(), e);
                }
            }
        }

        return props;
    }

    public Map<String, String> getImplementations()
    {
        return implementations;
    }

    public Map<String, Properties> getConfigurationDetails()
    {
        return configurationDetails;
    }

    public List<LdapTypeConfig> getLdapTypeConfigs()
    {
        return ldapTypeConfigs;
    }
}
