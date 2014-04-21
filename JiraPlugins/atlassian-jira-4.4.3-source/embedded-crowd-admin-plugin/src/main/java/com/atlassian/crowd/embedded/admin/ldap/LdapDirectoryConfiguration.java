package com.atlassian.crowd.embedded.admin.ldap;

import com.atlassian.crowd.embedded.api.PermissionOption;
import com.atlassian.crowd.embedded.admin.directory.LdapDirectoryAttributes;

/**
 * Configuration for an LDAP directory in Embedded Crowd.
 */
public final class LdapDirectoryConfiguration
{
    private long directoryId;
    private boolean active = true;
    private String name = "LDAP server"; // ultimate fallback value - not normally used because it isn't localised
    private String type;
    private String hostname;
    private int port = 389;
    private boolean useSSL;
    private PermissionOption ldapPermissionOption = PermissionOption.READ_ONLY;
    private boolean nestedGroupsEnabled;
    private boolean rolesDisabled = true;
    private String ldapAutoAddGroups;

    // all the fields below map exactly on to attributes of LdapDirectoryAttributes - do not rename them!
    private String ldapBasedn;
    private String ldapUserdn;
    private String ldapPassword;
    private String ldapUserDn;
    private String ldapGroupDn;
    private boolean ldapPagedresults;
    private String ldapPagedresultsSize = "1000";
    private boolean ldapReferral;
    private boolean ldapUsermembershipUseForGroups;
    private boolean ldapUsermembershipUse;
    private boolean ldapRelaxedDnStandardisation;
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
    private String ldapUserEncryption = "sha";
    private String ldapGroupObjectclass;
    private String ldapGroupFilter;
    private String ldapGroupName;
    private String ldapGroupDescription;
    private String ldapGroupUsernames;
    private boolean crowdSyncIncrementalEnabled = true;
    private String ldapCacheSynchroniseIntervalInMin = "60"; // in minutes
    private String ldapConnectionTimeoutInSec = "10"; // in seconds
    private String ldapReadTimeoutInSec = "120"; // in seconds (2 minutes)
    private String ldapSearchTimelimitInSec = "60"; // in seconds (1 minutes)

    private boolean newForm = true;

    public long getDirectoryId()
    {
        return directoryId;
    }

    public void setDirectoryId(long directoryId)
    {
        this.directoryId = directoryId;
    }

