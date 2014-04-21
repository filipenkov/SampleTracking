package com.atlassian.crowd.integration.rest.entity;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.model.user.UserWithAttributes;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;
import java.util.Set;

/**
 * Represents a User entity.
 *
 * @since v2.1
 */
@XmlRootElement (name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserEntity implements UserWithAttributes
{
    @XmlAttribute (name = "name")
    private final String name;

    @XmlElement (name = "first-name")
    private final String firstName;

    @XmlElement (name = "last-name")
    private final String lastName;

    @XmlElement (name = "display-name")
    private final String displayName;

    @XmlElement (name = "email")
    private final String emailAddress;

    @XmlElement (name = "password")
    private final PasswordEntity password; // will never be populated for a GET; only read for user creation or modification

    @XmlElement (name = "active")
    private final boolean active;

    @XmlElement(name = "attributes")
    private MultiValuedAttributeEntityList attributes;

    /**
     * JAXB requires a no-arg constructor.
     */
    private UserEntity()
    {
        this.name = null;
        this.firstName = null;
        this.lastName = null;
        this.displayName = null;
        this.emailAddress = null;
        this.password = null;
        this.active = false;
    }

    public UserEntity(final String name, final String firstName, final String lastName, final String displayName, final String emailAddress, final PasswordEntity password, final boolean active)
    {
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.active = active;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public PasswordEntity getPassword()
    {
        return password;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getName()
    {
        return name;
    }

    public void setAttributes(final MultiValuedAttributeEntityList attributes)
    {
        this.attributes = attributes;
    }

    public MultiValuedAttributeEntityList getAttributes()
    {
        return attributes;
    }

    public Set<String> getValues(final String key)
    {
        return attributes.getValues(key);
    }

    public String getValue(final String key)
    {
        return attributes.getValue(key);
    }

    public Set<String> getKeys()
    {
        return attributes.getKeys();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    public long getDirectoryId()
    {
        return 0L;
    }

    public int compareTo(final User user)
    {
        return UserComparator.compareTo(this, user);
    }

    @Override
    public boolean equals(final Object o)
    {
        return UserComparator.equalsObject(this, o);
    }

    @Override
    public int hashCode()
    {
        return UserComparator.hashCode(this);
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("name", getName()).
                append("active", isActive()).
                append("emailAddress", getEmailAddress()).
                append("firstName", getFirstName()).
                append("lastName", getLastName()).
                append("displayName", getDisplayName()).
                toString();
    }

    /**
     * Creates a new minimal user instance.
     *
     * @param username username for the user
     * @return minimal user instance
     */
    public static UserEntity newMinimalInstance(String username)
    {
        return new UserEntity(username, null, null, null, null, null, false);
    }
}
