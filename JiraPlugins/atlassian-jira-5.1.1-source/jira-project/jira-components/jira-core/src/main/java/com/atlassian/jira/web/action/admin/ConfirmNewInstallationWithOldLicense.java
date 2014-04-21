/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.BuildNumComparator;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventType;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import webwork.action.ServletActionContext;

import javax.annotation.Nullable;

import static com.atlassian.jira.license.LicenseJohnsonEventRaiser.LICENSE_TOO_OLD;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.any;

/**
 * Displays the page to update the current JIRA license when it has been detected that the current license is
 * <em>&quot;too old&quot;</em>. <p/> <p>Security: This action is only accessible when a Johnson Event of type {@link
 * com.atlassian.jira.license.LicenseJohnsonEventRaiser#LICENSE_TOO_OLD} is present in the {@link JohnsonEventContainer}
 * </p> <p/> <p>Trigger: The link to display this action is displayed in the Johnson errors page (errors.jsp)</p>
 *
 * @see com.atlassian.jira.license.LicenseJohnsonEventRaiser
 * @see com.atlassian.jira.upgrade.UpgradeLauncher
 * @see JohnsonEventContainer
 */
public class ConfirmNewInstallationWithOldLicense extends JiraWebActionSupport
{
    private static final String CROWD_EMBEDDED_INTEGRATION_VERSION = "602";

    private final JiraLicenseUpdaterService jiraLicenseService;
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraSystemRestarter jiraSystemRestarter;
    private final CrowdService crowdService;
    private final PermissionManager permissionManager;

    private String userName;
    private String password;
    private String licenseString;
    private String confirm;
    private boolean licenseUpdated = false;
    private boolean installationConfirmed = false;
    private LicenseDetails licenseDetails;
    private JiraLicenseService.ValidationResult validationResult;


    public ConfirmNewInstallationWithOldLicense(final JiraLicenseUpdaterService jiraLicenseService,
            final BuildUtilsInfo buildUtilsInfo, final JiraSystemRestarter jiraSystemRestarter,
            CrowdService crowdService, PermissionManager permissionManager)
    {
        this.crowdService = crowdService;
        this.permissionManager = permissionManager;
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.jiraSystemRestarter = notNull("jiraSystemRestarter", jiraSystemRestarter);
    }

    /**
     * Whether this page should be displayed to the end user.
     * @return true if the page should be displayed; otherwise, false.
     */
    private boolean shouldDisplay()
    {
        return isPresentInJohnsonEventContainer(EventType.get(LICENSE_TOO_OLD));
    }

    /**
     * Whether there is any event of the specified type in the current johnson event container.
     *
     *
     * @param eventType The event type to look for.
     * @return true if there is any event of the specified type; otherwise, false.
     */
    private boolean isPresentInJohnsonEventContainer(final EventType eventType)
    {
        final class IsEventOfType implements Predicate<Event>
        {
            private EventType eventType;

            private IsEventOfType(final EventType eventType) {this.eventType = eventType;}

            @Override
            public boolean apply(@Nullable com.atlassian.johnson.event.Event event)
            {
                return event != null && (event.getKey().equals(eventType));
            }
        }

        return any(getJohnsonEventContainer().getEvents(), new IsEventOfType(eventType));
    }

    @Override
    public String doDefault() throws Exception
    {
        if (!shouldDisplay())
        {
            return "securitybreach";
        }

        return INPUT;
    }

    protected void doValidation()
    {
        if (!shouldDisplay())
        {
            return; // break out of here early if we are not allowed to access this page.
        }
        if (getUserInfoAvailable())
        {
            //check that the user is an admin and that the password is correct
            User user = crowdService.getUser(userName);

            if (user == null)
            {
                addErrorMessage(getText("admin.errors.invalid.username.or.pasword"));
                return;
            }
            try
            {
                crowdService.authenticate(userName, password);
            }
            catch (FailedAuthenticationException e)
            {
                addErrorMessage(getText("admin.errors.invalid.username.or.pasword"));
                return;
            }

            if (!nonAdminUpgradeAllowed())
            {
                boolean hasAdminPermission = permissionManager.hasPermission(Permissions.ADMINISTER, user);

                if (!hasAdminPermission)
                {
                    addError("userName", getText("admin.errors.no.admin.permission"));
                }
            }
        }

        if (StringUtils.isNotBlank(licenseString))
        {
            validationResult = jiraLicenseService.validate(this, licenseString);
            addErrorCollection(validationResult.getErrorCollection());
        }
        else if (StringUtils.isBlank(confirm))
        {
            log.warn("Neither the License nor the Install Confirmation have been supplied.");
            addErrorMessage(getText("admin.errors.no.license"));
        }
    }

