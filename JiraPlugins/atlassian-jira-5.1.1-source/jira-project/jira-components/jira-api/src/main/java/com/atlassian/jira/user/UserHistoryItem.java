package com.atlassian.jira.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    // Rather than generating tens of thousands of these dynamically we just reuse a few and save the odd few Megabytes of memory.
    final private static Map<String, Type> KNOWN_TYPES;
    static
    {
        Map<String, Type> types = new HashMap<String, Type>();
        types.put(ISSUE.getName(), ISSUE);
        types.put(PROJECT.getName(), PROJECT);
        types.put(JQL_QUERY.getName(), JQL_QUERY);
        types.put(ADMIN_PAGE.getName(), ADMIN_PAGE);
        types.put(ASSIGNEE.getName(), ASSIGNEE);
        types.put(DASHBOARD.getName(), DASHBOARD);
        types.put(ISSUELINKTYPE.getName(), ISSUELINKTYPE);
        types.put(RESOLUTION.getName(), RESOLUTION);
        KNOWN_TYPES = Collections.unmodifiableMap(types);
    }

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

        /**
         * Create a new Type.  This really should be made private but that would break API compatability.
         * Prefer the use of {@link #getInstance(String name)} instead.
         * @param name Type name
         */
        public Type(final String name)
        {
            notBlank("name", name);
            this.name = name;
        }

        /**
         * Get a Type.  This will retrieve one of the well known types if it exists.  Otherwise we just creat one dynamically.
         * Plugin developers should create a single (static) instance of any types they require and reuse them if possible.
         *
         * @param name Type name
         * @return a Type
         */
        public static Type getInstance(final String name)
        {
            Type type = KNOWN_TYPES.get(name);
            if (type != null)
            {
                return type;
            }
            return new Type(name);
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
