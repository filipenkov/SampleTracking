package com.atlassian.crowd.model;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupComparator;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;

import java.util.Comparator;

/** Will compare one directory entity to another by Name (case-insensitive) */
public final class EntityComparator
{
    private EntityComparator()
    {
    }

    /**
     * Returns a comparator for the specified type.
     *
     * @param type type to compare
     * @return comparator for the specified type
     */
    @SuppressWarnings ({ "unchecked" })
    public static <T> Comparator<T> of(Class<T> type)
    {
        if (String.class.isAssignableFrom(type))
        {
            return (Comparator<T>) String.CASE_INSENSITIVE_ORDER;
        }
        else if (User.class.isAssignableFrom(type))
        {
            // both model and embedded/application users use the same comparator
            return (Comparator<T>) UserComparator.USER_COMPARATOR;
        }
        else if (Group.class.isAssignableFrom(type))
        {
            return (Comparator<T>) GroupComparator.GROUP_COMPARATOR;
        }
        else if (com.atlassian.crowd.model.group.Group.class.isAssignableFrom(type))
        {
            // a model group is different to an application/embedded group (uses directoryId as well as lowerName)
            return (Comparator<T>) com.atlassian.crowd.model.group.GroupComparator.GROUP_COMPARATOR;
        }
        else
        {
            throw new IllegalStateException("Can't find comparator for type " + type);
        }
    }
}