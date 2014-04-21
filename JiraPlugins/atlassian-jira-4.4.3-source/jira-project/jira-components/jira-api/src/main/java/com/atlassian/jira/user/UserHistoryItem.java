package com.atlassian.jira.user;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
public class UserHistoryItem
{
    /*
     * These are defined as field type "very-short" and hence must be 10 or less characters
     */
    final public static Type ISSUE = new Type("Issue");
    final public static Type PROJECT = new Type("Project");
    final public static Type JQL_QUERY = new Type("JQLQuery");
    final public static Type ADMIN_PAGE = new Type("AdminPage");
    final public static Type ASSIGNEE = new Type("Assignee");
    final public static Type DASHBOARD = new Type("Dashboard");
    final public static Type ISSUELINKTYPE = new Type("IssueLink");
    final public static Type RESOLUTION = new Type("Resolution");

    final private long lastViewed;
    private final Type type;
    private final String entityId;
    private final String data;

    public UserHistoryItem(final Type type, final String entityId, final long lastViewed, final String data)
    {
        notNull("type", type);
        notNull("entityId", entityId);
        notNull("lastViewed", lastViewed);

        this.type = type;
        this.entityId = entityId;
        this.lastViewed = lastViewed;
        this.data = data;
    }

    public UserHistoryItem(Type type, String entityId, long lastViewed)
    {
        this(type, entityId, lastViewed, null);
    }

    public UserHistoryItem(Type type, String entityId)
    {
        this(type, entityId, System.currentTimeMillis());
    }

    public UserHistoryItem(Type type, String entityId, String data)
    {
        this(type, entityId, System.currentTimeMillis(), data);
    }

    public long getLastViewed()
    {
        return lastViewed;
    }

    public Type getType()
    {
        return type;
    }

    public String getEntityId()
    {
        return entityId;
    }

    public String getData()
    {
        return data;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        UserHistoryItem that = (UserHistoryItem) o;

        if (!entityId.equals(that.entityId))
        {
            return false;
        }
        if (!(lastViewed == that.lastViewed))
        {
            return false;
        }
        if (!type.equals(that.type))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = ((Long) lastViewed).hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + entityId.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "UserHistoryItem{" +
                "type=" + type +
                ", entityId='" + entityId + '\'' +
                ", lastViewed=" + lastViewed +
                '}';
    }

    /**
     */
    public static class Type
    {
        private final String name;

        public Type(final String name)
        {
            notBlank("name", name);
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Type other = (Type) obj;
            if (name == null)
            {
                if (other.name != null)
                {
                    return false;
                }
            }
            else if (!name.equals(other.name))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
