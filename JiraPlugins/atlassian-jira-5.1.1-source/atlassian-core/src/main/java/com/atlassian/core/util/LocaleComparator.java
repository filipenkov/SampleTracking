/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.core.util;

import java.util.Comparator;
import java.util.Locale;

// Locale comparator - compares on locale display name
public class LocaleComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        Locale l1 = (Locale) o1;
        Locale l2 = (Locale) o2;

        if (l1 == null && l2 == null)
            return 0;
        else if (l2 == null) // any value is less than null
            return -1;
        else if (l1 == null) // null is greater than any value
            return 1;

        String displayName1 = l1.getDisplayName();
        String displayName2 = l2.getDisplayName();

        if (displayName1 == null)
        {
            return -1;
        }
        else if (displayName2 == null)
        {
            return 1;
        }
        else
        {
            return displayName1.toLowerCase().compareTo(displayName2.toLowerCase()); //do case insensitive sorting
        }
    }
}
