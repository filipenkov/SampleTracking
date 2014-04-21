package com.atlassian.gadgets;

/**
 * Thrown when the gadget spec is malformed or cannot be parsed for other reasons.
 */
public class GadgetParsingException extends RuntimeException
{
    public GadgetParsingException(String message)
    {
        super(message);
    }
    
    public GadgetParsingException(Throwable e)
    {
        super(e);
    }
}
