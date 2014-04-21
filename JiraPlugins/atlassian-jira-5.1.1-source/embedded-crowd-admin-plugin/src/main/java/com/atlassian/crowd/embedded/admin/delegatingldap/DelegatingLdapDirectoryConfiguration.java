package com.atlassian.crowd.embedded.admin.delegatingldap;

/**
 * Configuration for an LDAP directory in Embedded Crowd.
 */
public final class DelegatingLdapDirectoryConfiguration
{
    private long directoryId;
    private boolean active = true;
    private String name = "Delegated LDAP Authentication";
    private String type;
    private String hostname;
    private int port = 389;
    private boolean useSSL;

    // all the fields below map exactly on to attributes of LdapDirectoryAttributes - do not rename them!
    private String ldapBasedn;
    private String ldapUserdn;
    private String ldapPassword;

    private String ldapUserDn;
    private String ldapUserObjectclass;
    private String ldapUserFilter;
    private String ldapUserUsername;
    private String ldapUserUsernameRdn;
    private String ldapUserFirstname;
    private String ldapUserLastname;
    private String ldapUserDisplayname;
    private String ldapUserEmail;

    private String ldapGroupDn;
    private String ldapGroupObjectclass;
    private String ldapGroupFilter;
    private String ldapGroupName;
    private String ldapGroupDescription;

    private String ldapGroupUsernames;
    private String ldapUserGroup;
    private boolean ldapUsermembershipUseForGroups;
    private boolean ldapUsermembershipUse;

    private boolean synchroniseGroupMemberships;
    private boolean createUserOnAuth;
    private String ldapAutoAddGroups;
    private boolean ldapPagedresults;
    private String ldapPagedresultsSize = "1000";
    private boolean ldapReferral;

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

    public void setType(String type)
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

    public String getLdapUserDn()
    {
        return ldapUserDn;
    }

    public void setLdapUserDn(String ldapUserDn)
    {
        this.ldapUserDn = ldapUserDn;
    }

    public String getLdapUserObjectclass()
    {
        return ldapUserObjectclass;
    }

    public void setLdapUserObjectclass(String ldapUserObjectclass)
    {
        this.ldapUserObjectclass = ldapUserObjectclass;
    }

    public String getLdapUserFilter()
    {
        return ldapUserFilter;
    }

    public void setLdapUserFilter(String ldapUserFilter)
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

    public void setLdapUserUsernameRdn(String ldapUserUsernameRdn)
    {
        this.ldapUserUsernameRdn = ldapUserUsernameRdn;
    }

    public String getLdapUserFirstname()
    {
        return ldapUserFirstname;
    }

    public void setLdapUserFirstname(String ldapUserFirstname)
    {
        this.ldapUserFirstname = ldapUserFirstname;
    }

    public String getLdapUserLastname()
    {
        return ldapUserLastname;
    }

    public void setLdapUserLastname(String ldapUserLastname)
    {
        this.ldapUserLastname = ldapUserLastname;
    }

    public String getLdapUserDisplayname()
    {
        return ldapUserDisplayname;
    }

    public void setLdapUserDisplayname(String ldapUserDisplayname)
    {
        this.ldapUserDisplayname = ldapUserDisplayname;
    }

    public String getLdapUserEmail()
    {
        return ldapUserEmail;
    }

    public void setLdapUserEmail(String ldapUserEmail)
    {
        this.ldapUserEmail = ldapUserEmail;
    }

    public boolean isCreateUserOnAuth()
    {
        return createUserOnAuth;
    }

    public void setCreateUserOnAuth(boolean createUserOnAuth)
    {
        this.createUserOnAuth = createUserOnAuth;
    }

    public boolean isNewDirectory()
    {
        return directoryId <= 0;
    }

    public boolean isNewForm()
    {
        return newForm;
    }

    public void setNewForm(boolean newForm)
    {
        this.newForm = newForm;
    }

    public void setLdapAutoAddGroups(String groups)
    {
        this.ldapAutoAddGroups = groups;
    }

    public String getLdapAutoAddGroups()
    {
        return ldapAutoAddGroups;
    }

    public String getLdapGroupObjectclass()
    {
        return ldapGroupObjectclass;
    }

    public void setLdapGroupObjectclass(String ldapGroupObjectclass)
    {
        this.ldapGroupObjectclass = ldapGroupObjectclass;
    }

    public String getLdapGroupFilter()
    {
        return ldapGroupFilter;
    }

    public void setLdapGroupFilter(String ldapGroupFilter)
    {
        this.ldapGroupFilter = ldapGroupFilter;
    }

    public String getLdapGroupName()
    {
        return ldapGroupName;
    }

    public void setLdapGroupName(String ldapGroupName)
    {
        this.ldapGroupName = ldapGroupName;
    }

    public String getLdapGroupDescription()
    {
        return ldapGroupDescription;
    }

    public void setLdapGroupDescription(String ldapGroupDescription)
    {
        this.ldapGroupDescription = ldapGroupDescription;
    }

    public String getLdapGroupUsernames()
    {
        return ldapGroupUsernames;
    }

    public void setLdapGroupUsernames(String ldapGroupUsernames)
    {
        this.ldapGroupUsernames = ldapGroupUsernames;
    }

    public String getLdapUserGroup()
    {
        return ldapUserGroup;
    }

    public void setLdapUserGroup(String ldapUserGroup)
    {
        this.ldapUserGroup = ldapUserGroup;
    }

    public boolean isLdapUsermembershipUseForGroups()
    {
        return ldapUsermembershipUseForGroups;
    }

    public void setLdapUsermembershipUseForGroups(boolean ldapUsermembershipUseForGroups)
    {
        this.ldapUsermembershipUseForGroups = ldapUsermembershipUseForGroups;
    }

    public boolean isLdapUsermembershipUse()
    {
        return ldapUsermembershipUse;
    }

    public void setLdapUsermembershipUse(boolean ldapUsermembershipUse)
    {
        this.ldapUsermembershipUse = ldapUsermembershipUse;
    }

    public boolean isSynchroniseGroupMemberships()
    {
        return synchroniseGroupMemberships;
    }

    public void setSynchroniseGroupMemberships(boolean synchroniseGroupMemberships)
    {
        this.synchroniseGroupMemberships = synchroniseGroupMemberships;
    }

    public String getLdapGroupDn()
    {
        return ldapGroupDn;
    }

    public void setLdapGroupDn(String ldapGroupDn)
    {
        this.ldapGroupDn = ldapGroupDn;
    }

    public boolean isLdapPagedresults()
    {
        return ldapPagedresults;
    }

    public void setLdapPagedresults(boolean ldapPagedresults)
    {
        this.ldapPagedresults = ldapPagedresults;
    }

    public String getLdapPagedresultsSize()
    {
        return ldapPagedresultsSize;
    }

    public void setLdapPagedresultsSize(String ldapPagedresultsSize)
    {
        this.ldapPagedresultsSize = ldapPagedresultsSize;
    }

    public boolean isLdapReferral()
    {
        return ldapReferral;
    }

    public void setLdapReferral(boolean ldapReferral)
    {
        this.ldapReferral = ldapReferral;
    }
}
