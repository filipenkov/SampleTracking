package com.atlassian.gadgets.dashboard.internal.rest;

import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;

/**
 * Interface for changing the color of a gadget on a dashboard.
 */
public interface ChangeGadgetColorHandler
{
    /**
     * Sets the color of the specified gadget to the specified color.
     * @param dashboardId the dashboard hosting the gadget
     * @param gadgetRequestContext the context of this request
     * @param gadgetId the ID of the gadget to change
     * @param color the new color of the gadget
     * @return a {@code Response} for the client with details on the success
     * or failure
     */
    Response setGadgetColor(DashboardId dashboardId,
                            GadgetRequestContext gadgetRequestContext, GadgetId gadgetId, Color color);
}
