package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.model.event.Operation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractAttributeEventEntity extends AbstractEventEntity
{

    @XmlElement(name = "storedAttributes")
    private MultiValuedAttributeEntityList storedAttributes;

    @XmlElement(name = "deletedAttributes")
    private MultiValuedAttributeEntityList deletedAttributes;

    /**
     * JAXB requires a no-arg constructor
     */
    private AbstractAttributeEventEntity()
    {
        super(null);
        this.storedAttributes = null;
        this.deletedAttributes = null;
    }

    protected AbstractAttributeEventEntity(Operation operation, MultiValuedAttributeEntityList storedAttributes, MultiValuedAttributeEntityList deletedAttributes)
    {
        super(operation);
        this.storedAttributes = storedAttributes;
        this.deletedAttributes = deletedAttributes;
    }
}
