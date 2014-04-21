package com.atlassian.crowd.embedded.api;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.compareToInLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.equalsInLowerCase;
import java.util.Comparator;

/**
 * Comparator for a Group.
 */
public class GroupComparator implements Comparator<Group>
{
    public static final Comparator<Group> GROUP_COMPARATOR = new GroupComparator();

    private GroupComparator()
    {
        // Singleton
    }

    public int compare(Group group1, Group group2)
    {
        return compareTo(group1, group2);
    }

    /**
     * Checks whether the two Group objects are equal according to the contract of the {@link Group} interface.
     * <p>
     * If you are implementing {@link Group#equals(Object)} then just write code like this:
     * <pre>
     *    public boolean equals(Object o)
     *    {
     *        return (o instanceof Group) && GroupComparator.equal(this, (Group) o);
     *    }
     * </pre>
     *
     * @param group1 First Group
     * @param group2 Second Group
     * @return true if these are two equal Groups.
     */
    public static boolean equal(Group group1, Group group2)
    {
        if (group1 == group2)
        {
            return true;
        }

        if (group1 == null || group2 == null)
        {
            return false;
        }

        // null group-name is illegal and so throwing an NPE is acceptable.
        return  equalsInLowerCase(group1.getName(), group2.getName());
    }

    public static boolean equalsObject(Group group, Object o)
    {
        if (group == o)
        {
            return true;
        }
        else if (group == null)
        {
            return false;
        }
        else if (!(o instanceof Group))
        {
            return false;
        }

        Group otherGroup = (Group) o;

        return equal(group, otherGroup);
    }


    public static int hashCode(Group group)
    {
        return toLowerCase(group.getName()).hashCode();
    }

    public static int compareTo(Group group1, Group group2)
    {
        return compareToInLowerCase(group1.getName(), group2.getName());
    }
}
