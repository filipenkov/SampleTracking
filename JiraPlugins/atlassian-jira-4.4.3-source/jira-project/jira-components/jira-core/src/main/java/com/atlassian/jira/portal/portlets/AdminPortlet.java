package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.export.ExportService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.system.SystemInfoUtils;
import com.atlassian.jira.util.system.check.SystemEnvironmentChecklist;
import com.atlassian.jira.web.util.HelpUtil;
import com.opensymphony.user.User;

import java.util.Iterator;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class AdminPortlet extends PortletImpl
{
    private final JiraAuthenticationContext authenticationContext;
    private final SystemInfoUtils systemInfoUtils;
    private final ServiceManager serviceManager;
    private final GlobalPermissionManager globalPermissionManager;
    private final UserUtil userUtil;
    private final JiraLicenseService jiraLicenseService;
    private final BuildUtilsInfo buildUtilsInfo;

    public AdminPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ApplicationProperties applicationProperties, SystemInfoUtils systemInfoUtils, ServiceManager serviceManager, GlobalPermissionManager globalPermissionManager, UserUtil userUtil, final JiraLicenseService jiraLicenseService, final BuildUtilsInfo buildUtilsInfo)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.authenticationContext = authenticationContext;
        this.systemInfoUtils = systemInfoUtils;
        this.serviceManager = serviceManager;
        this.globalPermissionManager = globalPermissionManager;
        this.userUtil = userUtil;
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    protected Map<String, Object> getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        final LicenseDetails licenseDetails = jiraLicenseService.getLicense();

        final Map<String, Object> params = super.getVelocityParams(portletConfiguration);
        // Note: we could just put the action in the context and have velocity call the methods, but it makes the template a bit ugly
        params.put("externaUserManagement", isExternalUserManagement());
        params.put("isSystemAdministrator", isSystemAdministrator());
        params.put("licenseDescription", licenseDetails.getDescription());
        params.put("licensePartnerName", licenseDetails.getPartnerName());
        params.put("hasReachedUserLimit", !userUtil.canActivateNumberOfUsers(1));
        params.put("hasExceededUserLimit", userUtil.hasExceededUserLimit());
        params.put("userLicenseLimit", licenseDetails.getMaximumNumberOfUsers());
        params.put("nearExpiry", licenseDetails.isLicenseAlmostExpired());
        params.put("licenseStatusMessage", licenseDetails.getLicenseStatusMessage(authenticationContext.getI18nHelper(), authenticationContext.getOutlookDate(), "<br/><br/>"));
        params.put("licenseExpiryStatusMessage", licenseDetails.getLicenseExpiryStatusMessage(authenticationContext.getI18nHelper(), authenticationContext.getOutlookDate()));
        params.put("isUsingHsql", isUsingHsql());
        params.put("hasBackupService", hasBackupService());
        params.put("dbConfigDocs", HelpUtil.getInstance().getHelpPath("dbconfig"));
        params.put("version", buildUtilsInfo.getVersion());
        params.put("buildNumber", buildUtilsInfo.getCurrentBuildNumber());
        params.put("serverid", jiraLicenseService.getServerId());
        params.put("warningMessages", SystemEnvironmentChecklist.getWarningMessages(authenticationContext.getLocale(), true));
        return params;
    }

    private Boolean hasBackupService()
    {
        for (Iterator i = serviceManager.getServices().iterator(); i.hasNext();)
        {
            Object service = i.next();
            if (service instanceof JiraServiceContainer)
            {
                JiraServiceContainer container = (JiraServiceContainer) service;
                if (ExportService.class.getName().equals(container.getServiceClass()))
                {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    private Boolean isUsingHsql()
    {
        return systemInfoUtils.getDatabaseType().equalsIgnoreCase("hsql") ? Boolean.TRUE : Boolean.FALSE;
    }

    private Boolean isExternalUserManagement()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT) ? Boolean.TRUE : Boolean.FALSE;
    }

    private Boolean isSystemAdministrator()
    {
        final User user = authenticationContext.getUser();
        return (user != null) && globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }
}
