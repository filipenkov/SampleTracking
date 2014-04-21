package com.atlassian.crowd.integration.rest.entity;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * Represents a multi-valued attribute.
 *
 * @since v2.1
 */
@XmlRootElement (name = "attribute")
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiValuedAttributeEntity
{
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
    }

    /**
     * Constructs a new MultiValuedAttributeEntity.
     *
     * @param name name of the attribute
     * @param values values of the attribute
     */
    public MultiValuedAttributeEntity(final String name, final Collection<String> values)
    {
        this.name = name;
        this.values = values;
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
}
