package com.atlassian.gadgets.dashboard.internal;

import java.util.List;
import java.util.Map;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetNotFoundException;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.DashboardState.ColumnIndex;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;

public interface Dashboard
{
    DashboardId getId();

    String getTitle();
    
    Layout getLayout();
    
    Iterable<Gadget> getGadgetsInColumn(ColumnIndex column);
    
    DashboardState getState();

    /**
     * Lookup a Gadget in this Dashboard given the gadget's id.
     * @param gadgetId The gadgetId of the gadget to lookup
     * @return The Gadget or null if none could be found matching the id
     */
    Gadget findGadget(GadgetId gadgetId);

    /**
     * Returns an immutable list of changes that have been made to this {@code Dashboard}, in the order that they
     * occurred.
     *
     * @return immutable list of changes that have been made to this {@code Dashboard}
     */
    List<DashboardChange> getChanges();

    /**
     * Clears the stored list of changes that have been made to this {@code Dashboard}
     */
    public void clearChanges();

    /**
     * Add a gadget to the default tab.  Let the dashboard pick which column it should go in.
     *
     * @param gadget the gadget being added
     */
    void appendGadget(Gadget gadget);

    void addGadget(Gadget gadget);

    /**
     * Add a gadget to the end of the specified column
     *
     * @param column index of column to append the gadget to
     * @param gadget the gadget being added
     */
    void appendGadget(ColumnIndex column, Gadget gadget);

    /**
     * Add a gadget to the first position of the specified column
     * @param column index of the column to add the gadget to
     * @param gadget the gadget being added
     */
    void addGadget(ColumnIndex column, Gadget gadget);

    /**
     * Get the total number of gadgets on this dashboard
     * @return the total number of gadgets on the dashboard
     */
    int getNumberOfGadgets();

    /**
     * Remove the gadget with the provided id from the dashboard.
     * 
     * @param gadgetId id of the gadget to remove
     */
    void removeGadget(GadgetId gadgetId);

    /**
     * Rearrange and reposition the gadgets on the dashboard according to the specified layout.
     * 
     * @param layout Layout to match
     * @throws GadgetLayoutException thrown if the layout is invalid
     */
    void rearrangeGadgets(GadgetLayout layout) throws GadgetLayoutException;

    /**
     * Change the layout of the dashboard and use the gadget layout to update the gadget positioning.
     * 
     * @param layout New layout to use for the dashboard
     * @param gadgetLayout New positioning of the gadgets
     * @throws GadgetLayoutException thrown if the positioning of the gadgets is not valid for the new layout - e.g.
     *                               threre are gadgets in columns that don't exist in the new layout 
     */
    void changeLayout(Layout layout, GadgetLayout gadgetLayout) throws GadgetLayoutException;

    /**
     * Change the color of the gadgets chrome.
     * 
     * @param gadgetId Id of gadget to change the color of.
     * @param color Color to change the chrome to
     * @throws GadgetNotFoundException if the gadget could not be found by ID
     * @throws GadgetNotLoadedException if the gadget wasn't loaded
     */
    void changeGadgetColor(GadgetId gadgetId, Color color) throws GadgetNotFoundException, GadgetNotLoadedException;

    /**
     * Save user prefs for a gadget
     *
     * @param gadgetId Id of gadget to change the color of.
     * @param prefValues preferences to save
     * @throws GadgetNotFoundException if the gadget could not be found by ID
     * @throws GadgetNotLoadedException if the gadget wasn't loaded
     */
    void updateGadgetUserPrefs(GadgetId gadgetId, Map<String,String> prefValues) throws GadgetNotFoundException, GadgetNotLoadedException;
}
