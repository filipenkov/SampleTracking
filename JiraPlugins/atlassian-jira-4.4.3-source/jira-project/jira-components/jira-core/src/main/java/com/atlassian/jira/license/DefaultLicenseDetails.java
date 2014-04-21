package com.atlassian.jira.license;

import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.extras.api.LicenseType;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.OutlookDate;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the {@link LicenseDetails} interface.
 *
 * @since v3.13
 */
class DefaultLicenseDetails implements LicenseDetails
{
    private static final Logger log = Logger.getLogger(DefaultLicenseDetails.class);

    private static final int MAINTENANCE_WARNING_PERIOD_IN_DAYS = 42;
    private static final long GRACE_PERIOD_IN_MILLIS = 30L * 24 * 60 * 60 * 1000; // 30 days

    private final JiraLicense license;
    private final ApplicationProperties applicationProperties;
    private final ExternalLinkUtil externalLinkUtil;
    private final String licenseString;
    private final BuildUtilsInfo buildUtilsInfo;

    /**
     * @param license should never be null - see {@link com.atlassian.jira.license.NullLicenseDetails} instead
     * @param externalLinkUtil external link utils
     */
    DefaultLicenseDetails(final JiraLicense license, final String licenseString, final ApplicationProperties applicationProperties, final ExternalLinkUtil externalLinkUtil, final BuildUtilsInfo buildUtilsInfo)
    {
        this.licenseString = notNull("licenseString", licenseString);
        this.license = notNull("license", license);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.externalLinkUtil = notNull("externalLinkUtil", externalLinkUtil);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    private boolean isFullLicense()
    {
        return LicenseType.COMMERCIAL.equals(license.getLicenseType()) && !license.isEvaluation();
    }

    public boolean isPersonalLicense()
    {
        return LicenseType.PERSONAL.equals(license.getLicenseType());
    }

    private boolean isEvaluationLicense()
    {
        return license.isEvaluation();
    }

    private boolean isAcademicLicense()
    {
        return LicenseType.ACADEMIC.equals(license.getLicenseType());
    }

    private boolean isNonProfitLicense()
    {
        return LicenseType.NON_PROFIT.equals(license.getLicenseType());
    }

    private boolean isCommunityLicense()
    {
        return LicenseType.COMMUNITY.equals(license.getLicenseType());
    }

    private boolean isOpenSourceLicense()
    {
        return LicenseType.OPEN_SOURCE.equals(license.getLicenseType());
    }

    private boolean isDeveloperLicense()
    {
        return LicenseType.DEVELOPER.equals(license.getLicenseType());
    }

    private boolean isDemonstrationLicense()
    {
        return LicenseType.DEMONSTRATION.equals(license.getLicenseType());
    }

    private boolean isCommercialLicense()
    {
        return isFullLicense() || isAcademicLicense() || isEvaluationLicense() || LicenseType.HOSTED.equals(license.getLicenseType());
    }

    private boolean isSelfRenewable()
    {
        return isCommunityLicense() || isOpenSourceLicense() || isDeveloperLicense() || isPersonalLicense();
    }

    private boolean isNonCommercialNonRenewable()
    {
        return isNonProfitLicense() || isDemonstrationLicense() || LicenseType.TESTING.equals(license.getLicenseType());
    }

    public boolean isEntitledToSupport()
    {
        return !(isNonCommercialNonRenewable() || isPersonalLicense());
    }

    /**
     * If the license is Evaluation or Extended (New Build, Old License), returns true if we are within 7 days of the
     * expiry date.
     *
     * @return true if the license is close to expiry; false otherwise.
     * @see {@link #isExpired()}
     */
    public boolean isLicenseAlmostExpired()
    {
        if (isEvaluationLicense() || isNewBuildWithOldLicense())
        {
            final Date expiry = getLicenseExpiry();
            return ((expiry != null) && (expiry.getTime() - getCurrentTime() < 7L * DateUtils.DAY_MILLIS));
        }
        return false;
    }

    /**
     * Checks whether the license is either expired for Evaluation or Extended Licenses (New Build, Old License).
     *
     * @return true if has; false otherwise.
     */
    public boolean isExpired()
    {
        if (isEvaluationLicense())
        {
            return license.isExpired();
        }
        else if (isNewBuildWithOldLicense())
        {
            return isExtendLicenseExpired();
        }
        return false;
    }

    public String getPurchaseDate(final OutlookDate outlookDate)
    {
        return notNull("outlookDate", outlookDate).formatDMY(license.getPurchaseDate());
    }

    public boolean isEvaluation()
    {
        return license.isEvaluation();
    }

    public boolean isStarter()
    {
        return LicenseType.STARTER.equals(license.getLicenseType());
    }

    public boolean isCommercial()
    {
        return LicenseType.COMMERCIAL.equals(license.getLicenseType());
    }

    public boolean isCommunity()
    {
        return LicenseType.COMMUNITY.equals(license.getLicenseType());
    }

    public boolean isOpenSource()
    {
        return LicenseType.OPEN_SOURCE.equals(license.getLicenseType());
    }

    public boolean isNonProfit()
    {
        return LicenseType.NON_PROFIT.equals(license.getLicenseType());
    }

    public boolean isDemonstration()
    {
        return LicenseType.DEMONSTRATION.equals(license.getLicenseType());
    }

    public boolean isDeveloper()
    {
        return LicenseType.DEVELOPER.equals(license.getLicenseType());
    }

    public String getOrganisation()
    {
        return license.getOrganisation() == null ? "<Unknown>" : license.getOrganisation().getName();
    }

    public boolean hasLicenseTooOldForBuildConfirmationBeenDone()
    {
        return applicationProperties.getOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE);
    }

