package com.atlassian.crowd.directory.cache.exception;

/**
 * Used to indicate an Entity in cache is KNOWN to NOT exist
 * (as opposed to the state where the cache is not sure whether
 * the entity exists or not).
 *
 * This is a replacement checked-exception for the previous
 * unchecked ObjectNotFoundException.
 *
 * This exception is required to minimise legacy rework as
 * exceptions are heavily used in the DDC and LDAPDC implementations
 * to direct control flow. Yes, this is bad and someone with more
 * time can later fix this.
 */
public class EntityNotFoundException extends Exception
{
    public EntityNotFoundException()
    {
    }

    public EntityNotFoundException(String message)
    {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public EntityNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
