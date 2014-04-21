package com.atlassian.crowd.util;

import org.apache.commons.lang.StringUtils;

public class InternalEntityUtils
{
    /**
     * Maximum directory entity field size for string fields.
     */
    public static final int MAX_ENTITY_FIELD_LENGTH = 255;

    private InternalEntityUtils()
    {
        // Not to be instantiated
    }

    /**
     * Truncates the given value so that it will conform with database
     * constraints.
     *
     * Fields are limited to 255 characters. Values longer than 255 characters
     * are truncated to 252 first characters with "..." concatenated in the
     * end.
     *
     * @param value value to be truncated
     * @return value that has maximum of 255 charactersn
     */
    public static String truncateValue(String value)
    {
        return StringUtils.abbreviate(value, MAX_ENTITY_FIELD_LENGTH);
    }

    /**
     * Ensures that the given string is not longer than 255 characters.
     *
     * @param value value to be validated
     */
    public static void validateLength(String value)
    {
        if (value != null && value.length() > MAX_ENTITY_FIELD_LENGTH)
        {
            throw new IllegalArgumentException("Value '" + value + "' exceeds maximum allowed length of " + MAX_ENTITY_FIELD_LENGTH + " characters");
        }
    }
}
