package com.atlassian.security.auth.trustedapps;

public class Null
{
    public static void not(String name, Object notNull) /* sheepishly */ throws IllegalArgumentException
    {
        if (notNull == null)
        {
            throw new NullArgumentException(name);
        }
    }

    private Null() {}

    static class NullArgumentException extends IllegalArgumentException
    {
        NullArgumentException(String name)
        {
            super(name + " should not be null!");
        }
    }
}
