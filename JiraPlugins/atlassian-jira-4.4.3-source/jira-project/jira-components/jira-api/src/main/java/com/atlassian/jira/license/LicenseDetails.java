package com.atlassian.jira.license;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Date;

/**
 * A set of methods which describe the state of the currently installed license.
 *
 * @since v3.13
 */
public interface LicenseDetails
{
    /**
     * Tells whether a license has been set or not for this JIRA instance.
     *
     * @return {@code true} if a license has been set, {@code false} otherwise.
     */
    boolean isLicenseSet();

    /**
     * Gets the version of the current license, 0 if not set. This is the version of encoder/decoder that was used with
     * this license.
     *
     * @return the version of the current license, 0 if not set.
     */
    int getLicenseVersion();

    /**
     * @return true for licenses that are entitled to support and updates of JIRA. This includes everything which is not
     *         a Personal License or Non-Commercial-Non-Renewable.
     */
    boolean isEntitledToSupport();

    /**
     * If the license is Evaluation or Extended (New Build, Old License), returns true if we are within 7 days of the
     * expiry date. Otherwise, returns true if within 6 weeks before the supported period end date.
     *
     * @return true if the license is close to expiry; false otherwise.
     */
    boolean isLicenseAlmostExpired();

    /**
     * Return the HTML message that describes the current status of the license.
     *
     * @param i18n i18n bean
     * @param outlookDate outlookDate bean
     * @param delimiter the line delimiter for the message
     * @return the status message
     */
    String getLicenseStatusMessage(I18nHelper i18n, OutlookDate outlookDate, String delimiter);

    /**
     * Return the HTML message that briefly describes the expiry status of the license. Intended for use with the Admin
     * Portlet.
     *
     * @param i18n i18n bean
     * @param outlookDate outlookDate bean
     * @return the status message, null for normal license outside of support period
     */
    String getLicenseExpiryStatusMessage(I18nHelper i18n, OutlookDate outlookDate);

    /**
     * Return the single word description of the maintenance status of the license. Intended for use with the Support
     * Request and System Info pages.
     *
     * @param i18n i18n bean
     * @return the status message - either "Supported", "Expired" or "Unsupported"
     */
    String getBriefMaintenanceStatusMessage(I18nHelper i18n);

    /**
     * Return the HTML message for support request denial or send e-mail success page.
     *
     * @param i18n i18n bean
     * @param outlookDate outlookDate bean
     * @return the support request message
     */
    String getSupportRequestMessage(I18nHelper i18n, OutlookDate outlookDate);

    /**
     * Return the date string representing the end of maintenance of the license, whether the license is Evaluation, New
     * Build Old License or otherwise.
     * <p/>
     * Note that the return type here is a String to intentionally signify that this value should not be used in any
     * logic calculations and only for displaying to the user.
     *
     * @param outlookDate outlookDate bean
     * @return the date as a string (should never be null)
     */
    String getMaintenanceEndString(OutlookDate outlookDate);

    /**
     * Tells whether the current license authorise an unlimited number of users.
     *
     * @return {@code true} if the license authorise an unlimited number of users, {@code false} otherwise.
     * @see #getMaximumNumberOfUsers()
     */
    boolean isUnlimitedNumberOfUsers();

    /**
     * Gets the maximum number of users allowed by the current license
     *
     * @return the maximum number of user allowed by the license, -1 if unlimited
     * @see #isUnlimitedNumberOfUsers()
     */
    int getMaximumNumberOfUsers();

    /**
     * @return the description of the current license
     */
    String getDescription();

    /**
     * @return the Partner name inside the current license or null if its not set
     */
    String getPartnerName();

    /**
     * Checks whether the license is either expired or the grace period for an extended license (after upgrade) is
     * over.
     *
     * @return true if has, false otherwise.
     */
    boolean isExpired();

    /**
     * Gets a nicely formatted purchase date for the current license
     *
     * @param outlookDate the date formatter
     * @return a formatted purchased date.
     */
    String getPurchaseDate(OutlookDate outlookDate);

    /**
     * Tells whether this is an evaluation license or not
     *
     * @return {@code true} if this is an evaluation license, {@code false} otherwise.
     */
    boolean isEvaluation();

    /**
     * Tells wheter this is a starter license or not
     *
     * @return {@code true} if this is a starter license, {@code false} otherwise.
     */
    boolean isStarter();

    /**
     * Tells whether this is a commercial license or not
     *
     * @return {@code true} if this is a commercial license, {@code false} otherwise.
     */
    boolean isCommercial();

    /**
     * Tells whether this is a personal license or not
     *
     * @return {@code true} if this is a personal license, {@code false} otherwise.
     */
    boolean isPersonalLicense();

    /**
     * Tells whether this is a community license or not
     *
     * @return {@code true} if this is a community license, {@code false} otherwise.
     */
    boolean isCommunity();

    /**
     * Tells whether this is an open source license or not
     *
     * @return {@code true} if this is an open source license, {@code false} otherwise.
     */
    boolean isOpenSource();

    /**
     * Tells whether this is a non profit license or not
     *
     * @return {@code true} if this is a non profit license, {@code false} otherwise.
     */
    boolean isNonProfit();

    /**
     * Tells whether this is a demonstration license or not
     *
     * @return {@code true} if this is a demonstration license, {@code false} otherwise.
     */
    boolean isDemonstration();

    /**
     * Tells whether this is a developer license or not
     *
     * @return {@code true} if this is a developer license, {@code false} otherwise.
     */
    boolean isDeveloper();

    /**
     * Gets the organisation this license belongs to
     *
     * @return the organisation this license belongs to
     */
    String getOrganisation();

    /**
     * Tells whether the admin has acknowledged that the JIRA instance is running on a too old license for its build
     * number. Typically JIRA allows for a 30 days grace period when doing so.
     *
     * @return {@code true} if the license in use is too old with regards to the build number, {@code false} otherwise.
     */
    boolean hasLicenseTooOldForBuildConfirmationBeenDone();

    /**
     * @return the encoded license string that was was decode to produce the current lincence.  This will return null if
     *         it is not set
     */
    String getLicenseString();

    /**
     * Tells whether the current build date is within the maintenance of the license
     *
     * @param currentBuildDate the current build date
     * @return {@code true} if the build date is within the maintenance period, {@code false} otherwise.
     */
    boolean isMaintenanceValidForBuildDate(Date currentBuildDate);

    /**
     * Gets the SEN from the license
     *
     * @return the SEN from the license
     */
    String getSupportEntitlementNumber();
}
