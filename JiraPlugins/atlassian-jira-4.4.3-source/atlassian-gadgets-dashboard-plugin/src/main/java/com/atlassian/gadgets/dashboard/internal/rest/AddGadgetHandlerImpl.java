package com.atlassian.gadgets.dashboard.internal.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.GadgetSpecUrlChecker;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.internal.GadgetFactory;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.gadgets.dashboard.internal.rest.representations.GadgetRepresentation;
import com.atlassian.gadgets.dashboard.internal.rest.representations.RepresentationFactory;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation that adds a gadget to a live dashboard.
 */
public class AddGadgetHandlerImpl implements AddGadgetHandler
{
    private final Log log = LogFactory.getLog(AddGadgetHandlerImpl.class);

    private final GadgetSpecUrlChecker gadgetUrlChecker;
    private final GadgetFactory gadgetFactory;
    private final DashboardRepository repository;
    private final RepresentationFactory representationFactory;
    private final I18nResolver i18n;
    private final TransactionTemplate txTemplate;
    private final EventPublisher eventPublisher;

    /**
     * Constructor.
     * @param gadgetUrlChecker checks that the gadget can be added to a dashboard
     * @param gadgetFactory used to create the gadget domain object
     * @param repository persists dashboard changes
     * @param representationFactory Factory used to create the JAXB representation of a gadget
     * @param i18n the SAL {@code I18nResolver} to use
     * @param txTemplate transaction template needed for the 'move' operation
     * @param eventPublisher
     */
    public AddGadgetHandlerImpl(GadgetSpecUrlChecker gadgetUrlChecker,
            GadgetFactory gadgetFactory,
            DashboardRepository repository,
            RepresentationFactory representationFactory,
            I18nResolver i18n,
            TransactionTemplate txTemplate, final EventPublisher eventPublisher)
    {
        this.gadgetUrlChecker = gadgetUrlChecker;
        this.gadgetFactory = gadgetFactory;
        this.repository = repository;
        this.representationFactory = representationFactory;
        this.i18n = i18n;
        this.txTemplate = txTemplate;
        this.eventPublisher = eventPublisher;
    }

    public Response addGadget(DashboardId dashboardId,
                              GadgetRequestContext gadgetRequestContext,
                              String gadgetUrl)
    {
        return addGadget(dashboardId, gadgetRequestContext, gadgetUrl, DashboardState.ColumnIndex.ZERO);
    }

    public Response addGadget(DashboardId dashboardId,
                              GadgetRequestContext gadgetRequestContext,
                              String gadgetUrl,
                              DashboardState.ColumnIndex columnIndex)
    {
        try
        {
            gadgetUrlChecker.assertRenderable(gadgetUrl);

            Gadget gadget = gadgetFactory.createGadget(gadgetUrl, gadgetRequestContext);
            Dashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
            dashboard.addGadget(columnIndex, gadget);
            repository.save(dashboard);
            eventPublisher.publish(new GadgetAddedEvent(gadgetRequestContext.getViewer(), dashboardId, gadgetUrl));

            // Assume that writable=true since we've just added the gadget to the dashboard
            final GadgetRepresentation representation = representationFactory.createGadgetRepresentation(dashboard.getId(), gadget,
                    gadgetRequestContext, true, columnIndex);
            return Response.created(URI.create(representation.getGadgetUrl())).entity(representation).build();
        }
        catch (GadgetSpecUriNotAllowedException igsue)
        {
            return Response.status(Response.Status.BAD_REQUEST).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("gadget.spec.not.allowed", igsue.getMessage())).
                    build();
        }
        catch (GadgetParsingException gpe)
        {
            return Response.status(Response.Status.BAD_REQUEST).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("error.parsing.spec", gpe.getMessage())).
                    build();
        }
        catch (InconsistentDashboardStateException idse)
        {
            log.error("AddGadgetHandlerImpl: Unexpected error occurred", idse);
            return Response.status(Response.Status.CONFLICT).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("error.please.reload")).
                    build();
        }
    }

    public Response moveGadget(final DashboardId targetDashboardId, final GadgetId gadgetId, final DashboardId sourceDashboardId,
            final DashboardState.ColumnIndex columnIndex, final int rowIndex, final GadgetRequestContext gadgetRequestContext)
    {
        try
        {
            return (Response) txTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    final Dashboard targetDashboard = repository.get(targetDashboardId, gadgetRequestContext);
                    final Dashboard sourceDashboard = repository.get(sourceDashboardId, gadgetRequestContext);
                    final Gadget gadget = sourceDashboard.findGadget(gadgetId);
                    if (gadget == null)
                    {
                        throw new InconsistentDashboardStateException("Gadget not found with id '" + gadgetId + "' in dashboard with id '" + sourceDashboardId + "'.");
                    }

                    // If the source and target dashboards are the same, return 204 (no content)
                    if (sourceDashboardId.equals(targetDashboardId))
                    {
                        return Response.noContent().build();
                    }
                    // first remove the old gadget
                    sourceDashboard.removeGadget(gadgetId);
                    
                    // then add the gadget to the new dashboard
                    final GadgetLayout layout = createNewGadgetLayout(targetDashboard, gadgetId, columnIndex, rowIndex);
                    targetDashboard.addGadget(gadget);
                    targetDashboard.rearrangeGadgets(layout);

                    repository.save(sourceDashboard);
                    repository.save(targetDashboard);

                    // Assume that writable=true since we've just added the gadget to the dashboard
                    final GadgetRepresentation representation = representationFactory.createGadgetRepresentation(targetDashboardId, gadget,
                            gadgetRequestContext, true, columnIndex);
                    return Response.ok().location(URI.create(representation.getGadgetUrl())).entity(representation).build();
                }
            });
        }
        catch (InconsistentDashboardStateException idse)
        {
            log.error("AddGadgetHandlerImpl: Unexpected error occurred", idse);
            return Response.status(Response.Status.CONFLICT).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("error.please.reload")).
                    build();
        }
    }

    /**
     * Given a dashboard, this method will create a {@code GadgetLayout} instance with the new gadget inserted in the
     * position provided.
     */
    private GadgetLayout createNewGadgetLayout(Dashboard dashboard, GadgetId movedGadgetId, DashboardState.ColumnIndex column, int row)
    {
        final Layout layout = dashboard.getLayout();
        final List<Iterable<GadgetId>> columns = new ArrayList<Iterable<GadgetId>>(layout.getNumberOfColumns());
        for (DashboardState.ColumnIndex dashboardColumn : layout.getColumnRange())
        {
            //first add all the existing gadgets.
            final List<GadgetId> gadgets = new ArrayList<GadgetId>();
            for (Gadget gadget : dashboard.getGadgetsInColumn(dashboardColumn))
            {
                gadgets.add(gadget.getId());
            }

            //then insert the moved gadget at the right position in the right column.
            if(dashboardColumn.equals(column))
            {
                if(row < gadgets.size())
                {
                    gadgets.add(row, movedGadgetId);
                }
                else
                {
                    gadgets.add(movedGadgetId);
                }
            }
            columns.add(gadgets);
        }
        return new GadgetLayout(columns);
    }

    public static final class GadgetAddedEvent
    {
        public final DashboardId dashboardId;
        public final String gadgetUrl;
        public final String user;

        public GadgetAddedEvent(final String user, final DashboardId dashboardId, final String gadgetUrl)
        {
            this.user = user;
            this.dashboardId = dashboardId;
            this.gadgetUrl = gadgetUrl;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
