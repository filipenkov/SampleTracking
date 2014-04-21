package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.model.event.Operation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="userMembershipEvent")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserMembershipEventEntity extends AbstractEventEntity
{
    @XmlElement(name="childUser")
    private final UserEntity childUser;

    @XmlElement(name="parentGroups")
    private final GroupEntityList parentGroups;

    /**
     * JAXB requires a no-arg constructor
     */
    private UserMembershipEventEntity()
    {
        super(null);
        this.childUser = null;
        this.parentGroups = null;
    }

    public UserMembershipEventEntity(final Operation operation, final UserEntity childUser, final GroupEntityList parentGroups)
    {
        super(operation);
        this.childUser = childUser;
        this.parentGroups = parentGroups;
    }
}
