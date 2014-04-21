package com.atlassian.gadgets.dashboard.internal.rest;

import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;

/**
 * Interface for removing a gadget from a live dashboard.
 */
public interface DeleteGadgetHandler
{
    /**
     * Removes the specified gadget from the specified dashboard.
     * @param dashboardId the dashboard hosting the gadget
     * @param gadgetRequestContext the context of this request
     * @param gadgetId the gadget to remove
     * @return a {@code Response} for the client with details on success or
     * failure
     */
    Response deleteGadget(DashboardId dashboardId,
                          GadgetRequestContext gadgetRequestContext, GadgetId gadgetId);
}
