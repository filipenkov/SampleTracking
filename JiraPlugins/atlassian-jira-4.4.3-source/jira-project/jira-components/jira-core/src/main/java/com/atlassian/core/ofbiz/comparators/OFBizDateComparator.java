package com.atlassian.core.ofbiz.comparators;

import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Comparator;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @deprecated Please use {@link com.atlassian.jira.ofbiz.OfBizDateFieldComparator} instead. Since v4.3
 */

public class OFBizDateComparator implements Comparator<GenericValue>
{
    String fieldname;

    public OFBizDateComparator(String fieldname)
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

        Timestamp date1 = gv1.getTimestamp(fieldname);
        Timestamp date2 = gv2.getTimestamp(fieldname);

        if (date1 == null && date2 == null)
            return 0;
        if (date1 == null)
            return -1;
        if (date2 == null)
            return 1;

        return date1.compareTo(date2);
    }
}
