package com.atlassian.plugins.rest.module.jersey;

/**
 * Thrown when an unsupported content type is specified
 *
 * @since 2.0
 */
public class UnsupportedContentTypeException extends EntityConversionException
{
    public UnsupportedContentTypeException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
