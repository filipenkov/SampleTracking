package com.atlassian.core.user;

import com.opensymphony.user.User;

import java.util.Comparator;

/**
 * @deprecated Use {@link com.atlassian.jira.issue.comparator.UserBestNameComparator} instead. Since v4.4.
 */
public class FullNameComparator implements Comparator
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

        String fullname1 = u1.getFullName();
        String fullname2 = u2.getFullName();

        if (fullname1 == null)
        {
            return -1;
        }
        else if (fullname2 == null)
        {
            return 1;
        }
        else
        {
            return fullname1.toLowerCase().compareTo(fullname2.toLowerCase()); //do case insensitive sorting
        }
    }
}
