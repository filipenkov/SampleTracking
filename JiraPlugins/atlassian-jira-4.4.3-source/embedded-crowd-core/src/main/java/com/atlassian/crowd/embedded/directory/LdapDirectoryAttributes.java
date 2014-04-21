package com.atlassian.crowd.embedded.directory;

import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import org.apache.commons.lang.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Convenience class for setting attributes for an LDAP Directory.
 * <p/>
 * This class is not thread safe.
 */
public class LdapDirectoryAttributes
{
    private String ldapUrl;
    private String ldapBasedn;
    private String ldapUserdn;
    private boolean ldapSecure;
    private String ldapPassword;
    private boolean ldapPropogateChanges;
    private String ldapUserDn;
    private String ldapGroupDn;
    private boolean ldapNestedgroupsDisabled;
    private boolean rolesDisabled = true;
    private boolean ldapPagedresults;
    private String ldapPagedresultsSize;
    private boolean ldapReferral;
    private boolean ldapUsermembershipUseForGroups;
    private boolean ldapUsermembershipUse;
    private boolean ldapRelaxedDnStandardisation;
    private String ldapUserEncryption;
    private String ldapUserObjectclass;
    private String ldapUserFilter;
    private String ldapUserUsername;
    private String ldapUserUsernameRdn;
    private String ldapUserFirstname;
    private String ldapUserLastname;
    private String ldapUserDisplayname;
    private String ldapUserEmail;
    private String ldapUserGroup;
    private String ldapUserPassword;
    private String ldapGroupObjectclass;
    private String ldapGroupFilter;
    private String ldapGroupName;
    private String ldapGroupDescription;
    private String ldapGroupUsernames;
    private boolean localGroups;
    private boolean incrementalSyncEnabled;
    private String ldapCacheSynchroniseIntervalInMin;
    private String ldapPoolInitSize;
    private String ldapPoolPrefSize;
    private String ldapPoolMaxSize;
    private String ldapPoolTimeoutInSec;
    private String ldapConnectionTimeoutInSec;
    private String ldapReadTimeoutInSec;
    private String ldapSearchTimelimitInSec;
    private String ldapAutoAddGroups;

    //----------------------------------------------------------------------
    // Getters and Setters
    //----------------------------------------------------------------------

    public String getLdapUrl()
    {
        return ldapUrl;
    }

    public void setLdapUrl(final String ldapUrl)
    {
        this.ldapUrl = ldapUrl;
    }

    public boolean isLdapSecure()
    {
        return ldapSecure;
    }

    public void setLdapSecure(final boolean ldapSecure)
    {
        this.ldapSecure = ldapSecure;
    }

    public String getLdapBasedn()
    {
        return ldapBasedn;
    }

    public void setLdapBasedn(final String ldapBasedn)
    {
        this.ldapBasedn = ldapBasedn;
    }

    public String getLdapUserdn()
    {
        return ldapUserdn;
    }

    public void setLdapUserdn(final String ldapUserdn)
    {
        this.ldapUserdn = ldapUserdn;
    }

    public String getLdapPassword()
    {
        return ldapPassword;
    }

    public void setLdapPassword(final String ldapPassword)
    {
        this.ldapPassword = ldapPassword;
    }

    public boolean isLdapPropogateChanges()
    {
        return ldapPropogateChanges;
    }

    public void setLdapPropogateChanges(final boolean ldapPropogateChanges)
    {
        this.ldapPropogateChanges = ldapPropogateChanges;
    }

    public String getLdapUserDn()
    {
        return ldapUserDn;
    }

    public void setLdapUserDn(final String ldapUserDn)
    {
        this.ldapUserDn = ldapUserDn;
    }

    public String getLdapGroupDn()
    {
        return ldapGroupDn;
    }

    public void setLdapGroupDn(final String ldapGroupDn)
    {
        this.ldapGroupDn = ldapGroupDn;
    }

    public boolean isLdapNestedgroupsDisabled()
    {
        return ldapNestedgroupsDisabled;
    }

