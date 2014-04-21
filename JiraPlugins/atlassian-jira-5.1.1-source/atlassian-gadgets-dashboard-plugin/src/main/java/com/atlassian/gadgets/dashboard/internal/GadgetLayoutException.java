package com.atlassian.gadgets.dashboard.internal;

/**
 * An exception that is thrown if the gadgets are not layed out in accordance with the currently set dashboard 
 * {@link Layout}.
 */
public class GadgetLayoutException extends RuntimeException
{
    public GadgetLayoutException(String message)
    {
        super(message);
    }
}
