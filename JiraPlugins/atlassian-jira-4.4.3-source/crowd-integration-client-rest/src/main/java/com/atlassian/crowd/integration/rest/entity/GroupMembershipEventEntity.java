package com.atlassian.crowd.integration.rest.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="groupMembershipEvent")
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupMembershipEventEntity extends AbstractEventEntity
{
    @XmlElement(name="group")
    private final GroupEntity group;

    @XmlElement(name="parentGroups")
    private final GroupEntityList parentGroups;

    @XmlElement(name="childGroups")
    private final GroupEntityList childGroups;

    /**
     * JAXB requires a no-arg constructor
     */
    private GroupMembershipEventEntity()
    {
        this.group = null;
        this.parentGroups = null;
        this.childGroups = null;
    }

    public GroupEntity getGroup()
    {
        return group;
    }

    public GroupEntityList getParentGroups()
    {
        return parentGroups;
    }

    public GroupEntityList getChildGroups()
    {
        return childGroups;
    }
}
