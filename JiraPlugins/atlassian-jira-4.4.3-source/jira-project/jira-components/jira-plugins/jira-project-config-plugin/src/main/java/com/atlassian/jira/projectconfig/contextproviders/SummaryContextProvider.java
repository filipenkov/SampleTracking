package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.SimplePanel;
import com.atlassian.jira.projectconfig.beans.SimpleProject;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.component.ModuleWebComponent;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;
import com.google.common.collect.Lists;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Provides context for the main summary page, in particular the list of {@link SimplePanel} columns via leftColumn and
 * rightColumn objects.
 *
 * @since v4.4
 */
public class SummaryContextProvider implements CacheableContextProvider
{
    static final String SUMMARY_LEFT_PANELS_LOCATION = "webpanels.admin.summary.left-panels";
    static final String SUMMARY_RIGHT_PANELS_LOCATION = "webpanels.admin.summary.right-panels";

    static final String CONTEXT_PANEL_KEY = "panelKey";

    static final String CONTEXT_LEFT_COLUMN_KEY = "leftColumn";
    static final String CONTEXT_RIGHT_COLUMN_KEY = "rightColumn";
    static final String CONTEXT_SIMPLE_PROJECT_KEY = "simpleProject";

    private final WebInterfaceManager webInterfaceManager;
    private final ContextProviderUtils contextProviderUtils;
    private final ModuleWebComponent moduleWebComponent;
    private final JiraAuthenticationContext authenticationContext;


    public SummaryContextProvider(final WebInterfaceManager webInterfaceManager, final ContextProviderUtils contextProviderUtils,
            ModuleWebComponent moduleWebComponent, JiraAuthenticationContext authenticationContext)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.contextProviderUtils = contextProviderUtils;
        this.moduleWebComponent = moduleWebComponent;
        this.authenticationContext = authenticationContext;
    }

    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    /**
     * Gets the context used when rendering the summary web panel.
     * <p/>
     * In particular, contains:
     * <p/>
     * <dl>
     *     <dt>simpleProject</dt>
     *     <dd>
     *         A wrapped {@link Project} that has a subset of methods and renames description descriptionHtml to allow
     *         anti-xss escaping
     *     </dd>
     *     <dt>leftColumn</dt>
     *     <dd>
     *         Rendered web panels that should go on the left. Determined by the location SummaryContextProvider.SUMMARY_LEFT_PANELS_LOCATION
     *     </dd>
     *     <dt>rightColumn</dt>
     *     <dd>
     *         Rendered web panels that should go on the right. Determined by the location SummaryContextProvider.SUMMARY_RIGHT_PANELS_LOCATION
     *     </dd>
     * </dl>
     *
     * @param appProvidedContext
     * @return
     */
    public Map<String, Object> getContextMap(final Map<String, Object> appProvidedContext)
    {
        final Map<String, Object> defaultContext = contextProviderUtils.getDefaultContext();
        final Project project = (Project) defaultContext.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);

        final Map<String, Object> defaultPanelContext = MapBuilder.<String, Object>newBuilder()
                .addAll(appProvidedContext)
                .addAll(defaultContext)
                .toMap();

        final List<SimplePanel> leftSummaryPanels = getPanels(SUMMARY_LEFT_PANELS_LOCATION, defaultPanelContext, project);
        final List<SimplePanel> rightSummaryPanels = getPanels(SUMMARY_RIGHT_PANELS_LOCATION, defaultPanelContext, project);

        final SimpleProject wrappedProject = new SimpleProject(project);

        return MapBuilder.<String, Object>newBuilder()
                .addAll(defaultPanelContext)
                .add(CONTEXT_LEFT_COLUMN_KEY, leftSummaryPanels)
                .add(CONTEXT_RIGHT_COLUMN_KEY, rightSummaryPanels)
                .add(CONTEXT_SIMPLE_PROJECT_KEY, wrappedProject)
                .toMap();
    }

    private List<SimplePanel> getPanels(final String location, final Map<String, Object> defaultPanelContext, final Project project)
    {
        final List<SimplePanel> panels = Lists.newArrayList();

        final Map<String, Object> params = MapBuilder.<String, Object>build(ContextProviderUtils.CONTEXT_PROJECT_KEY, project);

        final List<WebPanelModuleDescriptor> summaryPanels = webInterfaceManager.getDisplayableWebPanelDescriptors(location, params);
        for (final WebPanelModuleDescriptor desc : summaryPanels)
        {
            final SimplePanel panel = getPanelFromDescriptor(desc, defaultPanelContext, project);
            panels.add(panel);
        }

        return panels;
    }

    private SimplePanel getPanelFromDescriptor(final WebPanelModuleDescriptor desc, final Map<String, Object> defaultPanelContext, final Project project)
    {
        String name;

        final WebLabel webLabel = desc.getWebLabel();
        final HttpServletRequest req = ExecutingHttpRequest.get();
        if (webLabel != null)
        {
            name = webLabel.getDisplayableLabel(req, defaultPanelContext);
        }
        else
        {
            name = desc.getCompleteKey();
        }
        final String panelDescriptorKey = desc.getKey();

        final Map<String, Object> panelContext = MapBuilder.newBuilder(defaultPanelContext)
                .add(CONTEXT_PANEL_KEY, panelDescriptorKey)
                .add("helper", new JiraHelper(req, project))
                .add("prefix", "project-config-webpanel-")
                .add("containerClass", "project-config-webpanel")
                .toMap();

        final String content = moduleWebComponent.renderModule(authenticationContext.getUser(), req, desc, panelContext);

        return new SimplePanel(name, panelDescriptorKey, content);
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }
}
