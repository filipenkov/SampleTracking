package com.atlassian.jira.plugin.issuenav.viewissue.webpanel;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.component.ModuleWebComponent;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import webwork.action.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.web.action.AjaxHeaders.isPjaxRequest;

/**
 * Utility class to help with rendering the pluggable web panels on the view issue page.
 *
 * Copied from JIRA master for now. TODO: MERGE THIS BACK INTO MASTER AND REMOVE THIS CLASS!
 *
 * @since 5.1
 */
public class IssueWebPanelRenderUtil
{
    private static final String JIRA_VIEW_ISSUE_INFO_CONTEXT = "atl.jira.view.issue.info.context";
    private static final String JIRA_VIEW_ISSUE_RIGHT_CONTEXT = "atl.jira.view.issue.right.context";
    private static final String JIRA_VIEW_ISSUE_LEFT_CONTEXT = "atl.jira.view.issue.left.context";
    private final User loggedInUser;
    private final Issue issue;
    private final WebInterfaceManager webInterfaceManager;
    private final ModuleWebComponent moduleWebComponent;
    private final Map<String,Object> webPanelParams;

    public IssueWebPanelRenderUtil(final User loggedInUser, final Issue issue,
            final Action action, final WebInterfaceManager webInterfaceManager,
            final ModuleWebComponent moduleWebComponent)
    {
        this.loggedInUser = loggedInUser;
        this.webInterfaceManager = webInterfaceManager;
        this.moduleWebComponent = moduleWebComponent;
        this.issue = issue;

        webPanelParams = new HashMap<String, Object>();
        webPanelParams.put("user", loggedInUser);
        webPanelParams.put("project", issue.getProjectObject());
        webPanelParams.put("issue", issue);
        webPanelParams.put("action", action);
        webPanelParams.put("helper", getHelper());
        webPanelParams.put("isAsynchronousRequest", isPjaxRequest(ExecutingHttpRequest.get()));
    }
    
    public JiraHelper getHelper()
    {
        return  new JiraHelper(ExecutingHttpRequest.get(), issue.getProjectObject(), webPanelParams);
    }

    public List<WebPanelModuleDescriptor> getInfoWebPanels()
    {
        return getWebPanels(JIRA_VIEW_ISSUE_INFO_CONTEXT);
    }

    public List<WebPanelModuleDescriptor> getRightWebPanels()
    {
        return getWebPanels(JIRA_VIEW_ISSUE_RIGHT_CONTEXT);
    }

    public List<WebPanelModuleDescriptor> getLeftWebPanels()
    {
        return getWebPanels(JIRA_VIEW_ISSUE_LEFT_CONTEXT);
    }

    public Map<String, Object> getWebPanelContext()
    {
        return webPanelParams;
    }

    private List<WebPanelModuleDescriptor> getWebPanels(final String location)
    {
        return webInterfaceManager.getDisplayableWebPanelDescriptors(location, webPanelParams);
    }

    public String renderPanels(List<WebPanelModuleDescriptor> panels)
    {
        if (panels != null)
        {
            return moduleWebComponent.renderModules(loggedInUser, ExecutingHttpRequest.get(), panels, webPanelParams);

        }

        return "";
    }

    public String renderPanel(WebPanelModuleDescriptor panel)
    {
        if (panel != null)
        {
            return moduleWebComponent.renderModule(loggedInUser, ExecutingHttpRequest.get(), panel, webPanelParams);
        }

        return "";
    }

    public String renderHeadlessPanel(WebPanelModuleDescriptor panel)
    {
        if (panel != null)
        {
            final Map<String, Object> params = new HashMap<String, Object>(webPanelParams);
            params.put("headless", true);
            return moduleWebComponent.renderModule(loggedInUser, ExecutingHttpRequest.get(), panel, params);
        }

        return "";
    }
}
