package com.atlassian.crowd.model.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mutable user template with mutable attributes.
 */
public class UserTemplateWithAttributes extends UserTemplate implements UserWithAttributes
{
    private final Map<String, Set<String>> attributes = new HashMap<String, Set<String>>();

    public UserTemplateWithAttributes(String username, long directoryId)
    {
        super(username, directoryId);
    }

    /**
     * Creates new UserTemplateWithAttributes based on the given user with attributes.
     *
     * @param user user to use as a template
     */
    public UserTemplateWithAttributes(UserWithAttributes user)
    {
        super(user);

        for (String key : user.getKeys())
        {
            this.attributes.put(key, new HashSet<String>(user.getValues(key)));
        }
    }

    protected UserTemplateWithAttributes(User user)
    {
        super(user);
    }

    /**
     * Creates new UserTemplateWithAttributes based on the given user with empty attributes.
     *
     * @param user user to use as a template
     * @return UserTemplateWithAttributes based on the given user with empty attributes
     */
    public static UserTemplateWithAttributes ofUserWithNoAttributes(User user)
    {
        return new UserTemplateWithAttributes(user);
    }

    public Map<String, Set<String>> getAttributes()
    {
        return attributes;
    }

    public Set<String> getValues(String name)
    {
        return attributes.get(name);
    }

    public String getValue(String name)
    {
        Set<String> values = getValues(name);
        if (values != null && values.size() > 0)
        {
            return values.iterator().next();
        }
        else
        {
            return null;
        }
    }

    public Set<String> getKeys()
    {
        return attributes.keySet();
    }

    public boolean isEmpty()
    {
        return attributes.isEmpty();
    }

    public void setAttribute(String name, String value)
    {
        attributes.put(name, Collections.singleton(value));
    }

    public void setAttribute(String name, Set<String> values)
    {
        attributes.put(name, values);
    }

    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }
}
