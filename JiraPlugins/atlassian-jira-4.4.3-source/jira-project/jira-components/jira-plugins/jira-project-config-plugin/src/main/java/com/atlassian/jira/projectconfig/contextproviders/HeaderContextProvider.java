package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLabel;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLink;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.SimpleProject;
import com.atlassian.jira.projectconfig.tab.SummaryTab;
import com.atlassian.jira.projectconfig.tab.WebPanelTab;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.sitemesh.AdminDecoratorHelper;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.plugin.web.WebInterfaceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Context provider for the header.
 *
 * @since v4.4
 */
public class HeaderContextProvider implements ContextProvider
{
    static final String CONTEXT_SIMPLE_PROJECT_KEY = "simpleProject";
    static final String CONTEXT_VIEW_PROJECT_OPERATIONS_KEY = "viewableProjectOperations";
    static final String CONTEXT_VIEW_PROJECT_EDIT_OPERATION_KEY = "editProjectOperation";
    static final String CURRENT_TAB = AdminDecoratorHelper.ACTIVE_TAB_LINK_KEY;
    static final String SHOW_ACTIONS_MENU = "showActionsMenu";

    private final ContextProviderUtils utils;
    private final JiraAuthenticationContext authenticationContext;
    private final JiraWebInterfaceManager jiraWebInterfaceManager;

    public HeaderContextProvider(ContextProviderUtils utils, JiraAuthenticationContext authenticationContext,
            WebInterfaceManager webInterfaceManager)
    {
        this.utils = utils;
        this.authenticationContext = authenticationContext;
        this.jiraWebInterfaceManager = new JiraWebInterfaceManager(webInterfaceManager);
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
        //Nothing to do.
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {

        final MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder(context);
        Map<String, Object> pcContext = utils.getDefaultContext();
        contextBuilder.addAll(pcContext);

        User user = authenticationContext.getLoggedInUser();

        final Project project = (Project) pcContext.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);
        final SimpleProject wrappedProject = new SimpleProject(project);

        contextBuilder.add(SHOW_ACTIONS_MENU, isSummaryTab(context));
        contextBuilder.add(CONTEXT_SIMPLE_PROJECT_KEY, wrappedProject);
        addOperations(user, project, contextBuilder);

        return createContext(contextBuilder.toMap());
    }

    private boolean isSummaryTab(Map<String, Object> context)
    {
        return "view_project_summary".equals(context.get(CURRENT_TAB))
                || SummaryTab.NAME.equals(context.get(WebPanelTab.CURRENT_TAB_NAME));
    }

    private void addOperations(User user, Project project, MapBuilder<String, Object> ctx)
    {
        final JiraHelper jiraHelper = getJiraHelper(project);

        @SuppressWarnings ( { "unchecked" })
        final List<JiraWebItemModuleDescriptor> displayableItems =
                jiraWebInterfaceManager.getDisplayableItems("system.view.project.operations", user, jiraHelper);

        final List<SimpleViewableProjectOperation> ops = new ArrayList<SimpleViewableProjectOperation>(displayableItems.size() - 1);
        for (JiraWebItemModuleDescriptor displayableItem : displayableItems)
        {
            final SimpleViewableProjectOperation operation = createViewableProjectOperation(displayableItem, user, jiraHelper);
            if (displayableItem.getCompleteKey().equals("jira.webfragments.view.project.operations:edit_project"))
            {
                ctx.add(CONTEXT_VIEW_PROJECT_EDIT_OPERATION_KEY, operation);
            }
            else
            {
                ops.add(operation);
            }
        }

        ctx.add(CONTEXT_VIEW_PROJECT_OPERATIONS_KEY, ops);
    }

    private SimpleViewableProjectOperation createViewableProjectOperation(final JiraWebItemModuleDescriptor displayableItem,
            final User user, final JiraHelper jiraHelper)
    {
        final JiraWebLink link = (JiraWebLink) displayableItem.getLink();
        final JiraWebLabel jiraWebLabel = (JiraWebLabel) displayableItem.getLabel();

        final String displayableUrl = getUrlEncodedRenderedUrl(link.getRenderedUrl(user, jiraHelper));
        final String displayableLabel = jiraWebLabel.getDisplayableLabel(user, jiraHelper);

        return new SimpleViewableProjectOperation(link.getId(), displayableUrl, displayableLabel);
    }

    JiraHelper getJiraHelper(Project project)
    {
        return new JiraHelper(ExecutingHttpRequest.get(), project);
    }

    String getUrlEncodedRenderedUrl(final String renderedUrl)
    {
        return createUrlBuilder(renderedUrl).asUrlString();
    }

    Map<String, Object> createContext(Map<String, Object> params)
    {
        return JiraVelocityUtils.getDefaultVelocityParams(params, authenticationContext);
    }

    private UrlBuilder createUrlBuilder(final String operation)
    {
        return utils.createUrlBuilder(operation);
    }

    public static class SimpleViewableProjectOperation
    {
        private final String linkId;
        private final String displayableUrl;
        private final String displayableLabel;

        public SimpleViewableProjectOperation(final String linkId, final String displayableUrl, final String displayableLabel)
        {
            this.linkId = linkId;
            this.displayableUrl = displayableUrl;
            this.displayableLabel = displayableLabel;
        }

        public String getLinkId()
        {
            return linkId;
        }

        public String getDisplayableUrl()
        {
            return displayableUrl;
        }

        public String getDisplayableLabel()
        {
            return displayableLabel;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleViewableProjectOperation that = (SimpleViewableProjectOperation) o;

            if (displayableLabel != null ? !displayableLabel.equals(that.displayableLabel) : that.displayableLabel != null)
            { return false; }
            if (displayableUrl != null ? !displayableUrl.equals(that.displayableUrl) : that.displayableUrl != null)
            { return false; }
            if (linkId != null ? !linkId.equals(that.linkId) : that.linkId != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = linkId != null ? linkId.hashCode() : 0;
            result = 31 * result + (displayableUrl != null ? displayableUrl.hashCode() : 0);
            result = 31 * result + (displayableLabel != null ? displayableLabel.hashCode() : 0);
            return result;
        }
    }
}
