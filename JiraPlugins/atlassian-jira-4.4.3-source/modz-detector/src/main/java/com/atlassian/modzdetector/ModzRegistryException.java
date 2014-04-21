package com.atlassian.modzdetector;

/**
 * Top level exception category for errors specific to the modz detector.
 */
public class ModzRegistryException extends Exception
{
    public ModzRegistryException(final String s)
    {
        super(s);
    }

    public ModzRegistryException(final String s, final Throwable throwable)
    {
        super(s, throwable);
    }
}
