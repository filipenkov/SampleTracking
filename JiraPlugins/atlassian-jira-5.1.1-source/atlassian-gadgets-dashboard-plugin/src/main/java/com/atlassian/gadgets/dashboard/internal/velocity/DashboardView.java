package com.atlassian.gadgets.dashboard.internal.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.internal.AbstractViewComponent;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.Tab;
import com.atlassian.gadgets.dashboard.internal.util.HelpLinkResolver;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewType;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.HelpPathResolver;
import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.IOUtils.closeQuietly;

class DashboardView extends AbstractViewComponent
{
    private final Dashboard dashboard;
    private final boolean writable;
    private final boolean canAddExternalGadgetsToDirectory;
    private final String username;
    private final TemplateRenderer renderer;
    private final Iterable<Tab> tabs;
    private final GadgetRequestContext gadgetRequestContext;
    private final String templateName;
    private final HelpLinkResolver linkResolver;
    private final int maxGadgets;
    private final DashboardEmbedder dashboardEmbedder;
    private final ApplicationProperties applicationProperties;

    DashboardView(TemplateRenderer renderer,
            Iterable<Tab> tabs,
            Dashboard dashboard,
            String username,
            int maxGadgets,
            GadgetRequestContext gadgetRequestContext,
            boolean writable,
            boolean canAddExternalGadgetsToDirectory,
            DashboardEmbedder dashboardEmbedder,
            HelpPathResolver helpPathResolver,
            ApplicationProperties applicationProperties)
    {
        super(dashboard.getId().toString(), dashboard.getTitle());
        this.renderer = renderer;
        this.tabs = tabs;
        this.applicationProperties = applicationProperties;
        this.templateName = "/dashboard.vm";
        this.dashboard = dashboard;
        this.username = username;
        this.gadgetRequestContext = gadgetRequestContext;
        this.writable = writable;
        this.canAddExternalGadgetsToDirectory = canAddExternalGadgetsToDirectory;
        this.linkResolver = new HelpLinkResolver(helpPathResolver);
        this.maxGadgets = maxGadgets;
        this.dashboardEmbedder = dashboardEmbedder;
    }

    public final void writeTo(Writer writer) throws RenderingException, IOException
    {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("id", getId());
        context.put("title", getTitle());
        context.put("writer", writer);
        context.put("dashboard", dashboard);
        context.put("username", username);
        context.put("viewSettings", new View.Builder().viewType(ViewType.DEFAULT).writable(writable).build());
        context.put("canAddExternalGadgetsToDirectory", canAddExternalGadgetsToDirectory);
        context.put("tabs", tabs);
        context.put("gadgetRequestContext", gadgetRequestContext);
        context.put("linkLearnHow", linkResolver.getLink("how.to.create.gadgets"));
        context.put("linkFindMore", linkResolver.getLink("find.more.gadgets"));
        context.put("whitelistExternalGadgets", linkResolver.getLink("whitelist.external.gadget"));
        context.put("linkLearnMoreAboutGadgets", linkResolver.getLink("learn.more.about.gadgets"));
        context.put("linkPluginHintGadgets", linkResolver.getLink("plugin.hint.gadgets"));
        String showMarketingHints = applicationProperties.getPropertyValue("show.plugin.marketing.hints");
        context.put("showMarketingPluginHints", (showMarketingHints == null || Boolean.valueOf(showMarketingHints)));
        context.put("maxGadgets", maxGadgets);
        context.put("writable", writable);
        context.put("embed", dashboardEmbedder);
        renderer.render(templateName, context, writer);
    }

}
