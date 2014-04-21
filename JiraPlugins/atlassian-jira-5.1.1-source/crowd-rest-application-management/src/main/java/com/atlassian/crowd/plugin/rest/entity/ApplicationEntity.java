package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.Link;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;

/**
 * Represents an Application entity.
 *
 * @since 2.2
 */
@XmlRootElement (name = "application")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationEntity
{
    @XmlElement (name = "link")
    private Link link;

    @XmlAttribute (name = "id")
    private Long id;

    @XmlElement (name = "name")
    private String name;

    @XmlElement (name = "type")
    private String type;

    @XmlElement (name = "description")
    private String description;

    @XmlElement (name = "active")
    private Boolean active;

    @XmlElement(name = "attributes")
    private AttributeEntityList attributes;

    @XmlElement (name = "password")
    private PasswordEntity password; // will never be populated for a GET; only read for application creation or modification

    @XmlElement (name = "directory-mappings")
    private DirectoryMappingEntityList directoryMappings;

    @XmlElement (name = "remote-addresses")
    private RemoteAddressEntitySet remoteAddresses;

    @XmlElement (name = "lowercase-output")
    private Boolean lowercaseOutput;

    @XmlElement (name = "aliasing-enabled")
    private Boolean aliasingEnabled;

    /**
     * JAXB requires a no-arg constructor.
     */
    private ApplicationEntity()
    {
        this.id = null;
        this.name = null;
        this.type = null;
        this.description = null;
        this.active = null;
        this.attributes = null;
        this.password = null;
        this.directoryMappings = null;
        this.remoteAddresses = null;
        this.lowercaseOutput = null;
        this.aliasingEnabled = null;
        this.link = null;
    }

    public ApplicationEntity(final Long id, final String name, final String type, final String description, final Boolean active, final PasswordEntity password, final Boolean lowercaseOutput, final Boolean aliasingEnabled, final Link link)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.active = active;
        this.password = password;
        this.lowercaseOutput = lowercaseOutput;
        this.aliasingEnabled = aliasingEnabled;
        this.link = link;
    }

    public Long getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public Boolean isActive()
    {
        return active;
    }

    public void setPassword(final PasswordEntity password)
    {
        this.password = password;
    }

    public PasswordEntity getPassword()
    {
        return password;
    }

    public Boolean isLowercaseOutput()
    {
        return lowercaseOutput;
    }

    public Boolean isAliasingEnabled()
    {
        return aliasingEnabled;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setType(final String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public void setAttributes(final AttributeEntityList attributes)
    {
        this.attributes = attributes;
    }

    public AttributeEntityList getAttributes()
    {
        return attributes;
    }

    public void setDirectoryMappings(final DirectoryMappingEntityList directoryMappings)
    {
        this.directoryMappings = directoryMappings;
    }

    public DirectoryMappingEntityList getDirectoryMappings()
    {
        return directoryMappings;
    }

    public void setRemoteAddresses(final RemoteAddressEntitySet remoteAddresses)
    {
        this.remoteAddresses = remoteAddresses;
    }

    public RemoteAddressEntitySet getRemoteAddresses()
    {
        return remoteAddresses;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("name", getName()).
                append("description", getDescription()).
                append("attributes", getAttributes()).
                append("directoryMappings", getDirectoryMappings()).
                append("remoteAddresses", getRemoteAddresses()).
                append("lowercaseOutput", isLowercaseOutput()).
                append("aliasingEnabled", isAliasingEnabled()).
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
}
