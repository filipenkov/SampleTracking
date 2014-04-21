package com.atlassian.plugins.rest.module.jersey;

import java.io.IOException;

/**
 * Acts as a wrapper for {@link IOException}s encountered when marshalling or unmarshalling objects
 *
 * @since 2.0
 */
public class EntityConversionException extends RuntimeException
{
    public EntityConversionException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public EntityConversionException(final Throwable cause)
    {
        super(cause);
    }
}
