package com.atlassian.gadgets.dashboard.internal.rest;

import java.util.Map;

import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;

/**
 * Interface for updating the values of user prefs in an active gadget.
 */
public interface UpdateGadgetUserPrefsHandler
{
    /**
     * Changes the user prefs on the specified gadget and dashboard to the values
     * supplied by the query.
     * @param dashboardId the ID of the dashboard hosting the gadget
     * @param gadgetRequestContext the context of this request
     * @param gadgetId the gadget to change the prefs for
     * @param prefs the user prefs with their updated values
     * @return a {@code Response} for the client with details on the success or
     * failure
     */
    Response updateUserPrefs(DashboardId dashboardId,
                             GadgetRequestContext gadgetRequestContext, GadgetId gadgetId,
                             Map<String, String> prefs);
}
