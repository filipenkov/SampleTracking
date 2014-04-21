package com.atlassian.jira.plugin.issuenav.viewissue.webpanel;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.component.ModuleWebComponent;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns a mapping of panel key -> panel html in order of webpanels.
 * @since v5.1
 */
public class WebPanelMapperUtil
{
    private final WebInterfaceManager webInterfaceManager;
    private final SimpleLinkManager simpleLinkManager;
    private final JiraAuthenticationContext authenticationContext;
    private final ModuleWebComponent moduleWebComponent;

    public WebPanelMapperUtil(WebInterfaceManager webInterfaceManager, SimpleLinkManager simpleLinkManager,
                              JiraAuthenticationContext authenticationContext, ModuleWebComponent moduleWebComponent)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.simpleLinkManager = simpleLinkManager;
        this.authenticationContext = authenticationContext;
        this.moduleWebComponent = moduleWebComponent;
    }

    public IssueWebPanelsBean create(Issue issue, Action action)
    {
        final IssueWebPanelRenderUtil issueWebPanelRenderUtil = new IssueWebPanelRenderUtil(authenticationContext.getLoggedInUser(),
                issue, action, this.webInterfaceManager, this.moduleWebComponent);

        List<WebPanelBean> leftPanels = this.mapAndRenderPanels(issueWebPanelRenderUtil, issueWebPanelRenderUtil.getLeftWebPanels());
        List<WebPanelBean> rightPanels = this.mapAndRenderPanels(issueWebPanelRenderUtil, issueWebPanelRenderUtil.getRightWebPanels());
        List<WebPanelBean> infoPanels = this.mapAndRenderPanels(issueWebPanelRenderUtil, issueWebPanelRenderUtil.getInfoWebPanels());

        return new IssueWebPanelsBean(leftPanels, rightPanels, infoPanels);
    }


    private List<WebPanelBean> mapAndRenderPanels(IssueWebPanelRenderUtil issueWebPanelRenderUtil,
                                                  List<WebPanelModuleDescriptor> webPanels)
    {
        final List<WebPanelBean> panels = new ArrayList<WebPanelBean>();
        for (WebPanelModuleDescriptor webPanel : webPanels)
        {
            final WebPanelBean bean = getWebPanelBeanBuilder(issueWebPanelRenderUtil, webPanel).build();
            if(bean != null)
            {
                panels.add(bean);
            }
        }
        return panels;
    }

    private WebPanelBeanBuilder getWebPanelBeanBuilder(final IssueWebPanelRenderUtil issueWebPanelRenderUtil, final WebPanelModuleDescriptor webPanel)
    {
        return new WebPanelBeanBuilder(webInterfaceManager, simpleLinkManager,
                issueWebPanelRenderUtil, authenticationContext.getI18nHelper(), authenticationContext.getLoggedInUser(), webPanel);
    }
}