    public void setLdapNestedgroupsDisabled(final boolean ldapNestedgroupsDisabled)
    {
        this.ldapNestedgroupsDisabled = ldapNestedgroupsDisabled;
    }

    public boolean isRolesDisabled()
    {
        return rolesDisabled;
    }

    public void setRolesDisabled(boolean rolesDisabled)
    {
        this.rolesDisabled = rolesDisabled;
    }

    public boolean isLdapPagedresults()
    {
        return ldapPagedresults;
    }

    public void setLdapPagedresults(final boolean ldapPagedresults)
    {
        this.ldapPagedresults = ldapPagedresults;
    }

    public String getLdapPagedresultsSize()
    {
        return ldapPagedresultsSize;
    }

    public void setLdapPagedresultsSize(final String ldapPagedresultsSize)
    {
        this.ldapPagedresultsSize = ldapPagedresultsSize;
    }

    public boolean isLdapReferral()
    {
        return ldapReferral;
    }

    public void setLdapReferral(final boolean ldapReferral)
    {
        this.ldapReferral = ldapReferral;
    }

    public boolean isLdapUsermembershipUseForGroups()
    {
        return ldapUsermembershipUseForGroups;
    }

    public void setLdapUsermembershipUseForGroups(final boolean ldapUsermembershipUseForGroups)
    {
        this.ldapUsermembershipUseForGroups = ldapUsermembershipUseForGroups;
    }

    public boolean isLdapUsermembershipUse()
    {
        return ldapUsermembershipUse;
    }

    public void setLdapUsermembershipUse(final boolean ldapUsermembershipUse)
    {
        this.ldapUsermembershipUse = ldapUsermembershipUse;
    }

    public boolean isLdapRelaxedDnStandardisation()
    {
        return ldapRelaxedDnStandardisation;
    }

    public void setLdapRelaxedDnStandardisation(final boolean ldapRelaxedDnStandardisation)
    {
        this.ldapRelaxedDnStandardisation = ldapRelaxedDnStandardisation;
    }

    public String getLdapUserEncryption()
    {
        return ldapUserEncryption;
    }

    public void setLdapUserEncryption(final String ldapUserEncryption)
    {
        this.ldapUserEncryption = ldapUserEncryption;
    }

    public String getLdapUserObjectclass()
    {
        return ldapUserObjectclass;
    }

    public void setLdapUserObjectclass(final String ldapUserObjectclass)
    {
        this.ldapUserObjectclass = ldapUserObjectclass;
    }

    public String getLdapUserFilter()
    {
        return ldapUserFilter;
    }

    public void setLdapUserFilter(final String ldapUserFilter)
    {
        this.ldapUserFilter = ldapUserFilter;
    }

    public String getLdapUserUsername()
    {
        return ldapUserUsername;
    }

    public void setLdapUserUsername(final String ldapUserUsername)
    {
        this.ldapUserUsername = ldapUserUsername;
    }

    public String getLdapUserUsernameRdn()
    {
        return ldapUserUsernameRdn;
    }

    public void setLdapUserUsernameRdn(final String ldapUserUsernameRdn)
    {
        this.ldapUserUsernameRdn = ldapUserUsernameRdn;
    }

    public String getLdapUserFirstname()
    {
        return ldapUserFirstname;
    }

    public void setLdapUserFirstname(final String ldapUserFirstname)
    {
        this.ldapUserFirstname = ldapUserFirstname;
    }

    public String getLdapUserLastname()
    {
        return ldapUserLastname;
    }

    public void setLdapUserLastname(final String ldapUserLastname)
    {
        this.ldapUserLastname = ldapUserLastname;
    }

    public String getLdapUserDisplayname()
    {
        return ldapUserDisplayname;
    }

    public void setLdapUserDisplayname(final String ldapUserDisplayname)
    {
        this.ldapUserDisplayname = ldapUserDisplayname;
    }

    public String getLdapUserEmail()
    {
        return ldapUserEmail;
    }

    public void setLdapUserEmail(final String ldapUserEmail)
    {
        this.ldapUserEmail = ldapUserEmail;
    }

