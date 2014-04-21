package com.atlassian.crowd.directory.ldap;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Allows LDAP directory connectors to obtain LDAP settings, such as directory-specific names for RDNs.
 */
public interface LDAPPropertiesMapper
{
    /**
     * Attribute key for the LDAP url.
     */
    String LDAP_URL_KEY = "ldap.url";

    /**
     * Attribute key for the SSL required flag.
     */
    String LDAP_SECURE_KEY = "ldap.secure";

    /**
     * Attribute key for the referral option.
     */
    String LDAP_REFERRAL_KEY = "ldap.referral";

    /**
     * Attribute key for connection pooling.
     */
    String LDAP_POOLING_KEY = "ldap.pooling";

    /**
     * Attribute key for the LDAP base DN.
     */
    String LDAP_BASEDN_KEY = "ldap.basedn";

    /**
     * Attribute key for the LDAP user DN.
     */
    String LDAP_USERDN_KEY = "ldap.userdn";

    /**
     * Attribute key for the LDAP password.
     */
    String LDAP_PASSWORD_KEY = "ldap.password";

    /**
     * Attribute key for the propagation of changes.
     */
    String LDAP_PROPOGATE_CHANGES = "ldap.propogate.changes";

    // group properties file keys
    /**
     * Attribute key for the LDAP group base dn.
     */
    String GROUP_DN_ADDITION = "ldap.group.dn";

    /**
     * Attribute key for the LDAP group description attribute.
     */
    String GROUP_DESCRIPTION_KEY = "ldap.group.description";

    /**
     * Attribute key for the LDAP group name attribute.
     */
    String GROUP_NAME_KEY = "ldap.group.name";

    /**
     * Attribute key for the LDAP group object class.
     */
    String GROUP_OBJECTCLASS_KEY = "ldap.group.objectclass";

    /**
     * Attribute key for the LDAP group object class.
     */
    String GROUP_OBJECTFILTER_KEY = "ldap.group.filter";

    /**
     * Attribute key for the LDAP group membership attribute.
     */
    String GROUP_USERNAMES_KEY = "ldap.group.usernames";

    // role properties file key
    /**
     * Attribute key for the LDAP role base dn.
     */
    String ROLE_DN_ADDITION = "ldap.role.dn";

    /**
     * Attribute key for the LDAP role description attribute.
     */
    String ROLE_DESCRIPTION_KEY = "ldap.role.description";

    /**
     * Attribute key for the LDAP role name attribute.
     */
    String ROLE_NAME_KEY = "ldap.role.name";

    /**
     * Attribute key for the LDAP role object class.
     */
    String ROLE_OBJECTCLASS_KEY = "ldap.role.objectclass";

    /**
     * Attribute key for the LDAP role object class.
     */
    String ROLE_OBJECTFILTER_KEY = "ldap.role.filter";

    /**
     * Attribute key for the LDAP role membership attribute.
     */
    String ROLE_USERNAMES_KEY = "ldap.role.usernames";

    // user properties file keys
    /**
     * Attribute key for the LDAP principal base dn.
     */
    String USER_DN_ADDITION = "ldap.user.dn";

    /**
     * Attribute key for the LDAP principal email attribute.
     */
    String USER_EMAIL_KEY = "ldap.user.email";

    /**
     * Attribute key for the LDAP principal lastname attribute.
     */
    String USER_FIRSTNAME_KEY = "ldap.user.firstname";

    /**
     * Attribute key for the LDAP principal memberships attribute.
     */
    String USER_GROUP_KEY = "ldap.user.group";

    /**
     * Attribute key for the LDAP principal firstname attribute.
     */
    String USER_LASTNAME_KEY = "ldap.user.lastname";

    /**
     * Attribute key for the LDAP principal displayName (full name) attribute.
     */
    String USER_DISPLAYNAME_KEY = "ldap.user.displayname";

    /**
     * Attribute key for the LDAP principal object class.
     */
    String USER_OBJECTCLASS_KEY = "ldap.user.objectclass";

    /**
     * Attribute key for the LDAP role object class.
     */
    String USER_OBJECTFILTER_KEY = "ldap.user.filter";

    /**
     * Attribute key for the LDAP principal name attribute.
     */
    String USER_USERNAME_KEY = "ldap.user.username";

    /**
     * The name to be used when building a DN for the user. In most cases this will be the same as {@see USER_USERNAME_KEY)
     * but for Active Directory it's different. RDN = Relative Distinguished Name, or the part of the DN containing the
     * username.
     */
    String USER_USERNAME_RDN_KEY = "ldap.user.username.rdn";

    /**
     * Attribute key for the LDAP principal password attribute.
     */
    String USER_PASSWORD_KEY = "ldap.user.password";

    /**
     * Attribute key for the LDAP paged results attribute.
     */
    String LDAP_PAGEDRESULTS_KEY = "ldap.pagedresults";

    /**
     * Key to fine whether or not we support nested groups for a given LDAP Directory
     */
    String LDAP_NESTED_GROUPS_DISABLED = "ldap.nestedgroups.disabled";

    /**
     * Key to decide whether we use the "memberOf" attribute on a user when making queries.
     */
    String LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE = "ldap.usermembership.use";

    /**
     * Key to decide whether we use the "memberOf" attribute on a user when making queries.
     */
    String LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE_FOR_GROUP_MEMBERSHIP = "ldap.usermembership.use.for.groups";            

    /**
     * LDAP password encrypion algorithm, used for updating a Principal's password with
     * the correct encryption algorithm
     */
    String LDAP_USER_ENCRYPTION_METHOD = "ldap.user.encryption";

