package com.atlassian.johnson.event;

import com.atlassian.johnson.config.JohnsonConfig;

public final class EventLevel
{
    public static final String WARNING = "warning";
    public static final String ERROR = "error";
    public static final String FATAL = "fatal";

    private String level;
    private String description;

    public EventLevel(String level, String description)
    {
        this.level = level;
        this.description = description;
    }

    public static EventLevel get(String level)
    {
        return JohnsonConfig.getInstance().getEventLevel(level);
    }

    public String getLevel()
    {
        return level;
    }

    public String getDescription()
    {
        return description;
    }

    public String toString()
    {
        return "(EventLevel: " + level + ")";
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof EventLevel)) return false;

        final EventLevel eventLevel = (EventLevel) o;

        if (description != null ? !description.equals(eventLevel.description) : eventLevel.description != null) return false;
        if (!level.equals(eventLevel.level)) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = level.hashCode();
        result = 29 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
