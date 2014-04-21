package com.atlassian.crowd.model.group;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.compareToInLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.equalsInLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

import java.util.Comparator;

/**
 * Supplies re-useable methods for equals, hashcode and compareTo that can be shared with different implementations of
 * {@link Group} in order to be compatible.
 *
 * You can also instantiate this class to get a Comparator of Group.
 *
 * Note: the GroupComparator is distinct from the EmbeddedGroupComparator as
 * model groups take the directoryId into consideration when performing equals/hashCode/compareTo.
 */
public class GroupComparator implements Comparator<Group>
{
    /** Singleton instance of Comparator<Group> */
    public static final Comparator<Group> GROUP_COMPARATOR = new GroupComparator();

    private GroupComparator()
    {
        // Singleton
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

        if (group1.getDirectoryId() != group2.getDirectoryId())
        {
            return false;
        }
        // null groupname is illegal and so throwing an NPE is acceptable.
        if (!equalsInLowerCase(group1.getName(), group2.getName()))
        {
            return false;
        }

        return true;
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
        // Taken from Long.hashCode()
        int result = (int)(group.getDirectoryId() ^ (group.getDirectoryId() >>> 32));
        return 31 * toLowerCase(group.getName()).hashCode() + result;
    }

    public static int compareTo(Group group1, Group group2)
    {
        // First compare names
        int nameCompare = compareToInLowerCase(group1.getName(), group2.getName());
        if (nameCompare != 0)
        {
            return nameCompare;
        }

        // Names are equal - use directoryId as the tie-breaker
        final long directoryId1 = group1.getDirectoryId();
        final long directoryId2 = group2.getDirectoryId();
        return (directoryId1 < directoryId2 ? -1 : (directoryId1 == directoryId2 ? 0 : 1));
    }

    public int compare(Group group1, Group group2)
    {
        return compareTo(group1, group2);
    }
}
