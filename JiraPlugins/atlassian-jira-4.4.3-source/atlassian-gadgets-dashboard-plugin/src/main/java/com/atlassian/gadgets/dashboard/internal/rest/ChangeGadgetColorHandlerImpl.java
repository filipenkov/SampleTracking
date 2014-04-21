package com.atlassian.gadgets.dashboard.internal.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.sal.api.message.I18nResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation that changes the color of a live gadget on a dashboard.
 */
public class ChangeGadgetColorHandlerImpl implements ChangeGadgetColorHandler
{
    private final Log log = LogFactory.getLog(ChangeGadgetColorHandlerImpl.class);

    private final DashboardRepository repository;
    private final I18nResolver i18n;

    /**
     * Constructor.
     * @param repository the repository to retrieve dashboards
     * @param i18n the lookup to resolve i18n messages
     */
    public ChangeGadgetColorHandlerImpl(DashboardRepository repository, I18nResolver i18n)
    {
        this.repository = repository;
        this.i18n = i18n;
    }

    public Response setGadgetColor(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext,
                                   GadgetId gadgetId, Color color)
    {
        Dashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
        dashboard.changeGadgetColor(gadgetId, color);
        try
        {
            repository.save(dashboard);
        }
        catch (InconsistentDashboardStateException idse)
        {
            log.error("ChangeGadgetColorHandlerImpl: Unexpected error occurred: ", idse);
            return Response.status(Response.Status.CONFLICT).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("error.please.reload")).
                    build();
        }
        return Response.noContent().build();
    }
}
