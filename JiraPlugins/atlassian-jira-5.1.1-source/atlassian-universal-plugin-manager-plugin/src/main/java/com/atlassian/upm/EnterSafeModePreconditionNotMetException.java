package com.atlassian.upm;

/**
 * Thrown if there are any preconditions not met in the plugin system when entering safe mode.
 */
public class EnterSafeModePreconditionNotMetException extends RuntimeException
{
    public EnterSafeModePreconditionNotMetException(Throwable cause)
    {
        super(cause);
    }
}
