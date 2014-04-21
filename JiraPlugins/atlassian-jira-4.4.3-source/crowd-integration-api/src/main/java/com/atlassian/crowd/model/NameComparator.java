package com.atlassian.crowd.model;

import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;

import java.util.Comparator;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.compareToInLowerCase;

/**
 * Provides case-insensitive name comparators for {@link String}, model {@link User}s and model {@link Group}s.
 */
public class NameComparator
{
    private static final UserNameComparator USER_NAME_COMPARATOR = new UserNameComparator();
    private static final GroupNameComparator GROUP_NAME_COMPARATOR = new GroupNameComparator();
    private static final StringNameComparator STRING_NAME_COMPARATOR = new StringNameComparator();

    private NameComparator()
    {
    }

    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> of(Class<T> type)
    {
        if (String.class.isAssignableFrom(type))
        {
            return (Comparator<T>) STRING_NAME_COMPARATOR;
        }
        else if (User.class.isAssignableFrom(type))
        {
            return (Comparator<T>) USER_NAME_COMPARATOR;
        }
        else if (Group.class.isAssignableFrom(type))
        {
            return (Comparator<T>) GROUP_NAME_COMPARATOR;
        }
        else
        {
            throw new IllegalStateException("Can't find name comparator for type " + type);
        }
    }

    private static class UserNameComparator implements Comparator<User>
    {
        public int compare(User o1, User o2)
        {
            return compareToInLowerCase(o1.getName(), o2.getName());
        }
    }

    private static class GroupNameComparator implements Comparator<Group>
    {
        public int compare(Group o1, Group o2)
        {
            return compareToInLowerCase(o1.getName(), o2.getName());
        }
    }

    private static class StringNameComparator implements Comparator<String>
    {
        public int compare(String o1, String o2)
        {
            return compareToInLowerCase(o1, o2);
        }
    }
}
