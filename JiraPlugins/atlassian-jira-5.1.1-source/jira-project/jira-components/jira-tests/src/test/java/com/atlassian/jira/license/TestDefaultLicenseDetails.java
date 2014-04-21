package com.atlassian.jira.license;

import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.extras.api.Contact;
import com.atlassian.extras.api.LicenseEdition;
import com.atlassian.extras.api.LicenseType;
import com.atlassian.extras.api.Organisation;
import com.atlassian.extras.api.Partner;
import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactoryStub;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.web.util.MockOutlookDate;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.OutlookDate;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultLicenseDetails extends ListeningTestCase
{
    private static final OutlookDate OUTLOOK_DATE = new MockOutlookDate(Locale.getDefault())
    {
        @Override
        public String formatDMY(final Date date)
        {
            return date.toString();
        }
    };
    private static final String LICENSE_STRING = "Some license String";

    private ApplicationProperties applicationProperties;
    private ExternalLinkUtil externalLinkUtil;
    private I18nHelper mockI18nHelper;
    private MockLicense mockLicense;
    private LicenseDetails licenseDetails;
    private BuildUtilsInfo buildUtilsInfo;

    private User fred;
    private Date now;
    private Date tenSecondsFromNow;
    private Date tenSecondsBeforeNow;
    private long tenDaysAgoInMillis;
    private long fiftyDaysAgoInMillis;
    private static final long GRACE_PERIOD_IN_MILLIS = 30L * 24 * 60 * 60 * 1000; // 30 days
    private Date tenDaysFromNow;
    private Date twentyFourDaysAgo;
    private I18nHelper.BeanFactory i18nFactory;
    private DateTimeFormatter dateTimeFormatter;

    @Before
    public void setUp() throws Exception
    {
        dateTimeFormatter = new DateTimeFormatterFactoryStub().formatter();
        buildUtilsInfo = createMock(BuildUtilsInfo.class);
        applicationProperties = new MockApplicationProperties();
        externalLinkUtil = new MockExternalLinkUtil();
        mockI18nHelper = new MockI18nHelper()
        {
            @Override
            public ResourceBundle getDefaultResourceBundle()
            {
                try
                {
                    return new PropertyResourceBundle(new ByteArrayInputStream(new byte[0]));
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
        now = new Date(System.currentTimeMillis() - 1);
        tenSecondsFromNow = new Date(now.getTime() + 10000);
        tenSecondsBeforeNow = new Date(now.getTime() - 10000);
        tenDaysFromNow = new Date(now.getTime() + (10 * DateUtils.DAY_MILLIS));
        twentyFourDaysAgo = new Date(now.getTime() - (24 * DateUtils.DAY_MILLIS));

        tenDaysAgoInMillis = now.getTime() - (DateUtils.DAY_MILLIS * 10);
        fiftyDaysAgoInMillis = now.getTime() - (DateUtils.DAY_MILLIS * 50);

        mockLicense = new MockLicense();
        mockLicense.setLicenseType(LicenseType.COMMERCIAL);
        mockLicense.setMaintenanceExpired(false);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);

        i18nFactory = createMock(I18nHelper.BeanFactory.class);
        expect(i18nFactory.getInstance(EasyMock.<com.atlassian.crowd.embedded.api.User>anyObject())).andStubReturn(mockI18nHelper);
        replay(i18nFactory);

        licenseDetails = new DefaultLicenseDetails(mockLicense, LICENSE_STRING, applicationProperties, externalLinkUtil, buildUtilsInfo, i18nFactory, dateTimeFormatter)
        {
            @Override
            User getConfirmedUser()
            {
                return fred;
            }

            @Override
            Date getCurrentBuildDate()
            {
                return now;
            }
        };
    }

    @After
    public void tearDown() throws Exception
    {
        applicationProperties = null;
        externalLinkUtil = null;
        now = null;
        mockLicense = null;
        licenseDetails = null;
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForExpiredEvaluation()
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(now);
        mockLicense.setExpired(true);

        applicationProperties.setOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE, true);

        assertTrue(licenseDetails.getLicenseExpiryStatusMessage(fred).indexOf("admin.license.expired") >= 0);
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForNonExpiredEvaluation()
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(tenSecondsFromNow);
        mockLicense.setExpired(false);
        assertTrue(licenseDetails.getLicenseExpiryStatusMessage(fred).indexOf("admin.license.expiresin") >= 0);
    }

    @Test
    public void testGetLicenseExpiryStatusMessageWhenTimestampedForOldLicenseOutsideTheGracePeriod()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow); // a time well before the current build date
        setLicenseExtenstionTimestamp(fiftyDaysAgoInMillis);// set the recorded extension timestanmp to be outside the grace period

        assertTrue(licenseDetails.getLicenseExpiryStatusMessage(fred).indexOf("admin.license.expired") >= 0);
    }

    @Test
    public void testGetLicenseExpiryStatusMessageWhenTimestampedForOldLicenseWithinTheGracePeriod()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow); // a tim well before the current build date
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis); // set the recorded extension timestanmp to be within the grace period

        assertTrue(licenseDetails.getLicenseExpiryStatusMessage(fred).indexOf("admin.license.expiresin") >= 0);
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForLicenseWithNonExpiredMaintenance()
    {
        mockLicense.setMaintenanceExpired(false);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);

        assertTrue(licenseDetails.getLicenseExpiryStatusMessage(fred).indexOf("admin.support.available.until") >= 0);
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForCommunityLicenseWithNonExpiredMaintenance()
    {
        mockLicense.setMaintenanceExpired(false);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);

        // other self renewable but supported licenses
        mockLicense.setLicenseType(LicenseType.COMMUNITY);
        assertTrue(licenseDetails.getLicenseExpiryStatusMessage(fred).indexOf("admin.support.available.until") >= 0);
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForPersonalLicenseWithNonExpiredMaintenance()
    {
        mockLicense.setMaintenanceExpired(false);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);
        // unsupported license
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        assertTrue(licenseDetails.getLicenseExpiryStatusMessage(fred).indexOf("admin.upgrades.available.until") >= 0);
    }

    @Test
    public void testGetLicenseExpiryStatusMessageForLicenseWithExpiredMaintenance()
    {
        // finally, if expired, message should be null
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        assertNull(licenseDetails.getLicenseExpiryStatusMessage(fred));
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageForExpiredEvaluationLicense()
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpired(true);
        assertTrue(licenseDetails.getBriefMaintenanceStatusMessage(mockI18nHelper).indexOf("supported.expired") >= 0);
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageWithValidCommercialLicenseWithinMaintenanceTimeframe()
    {
        assertTrue(licenseDetails.getBriefMaintenanceStatusMessage(mockI18nHelper).indexOf("supported.valid") >= 0);
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageWithOldLicenseOutsideOfGracePeriod()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(fiftyDaysAgoInMillis); // outside grace period

        assertTrue(licenseDetails.getBriefMaintenanceStatusMessage(mockI18nHelper).indexOf("supported.expired") >= 0);
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageWithOldLicenseWithinGracePeriod()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis); // within grace period

        assertTrue(licenseDetails.getBriefMaintenanceStatusMessage(mockI18nHelper).indexOf("supported.valid") >= 0);
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageForValidCommunityLicense()
    {
        mockLicense.setLicenseType(LicenseType.COMMUNITY);
        assertTrue(licenseDetails.getBriefMaintenanceStatusMessage(mockI18nHelper).indexOf("supported.valid") >= 0);
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageForValidPersonalLicense()
    {
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        assertTrue(licenseDetails.getBriefMaintenanceStatusMessage(mockI18nHelper).indexOf("unsupported") >= 0);
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageForMaintenanceExpiredPersonalLicense()
    {
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);

        assertTrue(licenseDetails.getBriefMaintenanceStatusMessage(mockI18nHelper).indexOf("unsupported") >= 0);
    }

    @Test
    public void testGetBriefMaintenanceStatusMessageMaintenanceExpiredLicense()
    {
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        assertTrue(licenseDetails.getBriefMaintenanceStatusMessage(mockI18nHelper).indexOf("supported.expired") >= 0);
    }

    @Test
    public void testGetMaintenanceEndStringEvaluation()
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(now);
        assertEquals(now.toString(), licenseDetails.getMaintenanceEndString(OUTLOOK_DATE));

    }

    @Test
    public void testGetMaintenanceEndStringOldNewBuild()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(tenSecondsBeforeNow.getTime());

        Date expected = new Date(tenSecondsBeforeNow.getTime() + GRACE_PERIOD_IN_MILLIS);
        assertEquals(expected.toString(), licenseDetails.getMaintenanceEndString(OUTLOOK_DATE));
    }

    @Test
    public void testGetMaintenanceEndStringNotEvaluationNorOldNewBuild()
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        assertEquals(tenSecondsBeforeNow.toString(), licenseDetails.getMaintenanceEndString(OUTLOOK_DATE));
    }

    @Test
    public void testGetLicenseStatusMessageEvaluation()
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(now);
        mockLicense.setExpired(true);

        // expired
        assertTrue(licenseDetails.getLicenseStatusMessage(fred, "").indexOf("admin.license.evaluation.expired") >= 0);

    }

    @Test
    public void testGetLicenseStatusMessageEvaluationAlmostExpired()
    {

        // almost expired
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(tenSecondsFromNow);
        assertTrue(licenseDetails.getLicenseStatusMessage(fred, "").indexOf("admin.license.evaluation.almost.expired") >= 0);

    }

    @Test
    public void testGetLicenseStatusMessageEvaluationNotAlmostExpired()
    {

        // not expired
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(tenDaysFromNow);
        assertNull(licenseDetails.getLicenseStatusMessage(fred, ""));
    }


    @Test
    public void testGetLicenseStatusMessageNewBuildOldLicenseForExpiredLicense()
    {
        // user: exists, license: commercial, expired: yes
        fred = new MockUser("fred", "Fred", "fred@example.com");
        mockLicense.setExpiryDate(tenSecondsBeforeNow);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(fiftyDaysAgoInMillis);

        String message = licenseDetails.getLicenseStatusMessage(fred, "");

        assertTrue(message.indexOf("admin.license.nbol.current.version") >= 0);
        assertTrue(message.indexOf("admin.license.nbol.support.and.updates") >= 0);
        assertTrue(message.indexOf("admin.license.renew.for.support.and.updates") >= 0);
        assertTrue(message.indexOf("admin.license.nbol.evaluation.period.has.expired") >= 0);
    }

    @Test
    public void testGetLicenseStatusMessageNewBuildOldLicenseForNonExpiredPersonalLicense()
    {
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis);

        // user: doesnt exist, mockLicense: self renewable, expired: no
        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.indexOf("admin.license.nbol.current.version.user.unknown") >= 0);
        assertTrue(message.indexOf("admin.license.nbol.updates.only") >= 0);
        assertTrue(message.indexOf("admin.license.renew.for.updates.only") >= 0);
        assertTrue(message.indexOf  ("admin.license.nbol.evaluation.period.will.expire.in") >= 0);
    }

    @Test
    public void testGetLicenseStatusMessageNewBuildOldLicenseForNonExpiredNonProfitLicense()
    {
        mockLicense.setLicenseType(LicenseType.NON_PROFIT);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis);
        // user: doesnt exist, mockLicense: other/deprecated, expired: no
        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.indexOf("admin.license.nbol.current.version.user.unknown") >= 0);
        assertTrue(message.indexOf("admin.license.nbol.updates.only") >= 0);
        assertTrue(message.indexOf("admin.license.renew.for.deprecated") >= 0);
        assertTrue(message.indexOf("admin.license.nbol.evaluation.period.will.expire.in") >= 0);
    }

    @Test
    public void testGetLicenseStatusMessageForMaintenanceExpiredLicense() throws Exception
    {
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);

        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.indexOf("admin.license.support.and.updates.has.ended") >= 0);
        assertTrue(message.indexOf("admin.license.renew.for.support.and.updates") >= 0);
    }

    @Test
    public void testGetLicenseStatusMessageNormalForMaintenanceExpiredPersonalLicense() throws Exception
    {
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);

        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.indexOf("admin.license.updates.only.has.ended") >= 0);
        assertTrue(message.indexOf("admin.license.renew.for.updates.only") >= 0);
    }

    @Test
    public void testGetLicenseStatusMessageNormalForMaintenanceExpiredNonProfitLicense() throws Exception
    {
        mockLicense.setLicenseType(LicenseType.NON_PROFIT);
        mockLicense.setMaintenanceExpired(true);
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);

        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.indexOf("admin.license.updates.only.has.ended") >= 0);
        assertTrue(message.indexOf("admin.license.renew.for.deprecated") >= 0);
    }

    @Test
    public void testGetLicenseStatusMessageForAlmostMaintenanceExpiredLicense() throws Exception
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);
        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.indexOf("admin.license.support.and.updates.will.end") >= 0);
        assertTrue(message.indexOf("admin.license.renew.for.support.and.updates.after") >= 0);
    }

    @Test
    public void testGetLicenseStatusMessageForAlmostMaintenanceExpiredPersonalLicense() throws Exception
    {
        mockLicense.setLicenseType(LicenseType.PERSONAL);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);
        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.indexOf("admin.license.updates.only.will.end") >= 0);
        assertTrue(message.indexOf("admin.license.renew.for.updates.only.after") >= 0);
    }

    @Test
    public void testGetLicenseStatusMessageForAlmostMaintenanceExpiredNonProfitLicense() throws Exception
    {
        mockLicense.setLicenseType(LicenseType.NON_PROFIT);
        mockLicense.setMaintenanceExpiryDate(tenSecondsFromNow);

        String message = licenseDetails.getLicenseStatusMessage(fred, "");
        assertTrue(message.indexOf("admin.license.updates.only.will.end") >= 0);
        assertTrue(message.indexOf("admin.license.renew.for.deprecated.after") >= 0);
    }

    @Test
    public void testGetLicenseStatusMessageForMaintenanceDefinitelyNotExpired() throws Exception
    {
        mockLicense.setNumberOfDaysBeforeMaintenanceExpiry(50);
        assertNull(licenseDetails.getLicenseStatusMessage(fred, ""));
    }


    @Test
    public void testIsEntitledToSupport() throws Exception
    {
        assertSupportEntitlement(LicenseType.ACADEMIC, true);
        assertSupportEntitlement(LicenseType.COMMUNITY, true);
        assertSupportEntitlement(LicenseType.COMMERCIAL, true);
        assertSupportEntitlement(LicenseType.DEMONSTRATION, false);
        assertSupportEntitlement(LicenseType.DEVELOPER, true);
        assertSupportEntitlement(LicenseType.HOSTED, true);
        assertSupportEntitlement(LicenseType.NON_PROFIT, false);
        assertSupportEntitlement(LicenseType.OPEN_SOURCE, true);
        assertSupportEntitlement(LicenseType.PERSONAL, false);
        assertSupportEntitlement(LicenseType.STARTER, true);
        assertSupportEntitlement(LicenseType.TESTING, false);
    }

    @Test
    public void testIsAlmostExpiredEvaluation() throws Exception
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(tenSecondsFromNow);
        assertTrue(licenseDetails.isLicenseAlmostExpired());
    }

    @Test
    public void testIsNotAlmostExpiredEvaluation() throws Exception
    {
        mockLicense.setEvaluation(true);
        mockLicense.setExpiryDate(tenDaysFromNow);
        assertFalse(licenseDetails.isLicenseAlmostExpired());
    }

    @Test
    public void testIsAlmostExpiredNewBuildOldLicense() throws Exception
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(twentyFourDaysAgo.getTime());
        assertTrue(licenseDetails.isLicenseAlmostExpired());
    }

    @Test
    public void testIsNotAlmostExpiredNewBuildOldLicense() throws Exception
    {
        mockLicense.setMaintenanceExpiryDate(tenSecondsBeforeNow);
        setLicenseExtenstionTimestamp(tenDaysAgoInMillis);
        assertFalse(licenseDetails.isLicenseAlmostExpired());
    }

    @Test
    public void testGetLicenseVersion() throws Exception
    {
        mockLicense.setLicenseVersion(666);
        assertEquals(666, licenseDetails.getLicenseVersion());
    }

    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new DefaultLicenseDetails(null, LICENSE_STRING, applicationProperties, externalLinkUtil, buildUtilsInfo, i18nFactory, dateTimeFormatter);
            fail("Should have barfed");
        }
        catch (Exception expected)
        {
        }
        try
        {
            new DefaultLicenseDetails(mockLicense, LICENSE_STRING, null, externalLinkUtil, buildUtilsInfo, i18nFactory, dateTimeFormatter);
            fail("Should have barfed");
        }
        catch (Exception expected)
        {
        }
        try
        {
            new DefaultLicenseDetails(mockLicense, LICENSE_STRING, applicationProperties, null, buildUtilsInfo, i18nFactory, dateTimeFormatter);
            fail("Should have barfed");
        }
        catch (Exception expected)
        {
        }
        try
        {
            new DefaultLicenseDetails(mockLicense, null, applicationProperties, externalLinkUtil, buildUtilsInfo, i18nFactory, dateTimeFormatter);
            fail("Should have barfed");
        }
        catch (Exception expected)
        {
        }
    }

    private void assertSupportEntitlement(final LicenseType licenceType, final boolean expectedEntitlement)
    {
        mockLicense.setLicenseType(licenceType);
        assertEquals(licenceType.name(), expectedEntitlement, licenseDetails.isEntitledToSupport());
    }

    private void setLicenseExtenstionTimestamp(final long timestamp)
    {
        applicationProperties.setOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE, true);
        applicationProperties.setString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_TIMESTAMP, String.valueOf(timestamp));
    }

    private static class MockLicense implements JiraLicense
    {
        private LicenseType licenseType;
        private Date expiryDate;
        private boolean isExpired;
        private boolean evaluation;
        private Date maintenanceExpiryDate;
        private boolean maintenanceExpired;
        private int numberOfDaysBeforeMaintenanceExpiry;
        private int licenseVersion;

        public int getLicenseVersion()
        {
            return licenseVersion;
        }

        public Date getDatePurchased()
        {
            return null;
        }

        public String getDescription()
        {
            return null;
        }

        public Product getProduct()
        {
            return null;
        }

        public String getServerId()
        {
            return null;
        }

        public Partner getPartner()
        {
            return null;
        }

        public Organisation getOrganisation()
        {
            return null;
        }

        public Collection<Contact> getContacts()
        {
            return null;
        }

        public Date getCreationDate()
        {
            return null;
        }

        public Date getPurchaseDate()
        {
            return null;
        }

        public LicenseType getLicenseType()
        {
            return licenseType;
        }

        public String getProperty(final String name)
        {
            return null;
        }

        public boolean isExpired()
        {
            return isExpired;
        }

        public Date getExpiryDate()
        {
            return expiryDate;
        }

        public int getNumberOfDaysBeforeExpiry()
        {
            return 0;
        }

        public String getSupportEntitlementNumber()
        {
            return null;
        }

        public Date getMaintenanceExpiryDate()
        {
            return maintenanceExpiryDate;
        }

        public int getNumberOfDaysBeforeMaintenanceExpiry()
        {
            return numberOfDaysBeforeMaintenanceExpiry;
        }

        public boolean isMaintenanceExpired()
        {
            return maintenanceExpired;
        }

        public int getMaximumNumberOfUsers()
        {
            return 0;
        }

        public boolean isUnlimitedNumberOfUsers()
        {
            return false;
        }

        public boolean isEvaluation()
        {
            return evaluation;
        }

        public void setLicenseType(final LicenseType licenseType)
        {
            this.licenseType = licenseType;
        }

        public void setExpiryDate(final Date date)
        {
            expiryDate = date;
        }

        public void setExpired(final boolean isExpired)
        {
            this.isExpired = isExpired;
        }

        public LicenseEdition getLicenseEdition()
        {
            return null;
        }

        public void setEvaluation(boolean evaluation)
        {
            this.evaluation = evaluation;
        }

        public void setMaintenanceExpiryDate(final Date maintenanceExpiryDate)
        {
            this.maintenanceExpiryDate = maintenanceExpiryDate;
        }

        public void setMaintenanceExpired(final boolean maintenanceExpired)
        {
            this.maintenanceExpired = maintenanceExpired;
        }

        public void setNumberOfDaysBeforeMaintenanceExpiry(final int numberOfDaysBeforeMaintenanceExpiry)
        {
            this.numberOfDaysBeforeMaintenanceExpiry = numberOfDaysBeforeMaintenanceExpiry;
        }

        public void setLicenseVersion(final int licenseVersion)
        {
            this.licenseVersion = licenseVersion;
        }
    }

    private class MockExternalLinkUtil implements ExternalLinkUtil
    {
        public String getPropertiesFilename()
        {
            return null;
        }

        public String getProperty(final String key)
        {
            return null;
        }

        public String getProperty(final String key, final String value1)
        {
            return null;
        }

        public String getProperty(final String key, final String value1, final String value2)
        {
            return null;
        }

        public String getProperty(final String key, final String value1, final String value2, final String value3)
        {
            return null;
        }

        public String getProperty(final String key, final String value1, final String value2, final String value3, final String value4)
        {
            return null;
        }

        public String getProperty(final String key, final Object parameters)
        {
            return null;
        }
    }
}
