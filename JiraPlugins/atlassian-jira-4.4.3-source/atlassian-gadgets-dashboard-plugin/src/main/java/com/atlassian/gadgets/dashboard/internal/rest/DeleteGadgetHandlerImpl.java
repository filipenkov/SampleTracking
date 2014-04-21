package com.atlassian.gadgets.dashboard.internal.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.sal.api.message.I18nResolver;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation that removes a gadget from a live dashboard.
 */
public class DeleteGadgetHandlerImpl implements DeleteGadgetHandler
{
    private final Log log = LogFactory.getLog(DeleteGadgetHandlerImpl.class);

    private final DashboardRepository repository;
    private final I18nResolver i18n;
    private final EventPublisher eventPublisher;

    /**
     * Constructor.
     * @param repository the {@code DashboardRepository} for getting/saving
     * dashboards
     * @param i18n the {@code I18nResolver} implementation to use
     * @param eventPublisher
     */
    public DeleteGadgetHandlerImpl(DashboardRepository repository, I18nResolver i18n, final EventPublisher eventPublisher)
    {
        this.repository = repository;
        this.i18n = i18n;
        this.eventPublisher = eventPublisher;
    }

    public Response deleteGadget(DashboardId dashboardId,
                                 GadgetRequestContext gadgetRequestContext, GadgetId gadgetId)
    {
        Dashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
        dashboard.removeGadget(gadgetId);

        try
        {
            repository.save(dashboard);
        }
        catch (InconsistentDashboardStateException idse)
        {
            log.error("DeleteGadgetHandlerImpl: Unexpected error occurred", idse);
            return Response.status(Response.Status.CONFLICT).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("error.please.reload")).
                    build();
        }

        eventPublisher.publish(new GadgetDeletedEvent(dashboardId, gadgetRequestContext.getViewer()));
        return Response.noContent().build();
    }

    public final class GadgetDeletedEvent
    {
        public final DashboardId dashboardId;
        public final String user;

        public GadgetDeletedEvent(final DashboardId dashboardId, final String user)
        {
            this.dashboardId = dashboardId;
            this.user = user;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
