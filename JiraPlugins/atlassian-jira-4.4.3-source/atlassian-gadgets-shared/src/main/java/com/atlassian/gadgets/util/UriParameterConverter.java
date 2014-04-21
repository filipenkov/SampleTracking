package com.atlassian.gadgets.util;

/**
 * Utility class that can be used to convert from {@code String}s to other types.  The main reason to use this instead
 * of just a valueOf() or other utility converter is that it will return {@link IllegalUriParameterException}s in the
 * case of failure.  These exceptions can be more explicity caught and turned into a proper response. 
 */
public class UriParameterConverter
{
    public static long asLong(String value) throws IllegalUriParameterException
    {
        try
        {
            return Long.parseLong(value);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalUriParameterException(value + " is not an integer value");
        }
    }
}
