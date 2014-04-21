package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.expand.Expandable;
import com.atlassian.plugins.rest.common.expand.Expander;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;

/**
 * Represents a User entity.
 *
 * @since v2.1
 */
@XmlRootElement (name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
@Expander(UserEntityExpander.class)
public class UserEntity implements NamedEntity
{
    /**
     * Name of the attributes field.
     */
    public static final String ATTRIBUTES_FIELD_NAME = "attributes";

    @SuppressWarnings("unused")
    @XmlAttribute
    private String expand;

    @XmlElement (name = "link")
    private Link link;

    @XmlAttribute (name = "name")
    private String name;

    @XmlElement (name = "first-name")
    private String firstName;

    @XmlElement (name = "last-name")
    private String lastName;

    @XmlElement (name = "display-name")
    private String displayName;

    @XmlElement (name = "email")
    private String emailAddress;

    @XmlElement (name = "password")
    private PasswordEntity password; // will never be populated for a GET; only read for user creation or modification

    /**
     * Only used when creating a minimal UserEntity.
     *
     * @see {@link #newMinimalUserEntity(String, String, com.atlassian.plugins.rest.common.Link)}
     */
    @XmlTransient
    private String applicationName;

    @XmlElement (name = "active")
    private Boolean active;

    @Expandable
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
        this.active = null;
        this.link = null;
    }

    public UserEntity(final String name, final String firstName, final String lastName, final String displayName, final String emailAddress, final PasswordEntity password, final Boolean active, final Link link)
    {
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.active = active;
        this.link = link;
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

    public void setEmail(final String email)
    {
        this.emailAddress = email;
    }

    public String getEmail()
    {
        return emailAddress;
    }

    public void setPassword(final PasswordEntity password)
    {
        this.password = password;
    }

    public PasswordEntity getPassword()
    {
        return password;
    }

    public void setActive(final boolean active)
    {
        this.active = active;
    }

    public Boolean isActive()
    {
        return active;
    }

    public void setName(final String name)
    {
        this.name = name;
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

    /**
     * Returns the application name. Should only be used by
     * {@link com.atlassian.crowd.plugin.rest.entity.UserEntityExpander} to expand the UserEntity.
     *
     * @return application name
     */
    String getApplicationName()
    {
        return applicationName;
    }

    /**
     * Creates a <tt>UserEntity</tt> with the minimal amount of information required.
     *
     * @param name Username.
     * @param applicationName Name of the application.
     * @param link Link to the canonical representation of the user. E.g. "/user?username=<username>".
     * @return UserEntity
     */
    public static UserEntity newMinimalUserEntity(final String name, final String applicationName, final Link link)
    {
        UserEntity user = new UserEntity(name, null, null, null, null, null, null, link);
        user.applicationName = applicationName;
        return user;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("name", getName()).
                append("active", isActive()).
                append("emailAddress", getEmail()).
                append("firstName", getFirstName()).
                append("lastName", getLastName()).
                append("displayName", getDisplayName()).
                toString();
    }

    public void setLink(final Link link)
    {
        this.link = link;
    }

    public Link getLink()
    {
        return link;
    }

    /**
     * Does this object represent an expanded user, or does it only contain a username.
     *
     * @return true if this object represents an expanded user
     */
    public boolean isExpanded()
    {
        return applicationName == null;
    }
}
