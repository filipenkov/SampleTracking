package com.atlassian.core.user;

import com.opensymphony.user.User;

import java.util.Comparator;

/**
 * This comparator tries to compare two users based on their 'best name'
 * ie their full name if possible, otherwise their username.
 *
 * This comparator completely ignores case.
 * @deprecated Use {@link com.atlassian.jira.issue.comparator.UserBestNameComparator} instead. Since v4.4.
 */
public class BestNameComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        User u1 = (User) o1;
        User u2 = (User) o2;

        if (u1 == null && u2 == null)
            return 0;
        else if (u2 == null) // any value is less than null
            return -1;
        else if (u1 == null) // null is greater than any value
            return 1;

        String name1 = u1.getFullName();
        String name2 = u2.getFullName();

        if (name1 == null)
            name1 = u1.getName();

        if (name2 == null)
            name2 = u2.getName();

        if (name1 == null || name2 == null)
			throw new RuntimeException("Null user name");

        final int fullNameComparison = name1.toLowerCase().compareTo(name2.toLowerCase());
        if (fullNameComparison == 0) //if full names are the same, we should check the username (JRA-5847)
        {
            return u1.getName().compareTo(u2.getName());
        }
        else
        {
            return fullNameComparison;
        }
    }
}
