package com.atlassian.gadgets.util;

import java.net.URI;

/**
 * Thrown when a directory gadget URI is either not a valid {@link URI} or if it does not begin with http:// or
 * https://.
 */
public class InvalidDirectoryGadgetUriException extends RuntimeException
{
    public InvalidDirectoryGadgetUriException(Throwable e)
    {
        super(e);
    }

    public InvalidDirectoryGadgetUriException(String message)
    {
        super(message);
    }
}