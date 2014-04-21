package com.atlassian.crowd.integration.rest.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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

    @XmlAttribute
    private final Boolean absolute;

    /**
     * JAXB requires a no-arg constructor
     */
    private UserMembershipEventEntity()
    {
        this.childUser = null;
        this.parentGroups = null;
        this.absolute = null;
    }

    public UserEntity getChildUser()
    {
        return childUser;
    }

    public GroupEntityList getParentGroups()
    {
        return parentGroups;
    }

    public Boolean getAbsolute()
    {
        return absolute;
    }
}
