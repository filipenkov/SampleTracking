package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.model.event.Operation;

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
        super(null);
        this.group = null;
        this.parentGroups = null;
        this.childGroups = null;
    }

    public GroupMembershipEventEntity(final Operation operation, final GroupEntity childGroup, final GroupEntityList parentGroups, final GroupEntityList childGroups)
    {
        super(operation);
        this.group = childGroup;
        this.parentGroups = parentGroups;
        this.childGroups = childGroups;
    }
}
