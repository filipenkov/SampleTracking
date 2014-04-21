package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.*;

import java.util.*;
import javax.xml.bind.annotation.*;

/**
 * Represents a multi-valued attribute.
 *
 * @since v2.1
 */
@XmlRootElement (name = "attribute")
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiValuedAttributeEntity
{
    @XmlElement (name = "link")
    private final Link link;

    @XmlAttribute (name = "name")
    private final String name;

    @XmlElementWrapper(name = "values")
    @XmlElements(@XmlElement(name = "value"))
    private final Collection<String> values;

    /**
     * JAXB requires a no-arg constructor.
     */
    private MultiValuedAttributeEntity()
    {
        name = null;
        values = null;
        link = null;
    }

    /**
     * Constructs a new AttributeEntity.
     *
     * @param name name of the attribute
     * @param values values of the attribute
     * @param link link to the attribute resource
     */
    public MultiValuedAttributeEntity(final String name, final Collection<String> values, final Link link)
    {
        this.name = name;
        this.values = values;
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
    public Collection<String> getValues()
    {
        return values;
    }

    /**
     * @return any value of the attribute
     */
    public String getValue()
    {
        return values.iterator().next();
    }

    /**
     * @return link to the attribute resource
     */
    public Link getLink()
    {
        return link;
    }
}
