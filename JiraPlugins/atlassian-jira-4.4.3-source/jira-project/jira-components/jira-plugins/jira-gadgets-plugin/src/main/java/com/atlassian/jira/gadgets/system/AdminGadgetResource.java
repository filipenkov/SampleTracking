package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.export.ExportService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.system.SystemInfoUtils;
import com.atlassian.jira.util.system.check.SystemEnvironmentChecklist;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.opensymphony.user.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * REST endpoint for the admin-gadget.
 *
 * @since v4.0
 */
@Path ("/admin")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class AdminGadgetResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final ApplicationProperties applicationProperties;
    private final SystemInfoUtils systemInfoUtils;
    private final ServiceManager serviceManager;
    private final GlobalPermissionManager globalPermissionManager;
    private final UserUtil userUtil;
    private final ExternalLinkUtil externalLinkUtil;
    private final JiraLicenseService jiraLicenseService;
    private final PluginAccessor pluginAccessor;

    public AdminGadgetResource(
            JiraAuthenticationContext authenticationContext,
            PermissionManager permissionManager,
            ApplicationProperties applicationProperties,
            SystemInfoUtils systemInfoUtils,
            ServiceManager serviceManager,
            GlobalPermissionManager globalPermissionManager,
            UserUtil userUtil,
            final JiraLicenseService jiraLicenseService,
            PluginAccessor pluginAccessor)
    {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        this.systemInfoUtils = systemInfoUtils;
        this.serviceManager = serviceManager;
        this.globalPermissionManager = globalPermissionManager;
        this.userUtil = userUtil;
        this.pluginAccessor = pluginAccessor;
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.externalLinkUtil = ExternalLinkUtilImpl.getInstance();
    }

    @GET
    public Response getData()
    {
        List<String> list = getWarningMessages();
        return Response.ok(createAdminProperties(list)).cacheControl(NO_CACHE).build();
    }

    private Object createAdminProperties(final List<String> list)
    {
        boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getUser());
        if (isAdmin)
        {
            return new AdminProperties(this, list);
        }
        else
        {
            return new NonAdminProperties();
        }
    }

    protected List<String> getWarningMessages()
    {
        return SystemEnvironmentChecklist.getWarningMessages(authenticationContext.getLocale(), true);
    }

    @XmlRootElement
    public static class NonAdminProperties
    {
        @XmlElement
        boolean isAdmin;

        public NonAdminProperties()
        {
            isAdmin = false;
        }
    }

    @XmlRootElement
    public static class AdminProperties
    {
        @XmlElement
        boolean isAdmin;
        @XmlElement
        boolean notExternalUserManagement;

        @XmlElement
        boolean hasZeroUserLicense;

        @XmlElement
        boolean hasExceededUserLimit;

        @XmlElement
        boolean hasReachedUserLimit;
        @XmlElement
        String dbConfigDocsUrl;
        @XmlElement
        boolean isSystemAdministrator;
        @XmlElement
        boolean isUsingHsql;
        @XmlElement
        boolean isGreenHopperInstalled;
        @XmlElement
        boolean isGreenHopperEnabled;
        @XmlElement
        boolean isJIMEnabled;
        @XmlElement
        boolean hasBackupService;
        @XmlElement
        String licenseStatusMessage;
        @XmlElement
        List<String> warningMessages;
        @XmlElement
        boolean nearExpiry;
        @XmlElement
        String licenseTypeNiceName;
        @XmlElement
        String partnerName;
        @XmlElement
        String licenseExpiryStatusMessage;
        @XmlElement
        String externalLinkMyAccount;
        @XmlElement
        String externalLinkPersonalSite;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private AdminProperties()
        {
        }

        private AdminProperties(AdminGadgetResource resource, List<String> warningMessages)
        {
            LicenseDetails licenseDetails = resource.jiraLicenseService.getLicense();
            notExternalUserManagement = !resource.applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
            isAdmin = resource.permissionManager.hasPermission(Permissions.ADMINISTER, resource.authenticationContext.getUser());
            hasZeroUserLicense = licenseDetails.getMaximumNumberOfUsers() == 0;
            hasExceededUserLimit = resource.userUtil.hasExceededUserLimit();
            hasReachedUserLimit = !resource.userUtil.canActivateNumberOfUsers(1);

            dbConfigDocsUrl = HelpUtil.getInstance().getHelpPath("dbconfig").getUrl();
            isSystemAdministrator = isSystemAdministrator(resource.authenticationContext, resource);
            hasBackupService = hasBackupService(resource.serviceManager);
            licenseStatusMessage = licenseDetails.getLicenseStatusMessage(resource.authenticationContext.getI18nHelper(), resource.authenticationContext.getOutlookDate(), "<br/><br/>");
            this.warningMessages = warningMessages;
            nearExpiry = licenseDetails.isLicenseAlmostExpired();
            licenseTypeNiceName = licenseDetails.getDescription();
            partnerName = licenseDetails.getPartnerName();
            isUsingHsql = resource.systemInfoUtils.getDatabaseType().equalsIgnoreCase("hsql");
            licenseExpiryStatusMessage = licenseDetails.getLicenseExpiryStatusMessage(resource.authenticationContext.getI18nHelper(), resource.authenticationContext.getOutlookDate());
            externalLinkMyAccount = resource.externalLinkUtil.getProperty("external.link.atlassian.my.account");
            externalLinkPersonalSite = resource.externalLinkUtil.getProperty("external.link.jira.personal.site");
            isGreenHopperInstalled = resource.pluginAccessor.getPlugin("com.pyxis.greenhopper.jira") != null;
            isGreenHopperEnabled = resource.pluginAccessor.isPluginEnabled("com.pyxis.greenhopper.jira");
            isJIMEnabled = resource.pluginAccessor.isPluginEnabled("com.atlassian.jira.plugins.jira-importers-plugin");
        }

        private boolean isSystemAdministrator(JiraAuthenticationContext authenticationContext, AdminGadgetResource adminGadgetResource)
        {
            User user = authenticationContext.getUser();
            return ((user != null) && adminGadgetResource.globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user));
        }

        private boolean hasBackupService(ServiceManager serviceManager)
        {
            for (Object service : serviceManager.getServices())
            {
                if (service instanceof JiraServiceContainer)
                {
                    JiraServiceContainer container = (JiraServiceContainer) service;
                    if (ExportService.class.getName().equals(container.getServiceClass()))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
