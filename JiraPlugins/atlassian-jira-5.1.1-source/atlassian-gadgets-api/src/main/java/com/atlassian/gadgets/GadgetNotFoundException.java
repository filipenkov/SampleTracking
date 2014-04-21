package com.atlassian.gadgets;

/**
 * Thrown if the gadget identified by the {@link GadgetId} does not exist.
 */
public class GadgetNotFoundException extends RuntimeException
{
    private final GadgetId gadgetId;

    public GadgetNotFoundException(GadgetId gadgetId)
    {
        super("No such gadget with id " + gadgetId + " exists on this dashboard");
        this.gadgetId = gadgetId;
    }

    public GadgetId getGadgetId()
    {
        return gadgetId;
    }
}
