package com.atlassian.gadgets.dashboard.internal.rest;

import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;

/**
 * Interface for adding a gadget spec URI to a dashboard.
 */
public interface AddGadgetHandler
{
    /**
     * Adds the specified gadget to the specified dashboard.
     * @param dashboardId the ID of the dashboard to add to
     * @param gadgetRequestContext the context of this request
     * @param gadgetUrl the URL to the gadget spec
     * @return a {@code Response} for the client with details on the success
     * or failure
     */
    Response addGadget(DashboardId dashboardId,
                       GadgetRequestContext gadgetRequestContext,
                       String gadgetUrl);

    /**
     * Adds the specified gadget to the specified dashboard in the specified column.
     * @param dashboardId the ID of the dashboard to add to
     * @param gadgetRequestContext the context of this request
     * @param gadgetUrl the URL to the gadget spec
     * @param columnIndex the column index to add the gadget to
     * @return a {@code Response} for the client with details on the success
     * or failure
     */
    Response addGadget(DashboardId dashboardId,
                       GadgetRequestContext gadgetRequestContext,
                       String gadgetUrl,
                       DashboardState.ColumnIndex columnIndex);

    /**
     * Moves the gadget specified by id from the source dashboard to the target dashboard.
     *
     * @param targetDashboardId The dashboard to move the gadget to
     * @param gadgetId The gadget to move
     * @param sourceDashboardId The source dashboard where the gadget will be deleted
     * @param columnIndex The column to add the gadget to
     * @param rowIndexAsInt The row to insert the gadget in
     * @param gadgetRequestContext the context of this request
     * @return a {@code Response} for the client with details on the success or failure
     */
    // TODO AG-681 : this shouldn't be here. It doesn't make sense to have a moveGadget in the AddGadgetHandler
    Response moveGadget(DashboardId targetDashboardId,
                        GadgetId gadgetId,
                        DashboardId sourceDashboardId,
                        DashboardState.ColumnIndex columnIndex,
                        int rowIndexAsInt,
                        GadgetRequestContext gadgetRequestContext);
}
