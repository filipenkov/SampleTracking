package com.atlassian.instrumentation.utils.dbc;

/**
 * Copy of Jeds DBC because I dont want to depend on atlassian-core.  Hardly worth it from a file this size!
 *
 * @since v4.0
 */
public class Assertions
{
    public static <T> T notNull(String name, T obj)
    {
        if (obj == null)
        {
            throw new IllegalArgumentException(String.valueOf(name) + " must not be null");
        }
        return obj;
    }

    /**
     * Asserts that the passed in value is not a negative number.  Zero is NOt considered negative and hence is a valid
     * value
     *
     * @param name the name of the parameter
     * @param value the value in question
     * @return  the value
     */
    public static double notNegative(String name, double value)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException(String.valueOf(name) + " must be >= 0");
        }
        return value;
    }
}
