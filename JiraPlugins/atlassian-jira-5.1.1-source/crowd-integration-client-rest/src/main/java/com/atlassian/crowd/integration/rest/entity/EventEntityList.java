package com.atlassian.crowd.integration.rest.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name="events")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventEntityList
{
    @XmlAttribute(name="newEventToken")
    private final String newEventToken;

    @XmlAttribute(name="incrementalSynchronisationAvailable")
    private final Boolean incrementalSynchronisationAvailable;

    @XmlElements({
            @XmlElement(name="userEvent", type=UserEventEntity.class),
            @XmlElement(name="groupEvent", type=GroupEventEntity.class),
            @XmlElement(name="userMembershipEvent", type=UserMembershipEventEntity.class),
            @XmlElement(name="groupMembershipEvent", type=GroupMembershipEventEntity.class)
    })
    private final List<AbstractEventEntity> events;

    /**
     * JAXB requires a no-arg constructor
     */
    private EventEntityList()
    {
        this.newEventToken = null;
        this.incrementalSynchronisationAvailable = null;
        this.events = null;
    }

    public String getNewEventToken()
    {
        return newEventToken;
    }

    public Boolean isIncrementalSynchronisationAvailable()
    {
        return incrementalSynchronisationAvailable;
    }

    public List<AbstractEventEntity> getEvents()
    {
        return events;
    }
}
