package com.atlassian.gadgets.dashboard.spi.changes;

import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;

/**
 * Details of updating the layout of a dashboard.
 * 
 * @since 2.0
 */
public final class UpdateLayoutChange implements DashboardChange
{
    private final Layout layout;
    private final GadgetLayout gadgetLayout;

    public UpdateLayoutChange(Layout layout, GadgetLayout gadgetLayout)
    {
        this.layout = layout;
        this.gadgetLayout = gadgetLayout;
    }

    /**
     * Invokes the {@code Visitor}s {@link Visitor#visit(UpdateLayoutChange)} method.
     */
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    /**
     * Get the {@code Layout} type, e.g. three column
     * @return the layout
     */
    public Layout getLayout()
    {
        return layout;
    }

    /**
     * Get the layout of all the {@code Gadgets} on the dashboard
     * @return the layout of all the {@code Gadgets} on the dashboard
     */
    public GadgetLayout getGadgetLayout()
    {
        return gadgetLayout;
    }
}
