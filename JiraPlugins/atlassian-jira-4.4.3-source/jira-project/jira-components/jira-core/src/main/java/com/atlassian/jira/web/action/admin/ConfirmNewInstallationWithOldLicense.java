/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.BuildNumComparator;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventType;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import webwork.action.ServletActionContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class ConfirmNewInstallationWithOldLicense extends JiraWebActionSupport
{
    private static final String CROWD_EMBEDDED_INTEGRATION_VERSION = "602";
    private String userName;
    private String password;
    private String license;
    private String confirm;
    private boolean licenseUpdated = false;
    private boolean installationConfirmed = false;
    private LicenseDetails licenseDetails;
    private JiraLicenseService.ValidationResult validationResult;

    private final JiraLicenseUpdaterService jiraLicenseService;
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraSystemRestarter jiraSystemRestarter;

    public ConfirmNewInstallationWithOldLicense(final JiraLicenseUpdaterService jiraLicenseService, final BuildUtilsInfo buildUtilsInfo, final JiraSystemRestarter jiraSystemRestarter)
    {
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.jiraSystemRestarter = notNull("jiraSystemRestarter", jiraSystemRestarter);
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setLicense(String license)
    {
        this.license = license;
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
        return license;
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

    protected void doValidation()
    {
        if (getUserInfoAvailable())
        {
            //check that the user is an admin and that the password is correct
            try
            {
                User user = UserUtils.getUser(userName);

                if (user == null || !user.authenticate(password))
                {
                    addErrorMessage(getText("admin.errors.invalid.username.or.pasword"));
                    return;
                }

                if (!nonAdminUpgradeAllowed())
                {
                    boolean hasAdminPermission = ManagerFactory.getPermissionManager().hasPermission(Permissions.ADMINISTER, user);

                    if (!hasAdminPermission)
                    {
                        addError("userName", getText("admin.errors.no.admin.permission"));
                        return;
                    }
                }
            }
            catch (EntityNotFoundException e)
            {
                addErrorMessage(getText("admin.errors.invalid.username.or.pasword"));
                return;
            }
        }
        if (StringUtils.isNotBlank(license))
        {
            validationResult = jiraLicenseService.validate(this, license);
            addErrorCollection(validationResult.getErrorCollection());
        }
        else if (StringUtils.isBlank(confirm))
        {
            log.warn("Neither the License nor the Install Confirmation have been supplied.");
            addErrorMessage(getText("admin.errors.no.license"));
        }
    }

    private boolean nonAdminUpgradeAllowed()
    {
        return Boolean.valueOf(System.getProperty(SystemPropertyKeys.UPGRADE_SYSTEM_PROPERTY));
    }

    public String doExecute() throws Exception
    {
        // Check if the license has been entered
        if (StringUtils.isNotBlank(license))
        {
            licenseDetails = jiraLicenseService.setLicense(validationResult);
            if (! licenseDetails.isMaintenanceValidForBuildDate(buildUtilsInfo.getCurrentBuildDate()))
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
        return getLicenseDetails().getLicenseStatusMessage(this, getOutlookDate(), "<br/>");
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
        return currentDatabaseVersion == null ? false : comparator.compare(currentDatabaseVersion, CROWD_EMBEDDED_INTEGRATION_VERSION) > 0;
    }
}
