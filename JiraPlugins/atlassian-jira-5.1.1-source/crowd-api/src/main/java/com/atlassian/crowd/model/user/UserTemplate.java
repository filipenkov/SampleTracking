package com.atlassian.crowd.model.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * A publicly mutable User implementation.
 * <p/>
 * Used to create or update a user.
 */
public class UserTemplate implements com.atlassian.crowd.model.user.User, Serializable
{
    private long directoryId;
    private String name;
    private boolean active;
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String displayName;

    /**
     * Build a template for a new user.
     * <p/>
     * Used to create a user.
     *
     * @param username    username of new user.
     * @param directoryId ID of the directory in which to store the new user.
     */
    public UserTemplate(String username, long directoryId)
    {
        Validate.isTrue(StringUtils.isNotBlank(username), "username argument cannot be null or blank");

        // lowercasing not enforced, only on the Internal User, since an LDAP user can handle both
        this.name = username;
        this.directoryId = directoryId;
    }

    public UserTemplate(final String name)
    {
        this(name, -1L);
    }

    /**
     * Build a template from an existing user.
     * <p/>
     * Used to update a user.
     *
     * @param user user to build template from.
     */
    public UserTemplate(final com.atlassian.crowd.model.user.User user)
    {
        Validate.notNull(user, "user argument cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(user.getName()), "user.name argument cannot be null or blank");

        // lowercasing not enforced, only on the Internal User, since an LDAP user can handle both
        this.name = user.getName();
        this.directoryId = user.getDirectoryId();
        this.active = user.isActive();
        this.emailAddress = user.getEmailAddress();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.displayName = user.getDisplayName();
    }

    public UserTemplate(String username, String firstName, String lastName, String displayName)
    {
        this(username);

        this.displayName = displayName;
        this.lastName = lastName;
        this.firstName = firstName;        
    }

    /**
     * Build a template from an existing user.
     * <p/>
     * Used to update a user.
     *
     * @param user user to build template from.
     */
    public UserTemplate(final com.atlassian.crowd.embedded.api.User user)
    {
        Validate.notNull(user, "user argument cannot be null");
        Validate.isTrue(StringUtils.isNotBlank(user.getName()), "user.name argument cannot be null or blank");

        // lowercasing not enforced, only on the Internal User, since an LDAP user can handle both
        this.name = user.getName();
        this.directoryId = user.getDirectoryId();
        this.active = user.isActive();
        this.emailAddress = user.getEmailAddress();
        this.displayName = user.getDisplayName();
    }

    public void setDirectoryId(long directoryId)
    {
        this.directoryId = directoryId;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public long getDirectoryId()
    {
        return directoryId;
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

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName == null ? "" : displayName;
    }

    public boolean equals(final Object o)
    {
        return UserComparator.equalsObject(this, o);
    }

    public int hashCode()
    {
        return UserComparator.hashCode(this);
    }

    public int compareTo(User other)
    {
        return UserComparator.compareTo(this, other);
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("name", name).
                append("directoryId", directoryId).
                append("active", active).
                append("emailAddress", emailAddress).
                append("firstName", firstName).
                append("lastName", lastName).
                append("displayName", displayName).
                toString();
    }
}
