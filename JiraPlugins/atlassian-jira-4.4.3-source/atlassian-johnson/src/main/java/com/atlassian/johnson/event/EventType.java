package com.atlassian.johnson.event;

import com.atlassian.johnson.config.JohnsonConfig;

public final class EventType
{
    private String type;
    private String description;

    public EventType(String type, String description)
    {
        this.type = type;
        this.description = description;
    }

    public String getType()
    {
        return type;
    }

    public String getDescription()
    {
        return description;
    }

    public static EventType get(String type)
    {
        return JohnsonConfig.getInstance().getEventType(type);
    }

    public String toString()
    {
        return "(EventType: " + type + ")";
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof EventType)) return false;

        final EventType eventType = (EventType) o;

        if (description != null ? !description.equals(eventType.description) : eventType.description != null) return false;
        if (!type.equals(eventType.type)) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = type.hashCode();
        result = 29 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