    public boolean isNewConfiguration()
    {
        return directoryId == 0;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(final String type)
    {
        this.type = type;
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname(final String hostname)
    {
        this.hostname = hostname;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(final int port)
    {
        this.port = port;
    }

    public boolean isUseSSL()
    {
        return useSSL;
    }

    public void setUseSSL(final boolean useSSL)
    {
        this.useSSL = useSSL;
    }

    public String getLdapUrl()
    {
        return (useSSL ? "ldaps" : "ldap") + "://" + hostname + ":" + port;
    }

    public void setLdapUrl(String url)
    {
        if (url == null || url.length() == 0 || !url.contains(("://")))
        {
            return;
        }
        useSSL = url.startsWith("ldaps");
        String remainder = url.substring(url.indexOf("://") + 3);
        if (remainder.contains(":"))
        {
            int index = remainder.indexOf(":");
            hostname = remainder.substring(0, index);
            port = Integer.parseInt(remainder.substring(index + 1));
        }
        else
        {
            hostname = remainder;
        }
    }

    // not actually used anywhere but need a value to make the property copying work
    public boolean isLdapSecure()
    {
        return useSSL;
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

    /**
     * Enum version of permissions which is mapped to {@link #isLdapPropogateChanges()} below.
     */
    public PermissionOption getLdapPermissionOption()
    {
        return ldapPermissionOption;
    }

    /**
     * Enum version of permissions which is mapped to {@link #isLdapPropogateChanges()} below.
     */
    public void setLdapPermissionOption(PermissionOption ldapPermissionOption)
    {
        this.ldapPermissionOption = ldapPermissionOption;
    }

    /**
     * Do not rename. Mapped to {@link LdapDirectoryAttributes#isLdapPropogateChanges()} .
     */
    public boolean isLdapPropogateChanges()
    {
        return ldapPermissionOption == PermissionOption.READ_WRITE;
    }

    /**
     * Do not rename. Mapped to {@link LdapDirectoryAttributes#getLdapUserDn()}.
     */
    public String getLdapUserDn()
    {
        return ldapUserDn;
    }

    /**
     * Do not rename. Mapped to {@link LdapDirectoryAttributes#setLdapUserDn(String)}.
     */
    public void setLdapUserDn(final String ldapUserDn)
    {
        this.ldapUserDn = ldapUserDn;
    }

    /**
     * Do not rename. Mapped to {@link LdapDirectoryAttributes#getLdapGroupDn()}.
     */
    public String getLdapGroupDn()
    {
        return ldapGroupDn;
    }

    /**
     * Do not rename. Mapped to {@link LdapDirectoryAttributes#setLdapGroupDn(String)}.
     */
    public void setLdapGroupDn(final String ldapGroupDn)
    {
        this.ldapGroupDn = ldapGroupDn;
    }

    /**
     * The real semantics for {@link #isLdapNestedgroupsDisabled()} provided for UI mapping and clarity of intent.
     */
    public boolean isNestedGroupsEnabled()
    {
        return nestedGroupsEnabled;
    }

    /**
     * The real semantics for {@link #setLdapNestedgroupsDisabled(boolean)} provided for UI mapping and clarity of intent.
     */
    public void setNestedGroupsEnabled(boolean nestedGroupsEnabled)
    {
        this.nestedGroupsEnabled = nestedGroupsEnabled;
    }

    public String getLdapAutoAddGroups()
    {
        return ldapAutoAddGroups;
    }

    public void setLdapAutoAddGroups(String ldapAutoAddGroups)
    {
        this.ldapAutoAddGroups = ldapAutoAddGroups;
    }

    /**
     * Do not rename. Mapped to {@link LdapDirectoryAttributes#isLdapNestedgroupsDisabled()},
     * which actually does the opposite of what its name says.
     */
    public boolean isLdapNestedgroupsDisabled()
    {
        return !nestedGroupsEnabled;
    }

    /**
     * Do not rename. Mapped to {@link LdapDirectoryAttributes#setLdapNestedgroupsDisabled(boolean)},
     * which actually does the opposite of what its name says.
     */
    public void setLdapNestedgroupsDisabled(final boolean ldapNestedgroupsDisabled)
    {
        this.nestedGroupsEnabled = !ldapNestedgroupsDisabled;
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

    public boolean isNewDirectory()
    {
        return directoryId <= 0;
    }

    public String getLdapConnectionTimeoutInSec()
    {
        return ldapConnectionTimeoutInSec;
    }

    public void setLdapConnectionTimeoutInSec(String ldapConnectionTimeoutInSec)
    {
        this.ldapConnectionTimeoutInSec = ldapConnectionTimeoutInSec;
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

    public boolean isCrowdSyncIncrementalEnabled()
    {
        return crowdSyncIncrementalEnabled;
    }

    public void setCrowdSyncIncrementalEnabled(boolean crowdSyncIncrementalEnabled)
    {
        this.crowdSyncIncrementalEnabled = crowdSyncIncrementalEnabled;
    }

    public String getLdapCacheSynchroniseIntervalInMin()
    {
        return ldapCacheSynchroniseIntervalInMin;
    }

    public void setLdapCacheSynchroniseIntervalInMin(String ldapCacheSynchroniseIntervalInMin)
    {
        this.ldapCacheSynchroniseIntervalInMin = ldapCacheSynchroniseIntervalInMin;
    }

    public boolean getLocalGroups()
    {
        return PermissionOption.READ_ONLY_LOCAL_GROUPS.equals(ldapPermissionOption);
    }

    public void setLocalGroups(boolean localGroups)
    {
        if (localGroups)
            setLdapPermissionOption(PermissionOption.READ_ONLY_LOCAL_GROUPS);
    }

    public boolean isNewForm()
    {
        return newForm;
    }

    public void setNewForm(final boolean newForm)
    {
        this.newForm = newForm;
    }
}
