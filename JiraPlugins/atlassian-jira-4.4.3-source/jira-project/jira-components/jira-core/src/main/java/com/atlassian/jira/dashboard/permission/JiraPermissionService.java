package com.atlassian.jira.dashboard.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserUtil;
import org.apache.commons.lang.StringUtils;

/**
 * Defines who has permission to read/update dashboards as well as adding gadgets to the external
 * gadget directory.
 *
 * @since v4.0
 */
public class JiraPermissionService implements DashboardPermissionService
{
    private final UserUtil userUtil;
    private final PortalPageService portalPageService;
    private final PermissionManager permissionManager;

    private static final ThreadLocal<Boolean> allowEditingOfDefaultDashboard = new ThreadLocal<Boolean>()
    {
        @Override
        protected Boolean initialValue()
        {
            return Boolean.TRUE;
        }
    };

    public JiraPermissionService(final UserUtil userUtil, final PortalPageService portalPageService,
            final PermissionManager permissionManager)
    {
        this.userUtil = userUtil;
        this.portalPageService = portalPageService;
        this.permissionManager = permissionManager;
    }

    public boolean isReadableBy(final DashboardId dashboardId, @javax.annotation.Nullable final String username)
    {
        final JiraServiceContext context = new JiraServiceContextImpl(getUser(username));
        return portalPageService.validateForGetPortalPage(context, Long.valueOf(dashboardId.value()));
    }

    public boolean isWritableBy(final DashboardId dashboardId, @javax.annotation.Nullable final String username)
    {
        final JiraServiceContext context = new JiraServiceContextImpl(getUser(username));
        final PortalPage portalPage = portalPageService.getPortalPage(context, Long.valueOf(dashboardId.value()));
        //JRA-17497: If the state changes such that a user doesn't have permission to see a dashboard any longer
        // while a user is viewing a dashboard, then the portalPage may be null.
        if(portalPage == null)
        {
            return false;
        }
        if(!portalPage.isSystemDefaultPortalPage())
        {
            //if this is not the system default page, just check if the user has permission to update this page
            return portalPageService.validateForUpdate(context, portalPage);
        }

        //if this is the system default, and we do allow editing of it, the check if the user has permission.
        return allowEditingOfDefaultDashboard.get() && portalPageService.validateForUpdate(context, portalPage);
    }

    public static void setAllowEditingOfDefaultDashboard(final boolean allow)
    {
        allowEditingOfDefaultDashboard.set(allow);
    }

    private User getUser(final String username)
    {
        if(StringUtils.isEmpty(username))
        {
            return null;
        }
        return userUtil.getUserObject(username);
    }
}
