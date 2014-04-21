package com.atlassian.upm;

/**
 * Thrown when the user tries to dynamically install a legacy plugin and the application doesn't support it.
 */
public class LegacyPluginsUnsupportedException extends RuntimeException
{
}
