package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.model.event.Operation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="groupEvent")
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupEventEntity extends AbstractAttributeEventEntity
{
    @XmlElement(name="group")
    private final GroupEntity group;

    /**
     * JAXB requires a no-arg constructor
     */
    private GroupEventEntity()
    {
        super(null, null, null);
        this.group = null;
    }

    public GroupEventEntity(Operation operation, GroupEntity group, MultiValuedAttributeEntityList storedAttributes, MultiValuedAttributeEntityList deletedAttributes)
    {
        super(operation, storedAttributes, deletedAttributes);
        this.group = group;
    }
}
