package com.atlassian.crowd.integration.rest.entity;

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
        this.user = null;
    }

    public UserEntity getUser()
    {
        return user;
    }
}
