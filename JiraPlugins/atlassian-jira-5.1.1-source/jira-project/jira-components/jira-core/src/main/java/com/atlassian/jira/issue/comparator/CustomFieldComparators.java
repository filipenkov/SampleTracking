package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.fields.CustomField;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

/**
 * Utility class for dealing with custom fields.
 *
 * @since v5.1
 */
public class CustomFieldComparators
{
    /**
     * Returns a comparator that compares {@code CustomField} instances by name.
     *
     * @return a Comparator&lt;CustomField&gt;
     */
    public static Comparator<CustomField> byName()
    {
        return new NameComparator();
    }

    /**
     * Returns a comparator that compares custom field {@code GenericValue} instances by name.
     *
     * @return a Comparator&lt;GenericValue&gt;
     */
    public static Comparator<GenericValue> byGvName()
    {
        return new CustomFieldComparator();
    }

    static int compareNames(String name1, String name2)
    {
        if (name1 == null && name2 == null)
            return 0;

        if (name1 == null)
            return -1;

        if (name2 == null)
            return 1;

        return name1.compareTo(name2);
    }

    /**
     * Compares two {@code CustomField} objects.
     */
    private static class NameComparator implements Comparator<CustomField>
    {
        @Override
        public int compare(CustomField o1, CustomField o2)
        {
            if (o1 == null && o2 == null)
                return 0;

            if (o1 == null)
                return -1;

            if (o2 == null)
                return 1;

            return compareNames(o1.getName(), o2.getName());
        }
    }
}
