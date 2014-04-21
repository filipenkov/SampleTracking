package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.plugin.webfragment.conditions.UserIsProjectAdminCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Administration Summary Action.  Displays the Administration Summary Page
 *
 * @since v4.4
 */
public class AdminSummary extends JiraWebActionSupport
{
    static final String CONTEXT_PANEL_KEY = "panelKey";
    static final String CONTEXT_TOP_PANEL_KEY = "webpanels.admin.summary.top-panels";
    static final String CONTEXT_LEFT_COLUMN_KEY = "webpanels.admin.summary.left-column";
    static final String CONTEXT_RIGHT_COLUMN_KEY = "webpanels.admin.summary.right-column";
    static final String CONTEXT_IS_ADMIN_KEY = "isAdmin";
    static final String CONTEXT_IS_SYSTEM_ADMIN_KEY = "isSystemAdmin";
    static final String CONTEXT_I18N_KEY = "i18n";


    private final WebInterfaceManager webInterfaceManager;
    private final WebResourceManager webResourceManager;
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final VelocityRequestContextFactory requestContextFactory;

    public AdminSummary(WebResourceManager webResourceManager, WebInterfaceManager webInterfaceManager,
            JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, VelocityRequestContextFactory requestContextFactory)
    {
        this.webResourceManager = webResourceManager;
        this.webInterfaceManager = webInterfaceManager;
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.requestContextFactory = requestContextFactory;
    }

    protected String doExecute() throws Exception
    {
        // clear the current project from the session
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();
        final VelocityRequestSession session = requestContext.getSession();
        session.removeAttribute(SessionKeys.CURRENT_ADMIN_PROJECT);
        session.removeAttribute(SessionKeys.CURRENT_ADMIN_PROJECT_TAB);

        final UserIsProjectAdminCondition condition = new UserIsProjectAdminCondition(permissionManager);
        //must be either project or normal admin to see this page!
        if(!condition.shouldDisplay(authenticationContext.getUser(), new JiraHelper(ExecutingHttpRequest.get())) &&
                !permissionManager.hasPermission(Permissions.ADMINISTER, getLoggedInUser()))
        {
            return PERMISSION_VIOLATION_RESULT;
        }

        webResourceManager.requireResource("jira.webresources:adminsummary");
        webResourceManager.requireResource("com.atlassian.jira.jira-admin-summary-plugin:admin-summary");
        return INPUT;
    }

    private Map<String, Object> getDefaultContext()
    {
        return MapBuilder.<String, Object>newBuilder()
                .add(CONTEXT_IS_ADMIN_KEY, hasAdminPermission())
                .add(CONTEXT_IS_SYSTEM_ADMIN_KEY, hasSystemAdminPermission())
                .add(CONTEXT_I18N_KEY, authenticationContext.getI18nHelper())
                .toMap();
    }

    public List<AdminSummaryPanel> getTopPanels()
    {
        return getPanels(CONTEXT_TOP_PANEL_KEY, getDefaultContext());
    }
    
    public List<AdminSummaryPanel> getLeftPanels()
    {
        return getPanels(CONTEXT_LEFT_COLUMN_KEY, getDefaultContext());
    }

    public List<AdminSummaryPanel> getRightPanels()
    {
        return getPanels(CONTEXT_RIGHT_COLUMN_KEY, getDefaultContext());
    }

    private List<AdminSummaryPanel> getPanels(final String location, final Map<String, Object> defaultPanelContext)
    {
        final List<AdminSummaryPanel> panels = Lists.newArrayList();

        final List<WebPanelModuleDescriptor> summaryPanels = webInterfaceManager.getDisplayableWebPanelDescriptors(location,
                Collections.<String, Object>emptyMap());
        for (final WebPanelModuleDescriptor desc : summaryPanels)
        {
            final AdminSummaryPanel panel = getPanelFromDescriptor(desc, defaultPanelContext);
            panels.add(panel);
        }

        return panels;
    }

    private AdminSummaryPanel getPanelFromDescriptor(final WebPanelModuleDescriptor desc, final Map<String, Object> defaultPanelContext)
    {
        String name;

        final WebLabel webLabel = desc.getWebLabel();
        if (webLabel != null)
        {
            name = webLabel.getDisplayableLabel(ExecutingHttpRequest.get(), defaultPanelContext);
        }
        else
        {
            name = desc.getCompleteKey();
        }
        final WebPanel webPanel = desc.getModule();
        final String panelDescriptorKey = desc.getKey();

        final Map<String, Object> panelContext = MapBuilder.newBuilder(defaultPanelContext)
                .add(CONTEXT_PANEL_KEY, panelDescriptorKey)
                .toMap();

        return new  AdminSummaryPanel(name, panelDescriptorKey, webPanel.getHtml(panelContext));
    }

    private boolean hasSystemAdminPermission()
    {
        return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, authenticationContext.getLoggedInUser());
    }

    private boolean hasAdminPermission()
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser());
    }

    private int getNumberOfTabs()
    {
        return 0;
    }
}
