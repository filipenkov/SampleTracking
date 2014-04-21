package com.atlassian.voorhees;

/**
 * Implementations can provide an error mapper to map between application exceptions and JSON errors. If one
 * is not provided, the default error mapper will just issue generic "An exception occurred" errors for any
 * failure condition.
 */
public interface ErrorMapper
{
    JsonError mapError(String methodName, Throwable throwable);
}
