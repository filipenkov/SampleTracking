package com.atlassian.johnson.event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * This class represents an ApplicationEvent
 */
public class Event
{
    private static String getFormattedDate()
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private EventType key;
    private String desc;
    private String exception;
    private EventLevel level;
    private String date;
    private int progress = -1;
    private final Map attributes;

    public Event(EventType key, String desc)
    {
        this.key = key;
        this.desc = desc;
        this.date = getFormattedDate();
        this.attributes = new HashMap();
    }

    public Event(EventType key, String desc, String exception)
    {
        this.key = key;
        this.desc = desc;
        this.exception = exception;
        this.date = getFormattedDate();
        this.attributes = new HashMap();
    }

    public Event(EventType key, String desc, EventLevel level)
    {
        this.key = key;
        this.desc = desc;
        this.level = level;
        this.date = getFormattedDate();
        this.attributes = new HashMap();
    }

    public Event(EventType key, String desc, String exception, EventLevel level)
    {
        this.key = key;
        this.desc = desc;
        this.exception = exception;
        this.level = level;
        this.date = getFormattedDate();
        this.attributes = new HashMap();
    }

    public EventType getKey()
    {
        return key;
    }

    public void setKey(EventType name)
    {
        this.key = name;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public String getException()
    {
        return exception;
    }

    public void setException(String exception)
    {
        this.exception = exception;
    }

    public EventLevel getLevel()
    {
        return level;
    }

    public void setLevel(EventLevel level)
    {
        this.level = level;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public int getProgress()
    {
        return progress;
    }

    public void setProgress(int progress)
    {
        this.progress = progress;
    }

    public boolean hasProgress()
    {
        return (progress != -1);
    }

    public void addAttribute(Object key, Object value)
    {
        attributes.put(key, value);
    }

    public Object getAttribute(Object key)
    {
        return attributes.get(key);
    }

    public Map getAttributes()
    {
        return Collections.unmodifiableMap(attributes);
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;

        final Event event = (Event) o;

        if (date != null ? !date.equals(event.date) : event.date != null)
        {
            return false;
        }
        if (desc != null ? !desc.equals(event.desc) : event.desc != null)
        {
            return false;
        }
        if (exception != null ? !exception.equals(event.exception) : event.exception != null)
        {
            return false;
        }
        if (key != null ? !key.equals(event.key) : event.key != null)
        {
            return false;
        }
        if (level != null ? !level.equals(event.level) : event.level != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (key != null ? key.hashCode() : 0);
        result = 29 * result + (desc != null ? desc.hashCode() : 0);
        result = 29 * result + (exception != null ? exception.hashCode() : 0);
        result = 29 * result + (level != null ? level.hashCode() : 0);
        result = 29 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return "Level = " + (getLevel() == null ? "" : getLevel() + " ") + ", Key = " + (getKey() == null ? "" : getKey() + " ") + ", Desc = "
            + (getDesc() == null ? "" : getDesc() + " ") + ", Exception = " + (getException() == null ? "" : getException() + " ");
    }
}