package com.atlassian.crowd.embedded.api;

import java.util.Comparator;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;

import com.google.common.base.Function;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.compareToInLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.equalsInLowerCase;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * Supplies re-useable methods for equals, hashcode and compareTo that can be shared with different implementations of
 * {@link User} in order to be compatible.
 *
 * You can also instantiate this class to get a Comparator of User.
 */
public class UserComparator implements Comparator<User>
{
    /** Singleton instance of Comparator<User> */
    public static final Comparator<User> USER_COMPARATOR = new UserComparator();

    /**
     * A {@link Function} to turn {@link User}s into representative keys that sort in the same
     * order as this {@link Comparator}.
     */
    public static Function<User, UserComparator.Key> KEY_MAKER = new Function<User, UserComparator.Key>()
    {
        @Override
        public Key apply(User t)
        {
            return new Key(IdentifierUtils.toLowerCase(t.getName()), t.getDirectoryId());
        }
    };

    private UserComparator()
    {
        // Singleton
    }

    /**
     * Checks whether the two User objects are equal according to the contract of the {@link User} interface.
     * <p>
     * If you are implementing {@link User#equals(Object)} then just write code like this:
     * <pre>
     *    public boolean equals(Object o)
     *    {
     *        return (o instanceof User) && UserComparator.equal(this, (User) o);
     *    }
     * </pre>
     *
     * @param user1 First User
     * @param user2 Second User
     * @return true if these are two equal Users.
     */
    public static boolean equal(User user1, User user2)
    {
        if (user1 == user2)
        {
            return true;
        }

        if (user1 == null || user2 == null)
        {
            return false;
        }

        if (user1.getDirectoryId() != user2.getDirectoryId())
        {
            return false;
        }
        // null username is illegal and so throwing an NPE is acceptable.
        if (!equalsInLowerCase(user1.getName(), user2.getName()))
        {
            return false;
        }

        return true;
    }

    public static boolean equalsObject(User user, Object o)
    {
        if (user == o)
        {
            return true;
        }
        else if (user == null)
        {
            return false;
        }
        else if (!(o instanceof com.atlassian.crowd.embedded.api.User))
        {
            return false;
        }

        final com.atlassian.crowd.embedded.api.User otherUser = (com.atlassian.crowd.embedded.api.User) o;

        return equal(user, otherUser);
    }

    public static int hashCode(User user)
    {
        // Taken from Long.hashCode()
        int result = (int)(user.getDirectoryId() ^ (user.getDirectoryId() >>> 32));
        return 31 * toLowerCase(user.getName()).hashCode() + result;
    }

    public static int compareTo(User user1, User user2)
    {
        // First compare names
        int nameCompare = compareToInLowerCase(user1.getName(), user2.getName());
        if (nameCompare != 0)
        {
            return nameCompare;
        }
        // Names are equal - use directoryId as the tie-breaker
        final long directoryId1 = user1.getDirectoryId();
        final long directoryId2 = user2.getDirectoryId();
        return (directoryId1 < directoryId2 ? -1 : (directoryId1 == directoryId2 ? 0 : 1));
    }

    public int compare(User user1, User user2)
    {
        return compareTo(user1, user2);
    }

    public static class Key implements Comparable<Key>
    {
        private final String name;
        private final Long directoryId;

        public Key(String name, long directoryId)
        {
            this.name = name;
            this.directoryId = directoryId;
        }

        @Override
        public int compareTo(Key o)
        {
            int nameComparison = name.compareTo(o.name);
            if (nameComparison != 0)
            {
                return nameComparison;
            }
            else
            {
                return directoryId.compareTo(o.directoryId);
            }
        }
    }
}