    /**
     * Attribute key for the LDAP paged results size attribute.
     */
    String LDAP_PAGEDRESULTS_SIZE = "ldap.pagedresults.size";

    /**
     * Key to decide whether we need full DN standardisation or can get away with faster, relaxed standardisation.
     */
    String LDAP_RELAXED_DN_STANDARDISATION = "ldap.relaxed.dn.standardisation";

    /**
     * If set, roles are disabled. Needed for some event-based caching configurations.
     */
    String ROLES_DISABLED = "ldap.roles.disabled";

    /**
     * Key to determine if using local storage for groups/group memberships.
     */
    String LOCAL_GROUPS = "ldap.local.groups";

    /**
     * Initial size of connection pool, e.g. number of connections to open at start-up. Default: 1
     */
    String LDAP_POOL_INITSIZE = "ldap.pool.initsize";

    /**
     * Preferred size of connection pool. Default: 10
     */
    String LDAP_POOL_PREFSIZE = "ldap.pool.prefsize";

    /**
     * Maximum size of connection pool. Zero means no maximum size. Default: 0
     */
    String LDAP_POOL_MAXSIZE = "ldap.pool.maxsize";

    /**
     * Idle time stored in milliseconds for a connection before it is removed from the pool. Default: 30 seconds (30000ms)
     */
    String LDAP_POOL_TIMEOUT = "ldap.pool.timeout";

    /**
     * Time limit on searches stored in milliseconds. Zero means no limit. Default : 60 seconds (60000ms)
     */
    String LDAP_SEARCH_TIMELIMIT = "ldap.search.timelimit";

    /**
     * Timeout stored in milliseconds when opening new server connections.
     * When connection pooling has been requested, this property also specifies the maximum wait time for a connection when all connections in pool are in use and the maximum pool size has been reached.
     * Default: 0
     */
    String LDAP_CONNECTION_TIMEOUT = "ldap.connection.timeout";

    /**
     * Timeout stored in milliseconds for search and other read operations. Default: 120 seconds (120000ms)
     * <p>
     * Warning: CWD-2494: When read timeout is enabled, operations can fail
     * randomly with "javax.naming.NamingException: LDAP response read timed out..."
     * error message without waiting for the timeout to pass.
     */
    String LDAP_READ_TIMEOUT = "ldap.read.timeout";


    Map<String, String> getImplementations();

    Map<String, Properties> getConfigurationDetails();

    Map<String, String> getEnvironment();

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    String getAttribute(String key);

    String getGroupFilter();

    String getConnectionURL();

    String getUsername();

    String getPassword();

    String getGroupNameAttribute();

    String getObjectClassAttribute();

    String getRoleFilter();

    String getRoleNameAttribute();

    String getUserFilter();

    String getUserNameAttribute();

    String getUserNameRdnAttribute();

    String getUserEmailAttribute();

    /**
     * The attribute on a principal that specifies their group memberships (usually "memberOf"). Not implemented by all
     * directories.
     * @return
     */
    String getUserGroupMembershipsAttribute();

    String getGroupObjectClass();

    String getGroupDescriptionAttribute();

    String getGroupMemberAttribute();

    String getRoleObjectClass();

    String getRoleDescriptionAttribute();

    String getRoleMemberAttribute();

    String getUserObjectClass();

    String getUserFirstNameAttribute();

    String getUserLastNameAttribute();

    String getUserDisplayNameAttribute();

    String getUserPasswordAttribute();

    String getUserEncryptionMethod();

    boolean isPagedResultsControl();

    int getPagedResultsSize();

    int getSearchTimeLimit();

    boolean isNestedGroupsDisabled();

    /**
     * Whether we should use the "memberOf" (or equivalent) attribute in LDAP queries.
     * @return
     */
    boolean isUsingUserMembershipAttribute();

    /**
     * Whether we should use the "memberOf" (or equivalent) attribute when fetching the list of groups a user belongs to.
     * @return
     */
    boolean isUsingUserMembershipAttributeForGroupMembership();

    /**
     * Returns true if referrals should be followed.
     *
     * @return true if referrals should be followed
     */
    boolean isReferral();

    /**
     * Whether we should use the more expensive but completely cross-directory
     * compatible method for standardising DNs when mapping object DNs and
     * and memberDNs (value = <code>false</code>); or if we can use a more
     * efficient but relaxed form of standardisation (value = <code>true</code>).
     *
     * See <code>DNStandardiser</code> for more information.
     * 
     * @return <code>false</code> if proper standardisation is required.
     */
    boolean isRelaxedDnStandardisation();

    /**
     * Returns true if roles should be disabled, as in some caching setups. The grammatical atrocity that is the name of
     * this method pains me more than you can imagine.
     * @return
     */
    boolean isRolesDisabled();

    /**
     * Returns <code>true</code> if groups and group memberships are to be mutated only
     * (created, updated, deleted) in local storage, otherwise the mutations will be
     * propagated to the underlying LDAP implementation (full read-write LDAP groups).
     *
     * @return <code>true</code> if using local storage for groups and memberships
     */
    boolean isLocalGroupsEnabled();

    /**
     * Returns the interval  in seconds when the local Cache should be synchronized with LDAP.
     *
     * @return the interval  in seconds when the local Cache should be synchronized with LDAP.
     */
    int getCacheSynchroniseInterval();

    /**
     * Get a list of Ldap Type Configuration objects.
     * @return List of LdapTypeConfigurations
     */
    List<LdapTypeConfig> getLdapTypeConfigurations();
}
