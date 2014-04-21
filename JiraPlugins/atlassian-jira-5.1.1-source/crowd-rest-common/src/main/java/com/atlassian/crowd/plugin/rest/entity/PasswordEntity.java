package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.*;

import javax.xml.bind.annotation.*;

/**
 * Represents a password entity.
 * 
 * @since v2.1
 */
@XmlRootElement (name = "password")
@XmlAccessorType(XmlAccessType.FIELD)
public class PasswordEntity
{
    @XmlElement (name = "link")
    private final Link link;

    @XmlElement (name = "value")
    private final String value;

    /**
     * JAXB requires a no-arg constructor.
     */
    private PasswordEntity()
    {
        this.value = null;
        this.link = null;
    }

    /**
     * Constructs a password entity.
     *
     * @param value value of the password
     * @param link link to the password resource.
     */
    public PasswordEntity(final String value, final Link link)
    {
        this.value = value;
        this.link = link;
    }

    public String getValue()
    {
        return value;
    }

    public Link getLink()
    {
        return link;
    }
}
