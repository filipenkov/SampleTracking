package com.atlassian.renderer.v2.macro;

/**
 * A macro exception means that a macro has failed to execute successfully, but that this
 * is not a problem with Confluence itself. (i.e. an RSS macro failed to connect to a remote
 * HTTP server, a junitreport macro failed to parse the XML files, etc).
 *
 * <p>MacroExceptions are displayed to the page viewer, but are logged at a low level.
 */
public class MacroException extends Exception
{
    public MacroException()
    {
    }

    public MacroException(String message)
    {
        super(message);
    }

    public MacroException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MacroException(Throwable cause)
    {
        super(cause);
    }
}
