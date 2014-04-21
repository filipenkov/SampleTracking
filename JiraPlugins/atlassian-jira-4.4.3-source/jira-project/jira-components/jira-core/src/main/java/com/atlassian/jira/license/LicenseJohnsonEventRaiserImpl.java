package com.atlassian.jira.license;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.text.NumberFormat;
import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of {@link LicenseJohnsonEventRaiser}
 *
 * @since v4.0
 */
public class LicenseJohnsonEventRaiserImpl implements LicenseJohnsonEventRaiser
{
    private static final Logger log = Logger.getLogger(LicenseJohnsonEventRaiserImpl.class);
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraLicenseService jiraLicenseService;
    private final I18nHelper.BeanFactory i18nBeanFactory;
    private final UserUtil userUtil;
    private static final int V1 = 1;

    public LicenseJohnsonEventRaiserImpl(BuildUtilsInfo buildUtilsInfo, final JiraLicenseService jiraLicenseService, final I18nHelper.BeanFactory i18nBeanFactory, final UserUtil userUtil)
    {
        this.i18nBeanFactory = notNull("i18nBeanFactory", i18nBeanFactory);
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.userUtil = notNull("userUtil", userUtil);
    }

    public boolean checkLicenseIsTooOldForBuild(final ServletContext servletContext, final LicenseDetails licenseDetails)
    {
        final boolean shouldRaiseEvent = licenseDetails.isLicenseSet() &&
                licenseDetails.getLicenseVersion() > V1 && // if the license is V1 let the other check catch it.
                !licenseDetails.isMaintenanceValidForBuildDate(buildUtilsInfo.getCurrentBuildDate()) &&
                !licenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone();

        if (shouldRaiseEvent)
        {
            // If it hasn't we need to get the user to update the license or confirm the installation under
            // the Evaluation Terms (Note: the user can always fall back to their previous release of JIRA)
            log.error("The current license is too old (" + licenseDetails.getMaintenanceEndString(new OutlookDate(Locale.getDefault())) + ") to run this version (" + buildUtilsInfo.getVersion() + " - " + buildUtilsInfo.getCurrentBuildDate() + ") of JIRA.");

            JohnsonEventContainer cont = JohnsonEventContainer.get(servletContext);
            Event newEvent = new Event(EventType.get(LICENSE_TOO_OLD), "The current license is too old to install this version of JIRA (" + buildUtilsInfo.getVersion() + ")", EventLevel.get(EventLevel.ERROR));
            cont.addEvent(newEvent);
        }

        return shouldRaiseEvent;
    }

    public boolean checkLicenseIsInvalid(final ServletContext servletContext, final LicenseDetails licenseDetails)
    {
        final I18nHelper i18nHelper = i18nBeanFactory.getInstance((User) null);
        final JiraLicenseService.ValidationResult validationResult = jiraLicenseService.validate(i18nHelper, licenseDetails.getLicenseString());

        if (validationResult.getErrorCollection().hasAnyErrors())
        {
            StringBuilder messages = new StringBuilder();
            // if we have a v1 problem then we only want to show that error.  Dont worry if its also too old at this stage.
            if (validationResult.getLicenseVersion() == V1)
            {
                final NumberFormat nf = NumberFormat.getNumberInstance();
                messages.append(i18nHelper.getText("setup.error.invalidlicensekey.wrong.license.version.johnson", nf.format(validationResult.getTotalUserCount()), nf.format(validationResult.getActiveUserCount())));
            } else
            {
                for (Object msg : validationResult.getErrorCollection().getErrors().values())
                {
                    log.error(msg);
                    messages.append(msg).append("\n");
                }
            }
            JohnsonEventContainer cont = JohnsonEventContainer.get(servletContext);
            Event newEvent = new Event(EventType.get(LICENSE_INVALID), messages.toString(), EventLevel.get(EventLevel.ERROR));
            cont.addEvent(newEvent);
            return true;
        }
        return false;
    }
}
