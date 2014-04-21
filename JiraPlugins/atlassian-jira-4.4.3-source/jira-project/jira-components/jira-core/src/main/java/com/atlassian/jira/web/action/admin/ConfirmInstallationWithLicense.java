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
import com.atlassian.jira.license.LicenseJohnsonEventRaiser;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.BuildNumComparator;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventType;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class ConfirmInstallationWithLicense extends JiraWebActionSupport
{
    private static final String CROWD_EMBEDDED_INTEGRATION_VERSION = "602";

    private final JiraLicenseUpdaterService jiraLicenseService;
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraSystemRestarter jiraSystemRestarter;
    private final ExternalLinkUtil externalLinkUtil;

    private String userName;
    private String password;
    private String licenseString;
    private String licenseProblem;
    private boolean licenseUpdated = false;
    private boolean installationConfirmed = false;
    private JiraLicenseService.ValidationResult validationResult;
    private static final String A = "<a target=\"_blank\" href=\"";
    private static final String QT_GT = "\">";
    private static final String SLASH_A = "</a>";
    private static final int V1 = 1;

    public ConfirmInstallationWithLicense(final JiraLicenseUpdaterService jiraLicenseService, final BuildUtilsInfo buildUtilsInfo, final JiraSystemRestarter jiraSystemRestarter, final ExternalLinkUtil externalLinkUtil)
    {
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.jiraSystemRestarter = notNull("jiraSystemRestarter", jiraSystemRestarter);
        this.externalLinkUtil = notNull("externalLinkUtil", externalLinkUtil);
    }

    /**
     * Whats wrong with this license?  Why are we here?
     */
    private void workOutLicenseProblem()
    {
        final LicenseDetails licenseDetails = getLicenseDetails();
        validationResult = jiraLicenseService.validate(this, licenseDetails.getLicenseString());
        if (validationResult.getLicenseVersion() == V1)
        {
            final NumberFormat nf = NumberFormat.getNumberInstance();
            final String upgradeLink = A + externalLinkUtil.getProperty("external.link.jira.upgrade.lic", Arrays.<String>asList(buildUtilsInfo.getVersion(), buildUtilsInfo.getCurrentBuildNumber(), "enterprise", getServerId(), String.valueOf(validationResult.getTotalUserCount()), String.valueOf(validationResult.getActiveUserCount()))) + QT_GT;
            final String evaluationLink = A + externalLinkUtil.getProperty("external.link.jira.license.view", Arrays.<String>asList(buildUtilsInfo.getVersion(), buildUtilsInfo.getCurrentBuildNumber(), "enterprise", getServerId())) + QT_GT;

            licenseProblem = getText("setup.error.invalidlicensekey.wrong.license.version.my.atlassian.link", upgradeLink, SLASH_A, evaluationLink, SLASH_A);
            if (getUserInfoAvailable())
            {
                licenseProblem += "<p>" +
                        getText("setup.error.invalidlicensekey.wrong.license.version.how.many.users", nf.format(validationResult.getTotalUserCount()), nf.format(validationResult.getActiveUserCount())) +
                        "<p>" +
                        getText("setup.error.invalidlicensekey.whatisactive", "<strong>", "</strong>");
            }
        }
        else if (isBuildPartnerNameExists())
        {
            licenseProblem = getText("setup.error.invalidlicensekey.confirminstall.problem.partnerset", buildUtilsInfo.getVersion(), buildUtilsInfo.getCurrentBuildNumber(), getBuildPartnerName());
        }
        else
        {
            licenseProblem = getText("setup.error.invalidlicensekey.confirminstall.problem.partnernotset", buildUtilsInfo.getVersion(), buildUtilsInfo.getCurrentBuildNumber());
        }
    }

    @Override
    public String doDefault() throws Exception
    {
        workOutLicenseProblem();
        return INPUT;
    }

    protected void doValidation()
    {
        workOutLicenseProblem();

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
                    }
                }
            }
            catch (EntityNotFoundException e)
            {
                addErrorMessage(getText("admin.errors.invalid.username.or.pasword"));
            }
        }

        if (StringUtils.isNotBlank(licenseString))
        {
            validationResult = jiraLicenseService.validate(this, licenseString);
            addErrorCollection(validationResult.getErrorCollection());
        }
        else
        {
            addErrorMessage(getText("admin.errors.no.license.supplied"));
        }
    }

    public String doExecute() throws Exception
    {
        if (StringUtils.isNotBlank(licenseString))
        {
            jiraLicenseService.setLicense(validationResult);
            licenseUpdated = true;
        }
        else
        {
            throw new IllegalStateException("This will never happen!");
        }

        // rock JIRA's world!
        jiraSystemRestarter.ariseSirJIRAandUpgradeThySelf(ActionContext.getServletContext());

        // Remove the Old Licence Event
        JohnsonEventContainer cont = JohnsonEventContainer.get(ServletActionContext.getServletContext());
        for (Iterator iterator = cont.getEvents().iterator(); iterator.hasNext();)
        {
            Event event = (Event) iterator.next();
            if (event != null && event.getKey().equals(EventType.get(LicenseJohnsonEventRaiser.LICENSE_INVALID)))
            {
                cont.removeEvent(event);
            }
        }
        return getRedirect("/");
    }

    public String getLicenseProblem()
    {
        return licenseProblem;
    }

    public LicenseDetails getLicenseDetails()
    {
        return jiraLicenseService.getLicense();
    }

    public String getLicensePurchaseDate()
    {
        return getLicenseDetails().getPurchaseDate(getOutlookDate());
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

    public String getBuildPartnerName()
    {
        return buildUtilsInfo.getBuildPartnerName();
    }

    public BuildUtilsInfo getBuildUtilsInfo()
    {
        return buildUtilsInfo;
    }

    public boolean isBuildPartnerNameExists()
    {
        return StringUtils.isNotBlank(getBuildPartnerName());
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
        return comparator.compare(currentDatabaseVersion, CROWD_EMBEDDED_INTEGRATION_VERSION) > 0;
    }


}
