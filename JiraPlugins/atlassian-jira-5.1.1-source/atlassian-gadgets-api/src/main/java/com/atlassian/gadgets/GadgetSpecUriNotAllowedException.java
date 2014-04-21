package com.atlassian.gadgets;

/**
 * Thrown when a gadget spec URI is not valid for rendering on the dashboard. This can occur if the URI is invalid, or
 * if the gadget is denied permission to be rendered on the dashboard.
 */
public class GadgetSpecUriNotAllowedException extends RuntimeException
{
    public GadgetSpecUriNotAllowedException(Throwable e)
    {
        super(e);
    }

    public GadgetSpecUriNotAllowedException(String message)
    {
        super(message);
    }
}
