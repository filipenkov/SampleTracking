package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.*;

import java.util.*;
import javax.xml.bind.annotation.*;

/**
 * Represents a single-valued attribute.
 *
 * @since v2.1
 */
@XmlRootElement (name = "attribute")
@XmlAccessorType(XmlAccessType.FIELD)
public class AttributeEntity
{
    @XmlElement (name = "link")
    private final Link link;

    @XmlAttribute (name = "name")
    private final String name;

    @XmlElement(name = "value")
    private final String value;

    /**
     * JAXB requires a no-arg constructor.
     */
    private AttributeEntity()
    {
        name = null;
        value = null;
        link = null;
    }

    /**
     * Constructs a new AttributeEntity.
     *
     * @param name name of the attribute
     * @param value value of the attribute
     * @param link link to the attribute resource
     */
    public AttributeEntity(final String name, final String value, final Link link)
    {
        this.name = name;
        this.value = value;
        this.link = link;
    }

    /**
     * @return name of the attribute
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return values of the attribute
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @return link to the attribute resource
     */
    public Link getLink()
    {
        return link;
    }
}
