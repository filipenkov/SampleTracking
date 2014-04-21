package com.atlassian.crowd.integration.rest.entity;

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
    protected AbstractAttributeEventEntity()
    {
        this.storedAttributes = null;
        this.deletedAttributes = null;
    }

    public MultiValuedAttributeEntityList getStoredAttributes()
    {
        return storedAttributes;
    }

    public MultiValuedAttributeEntityList getDeletedAttributes()
    {
        return deletedAttributes;
    }
}
