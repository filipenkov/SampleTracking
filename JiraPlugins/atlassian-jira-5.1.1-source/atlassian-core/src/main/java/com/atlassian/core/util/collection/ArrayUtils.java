package com.atlassian.core.util.collection;

import org.apache.commons.lang.StringUtils;

/**
 * Collection of useful Array methods
 */
public class ArrayUtils
{
    /**
     * Adds a string to an array and resizes the array. Method is null safe.
     * @param array - original array
     * @param obj - String to add
     * @return an array with the new straing addded to the end
     */
    public static String[] add(String[] array, String obj)
    {
        if (array != null)
        {
            String[] newArray = new String[array.length + 1];
            for (int i = 0; i < array.length; i++)
            {
                newArray[i] = array[i];
            }
            newArray[array.length] = obj;

            return newArray;
        }
        else if (obj != null)
        {
            return new String[]{obj};
        }
        else
        {
            return null;
        }
    }

    /**
     * Checks if the array is not null, and contains one and only one element, which is blank (see {@link StringUtils#isBlank})
     * @param array
     * @return true or false
     */
    public static boolean isContainsOneBlank(String[] array)
    {
        return array != null && array.length == 1 && StringUtils.isBlank(array[0]);
    }
}
