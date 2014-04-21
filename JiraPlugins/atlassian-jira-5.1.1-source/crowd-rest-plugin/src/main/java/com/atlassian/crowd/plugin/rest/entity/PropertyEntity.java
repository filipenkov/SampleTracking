package com.atlassian.crowd.plugin.rest.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a property entity.
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class PropertyEntity
{
    @XmlElement (name = "name")
    private final String name;
    @XmlElement (name = "type")
    private final String type;

    /**
     * JAXB requires a no-arg constructor.
     */
    private PropertyEntity()
    {
        name = null;
        type = null;
    }

    /**
     * Constructs a property with the specified name and type.
     *
     * @param name name of the property
     * @param type type of the property
     */
    public PropertyEntity(final String name, final String type)
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }
}
