package com.atlassian.jira.util;

import java.io.IOException;

/**
 * An IOException was encountered and the stupid programmer didn't know how to recover, so this got thrown instead.
 */
public class RuntimeIOException extends RuntimeException
{
    private static final long serialVersionUID = -8317205499816761123L;

    public RuntimeIOException(final @NotNull String message, final @NotNull IOException cause)
    {
        super(message, cause);
    }

    public RuntimeIOException(final @NotNull IOException cause)
    {
        super(cause);
    }
}
