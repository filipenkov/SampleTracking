package com.atlassian.jira.license;

import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Date;

/**
 * Implementation of the {@link LicenseDetails} interface used when the {@link JiraLicense} object is {@code null}, to
 * avoid having to do null checks everywhere in the {@link DefaultLicenseDetails} implementation.
 *
 * @since v3.13
 */
class NullLicenseDetails implements LicenseDetails
{
    static final LicenseDetails NULL_LICENSE_DETAILS = new NullLicenseDetails();

    /**
     * Should not be constructed; use {@link NullLicenseDetails#NULL_LICENSE_DETAILS}
     */
    private NullLicenseDetails()
    {}

    public String getSupportRequestMessage(final I18nHelper i18n, final OutlookDate outlookDate)
    {
        return null;
    }

    public String getMaintenanceEndString(final OutlookDate outlookDate)
    {
        return null;
    }

    public boolean isUnlimitedNumberOfUsers()
    {
        return false;
    }

    public int getMaximumNumberOfUsers()
    {
        return 0;
    }

    public boolean isLicenseSet()
    {
        return false;
    }

    public int getLicenseVersion()
    {
        return 0;
    }

    public String getDescription()
    {
        return "";
    }

    public String getPartnerName()
    {
        return null;
    }

    public boolean isExpired()
    {
        return false;
    }

    public String getPurchaseDate(final OutlookDate outlookDate)
    {
        return "";
    }

    public boolean isEvaluation()
    {
        return false;
    }

    public boolean isStarter()
    {
        return false;
    }

    public boolean isCommercial()
    {
        return false;
    }

    public boolean isPersonalLicense()
    {
        return false;
    }

    public boolean isCommunity()
    {
        return false;
    }

    public boolean isOpenSource()
    {
        return false;
    }

    public boolean isNonProfit()
    {
        return false;
    }

    public boolean isDemonstration()
    {
        return false;
    }

    public boolean isDeveloper()
    {
        return false;
    }

    public String getOrganisation()
    {
        return "<Unknown>";
    }

    public boolean isEntitledToSupport()
    {
        return false;
    }

    public boolean isLicenseAlmostExpired()
    {
        return false;
    }

    public boolean hasLicenseTooOldForBuildConfirmationBeenDone()
    {
        return false;
    }

    public String getLicenseString()
    {
        return "";
    }

    public boolean isMaintenanceValidForBuildDate(final Date currentBuildDate)
    {
        return false;
    }

    public String getSupportEntitlementNumber()
    {
        return null;
    }

    public String getLicenseStatusMessage(final I18nHelper i18n, final OutlookDate outlookDate, final String delimiter)
    {
        return null;
    }

    public String getLicenseExpiryStatusMessage(final I18nHelper i18n, final OutlookDate outlookDate)
    {
        return null;
    }

    public String getBriefMaintenanceStatusMessage(final I18nHelper i18n)
    {
        return null;
    }
}
