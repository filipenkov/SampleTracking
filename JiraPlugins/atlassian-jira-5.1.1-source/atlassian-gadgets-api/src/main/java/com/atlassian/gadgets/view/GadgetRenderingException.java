package com.atlassian.gadgets.view;

import com.atlassian.gadgets.GadgetState;

/**
 * Thrown if there is a problem trying to render a gadget (for example, if the request view is not defined)
 */
public class GadgetRenderingException extends RuntimeException
{
    private final GadgetState gadget;

    public GadgetRenderingException(String message, GadgetState gadget, Throwable cause)
    {
        super(message, cause);
        this.gadget = gadget;
    }

    public GadgetRenderingException(String message, GadgetState gadget)
    {
        super(message);
        this.gadget = gadget;
    }

    public GadgetRenderingException(GadgetState gadget, Throwable cause)
    {
        super(cause);
        this.gadget = gadget;
    }
    
    public GadgetState getGadgetState()
    {
        return gadget;
    }
}
