package com.atlassian.gadgets.dashboard.internal.rest.representations;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.Gadget;

/**
 * Factory to construct new REST representations given the required domain objects.
 */
public interface RepresentationFactory
{
    /**
     * Given a {@code Dashboard}, this creates a new JAXB dashboard representation.
     *
     * @param dashboard The dashboard state for the JAXB representation
     * @param gadgetRequestContext request context
     * @param writable If the user has permission to udpate this dashboard
     * @return A {@code DashboardRepresentation} containing all the dashboard information, as well as all
     *         {@code GadgetRepresentation}s for this dashboard
     */
    DashboardRepresentation createDashboardRepresentation(final Dashboard dashboard, final GadgetRequestContext gadgetRequestContext, boolean writable);

    /**
     * Given a {@code Gadget}, this creates a new JAXB gadget representation
     *
     * @param dashboardId The dashboard this gadget is on
     * @param gadget The gadet state for the JAXB representation
     * @param gadgetRequestContext request context
     * @param writable If the user has permission to update this dashboard
     * @param column The column in which this gadget resides on the dashbaord.
     * @return A {@code GadgetRepresentation} containing all information required to render the gadget
     */
    GadgetRepresentation createGadgetRepresentation(final DashboardId dashboardId, final Gadget gadget, final GadgetRequestContext gadgetRequestContext, final boolean writable, final DashboardState.ColumnIndex column);
}
