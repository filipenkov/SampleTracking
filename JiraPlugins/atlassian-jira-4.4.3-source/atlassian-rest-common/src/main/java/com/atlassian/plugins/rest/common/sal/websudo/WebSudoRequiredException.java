package com.atlassian.plugins.rest.common.sal.websudo;

public final class WebSudoRequiredException extends SecurityException
{
    public WebSudoRequiredException(final String message)
    {
        super(message);
    }
}
