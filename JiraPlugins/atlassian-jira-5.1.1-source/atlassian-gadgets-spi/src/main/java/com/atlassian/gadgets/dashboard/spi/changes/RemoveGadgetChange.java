package com.atlassian.gadgets.dashboard.spi.changes;

import com.atlassian.gadgets.GadgetId;

/**
 * Details of removing a gadget from a dashboard.
 * 
 * @since 2.0
 */
public final class RemoveGadgetChange implements DashboardChange
{
    private final GadgetId gadgetId;

    public RemoveGadgetChange(GadgetId gadgetId)
    {
        this.gadgetId = gadgetId;
    }

    /**
     * Invokes the {@code Visitor}s {@link Visitor#visit(RemoveGadgetChange)} method.
     */
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    /**
     * Get the id of the gadget we are removing
     * @return the id of the gadget we are removing
     */
    public GadgetId getGadgetId()
    {
        return gadgetId;
    }
}
