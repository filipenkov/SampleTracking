package com.atlassian.crowd.directory.ldap;

import com.atlassian.crowd.directory.SpringLDAPConnector;
import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.directory.ldap.util.LDAPPropertiesHelper;
import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.directory.ssl.LdapHostnameVerificationSSLSocketFactory;

import org.apache.commons.lang.StringUtils;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LDAPPropertiesMapperImpl implements LDAPPropertiesMapper
{
    private Map<String, String> attributes;

    private final LDAPPropertiesHelper ldapPropertiesHelper;

    /**
     * LDAP initial context factory.
     */
    public static final String CONNECTION_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    /**
     * LDAP connection method.
     */
    public static final String CONNECTION_SECURITY_AUTHENTICATION = "simple";

    /**
     * LDAP secure connection method.
     */
    public static final String CONNECTION_SSL_SECURITY_PROTOCOL = "ssl";

    /**
     * LDAP connection factory.
     */
    public static final String CONNECTION_FACTORY = "java.naming.ldap.factory.socket";

    /**
     * LDAP secure connection factory.
     */
    public static final String CONNECTION_FACTORY_SSL_IMPL = LdapHostnameVerificationSSLSocketFactory.class.getName();

    /**
     * LDAP binary attributes.
     */
    public static final String CONNECTION_BINARY_ATTRIBUTES = "java.naming.ldap.attributes.binary";

    public LDAPPropertiesMapperImpl(LDAPPropertiesHelper ldapPropertiesHelper)
    {
        this.ldapPropertiesHelper = ldapPropertiesHelper;
    }

    /**
     * Returns a map of the LDAP names as the keys and the implementation class as Strings.
     *
     * @return The implementations.
     */
    public Map<String, String> getImplementations()
    {
        return ldapPropertiesHelper.getImplementations();
    }

    /**
     * Returns a map of the LDAP names as the keys and the {@link Properties properties} associated
     * with that LDAP connector.
     *
     * @return The configuration details.
     */
    public Map<String, Properties> getConfigurationDetails()
    {
        return ldapPropertiesHelper.getConfigurationDetails();
    }

    public Map<String, String> getEnvironment()
    {
        Map<String, String> environment = new HashMap<String, String>();

        // TODO -
        environment.put(Context.INITIAL_CONTEXT_FACTORY, CONNECTION_INITIAL_CONTEXT_FACTORY);
        environment.put(Context.SECURITY_AUTHENTICATION, CONNECTION_SECURITY_AUTHENTICATION);

        //environment.put(Context.PROVIDER_URL, getSingleAttribute(LDAP_URL_KEY));
        //environment.put(Context.SECURITY_PRINCIPAL, getSingleAttribute(LDAP_USERDN_KEY));
        //environment.put(Context.SECURITY_CREDENTIALS, getSingleAttribute(LDAP_PASSWORD_KEY));

        // follow referrals -- common problem with active directory
        // http://www-1.ibm.com/support/docview.wss?uid=swg21161164
        if (isReferral())
        {
            environment.put(Context.REFERRAL, "follow");
        }

        if (isSecureSSL())
        {
            environment.put(Context.SECURITY_PROTOCOL, CONNECTION_SSL_SECURITY_PROTOCOL);
            environment.put(CONNECTION_FACTORY, CONNECTION_FACTORY_SSL_IMPL);
        }

        if (isUsingConnectionPooling())
        {
            // Enable connection pooling
            // http://java.sun.com/products/jndi/tutorial/ldap/connect/pool.html
            environment.put("com.sun.jndi.ldap.connect.pool", "true");

            // Most connection pool settings are actually System properties and not environment variables
            // http://java.sun.com/products/jndi/tutorial/ldap/connect/config.html
            // These settings are managed by DefaultConnectionPoolProperties
        }

        // Set connection/read options if provided
        // This connectionTimeout value is NOT THE SAME as pool timeout
        // http://download.oracle.com/javase/6/docs/technotes/guides/jndi/jndi-ldap.html (3.4 Provider-specific Properties & 15.4.2 Connection Timeout)
        // It serves two purposes
        //  1. used to specify a timeout period for establishment of the LDAP connection
        //  2. also specifies the maximum wait time for a connection when all connections in pool are in use and the maximum pool size has been reached.
        // If this property has not been specified:
        //   1. wait for the default TCP timeout to take effect when creating a new connection.
        //   2. wait indefinitely for a pooled connection to become available
        String connectionTimeout = getConnectionTimeout();
        if (StringUtils.isNotBlank(connectionTimeout))
        {
            environment.put("com.sun.jndi.ldap.connect.timeout", connectionTimeout);
        }

        String readTimeout = getReadTimeout();
        if (StringUtils.isNotBlank(readTimeout))
        {
            environment.put("com.sun.jndi.ldap.read.timeout", readTimeout);
        }
        // TODO - what is the second attribute?
        return environment;
    }

    /**
     * If the connection must be secure.
     *
     * @return <code>true</code> if and only if the connection must be secure, otherwise <code>false</code>.
     */
    protected boolean isSecureSSL()
    {
        return getBooleanKey(LDAP_SECURE_KEY);
    }

    protected boolean isUsingConnectionPooling()
    {
        return getBooleanKey(LDAP_POOLING_KEY);
    }

    protected boolean getBooleanKey(String key)
    {
        return getBooleanKey(key, false);
    }

    protected boolean getBooleanKey(String key, boolean defaultValue)
    {
        String value = attributes.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    protected String getPoolInitSize()
    {
        return getAttribute(LDAP_POOL_INITSIZE);
    }

    protected String getPoolPrefSize()
    {
        return getAttribute(LDAP_POOL_PREFSIZE);
    }

    protected String getPoolMaxSize()
    {
        return getAttribute(LDAP_POOL_MAXSIZE);
    }

    protected String getPoolTimeout()
    {
        return getAttribute(LDAP_POOL_TIMEOUT);
    }

    protected String getConnectionTimeout()
    {
        return getAttribute(LDAP_CONNECTION_TIMEOUT);
    }

    protected String getReadTimeout()
    {
        return getAttribute(LDAP_READ_TIMEOUT);
    }

    public int getSearchTimeLimit()
    {
        String timeLimit = getAttribute(LDAP_SEARCH_TIMELIMIT);
        if (StringUtils.isNotBlank(timeLimit))
        {
            return Integer.valueOf(timeLimit);
        }
        return 0;
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes)
    {
        this.attributes = attributes;   }

    public String getAttribute(String key)
    {
        String value = attributes.get(key);
        return value == null ? "" : value;
    }

    public String getBaseDN() throws InvalidNameException
    {
        return getAttribute(LDAP_BASEDN_KEY);
    }

    public String getGroupBaseDN() throws InvalidNameException
    {
        // build the search DN location
        String groupSearchDN;
        String additionalDN = getAttribute(GROUP_DN_ADDITION);

        if (additionalDN != null && !additionalDN.equals(""))
        {
            groupSearchDN = additionalDN + "," + getAttribute(LDAP_BASEDN_KEY);

        }
        else
        {
            groupSearchDN = getAttribute(LDAP_BASEDN_KEY);
        }

        return groupSearchDN;
    }

    public String getGroupFilter()
    {
        return getAttribute(GROUP_OBJECTFILTER_KEY);
    }

    public String getConnectionURL()
    {
        return getAttribute(LDAP_URL_KEY);
    }

    public String getUsername()
    {
        return getAttribute(LDAP_USERDN_KEY);
    }

    public String getPassword()
    {
        return getAttribute(LDAP_PASSWORD_KEY);
    }

    public String getGroupNameAttribute()
    {
        return getAttribute(GROUP_NAME_KEY);
    }

    public String getObjectClassAttribute()
    {
        return "objectClass";
    }

    public String getRoleFilter()
    {
        return getAttribute(ROLE_OBJECTFILTER_KEY);
    }

    public String getRoleBaseDN() throws InvalidNameException
    {
        // build the search DN location
        String roleSearchDN;
        String additionalDN = getAttribute(ROLE_DN_ADDITION);

        if (additionalDN != null && !additionalDN.equals(""))
        {
            roleSearchDN = additionalDN + "," + getAttribute(LDAP_BASEDN_KEY);

        }
        else
        {
            roleSearchDN = getAttribute(LDAP_BASEDN_KEY);
        }

        return roleSearchDN;
    }

    public String getRoleNameAttribute()
    {
        return getAttribute(ROLE_NAME_KEY);
    }

    public String getUserFilter()
    {
        return getAttribute(USER_OBJECTFILTER_KEY);
    }

    public String getPrincipalBaseDN()
    {
        // build the search DN location
        String principalSearchDN;
        String additionalDN = getAttribute(USER_DN_ADDITION);

        if (additionalDN != null && !additionalDN.equals(""))
        {
            principalSearchDN = additionalDN + "," + getAttribute(LDAP_BASEDN_KEY);

        }
        else
        {
            principalSearchDN = getAttribute(LDAP_BASEDN_KEY);
        }

        return principalSearchDN;
    }

    public String getUserNameAttribute()
    {
        return getAttribute(USER_USERNAME_KEY);
    }

    public String getUserNameRdnAttribute()
    {
        return getAttribute(USER_USERNAME_RDN_KEY);
    }

    public String getUserEmailAttribute()
    {
        return getAttribute(USER_EMAIL_KEY);
    }

    public String getUserGroupMembershipsAttribute()
    {
        return getAttribute(USER_GROUP_KEY);
    }

    public String getGroupObjectClass()
    {
        return getAttribute(GROUP_OBJECTCLASS_KEY);
    }

    public String getGroupDescriptionAttribute()
    {
        return getAttribute(GROUP_DESCRIPTION_KEY);
    }

    public String getGroupMemberAttribute()
    {
        return getAttribute(GROUP_USERNAMES_KEY);
    }

    public String getRoleObjectClass()
    {
        return getAttribute(ROLE_OBJECTCLASS_KEY);
    }

    public String getRoleDescriptionAttribute()
    {
        return getAttribute(ROLE_DESCRIPTION_KEY);
    }

    public String getRoleMemberAttribute()
    {
        return getAttribute(ROLE_USERNAMES_KEY);
    }

    public String getUserObjectClass()
    {
        return getAttribute(USER_OBJECTCLASS_KEY);
    }

    public String getUserFirstNameAttribute()
    {
        return getAttribute(USER_FIRSTNAME_KEY);
    }

    public String getUserLastNameAttribute()
    {
        return getAttribute(USER_LASTNAME_KEY);
    }

    public String getUserDisplayNameAttribute()
    {
        return getAttribute(USER_DISPLAYNAME_KEY);
    }

    public String getUserPasswordAttribute()
    {
        return getAttribute(USER_PASSWORD_KEY);
    }

    public String getUserEncryptionMethod()
    {
        return getAttribute(LDAP_USER_ENCRYPTION_METHOD);
    }

    /**
     * Checks if the configuration of the LDAP directory server uses paged results.
     *
     * @return <code>true</code> if and only if paged results is enabled for the LDAP server, otherwise <code>false</code>.
     */
    public boolean isPagedResultsControl()
    {
        boolean isPagedResultsControl = false;

        String isPagedResultsControlStr = getAttribute(LDAP_PAGEDRESULTS_KEY);

        if (isPagedResultsControlStr != null)
        {
            isPagedResultsControl = Boolean.valueOf(isPagedResultsControlStr).booleanValue();
        }

        return isPagedResultsControl;
    }

    public int getPagedResultsSize()
    {
        int pagedResultsControlSize = SpringLDAPConnector.DEFAULT_PAGE_SIZE;

        String isPagedResultsControlSizeStr = getAttribute(LDAP_PAGEDRESULTS_SIZE);

        if (isPagedResultsControlSizeStr != null)
        {
            pagedResultsControlSize = Integer.valueOf(isPagedResultsControlSizeStr).intValue();
        }

        return pagedResultsControlSize;
    }

    public boolean isNestedGroupsDisabled()
    {
        // by default, nested groups is DISABLED (ie. if not explicitly enabled, then it is disabled)
        return getBooleanKey(LDAP_NESTED_GROUPS_DISABLED, true);
    }

    public boolean isUsingUserMembershipAttribute()
    {
        return getBooleanKey(LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE);
    }

    public boolean isUsingUserMembershipAttributeForGroupMembership()
    {
        return getBooleanKey(LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE_FOR_GROUP_MEMBERSHIP);
    }

    public boolean isReferral()
    {
        return getBooleanKey(LDAP_REFERRAL_KEY);
    }

    public boolean isRelaxedDnStandardisation()
    {
        return getBooleanKey(LDAP_RELAXED_DN_STANDARDISATION);
    }

    public boolean isRolesDisabled()
    {
        return getBooleanKey(ROLES_DISABLED);
    }

    public boolean isLocalGroupsEnabled()
    {
        return getBooleanKey(LOCAL_GROUPS);
    }

    public int getCacheSynchroniseInterval()
    {
        int cacheSynchroniseInterval = DirectoryPoller.DEFAULT_CACHE_SYNCHRONISE_INTERVAL;

        String cacheSynchroniseIntervalStr = getAttribute(SynchronisableDirectoryProperties.CACHE_SYNCHRONISE_INTERVAL);

        if (cacheSynchroniseIntervalStr != null)
        {
            cacheSynchroniseInterval = Integer.valueOf(cacheSynchroniseIntervalStr).intValue();
        }

        return cacheSynchroniseInterval;
    }

    public List<LdapTypeConfig> getLdapTypeConfigurations()
    {
        return ldapPropertiesHelper.getLdapTypeConfigs();
    }
}
