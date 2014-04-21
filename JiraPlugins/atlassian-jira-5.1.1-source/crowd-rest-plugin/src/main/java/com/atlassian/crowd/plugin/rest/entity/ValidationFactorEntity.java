package com.atlassian.crowd.plugin.rest.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST version of a validation factor.
 */
@XmlRootElement (name = "validation-factor")
@XmlAccessorType (XmlAccessType.FIELD)
public class ValidationFactorEntity
{
    @XmlElement (name = "name")
    private final String name;
    @XmlElement (name = "value")
    private final String value;

    private ValidationFactorEntity()
    {
        name = null;
        value = null;
    }

    public ValidationFactorEntity(final String name, final String value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }
}
