package com.atlassian.gadgets.dashboard.internal.velocity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.DashboardTab;
import com.atlassian.gadgets.dashboard.internal.StateConverter;
import com.atlassian.gadgets.dashboard.internal.Tab;
import com.atlassian.gadgets.dashboard.internal.impl.TabImpl;
import com.atlassian.gadgets.dashboard.internal.rest.representations.RepresentationFactory;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.gadgets.dashboard.view.DashboardTabViewFactory;
import com.atlassian.gadgets.directory.spi.DirectoryPermissionService;
import com.atlassian.gadgets.view.ViewComponent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.HelpPathResolver;
import com.atlassian.templaterenderer.TemplateRenderer;

import static com.google.common.base.Preconditions.checkNotNull;

public class VelocityViewFactory implements DashboardTabViewFactory
{
    private final TemplateRenderer renderer;
    private final DashboardPermissionService dashboardPermissionService;
    private final DirectoryPermissionService directoryPermissionService;
    private final StateConverter stateConverter;
    private final DashboardEmbedder dashboardEmbedder;
    private final HelpPathResolver helpPathResolver;
    private final ApplicationProperties applicationProperties;

    public VelocityViewFactory(TemplateRenderer renderer,
            StateConverter stateConverter,
            DashboardPermissionService dashboardPermissionService,
            DirectoryPermissionService directoryPermissionService,
            RepresentationFactory representationFactory,
            DashboardEmbedder dashboardEmbedder, final HelpPathResolver helpPathResolver, final ApplicationProperties applicationProperties)
    {
        this.helpPathResolver = helpPathResolver;
        this.applicationProperties = applicationProperties;
        this.renderer = checkNotNull(renderer, "renderer");
        this.stateConverter = checkNotNull(stateConverter, "stateConverter");
        this.dashboardPermissionService = checkNotNull(dashboardPermissionService, "dashboardPermissionService");
        this.directoryPermissionService = checkNotNull(directoryPermissionService, "directoryPermissionService");
        this.dashboardEmbedder = checkNotNull(dashboardEmbedder, "dashboardEmbedder");
    }

    public ViewComponent createDashboardView(final Iterable<DashboardTab> tabs, final DashboardState selectedTabDashboardState,
            @Nullable final String username, final int maxGadgets, final GadgetRequestContext gadgetRequestContext)
    {
        return new DashboardView(renderer,
                getVisibleTabs(tabs, username),
                stateConverter.convertStateToDashboard(selectedTabDashboardState, gadgetRequestContext),
                username,
                maxGadgets,
                gadgetRequestContext,
                dashboardPermissionService.isWritableBy(selectedTabDashboardState.getId(), username),
                directoryPermissionService.canConfigureDirectory(username),
                dashboardEmbedder,
                helpPathResolver,
                applicationProperties);
    }

    private Iterable<Tab> getVisibleTabs(final Iterable<DashboardTab> tabs, final String username)
    {
        final List<Tab> ret = new ArrayList<Tab>();
        for (DashboardTab tab : tabs)
        {
            if (dashboardPermissionService.isReadableBy(tab.getDashboardId(), username))
            {
                ret.add(new TabImpl(tab, dashboardPermissionService.isWritableBy(tab.getDashboardId(), username)));
            }
        }
        return ret;
    }
}
