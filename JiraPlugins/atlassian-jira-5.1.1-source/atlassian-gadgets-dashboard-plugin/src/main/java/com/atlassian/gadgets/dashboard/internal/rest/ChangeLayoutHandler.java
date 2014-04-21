package com.atlassian.gadgets.dashboard.internal.rest;

import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;

import org.json.JSONObject;

/**
 * Interface for changing the layout of a dashboard.
 */
public interface ChangeLayoutHandler
{
    /**
     * Attempts to change the layout of the specified dashboard.
     * @param dashboardId the ID of the dashboard to change
     * @param gadgetRequestContext the context of this request
     * @param newLayout parameters sent along with the request
     * @return a {@code Response} for the client with details on the success or failure
     */
    Response changeLayout(DashboardId dashboardId,
                          GadgetRequestContext gadgetRequestContext,
                          JSONObject newLayout);
}