    public String getLicenseString()
    {
        return licenseString;
    }

    public boolean isMaintenanceValidForBuildDate(final Date currentBuildDate)
    {
        return license.getMaintenanceExpiryDate() == null || license.getMaintenanceExpiryDate().compareTo(currentBuildDate) >= 0;
    }

    public String getSupportEntitlementNumber()
    {
        return license.getSupportEntitlementNumber();
    }

    /**
     * If the license is Evaluation or Extended (New Build, Old License), returns the date when the license will
     * expire.
     *
     * @return a date when this license will "expire", or null if there is no expiry date.
     */
    private Date getLicenseExpiry()
    {
        if (isEvaluationLicense())
        {
            return license.getExpiryDate();
        }
        else if (isNewBuildWithOldLicense())
        {
            return getExtendedLicenseExpiry();
        }
        return null;
    }

    private Date getExtendedLicenseExpiry()
    {
        final long installationWithExpiredLicenseDate = Long.parseLong(getConfirmedInstallWithOldLicenseTimestamp());
        return new Date(installationWithExpiredLicenseDate + GRACE_PERIOD_IN_MILLIS);
    }

    private boolean isNewBuildWithOldLicense()
    {
        return license.getMaintenanceExpiryDate().compareTo(getCurrentBuildDate()) < 0 && hasLicenseTooOldForBuildConfirmationBeenDone();
    }

    private String getTimeUntilExpiry(final I18nHelper i18n)
    {
        return DateUtils.dateDifference(getCurrentTime(), getLicenseExpiry().getTime(), 2, i18n.getDefaultResourceBundle());
    }

    /**
     * @return true if the support period end date has almost passed; false otherwise.
     */
    private boolean isMaintenanceAlmostEnded()
    {
        return license.getNumberOfDaysBeforeMaintenanceExpiry() < MAINTENANCE_WARNING_PERIOD_IN_DAYS;
    }

    /**
     * @return true if the support period end date has passed; false otherwise.
     * @see {@link #getMaintenanceExpiryDate}
     */
    private boolean isMaintenanceExpired()
    {
        return license.isMaintenanceExpired();
    }

    /**
     * Calculates the end of the support period (during which customers are entitled to updates and commercial support)
     *
     * @return the license creation date plus the length of the support period
     */
    private Date getMaintenanceExpiryDate()
    {
        return new Date(license.getMaintenanceExpiryDate().getTime());
    }

    /**
     * Determines if the confirmation of the new build with the old license has occured more than 30 days ago
     *
     * @return true if date confirmed is older than 30 days
     */
    private boolean isExtendLicenseExpired()
    {
        try
        {
            return System.currentTimeMillis() > getExtendedLicenseExpiry().getTime();
        }
        catch (final NumberFormatException e)
        {
            log.debug("The Confirm Install Timestamp does not exist or is in the wrong format.", e);
        }
        return false;
    }

