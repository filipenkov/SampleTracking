package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.model.event.Operation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="userEvent")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserEventEntity extends AbstractAttributeEventEntity
{
    @XmlElement(name="user")
    private final UserEntity user;

    /**
     * JAXB requires a no-arg constructor
     */
    private UserEventEntity()
    {
        super(null, null, null);
        this.user = null;
    }

    public UserEventEntity(Operation operation, UserEntity user, MultiValuedAttributeEntityList storedAttributes, MultiValuedAttributeEntityList deletedAttributes)
    {
        super(operation, storedAttributes, deletedAttributes);
        this.user = user;
    }
}
