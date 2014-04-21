package com.atlassian.jira.plugin.navigation;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.web.component.webfragment.SystemNavContextLayoutBean;
import com.atlassian.jira.web.component.webfragment.UserNavContextLayoutBean;
import com.atlassian.jira.web.component.webfragment.WebFragmentWebComponent;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.util.profiling.UtilTimerStack;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Module descriptor used for plugins that render the top navigation in JIRA.
 *
 * @since v3.12
 */
//@RequiresRestart
public class TopNavigationModuleDescriptor extends JiraResourcedModuleDescriptor<PluggableTopNavigation> implements OrderableModuleDescriptor
{
    private static final Logger log = Logger.getLogger(TopNavigationModuleDescriptor.class);
    private static final String VIEW_TEMPLATE = "view";

    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final WebResourceManager webResourceManager;
    private final PermissionManager permissionManager;
    private final WebFragmentWebComponent webFragmentWebComponent;
    private final UserProjectHistoryManager userProjectHistoryManager;
    private final JiraWebInterfaceManager webInterfaceManager;

    private int order;

    public TopNavigationModuleDescriptor(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties,
                                         WebResourceManager webResourceManager, PermissionManager permissionManager,
                                         WebFragmentWebComponent webFragmentWebComponent, UserProjectHistoryManager userProjectHistoryManager,
                                         final ModuleFactory moduleFactory, final JiraWebInterfaceManager webInterfaceManager)
    {
        super(authenticationContext, moduleFactory);
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.webResourceManager = webResourceManager;
        this.permissionManager = permissionManager;
        this.webFragmentWebComponent = webFragmentWebComponent;
        this.userProjectHistoryManager = userProjectHistoryManager;
        this.webInterfaceManager = webInterfaceManager;
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        order = ModuleDescriptorXMLUtils.getOrder(element);
    }

    public int getOrder()
    {
        return order;
    }

    public String getTopNavigationHtml(HttpServletRequest request, Map<String, Object> startingParms)
    {
        Map params = createVelocityParams(request, startingParms);
        return getHtml(VIEW_TEMPLATE, params);
    }

    private Map createVelocityParams(HttpServletRequest request, Map<String, Object> startingParams)
    {
        Map<String, Object> params = (startingParams != null) ? new HashMap<String, Object>(startingParams) : new HashMap<String, Object>();

        LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);

        // Add the specific things we will need in a navigation bar
        params.put("lookAndFeelBean", lookAndFeelBean);
        params.put("topBgColour", lookAndFeelBean.getTopBackgroundColour());
        params.put("menuBgColour", lookAndFeelBean.getMenuBackgroundColour());
        params.put("linkColour", lookAndFeelBean.getTextLinkColour());
        params.put("linkAColour", lookAndFeelBean.getTextActiveLinkColour());
        String jiraLogo = lookAndFeelBean.getLogoUrl();
        if (jiraLogo != null && !jiraLogo.startsWith("http://") && !jiraLogo.startsWith("https://"))
        {
            jiraLogo = webResourceManager.getStaticResourcePrefix() + jiraLogo;
        }
        params.put("jiraLogo", jiraLogo);
        //
        // IF we dont have a PNG image as the base image, we cant do IE tricks, but when need to know up front
        //
        params.put("jiraLogoIsPNG", jiraLogo != null && jiraLogo.endsWith(".png"));
        params.put("jiraLogoWidth", lookAndFeelBean.getLogoWidth());
        params.put("jiraLogoHeight", lookAndFeelBean.getLogoHeight());
        params.put("jiraTitle", applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE));
        try
        {
            params.put("hasAnyProjects", permissionManager.hasProjects(Permissions.BROWSE, authenticationContext.getUser()));
            params.put("canCreateIssue", permissionManager.hasProjects(Permissions.CREATE_ISSUE, authenticationContext.getUser()));
        }
        catch (Exception e)
        {
            log.warn("Unable to find if user has browse project permission.", e);
        }

        params.put("currentUser", authenticationContext.getUser());
        params.put("navWebFragment", webFragmentWebComponent);
        params.put("webInterfaceManager", webInterfaceManager);
        params.put("userNavLayout", new UserNavContextLayoutBean(request));
        params.put("systemNavLayout", new SystemNavContextLayoutBean(request));
        params.put("utilTimerStack", new UtilTimerStack());
        Project selectedProject = userProjectHistoryManager.getCurrentProject(Permissions.BROWSE, authenticationContext.getUser());
        params.put("selectedProject", selectedProject);
        params.put("quickSearchHelpPath", new HelpUtil().getHelpPath("quicksearch"));
        params.put("jiraHelperNoProject", new JiraHelper(request));
        params.put("jiraHelperWithProject", new JiraHelper(request, selectedProject));
        params.put("helpPath", HelpUtil.getInstance().getHelpPath("default").getUrl());
        params.put("modifierKey", BrowserUtils.getModifierKey());

        params.put("selectSection", request.getAttribute("jira.selected.section"));

        params.put("inAdminMode",request.getAttribute("jira.admin.mode") != null);

        params.put("externalLinkUtil", ExternalLinkUtilImpl.getInstance());

        params.put("textutils", new TextUtils());

        return params;
    }

}
