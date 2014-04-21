package com.atlassian.core.ofbiz.comparators;

import org.ofbiz.core.entity.GenericValue;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @deprecated Please use {@link com.atlassian.jira.ofbiz.OfBizStringFieldComparator} instead. Since v4.3
 */
public class OFBizFieldComparator implements java.util.Comparator<GenericValue>
{
    String fieldname;

    public OFBizFieldComparator(String fieldname)
    {
        this.fieldname = fieldname;
    }

    public int compare(GenericValue gv1, GenericValue gv2)
    {
        if (gv1 == null && gv2 == null)
            return 0;
        else if (gv2 == null) // any value is less than null
            return -1;
        else if (gv1 == null) // null is greater than any value
            return 1;

        String s1 = gv1.getString(fieldname);
        String s2 = gv2.getString(fieldname);

        if (s1 == null && s2 == null)
            return 0;
        else if (s2 == null) // any value is less than null
            return -1;
        else if (s1 == null) // null is greater than any value
            return 1;
        else
            return s1.compareToIgnoreCase(s2);
    }
}
