package com.atlassian.gadgets.directory.internal.impl;

/**
 * Thrown when trying to add a gadget spec to the directory that requires features that are unavailable in the container
 */
public class UnavailableFeatureException extends RuntimeException
{
    public UnavailableFeatureException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UnavailableFeatureException(String message)
    {
        super(message);
    }
}