    public String getLdapUserGroup()
    {
        return ldapUserGroup;
    }

    public void setLdapUserGroup(final String ldapUserGroup)
    {
        this.ldapUserGroup = ldapUserGroup;
    }

    public String getLdapUserPassword()
    {
        return ldapUserPassword;
    }

    public void setLdapUserPassword(final String ldapUserPassword)
    {
        this.ldapUserPassword = ldapUserPassword;
    }

    public String getLdapGroupObjectclass()
    {
        return ldapGroupObjectclass;
    }

    public void setLdapGroupObjectclass(final String ldapGroupObjectclass)
    {
        this.ldapGroupObjectclass = ldapGroupObjectclass;
    }

    public String getLdapGroupFilter()
    {
        return ldapGroupFilter;
    }

    public void setLdapGroupFilter(final String ldapGroupFilter)
    {
        this.ldapGroupFilter = ldapGroupFilter;
    }

    public String getLdapGroupName()
    {
        return ldapGroupName;
    }

    public void setLdapGroupName(final String ldapGroupName)
    {
        this.ldapGroupName = ldapGroupName;
    }

    public String getLdapGroupDescription()
    {
        return ldapGroupDescription;
    }

    public void setLdapGroupDescription(final String ldapGroupDescription)
    {
        this.ldapGroupDescription = ldapGroupDescription;
    }

    public String getLdapGroupUsernames()
    {
        return ldapGroupUsernames;
    }

    public void setLdapGroupUsernames(final String ldapGroupUsernames)
    {
        this.ldapGroupUsernames = ldapGroupUsernames;
    }

    public boolean isIncrementalSyncEnabled()
    {
        return incrementalSyncEnabled;
    }

    public void setIncrementalSyncEnabled(final boolean incrementalSyncEnabled)
    {
        this.incrementalSyncEnabled = incrementalSyncEnabled;
    }

    public String getLdapCacheSynchroniseIntervalInMin()
    {
        return ldapCacheSynchroniseIntervalInMin;
    }

    public void setLdapCacheSynchroniseIntervalInMin(final String ldapCacheSynchroniseIntervalInMin)
    {
        this.ldapCacheSynchroniseIntervalInMin = ldapCacheSynchroniseIntervalInMin;
    }

    public boolean isLocalGroups()
    {
        return localGroups;
    }

    public void setLocalGroups(final boolean localGroups)
    {
        this.localGroups = localGroups;
    }

    public String getLdapPoolInitSize()
    {
        return ldapPoolInitSize;
    }

    public void setLdapPoolInitSize(String ldapPoolInitSize)
    {
        this.ldapPoolInitSize = ldapPoolInitSize;
    }

    public String getLdapPoolMaxSize()
    {
        return ldapPoolMaxSize;
    }

    public void setLdapPoolMaxSize(String ldapPoolMaxSize)
    {
        this.ldapPoolMaxSize = ldapPoolMaxSize;
    }

    public String getLdapPoolPrefSize()
    {
        return ldapPoolPrefSize;
    }

    public void setLdapPoolPrefSize(String ldapPoolPrefSize)
    {
        this.ldapPoolPrefSize = ldapPoolPrefSize;
    }

    public String getLdapConnectionTimeoutInSec()
    {
        return ldapConnectionTimeoutInSec;
    }

    public void setLdapConnectionTimeoutInSec(String ldapConnectionTimeoutInSec)
    {
        this.ldapConnectionTimeoutInSec = ldapConnectionTimeoutInSec;
    }

    public String getLdapPoolTimeoutInSec()
    {
        return ldapPoolTimeoutInSec;
    }

    public void setLdapPoolTimeoutInSec(String ldapPoolTimeoutInSec)
    {
        this.ldapPoolTimeoutInSec = ldapPoolTimeoutInSec;
    }

    public String getLdapReadTimeoutInSec()
    {
        return ldapReadTimeoutInSec;
    }

    public void setLdapReadTimeoutInSec(String ldapReadTimeoutInSec)
    {
        this.ldapReadTimeoutInSec = ldapReadTimeoutInSec;
    }

