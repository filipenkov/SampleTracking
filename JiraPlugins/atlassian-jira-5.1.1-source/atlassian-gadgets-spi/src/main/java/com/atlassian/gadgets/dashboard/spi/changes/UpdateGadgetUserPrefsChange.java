package com.atlassian.gadgets.dashboard.spi.changes;

import java.util.Map;

import com.atlassian.gadgets.GadgetId;

/**
 * Details of updating the user preferences of a gadget on a dashboard.
 * 
 * @since 2.0
 */
public final class UpdateGadgetUserPrefsChange implements DashboardChange
{
    private final GadgetId gadgetId;
    private final Map<String, String> prefValues;

    public UpdateGadgetUserPrefsChange(GadgetId gadgetId, Map<String,String> prefValues)
    {
        this.gadgetId = gadgetId;
        this.prefValues = prefValues;
    }

    /**
     * Invokes the {@code Visitor}s {@link Visitor#visit(UpdateGadgetUserPrefsChange)} method.
     */
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    /**
     * Get the id of the gadget we are updating user prefs for
     * @return the id of the gadget we are updating user prefs for
     */
    public GadgetId getGadgetId()
    {
        return gadgetId;
    }

    /**
     * Get the updated user pref values
     * @return the updated user pref values
     */
    public Map<String, String> getPrefValues()
    {
        return prefValues;
    }
}
