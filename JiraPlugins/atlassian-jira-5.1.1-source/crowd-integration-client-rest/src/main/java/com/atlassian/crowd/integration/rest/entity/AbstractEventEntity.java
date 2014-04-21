package com.atlassian.crowd.integration.rest.entity;

import com.atlassian.crowd.model.event.Operation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractEventEntity
{

    @XmlElement
    private final Operation operation;

    /**
     * JAXB requires a no-arg constructor
     */
    protected AbstractEventEntity()
    {
        this.operation = null;
    }

    public Operation getOperation()
    {
        return operation;
    }
}