    public String getLdapSearchTimelimitInSec()
    {
        return ldapSearchTimelimitInSec;
    }

    public void setLdapSearchTimelimitInSec(String ldapSearchTimelimitInSec)
    {
        this.ldapSearchTimelimitInSec = ldapSearchTimelimitInSec;
    }

    public void setLdapAutoAddGroups(String groups)
    {
        this.ldapAutoAddGroups = groups;
    }
    
    public String getLdapAutoAddGroups()
    {
        return ldapAutoAddGroups;
    }
    
    //----------------------------------------------------------------------
    // Converters
    //----------------------------------------------------------------------

    public Map<String, String> toAttributesMap()
    {
        // Obtains values from UI to be stored into crowd
        HashMap<String, String> map = new HashMap<String, String>(50);
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_URL_KEY, ldapUrl);
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_SECURE_KEY, String.valueOf(ldapSecure));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_BASEDN_KEY, ldapBasedn);
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_USERDN_KEY, ldapUserdn);
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_PASSWORD_KEY, ldapPassword);
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_PROPOGATE_CHANGES, String.valueOf(ldapPropogateChanges));
        addAttributeToMap(map, LDAPPropertiesMapper.USER_DN_ADDITION, ldapUserDn);
        addAttributeToMap(map, LDAPPropertiesMapper.GROUP_DN_ADDITION, ldapGroupDn);
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_NESTED_GROUPS_DISABLED, String.valueOf(ldapNestedgroupsDisabled));
        addAttributeToMap(map, LDAPPropertiesMapper.ROLES_DISABLED, String.valueOf(rolesDisabled));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_PAGEDRESULTS_KEY, String.valueOf(ldapPagedresults));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_PAGEDRESULTS_SIZE, ldapPagedresultsSize);
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_REFERRAL_KEY, String.valueOf(ldapReferral));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE_FOR_GROUP_MEMBERSHIP, String.valueOf(ldapUsermembershipUseForGroups));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE, String.valueOf(ldapUsermembershipUse));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_RELAXED_DN_STANDARDISATION, String.valueOf(ldapRelaxedDnStandardisation));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_USER_ENCRYPTION_METHOD, ldapUserEncryption);
        addAttributeToMap(map, LDAPPropertiesMapper.USER_OBJECTCLASS_KEY, ldapUserObjectclass);
        addAttributeToMap(map, LDAPPropertiesMapper.USER_OBJECTFILTER_KEY, ldapUserFilter);
        addAttributeToMap(map, LDAPPropertiesMapper.USER_USERNAME_KEY, ldapUserUsername);
        addAttributeToMap(map, LDAPPropertiesMapper.USER_USERNAME_RDN_KEY, ldapUserUsernameRdn);
        addAttributeToMap(map, LDAPPropertiesMapper.USER_FIRSTNAME_KEY, ldapUserFirstname);
        addAttributeToMap(map, LDAPPropertiesMapper.USER_LASTNAME_KEY, ldapUserLastname);
        addAttributeToMap(map, LDAPPropertiesMapper.USER_DISPLAYNAME_KEY, ldapUserDisplayname);
        addAttributeToMap(map, LDAPPropertiesMapper.USER_EMAIL_KEY, ldapUserEmail);
        addAttributeToMap(map, LDAPPropertiesMapper.USER_GROUP_KEY, ldapUserGroup);
        addAttributeToMap(map, LDAPPropertiesMapper.USER_PASSWORD_KEY, ldapUserPassword);
        addAttributeToMap(map, LDAPPropertiesMapper.GROUP_OBJECTCLASS_KEY, ldapGroupObjectclass);
        addAttributeToMap(map, LDAPPropertiesMapper.GROUP_OBJECTFILTER_KEY, ldapGroupFilter);
        addAttributeToMap(map, LDAPPropertiesMapper.GROUP_NAME_KEY, ldapGroupName);
        addAttributeToMap(map, LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY, ldapGroupDescription);
        addAttributeToMap(map, LDAPPropertiesMapper.GROUP_USERNAMES_KEY, ldapGroupUsernames);
        addAttributeToMap(map, LDAPPropertiesMapper.LOCAL_GROUPS, String.valueOf(localGroups));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_POOL_INITSIZE, ldapPoolInitSize);
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_POOL_PREFSIZE, ldapPoolPrefSize);
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_POOL_MAXSIZE, ldapPoolMaxSize);
        addAttributeToMap(map, SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED, String.valueOf(incrementalSyncEnabled));
        // Convert user input values to appropriate time units for storage
        addAttributeToMap(map, SynchronisableDirectoryProperties.CACHE_SYNCHRONISE_INTERVAL, Long.toString(NumberUtils.toLong(ldapCacheSynchroniseIntervalInMin) * 60));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_POOL_TIMEOUT, Long.toString(TimeUnit.MILLISECONDS.convert(NumberUtils.toLong(ldapPoolTimeoutInSec), TimeUnit.SECONDS)));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_CONNECTION_TIMEOUT, Long.toString(TimeUnit.MILLISECONDS.convert(NumberUtils.toLong(ldapConnectionTimeoutInSec), TimeUnit.SECONDS)));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_READ_TIMEOUT, Long.toString(TimeUnit.MILLISECONDS.convert(NumberUtils.toLong(ldapReadTimeoutInSec), TimeUnit.SECONDS)));
        addAttributeToMap(map, LDAPPropertiesMapper.LDAP_SEARCH_TIMELIMIT, Long.toString(TimeUnit.MILLISECONDS.convert(NumberUtils.toLong(ldapSearchTimelimitInSec), TimeUnit.SECONDS)));
        map.put(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS, ldapAutoAddGroups);
        return map;
    }

    private void addAttributeToMap(final HashMap<String, String> map, final String key, final String value)
    {
        // Add the entry to the map if it is not null or empty
        if (value != null && value.length() > 0)
        {
            map.put(key, value);
        }
    }

    public static LdapDirectoryAttributes fromAttributesMap(Map<String, String> map)
    {
        // Grabs values from Crowd to be displayed in the UI
        final LdapDirectoryAttributes attributes = new LdapDirectoryAttributes();
        attributes.setLdapUrl(map.get(LDAPPropertiesMapper.LDAP_URL_KEY));
        attributes.setLdapSecure(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_SECURE_KEY)));
        attributes.setLdapBasedn(map.get(LDAPPropertiesMapper.LDAP_BASEDN_KEY));
        attributes.setLdapUserdn(map.get(LDAPPropertiesMapper.LDAP_USERDN_KEY));
        attributes.setLdapPassword(map.get(LDAPPropertiesMapper.LDAP_PASSWORD_KEY));
        attributes.setLdapPropogateChanges(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_PROPOGATE_CHANGES)));
        attributes.setLdapUserDn(map.get(LDAPPropertiesMapper.USER_DN_ADDITION));
        attributes.setLdapGroupDn(map.get(LDAPPropertiesMapper.GROUP_DN_ADDITION));
        attributes.setLdapNestedgroupsDisabled(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_NESTED_GROUPS_DISABLED)));
        attributes.setRolesDisabled(Boolean.valueOf(map.get(LDAPPropertiesMapper.ROLES_DISABLED)));
        attributes.setLdapPagedresults(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_PAGEDRESULTS_KEY)));
        attributes.setLdapPagedresultsSize(map.get(LDAPPropertiesMapper.LDAP_PAGEDRESULTS_SIZE));
        attributes.setLdapReferral(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_REFERRAL_KEY)));
        attributes.setLdapUsermembershipUseForGroups(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE_FOR_GROUP_MEMBERSHIP)));
        attributes.setLdapUsermembershipUse(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_USING_USER_MEMBERSHIP_ATTRIBUTE)));
        attributes.setLdapRelaxedDnStandardisation(Boolean.valueOf(map.get(LDAPPropertiesMapper.LDAP_RELAXED_DN_STANDARDISATION)));
        attributes.setLdapUserEncryption(map.get(LDAPPropertiesMapper.LDAP_USER_ENCRYPTION_METHOD));
        attributes.setLdapUserObjectclass(map.get(LDAPPropertiesMapper.USER_OBJECTCLASS_KEY));
        attributes.setLdapUserFilter(map.get(LDAPPropertiesMapper.USER_OBJECTFILTER_KEY));
        attributes.setLdapUserUsername(map.get(LDAPPropertiesMapper.USER_USERNAME_KEY));
        attributes.setLdapUserUsernameRdn(map.get(LDAPPropertiesMapper.USER_USERNAME_RDN_KEY));
        attributes.setLdapUserFirstname(map.get(LDAPPropertiesMapper.USER_FIRSTNAME_KEY));
        attributes.setLdapUserLastname(map.get(LDAPPropertiesMapper.USER_LASTNAME_KEY));
        attributes.setLdapUserDisplayname(map.get(LDAPPropertiesMapper.USER_DISPLAYNAME_KEY));
        attributes.setLdapUserEmail(map.get(LDAPPropertiesMapper.USER_EMAIL_KEY));
        attributes.setLdapUserGroup(map.get(LDAPPropertiesMapper.USER_GROUP_KEY));
        attributes.setLdapUserPassword(map.get(LDAPPropertiesMapper.USER_PASSWORD_KEY));
        attributes.setLdapGroupObjectclass(map.get(LDAPPropertiesMapper.GROUP_OBJECTCLASS_KEY));
        attributes.setLdapGroupFilter(map.get(LDAPPropertiesMapper.GROUP_OBJECTFILTER_KEY));
        attributes.setLdapGroupName(map.get(LDAPPropertiesMapper.GROUP_NAME_KEY));
        attributes.setLdapGroupDescription(map.get(LDAPPropertiesMapper.GROUP_DESCRIPTION_KEY));
        attributes.setLdapGroupUsernames(map.get(LDAPPropertiesMapper.GROUP_USERNAMES_KEY));
        attributes.setLocalGroups(Boolean.valueOf(map.get(LDAPPropertiesMapper.LOCAL_GROUPS)));
        attributes.setLdapPoolInitSize(map.get(LDAPPropertiesMapper.LDAP_POOL_INITSIZE));
        attributes.setLdapPoolPrefSize(map.get(LDAPPropertiesMapper.LDAP_POOL_PREFSIZE));
        attributes.setLdapPoolMaxSize(map.get(LDAPPropertiesMapper.LDAP_POOL_MAXSIZE));
        attributes.setIncrementalSyncEnabled(Boolean.parseBoolean(map.get(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)));
        // Convert polling interval to minutes and timeout values to seconds to display to user
        attributes.setLdapCacheSynchroniseIntervalInMin(Long.toString(NumberUtils.toLong(map.get(SynchronisableDirectoryProperties.CACHE_SYNCHRONISE_INTERVAL))/60));
        attributes.setLdapPoolTimeoutInSec(Long.toString(TimeUnit.SECONDS.convert(NumberUtils.toLong(map.get(LDAPPropertiesMapper.LDAP_POOL_TIMEOUT)), TimeUnit.MILLISECONDS)));
        attributes.setLdapConnectionTimeoutInSec(Long.toString(TimeUnit.SECONDS.convert(NumberUtils.toLong(map.get(LDAPPropertiesMapper.LDAP_CONNECTION_TIMEOUT)), TimeUnit.MILLISECONDS)));
        attributes.setLdapReadTimeoutInSec(Long.toString(TimeUnit.SECONDS.convert(NumberUtils.toLong(map.get(LDAPPropertiesMapper.LDAP_READ_TIMEOUT)), TimeUnit.MILLISECONDS)));
        attributes.setLdapSearchTimelimitInSec(Long.toString(TimeUnit.SECONDS.convert(NumberUtils.toLong(map.get(LDAPPropertiesMapper.LDAP_SEARCH_TIMELIMIT)), TimeUnit.MILLISECONDS)));
        attributes.setLdapAutoAddGroups(map.get(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));

        return attributes;
    }
}