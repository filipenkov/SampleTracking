package com.atlassian.crowd.integration.rest.entity;

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
    @XmlElement (name = "value")
    private final String value;

    /**
     * JAXB requires a no-arg constructor.
     */
    private PasswordEntity()
    {
        this.value = null;
    }

    /**
     * Constructs a password entity.
     *
     * @param value value of the password
     */
    public PasswordEntity(final String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}
