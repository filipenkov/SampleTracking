package com.atlassian.crowd.model.group;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mutable group template with mutable attributes.
 */
public class GroupTemplateWithAttributes extends GroupTemplate implements GroupWithAttributes
{
    private final Map<String, Set<String>> attributes = new HashMap<String, Set<String>>();

    public GroupTemplateWithAttributes(String groupName, long directoryId, GroupType groupType)
    {
        super(groupName, directoryId, groupType);
    }

    /**
     * Creates new GroupTemplateWithAttributes based on the given group and attributes.
     *
     * @param group group to use as a template
     */
    public GroupTemplateWithAttributes(GroupWithAttributes group)
    {
        super(group);

        for (String key : group.getKeys())
        {
            this.attributes.put(key, new HashSet<String>(group.getValues(key)));
        }
    }

    protected GroupTemplateWithAttributes(Group group)
    {
        super(group);
    }

    /**
     * Creates new GroupTemplateWithAttributes based on the given group with empty attributes.
     *
     * @param group group to use as a template
     * @return GroupTemplateWithAttributes based on the given group with empty attributes
     */
    public static GroupTemplateWithAttributes ofGroupWithNoAttributes(Group group)
    {
        return new GroupTemplateWithAttributes(group);
    }

    public Map<String, Set<String>> getAttributes()
    {
        return attributes;
    }

    public Set<String> getValues(String name)
    {
        return attributes.get(name);
    }

    public String getValue(final String name)
    {
        Set<String> vals = getValues(name);
        if (vals != null && vals.size() > 0)
        {
            return vals.iterator().next();
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
