package com.atlassian.gadgets.dashboard.internal.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.sal.api.message.I18nResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation that updates the prefs in a live gadget.
 */
public class UpdateGadgetUserPrefsHandlerImpl implements UpdateGadgetUserPrefsHandler
{
    private final Log log = LogFactory.getLog(UpdateGadgetUserPrefsHandlerImpl.class);

    private static final String USER_PREF_PREFIX = "up_";

    private final DashboardRepository repository;
    private final I18nResolver i18n;

    /**
     * Constructor.
     * @param repository the {@code DashboardRepository} for getting/saving
     * dashboards
     * @param i18n the {@code I18nResolver} to use for looking up i18n messages
     */
    public UpdateGadgetUserPrefsHandlerImpl(DashboardRepository repository, I18nResolver i18n)
    {
        this.repository = repository;
        this.i18n = i18n;
    }

    public Response updateUserPrefs(DashboardId dashboardId,
                                    GadgetRequestContext gadgetRequestContext, GadgetId gadgetId,
                                    Map<String, String> prefs)
    {
        // Synchronized temporarily to prevent race conditions.
        // See https://studio.atlassian.com/browse/AG-471
        //     https://studio.atlassian.com/browse/AG-229
        //     https://studio.atlassian.com/browse/AG-490
        Dashboard dashboard = repository.get(dashboardId, gadgetRequestContext);

        try
        {
            dashboard.updateGadgetUserPrefs(gadgetId, adaptParameterMapToUserPrefValues(prefs));
            repository.save(dashboard);
            return Response.noContent().build();
        }
        catch (IllegalArgumentException iae)
        {
            // thrown if a required pref was set to an illegal value
            return Response.status(Response.Status.BAD_REQUEST).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("invalid.value.for.required.pref")).
                    build();
        }
        catch (InconsistentDashboardStateException idse)
        {
            log.error("UpdateGadgetUserPrefsHandlerImpl: Unexpected error occurred: ", idse);
            return Response.status(Response.Status.CONFLICT).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("error.please.reload")).
                    build();
        }
    }

    private Map<String, String> adaptParameterMapToUserPrefValues(Map<String, String> params)
    {
        Map<String, String> values = new HashMap<String, String>();
        for (String param : params.keySet())
        {
            if (param.startsWith(USER_PREF_PREFIX) && params.get(param) != null)
            {
                values.put(param.substring(USER_PREF_PREFIX.length()), params.get(param));
            }
        }
        return values;
    }
}