    /**
     * @return the String representation of the time when JIRA was confirmed to be installed with an old license.
     */
    private String getConfirmedInstallWithOldLicenseTimestamp()
    {
        return applicationProperties.getString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_TIMESTAMP);
    }

    ///CLOVER:OFF
    User getConfirmedUser()
    {
        final String userName = applicationProperties.getString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_USER);
        try
        {
            return UserUtils.getUser(userName);
        }
        catch (final EntityNotFoundException e)
        {
            log.warn("Could not find user [" + userName + "]", e);
        }
        return null;
    }
    ///CLOVER:ON

    private long getCurrentTime()
    {
        return System.currentTimeMillis();
    }

    private String getCurrentVersion()
    {
        return buildUtilsInfo.getVersion();
    }

    Date getCurrentBuildDate()
    {
        return buildUtilsInfo.getCurrentBuildDate();
    }

    public String getSupportRequestMessage(final I18nHelper i18n, final OutlookDate outlookDate)
    {
        final StringBuffer msg = new StringBuffer();

        if (isEntitledToSupport())
        {
            if (isExpired())
            {
                msg.append("<p>").append(i18n.getText("admin.supportrequest.success.description")).append("</p>");
                msg.append("<p>").append(i18n.getText("admin.supportrequest.success.supported.expired")).append("</p>");
                msg.append("<p>- ").append(i18n.getText("admin.supportrequest.atlassian.team")).append("</p>");
            }
            else
            {
                final String mailTo = externalLinkUtil.getProperty("external.link.jira.support.mail.to");
                final String mailToUrl = "mailto:" + mailTo;
                msg.append("<p>").append(i18n.getText("admin.supportrequest.success.description")).append("</p>");
                msg.append("<p>").append(i18n.getText("admin.supportrequest.success.supported.next")).append("</p>");
                msg.append("<p>").append(
                        i18n.getText("admin.supportrequest.success.supported.email", "<a href=\"" + mailToUrl + "\">", mailTo, "</a>")).append("</p>");
                msg.append("<p>- ").append(i18n.getText("admin.supportrequest.atlassian.team")).append("</p>");
            }
        }
        else
        {
            final Link urlDocumentation = new Link("external.link.jira.documentation", getCurrentVersion());
            final Link urlForums = new Link("external.link.atlassian.forums");
            final Link urlPurchaseJira = new Link(isPersonalLicense() ? "external.link.jira.personal.site" : "external.link.jira.license.new");
            final List urls = EasyList.build(urlDocumentation.getStart(), urlDocumentation.getEnd(), urlForums.getStart(), urlForums.getEnd(),
                    urlPurchaseJira.getStart(), urlPurchaseJira.getEnd());

            msg.append("<p>").append(i18n.getText("admin.supportrequest.not.supported", license.getDescription())).append("</p>");
            msg.append("<p>").append(i18n.getText("admin.supportrequest.not.supported.links", urls)).append("</p>");
            msg.append("<p>- ").append(i18n.getText("admin.supportrequest.atlassian.team")).append("</p>");
        }
        return msg.toString();
    }

    public String getMaintenanceEndString(final OutlookDate outlookDate)
    {
        Date end;
        if (isEvaluationLicense() || isNewBuildWithOldLicense())
        {
            end = getLicenseExpiry();
        }
        else
        {
            end = getMaintenanceExpiryDate();
        }
        return outlookDate.formatDMY(end);
    }

    public boolean isUnlimitedNumberOfUsers()
    {
        return license.isUnlimitedNumberOfUsers();
    }

    public int getMaximumNumberOfUsers()
    {
        return license.getMaximumNumberOfUsers();
    }

    public boolean isLicenseSet()
    {
        return true;
    }

    public int getLicenseVersion()
    {
        return license.getLicenseVersion();
    }

    public String getDescription()
    {
        return license.getDescription();
    }

    public String getPartnerName()
    {
        return license.getPartner() == null ? null : license.getPartner().getName();
    }

    public String getLicenseExpiryStatusMessage(final I18nHelper i18n, final OutlookDate outlookDate)
    {
        final String msg;
        if (isEvaluationLicense() || isNewBuildWithOldLicense())
        {
            if (isExpired())
            {
                msg = i18n.getText("admin.license.expired");
            }
            else
            {
                msg = i18n.getText("admin.license.expiresin", getTimeUntilExpiry(i18n), outlookDate.formatDMY(getLicenseExpiry()));
            }
        }
        else if (!isMaintenanceExpired())
        {
            if (isEntitledToSupport())
            {
                msg = i18n.getText("admin.support.available.until", "<b>" + outlookDate.formatDMY(getMaintenanceExpiryDate()) + "</b>");
            }
            else
            {
                msg = i18n.getText("admin.upgrades.available.until", "<b>" + outlookDate.formatDMY(getMaintenanceExpiryDate()) + "</b>");
            }
        }
        else
        {
            return null;
        }
        return "<br><small>(" + msg + ")</small>";
    }

    public String getBriefMaintenanceStatusMessage(final I18nHelper i18n)
    {
        String msg;
        if (!isEntitledToSupport())
        {
            msg = i18n.getText("admin.license.maintenance.status.unsupported");
        }
        else
        {
            msg = i18n.getText("admin.license.maintenance.status.supported.valid");
            // if eval or new build old license, check license expiry
            if ((isEvaluationLicense() || isNewBuildWithOldLicense()))
            {
                if (isExpired())
                {
                    msg = i18n.getText("admin.license.maintenance.status.supported.expired");
                }
            }
            // otherwise (regular license), check maintenance end date
            else if (isMaintenanceExpired())
            {
                msg = i18n.getText("admin.license.maintenance.status.supported.expired");
            }
        }
        return msg;
    }

    public String getLicenseStatusMessage(final I18nHelper i18n, final OutlookDate outlookDate, final String delimiter)
    {
        final Link urlRenew = new Link("external.link.jira.license.new");
        final Link urlEvalExpired = new Link("external.link.jira.license.expiredeval");
        final Link urlSelfRenew = new Link("external.link.jira.license.renew.noncommercial");
        final Link urlContact = new Link("external.link.jira.license.renew.contact");
        final Link urlWhyRenew = new Link("external.link.jira.license.whyrenew");

        // first case: license is purely an evaluation license
        if (isEvaluationLicense())
        {
            final String licenseExpiry = "<strong>" + TextUtils.htmlEncode(outlookDate.formatDMY(getLicenseExpiry())) + "</strong>";

            // expired
            if (isExpired())
            {
                // Your JIRA evaluation period expired on <date>. You are not able to create new issues in JIRA.
                //
                // To reactivate JIRA, please _purchase JIRA_ (link to order form).

                final StringBuffer sb = new StringBuffer();
                sb.append(i18n.getText("admin.license.evaluation.expired", licenseExpiry));
                sb.append(delimiter);
                sb.append(i18n.getText("admin.license.evaluation.expired.renew", urlEvalExpired.getStart(), urlEvalExpired.getEnd()));
                return sb.toString();
            }

            // almost expired
            if (isLicenseAlmostExpired())
            {
                // Your JIRA evaluation period will expire on <date>. You will not be
                // able to create new issues in JIRA.
                //
                // Please consider _purchasing JIRA_ (link to order form).

                final StringBuffer sb = new StringBuffer();
                sb.append(i18n.getText("admin.license.evaluation.almost.expired", licenseExpiry));
                sb.append(delimiter);
                sb.append(i18n.getText("admin.license.evaluation.almost.expired.renew", urlEvalExpired.getStart(), urlEvalExpired.getEnd()));
                return sb.toString();
            }

            // license is valid and within its supported period - no message
            return null;
        }

        // second case: license is not evaluation, but the maintenance period has expired, and the current build of JIRA
        // is more recent than the expiry date of the maintenance period of the license
        else if (isNewBuildWithOldLicense())
        {
            final String maintenanceEnd = "<strong>" + TextUtils.htmlEncode(outlookDate.formatDMY(getMaintenanceExpiryDate())) + "</strong>";
            final String extendedDaysLeft = getTimeUntilExpiry(i18n);
            // COMMERCIAL: Your JIRA support and updates for this license have ended on <date>. You are currently running a version of JIRA that was created after that date.
            // OTHER: Your JIRA updates for this license have ended on <date>. You are currently running a version of JIRA that was created after that date.

            // The current version of JIRA (<version>) was installed by <user full name> (<username>)
            // on <date>. As your current license is not valid for this version, your use of JIRA should be
            // considered an evaluation.

            // NOT EXPIRED: Your evaluation period will expire in <timeremaining>. After this date you will not be able to create new issues in JIRA.
            // EXPIRED: Your evaluation period has expired. You are not able to create new issues in JIRA.

            // ENTITLED TO SUPPORT: If you wish to have access to support and updates, please _renew your maintenance_ (link to order form/my.atlassian.com).
            // NOT ENTITLED TO SUPPORT (SELF RENEW): If you wish to have access to updates, please _renew your maintenance_ (link to my.atlassian.com).
            // NOT ENTITLED TO SUPPORT (OTHER): If you wish to have access to support and updates, please _contact Atlassian_ (link to contact page) for purchase and upgrade details.
            // Renewing your maintenance allows you _continued access to great benefits_ (link to the why renew page).

            final String supportAndUpdates;
            final String currentVersion;
            final User user = getConfirmedUser();
            if (user != null)
            {
                currentVersion = i18n.getText("admin.license.nbol.current.version", getCurrentVersion(), user.getDisplayName(), user.getName());
            }
            else
            {
                currentVersion = i18n.getText("admin.license.nbol.current.version.user.unknown", getCurrentVersion());
            }

            final String expired;
            final String renew;
            final String renewKey;
            final Link renewLink;
            if (isEntitledToSupport())
            {
                supportAndUpdates = i18n.getText("admin.license.nbol.support.and.updates", maintenanceEnd);
                renewKey = "admin.license.renew.for.support.and.updates";
            }
            else
            {
                supportAndUpdates = i18n.getText("admin.license.nbol.updates.only", maintenanceEnd);
                renewKey = isSelfRenewable() ? "admin.license.renew.for.updates.only" : "admin.license.renew.for.deprecated";
            }
            if (isCommercialLicense())
            {
                renewLink = urlRenew;
            }
            else
            {
                renewLink = isSelfRenewable() ? urlSelfRenew : urlContact;
            }
            renew = i18n.getText(renewKey, renewLink.getStart(), renewLink.getEnd());

            // expired
            if (isExpired())
            {
                expired = i18n.getText("admin.license.nbol.evaluation.period.has.expired");
            }
            // otherwise
            else
            {
                expired = i18n.getText("admin.license.nbol.evaluation.period.will.expire.in", "<strong>" + extendedDaysLeft + "</strong>");
            }

            final String whyRenew = i18n.getText("admin.license.why.renew", urlWhyRenew.getStart(), urlWhyRenew.getEnd());

            final StringBuffer sb = new StringBuffer();
            sb.append(supportAndUpdates).append(delimiter);
            sb.append(currentVersion).append(delimiter);
            sb.append(expired).append(delimiter);
            sb.append(renew).append(" ");
            sb.append(whyRenew);
            return sb.toString();
        }

        // other licenses
        else
        {
            final String maintenancePeriodEnd = "<strong>" + TextUtils.htmlEncode(outlookDate.formatDMY(getMaintenanceExpiryDate())) + "</strong>";
            // maintenance ended
            if (isMaintenanceExpired())
            {
                // COMMERCIAL: Your JIRA support and updates for this license have ended on {0}. JIRA updates created after {0} are not valid for this license.
                // OTHER:      JIRA updates for this license ended on {0}. JIRA updates created after {0} are not valid for this license.
                //
                // ENTITLED TO SUPPORT: If you wish to have access to support and updates, please _renew your maintenance_ (link to order form/my.atlassian.com).
                // NOT ENTITLED TO SUPPORT (SELF RENEW): If you wish to have access to updates, please _renew your maintenance_ (link to my.atlassian.com).
                // NOT ENTITLED TO SUPPORT (OTHER): If you wish to have access to support and updates, please _contact Atlassian_ (link to contact page) for purchase and upgrade details.
                // Renewing your maintenance allows you _continued access to great benefits_ (link to the why renew page).

                final String supportAndUpdates;
                final String renew;
                final String renewKey;
                final Link renewLink;
                if (isEntitledToSupport())
                {
                    supportAndUpdates = i18n.getText("admin.license.support.and.updates.has.ended", maintenancePeriodEnd);
                    renewKey = "admin.license.renew.for.support.and.updates";
                }
                else
                {
                    supportAndUpdates = i18n.getText("admin.license.updates.only.has.ended", maintenancePeriodEnd);
                    renewKey = isSelfRenewable() ? "admin.license.renew.for.updates.only" : "admin.license.renew.for.deprecated";
                }
                if (isCommercialLicense())
                {
                    renewLink = urlRenew;
                }
                else
                {
                    renewLink = isSelfRenewable() ? urlSelfRenew : urlContact;
                }
                renew = i18n.getText(renewKey, renewLink.getStart(), renewLink.getEnd());
                final String whyRenew = i18n.getText("admin.license.why.renew", urlWhyRenew.getStart(), urlWhyRenew.getEnd());

                final StringBuffer sb = new StringBuffer();
                sb.append(supportAndUpdates).append(delimiter);
                sb.append(renew).append(" ");
                sb.append(whyRenew);
                return sb.toString();
            }
            // almost expired
            else if (isMaintenanceAlmostEnded())
            {
                // COMMERCIAL: Your JIRA support and updates for this license will end on {0}. JIRA updates created after {0} will not be valid for this license.
                // OTHER:      JIRA updates for this license will end on {0}. JIRA updates created after {0} will not be valid for this license.
                //
                // ENTITLED TO SUPPORT: If you wish to have access to support and updates after this date, please _renew your maintenance_ (link to order form/my.atlassian.com).
                // NOT ENTITLED TO SUPPORT (SELF RENEW): If you wish to have access to updates after this date, please _renew your maintenance_ (link to my.atlassian.com).
                // NOT ENTITLED TO SUPPORT (OTHER): If you wish to have access to support and updates after this date, please _contact Atlassian_ (link to contact page) for purchase and upgrade details.
                // Renewing your maintenance allows you _continued access to great benefits_ (link to the why renew page).

                final String supportAndUpdates;
                final String renew;
                final String renewKey;
                final Link renewLink;
                if (isEntitledToSupport())
                {
                    supportAndUpdates = i18n.getText("admin.license.support.and.updates.will.end", maintenancePeriodEnd);
                    renewKey = "admin.license.renew.for.support.and.updates.after";
                }
                else
                {
                    supportAndUpdates = i18n.getText("admin.license.updates.only.will.end", maintenancePeriodEnd);
                    renewKey = isSelfRenewable() ? "admin.license.renew.for.updates.only.after" : "admin.license.renew.for.deprecated.after";
                }
                if (isCommercialLicense())
                {
                    renewLink = urlRenew;
                }
                else
                {
                    renewLink = isSelfRenewable() ? urlSelfRenew : urlContact;
                }
                renew = i18n.getText(renewKey, renewLink.getStart(), renewLink.getEnd());
                final String whyRenew = i18n.getText("admin.license.why.renew", urlWhyRenew.getStart(), urlWhyRenew.getEnd());

                final StringBuffer sb = new StringBuffer();
                sb.append(supportAndUpdates).append(delimiter);
                sb.append(renew).append(" ");
                sb.append(whyRenew);
                return sb.toString();
            }
            // otherwise
            else
            {
                // license is valid and within its supported period - no message
                return null;
            }
        }
    }

    /**
     * Utility class for HTML anchor tags
     */
    private class Link
    {
        private final String key;
        private final String param0;

        private Link(final String key)
        {
            this.key = key;
            param0 = null;
        }

        private Link(final String key, final String param)
        {
            this.key = key;
            param0 = param;
        }

        public String getStart()
        {
            final ExternalLinkUtil linkUtil = externalLinkUtil;
            final String url = param0 == null ? linkUtil.getProperty(key) : linkUtil.getProperty(key, param0);
            return "<a href=\"" + url + "\">";
        }

        public String getEnd()
        {
            return "</a>";
        }
    }
}
