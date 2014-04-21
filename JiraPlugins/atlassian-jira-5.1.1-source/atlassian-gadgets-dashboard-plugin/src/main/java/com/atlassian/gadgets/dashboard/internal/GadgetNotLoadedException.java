package com.atlassian.gadgets.dashboard.internal;

import com.atlassian.gadgets.GadgetId;

public class GadgetNotLoadedException extends RuntimeException
{
    private final GadgetId gadgetId;

    public GadgetNotLoadedException(GadgetId gadgetId)
    {
        super("Gadget with id " + gadgetId + " could not be loaded, so some operations (e.g. change color, user prefs) cannot be performed on it");
        this.gadgetId = gadgetId;
    }

    public GadgetId getGadgetId()
    {
        return gadgetId;
    }
}