    public String doExecute() throws Exception
    {
        if (!shouldDisplay())
        {
            return "securitybreach";
        }

        // Check if the license has been entered
        if (StringUtils.isNotBlank(licenseString))
        {
            licenseDetails = jiraLicenseService.setLicense(validationResult);
            if (!licenseDetails.isMaintenanceValidForBuildDate(buildUtilsInfo.getCurrentBuildDate()))
            {
                addError("license", getText("admin.errors.license.too.old"));
                return ERROR;
            }
            else
            {
                licenseUpdated = true;
            }
        }
        else if (StringUtils.isNotBlank(confirm)) // Check that the Installation under the Evaluation Terms has been confirmed
        {
            jiraLicenseService.confirmProceedUnderEvaluationTerms(userName);
            installationConfirmed = true;
        }
        else
        {
            throw new IllegalStateException("This will never happen!");
        }

        // rock JIRA's world!
        jiraSystemRestarter.ariseSirJIRA();

        // Remove the Old Licence Event
        JohnsonEventContainer cont = JohnsonEventContainer.get(ServletActionContext.getServletContext());
        for (final Object o : cont.getEvents())
        {
            Event event = (Event) o;
            if (event != null && event.getKey().equals(EventType.get("license-too-old")))
            {
                cont.removeEvent(event);
            }
        }
        return SUCCESS;
    }

    JohnsonEventContainer getJohnsonEventContainer()
    {
        return JohnsonEventContainer.get(ServletActionContext.getServletContext());
    }

    public LicenseDetails getLicenseDetails()
    {
        if (licenseDetails == null)
        {
            licenseDetails = jiraLicenseService.getLicense();
        }
        return licenseDetails;
    }

    public String getLicenseStatusMessage()
    {
        return getLicenseDetails().getLicenseStatusMessage(getLoggedInUser(), "<br/>");
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setLicense(String licenseString)
    {
        this.licenseString = licenseString;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getPassword()
    {
        return password;
    }

    public String getLicense()
    {
        return licenseString;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public boolean isLicenseUpdated()
    {
        return licenseUpdated;
    }

    public boolean isInstallationConfirmed()
    {
        return installationConfirmed;
    }

    private boolean nonAdminUpgradeAllowed()
    {
        return Boolean.valueOf(System.getProperty(SystemPropertyKeys.UPGRADE_SYSTEM_PROPERTY));
    }

    public BuildUtilsInfo getBuildUtilsInfo()
    {
        return buildUtilsInfo;
    }

    public String getCurrentBuildDate()
    {
        return getOutlookDate().formatDMY(buildUtilsInfo.getCurrentBuildDate());
    }

    /**
     * During upgrades from 4.2 or earlier to 4.3 or later the user information is not available until
     * after the upgrade has run.
     * @return True if user information is available and we can authenticate users.
     */
    public boolean getUserInfoAvailable()
    {
        BuildNumComparator comparator = new BuildNumComparator();
        // If the code version running is pre the crowd integration then just return true.
        if (comparator.compare(buildUtilsInfo.getCurrentBuildNumber(), CROWD_EMBEDDED_INTEGRATION_VERSION) < 0)
        {
            return true;
        }

        String currentDatabaseVersion = getApplicationProperties().getString(APKeys.JIRA_PATCHED_VERSION);
        return !Strings.isNullOrEmpty(currentDatabaseVersion) && comparator.compare(currentDatabaseVersion, CROWD_EMBEDDED_INTEGRATION_VERSION) > 0;
    }
}
