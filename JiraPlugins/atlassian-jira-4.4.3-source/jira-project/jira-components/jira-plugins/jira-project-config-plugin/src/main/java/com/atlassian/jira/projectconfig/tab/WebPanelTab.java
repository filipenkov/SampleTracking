package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * <p/>
 * A tab panel on the project configuration page that is rendered using a webpanel.
 *
 * <p/>
 * It will construct the tab contents from two web panels:
 * <ul>
 * <li>A header web-panel, assumed to be common for all inheriting tabs, located under 'tabs.admin.projectconfig.header'</li>
 * <li>A body web-panel, specific to a given tab, located under 'tabs.admin.projectconfig.{tab ID}'</li>
 * </ul>
 *
 * @since v4.4
 */
public abstract class WebPanelTab implements ProjectConfigTab
{
    private static final Logger log = Logger.getLogger(WebPanelTab.class);
    static final String HEADER_TAB_LOCATION = "tabs.admin.projectconfig.header";

    public static final String CURRENT_PROJECT = SessionKeys.CURRENT_ADMIN_PROJECT;
    public static final String CURRENT_TAB_NAME = SessionKeys.CURRENT_ADMIN_PROJECT_TAB;

    private final VelocityContextFactory factory;

    private final String id;
    private final String linkId;
    private final WebInterfaceManager webInterfaceManager;

    public WebPanelTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory factory, String id, String linkId)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.factory = factory;
        this.id = id;
        this.linkId = linkId;
    }

    public String getId()
    {
        return id;
    }

    public String getLinkId()
    {
        return linkId;
    }

    public String getTab(ProjectConfigTabRenderContext context)
    {
        final StringBuilder contents = new StringBuilder();
        final Map<String,Object> velocityContext = ImmutableMap.<String,Object>builder()
                .putAll(factory.createDefaultVelocityContext())
                .put(CURRENT_PROJECT, context.getProject())
                .put(CURRENT_TAB_NAME, id)
                .build();
        WebPanel headerPanel = getHeaderWebPanel(velocityContext);
        if (headerPanel != null)
        {
            contents.append(headerPanel.getHtml(velocityContext));
        }
        WebPanel bodyPanel = getBodyWebPanel(velocityContext);
        if (bodyPanel != null)
        {
            contents.append(bodyPanel.getHtml(velocityContext));
        }
        return contents.toString();
    }

    public void addResourceForProject(ProjectConfigTabRenderContext context)
    {
    }

    private WebPanel getBodyWebPanel(Map<String, Object> velocityContext)
    {
        String panelLocation = String.format("tabs.admin.projectconfig.%s", id);
        return getWebPanel(panelLocation, velocityContext, true);
    }

    private WebPanel getHeaderWebPanel(Map<String, Object> velocityContext)
    {
        return getWebPanel(HEADER_TAB_LOCATION, velocityContext, false);
    }

    private WebPanel getWebPanel(String location, Map<String, Object> velocityContext, boolean logWarns)
    {
        List<WebPanelModuleDescriptor> panels = webInterfaceManager.getDisplayableWebPanelDescriptors(location, velocityContext);
        if (panels.isEmpty())
        {
            if (logWarns)
            {
                log.warn("There are no panels that match '" + location + "'.");
            }
            return null;
        }
        else
        {
            if (panels.size() > 1 && logWarns)
            {
                log.warn("There are " + panels.size() + " panels that match '" + location + "', using the first.");
            }
            return panels.get(0).getModule();
        }
    }
}
