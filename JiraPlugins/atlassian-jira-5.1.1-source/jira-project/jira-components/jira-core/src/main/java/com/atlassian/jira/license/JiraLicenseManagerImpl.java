package com.atlassian.jira.license;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.extras.api.LicenseManager;
import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.extras.common.LicenseException;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.license.SIDManager;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
public class JiraLicenseManagerImpl implements JiraLicenseManager
{
    private final JiraLicenseStore licenseStore;
    private final BuildUtilsInfo buildUtilsInfo;
    private final LicenseManager licenseManager;
    private final SIDManager sidManager;
    private final ApplicationProperties applicationProperties;
    private final ExternalLinkUtil externalLinkUtil;
    private final DateTimeFormatter dateTimeFormatter;
    private final I18nHelper.BeanFactory i18nFactory;
    private final EventPublisher eventPublisher;

    public JiraLicenseManagerImpl(final JiraLicenseStore licenseStore, final BuildUtilsInfo buildUtilsInfo, final LicenseManager licenseManager, final ApplicationProperties applicationProperties, final ExternalLinkUtil externalLinkUtil, final SIDManager sidManager, DateTimeFormatter dateTimeFormatter, I18nHelper.BeanFactory i18nFactory, EventPublisher eventPublisher)
    {
        this.dateTimeFormatter = dateTimeFormatter;
        this.i18nFactory = i18nFactory;
        this.eventPublisher = eventPublisher;
        this.licenseStore = notNull("licenseStore", licenseStore);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.licenseManager = notNull("licenseManager", licenseManager);
        this.externalLinkUtil = notNull("externalLinkUtil", externalLinkUtil);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.sidManager = notNull("sidManager", sidManager);
    }

    @Override
    public String getServerId()
    {
        String serverId = licenseStore.retrieveServerId();
        if (StringUtils.isBlank(serverId))
        {
            serverId = sidManager.generateSID();
            licenseStore.storeServerId(serverId);
        }
        return serverId;
    }

    @Override
    public LicenseDetails getLicense()
    {
        return getLicense(licenseStore.retrieve());
    }

    @Override
    public LicenseDetails getLicense(String licenseString)
    {
        final JiraLicense jiraLicense = getLicenseInternal(licenseString);
        return jiraLicense == null ? NullLicenseDetails.NULL_LICENSE_DETAILS : new DefaultLicenseDetails(jiraLicense, licenseString, applicationProperties, externalLinkUtil, buildUtilsInfo, i18nFactory, dateTimeFormatter);
    }

    @Override
    public boolean isDecodeable(final String licenseString)
    {
        try
        {
            return getLicenseInternal(licenseString) != null;
        }
        catch (LicenseException e)
        {
            return false;
        }
    }

    @Override
    public LicenseDetails setLicense(final String licenseString)
    {
        return setLicense(licenseString, true);
    }

    @Override
    public LicenseDetails setLicenseNoEvent(String licenseString)
    {
        return setLicense(licenseString, false);
    }

    private LicenseDetails setLicense(final String licenseString, final boolean fireEvent)
    {
        if (!isDecodeable(licenseString))
        {
            throw new IllegalArgumentException("The licenseString is invalid and will not be stored.");
        }
        licenseStore.store(licenseString);

        final LicenseDetails licenseDetails = getLicense(licenseString);

        // if the license maintenance is valid, then we should reset any app properties
        // if the license is not too old and the confirmation of installation with old license was made, remove the conformation
        if (licenseDetails.isMaintenanceValidForBuildDate(buildUtilsInfo.getCurrentBuildDate()) && licenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone())
        {
            licenseStore.resetOldBuildConfirmation();
        }

        if (fireEvent)
        {
            eventPublisher.publish(new NewLicenseEvent(licenseDetails));
        }
        return licenseDetails;
    }

    @Override
    public void confirmProceedUnderEvaluationTerms(final String userName)
    {
        licenseStore.confirmProceedUnderEvaluationTerms(userName);
    }

    private JiraLicense getLicenseInternal(final String licenseString)
    {
        if (StringUtils.isBlank(licenseString))
        {
            return null;
        }
        return (JiraLicense) licenseManager.getLicense(licenseString).getProductLicense(Product.JIRA);
    }
}
