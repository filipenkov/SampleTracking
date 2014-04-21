package com.atlassian.gadgets.dashboard.internal.rest.representations;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardUrlBuilder;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.view.RenderedGadgetUriBuilder;

public class RepresentationFactoryImpl implements RepresentationFactory
{
    private final RenderedGadgetUriBuilder renderedGadgetUriBuilder;
    private final DashboardUrlBuilder dashboardUrlBuilder;

    public RepresentationFactoryImpl(final RenderedGadgetUriBuilder renderedGadgetUriBuilder, final DashboardUrlBuilder dashboardUrlBuilder)
    {
        this.renderedGadgetUriBuilder = renderedGadgetUriBuilder;
        this.dashboardUrlBuilder = dashboardUrlBuilder;
    }

    public DashboardRepresentation createDashboardRepresentation(final Dashboard dashboard, final GadgetRequestContext gadgetRequestContext, final boolean writable)
    {
        final List<GadgetRepresentation> gadgets = new ArrayList<GadgetRepresentation>();
        for (final DashboardState.ColumnIndex index : dashboard.getLayout().getColumnRange())
        {
            for (Gadget gadget : dashboard.getGadgetsInColumn(index))
            {
                gadgets.add(createGadgetRepresentation(dashboard.getId(), gadget, gadgetRequestContext, writable, index));
            }
        }

        return new DashboardRepresentation.Builder(dashboard).writable(writable).gadgets(gadgets).build();
    }

    public GadgetRepresentation createGadgetRepresentation(final DashboardId dashboardId, final Gadget gadget, final GadgetRequestContext gadgetRequestContext, final boolean writable, final DashboardState.ColumnIndex column)
    {
        final GadgetRepresentation.GadgetUrlContainer gadgetUrls =
                new GadgetRepresentation.GadgetUrlContainer(renderedGadgetUriBuilder, dashboardUrlBuilder, dashboardId, gadget, gadgetRequestContext, writable);
        return new GadgetRepresentation(gadget, gadgetUrls, column);
    }
}
