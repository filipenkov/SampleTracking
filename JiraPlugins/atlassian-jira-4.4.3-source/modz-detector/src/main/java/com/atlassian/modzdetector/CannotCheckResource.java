package com.atlassian.modzdetector;

/**
 * Indicates a failure to determine whether a given resource is modified or missing.
 */
class CannotCheckResource extends Exception
{
    CannotCheckResource(final String s)
    {
        super(s);
    }

    CannotCheckResource(final String s, final Throwable throwable)
    {
        super(s, throwable);
    }
}
