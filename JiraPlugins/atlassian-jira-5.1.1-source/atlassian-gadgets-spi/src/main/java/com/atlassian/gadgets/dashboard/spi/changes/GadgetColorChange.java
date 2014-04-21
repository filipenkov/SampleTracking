package com.atlassian.gadgets.dashboard.spi.changes;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.Color;

import static com.atlassian.plugin.util.Assertions.notNull;

/**
 * Details of changing the color of a gadget on a dashboard.
 * 
 * @since 2.0
 */
public final class GadgetColorChange implements DashboardChange
{
    private final GadgetId gadgetId;
    private final Color color;

    public GadgetColorChange(GadgetId gadgetId, Color color)
    {
        this.gadgetId = notNull("gadgetId", gadgetId);
        this.color = notNull("color", color);
    }

    /**
     * Invokes the {@code Visitor}s {@link Visitor#visit(GadgetColorChange)} method.
     */
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    /**
     * Get the updated gadget color
     * @return the updated gadget color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Get the id of the gadget we are changing the color of
     * @return the id of the gadget we are changing the color of
     */
    public GadgetId getGadgetId()
    {
        return gadgetId;
    }

}
