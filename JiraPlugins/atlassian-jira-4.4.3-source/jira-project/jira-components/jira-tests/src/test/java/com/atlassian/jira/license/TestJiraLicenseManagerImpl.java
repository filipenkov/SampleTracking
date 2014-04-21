package com.atlassian.jira.license;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.extras.api.AtlassianLicense;
import com.atlassian.extras.api.LicenseManager;
import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.extras.common.LicenseException;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.license.SIDManager;

import java.util.Date;

/**
 */
public class TestJiraLicenseManagerImpl extends MockControllerTestCase
{
    private static final String BAD_TO_THE_BONE = "Bad To The Bone";
    private static final String GOOD_LICENSE = "Good License";
    private static final String USER_NAME = "userName";

    private JiraLicenseStore licenseStore;
    private LicenseManager licenseManager;
    private AtlassianLicense atlassianLicense;
    private JiraLicense jiraLicense;
    private BuildUtilsInfo buildUtilsInfo;
    private ApplicationProperties applicationProperties;
    private SIDManager sidManager;

    @Before
    public void setUp()
    {
        licenseStore = getMock(JiraLicenseStore.class);
        licenseManager = getMock(LicenseManager.class);
        atlassianLicense = getMock(AtlassianLicense.class);
        jiraLicense = getMock(JiraLicense.class);
        buildUtilsInfo = getMock(BuildUtilsInfo.class);
        applicationProperties = getMock(ApplicationProperties.class);
        sidManager = getMock(SIDManager.class);
    }

    @Test
    public void testIsValid_ButItsNot()
    {
        expect(licenseManager.getLicense(BAD_TO_THE_BONE)).andThrow(new LicenseException());

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        assertFalse(licenseManager.isDecodeable(BAD_TO_THE_BONE));
    }

    @Test
    public void testIsValid_ButItsBlank() throws Exception
    {
        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        assertFalse(licenseManager.isDecodeable(""));
    }

    @Test
    public void testIsValid_HappyPath()
    {

        expect(licenseManager.getLicense(GOOD_LICENSE)).andReturn(atlassianLicense);
        expect(atlassianLicense.getProductLicense(Product.JIRA)).andReturn(jiraLicense);

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        assertTrue(licenseManager.isDecodeable(GOOD_LICENSE));
    }

    @Test
    public void testGetLicense_WithNotStoredLicense() throws Exception
    {
        expect(licenseStore.retrieve()).andReturn(null);

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        assertEquals(NullLicenseDetails.NULL_LICENSE_DETAILS, licenseManager.getLicense());
    }

    @Test
    public void testGetLicense_BadlyStoredLicense() throws Exception
    {
        expect(licenseStore.retrieve()).andReturn(BAD_TO_THE_BONE);

        expect(licenseManager.getLicense(BAD_TO_THE_BONE)).andThrow(new LicenseException());

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        try
        {
            licenseManager.getLicense();
            fail("Should have barfed");
        }
        catch (LicenseException expected)
        {
        }
    }

    @Test
    public void testGetLicense_HappyPath() throws Exception
    {
        expect(licenseStore.retrieve()).andReturn(GOOD_LICENSE);

        expect(licenseManager.getLicense(GOOD_LICENSE)).andReturn(atlassianLicense).times(1);
        expect(atlassianLicense.getProductLicense(Product.JIRA)).andReturn(jiraLicense).times(1);

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        final LicenseDetails actualLicenseDetails = licenseManager.getLicense();
        assertNotNull(actualLicenseDetails);
        assertTrue(actualLicenseDetails.isLicenseSet());
        assertEquals(GOOD_LICENSE, actualLicenseDetails.getLicenseString());
    }

    @Test
    public void testGetLicenseString_BadlyInputLicense() throws Exception
    {
        expect(licenseManager.getLicense(BAD_TO_THE_BONE)).andThrow(new LicenseException());

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        try
        {
            licenseManager.getLicense(BAD_TO_THE_BONE);
            fail("Should have barfed");
        }
        catch (LicenseException expected)
        {
        }
    }


    @Test
    public void testGetLicenseString_HappyPath() throws Exception
    {
        expect(licenseManager.getLicense(GOOD_LICENSE)).andReturn(atlassianLicense).times(1);
        expect(atlassianLicense.getProductLicense(Product.JIRA)).andReturn(jiraLicense).times(1);

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        final LicenseDetails actualLicenseDetails = licenseManager.getLicense(GOOD_LICENSE);
        assertNotNull(actualLicenseDetails);
        assertTrue(actualLicenseDetails.isLicenseSet());
        assertEquals(GOOD_LICENSE, actualLicenseDetails.getLicenseString());
    }

    @Test
    public void testSetLicense_InvalidLicenseInput()
    {
        expect(licenseManager.getLicense(BAD_TO_THE_BONE)).andThrow(new LicenseException());

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        try
        {
            licenseManager.setLicense(BAD_TO_THE_BONE);
            fail("Should have barfed");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testSetLicense_HappyAndNeedsConfirmationReset() throws Exception
    {
        assertLicenseCanBeSetAndTestConfirmationPaths(true, true);
    }

    @Test
    public void testSetLicense_HappyMayNeedNeedsConfirmationReset() throws Exception
    {
        assertLicenseCanBeSetAndTestConfirmationPaths(true, false);
    }

    @Test
    public void testSetLicense_HappyMayNeedNeedsConfirmationReset2() throws Exception
    {
        assertLicenseCanBeSetAndTestConfirmationPaths(false, true);
    }

    @Test
    public void testConfirmProceed()
    {
        licenseStore.confirmProceedUnderEvaluationTerms(USER_NAME);
        expectLastCall();

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        licenseManager.confirmProceedUnderEvaluationTerms(USER_NAME);
    }

    private void assertLicenseCanBeSetAndTestConfirmationPaths(final boolean isMaintenanceValidForBuildDate, final boolean isUsingOldLicenseToRunNewJiraBuild)
    {
        expect(licenseManager.getLicense(GOOD_LICENSE)).andReturn(atlassianLicense).times(2);
        expect(atlassianLicense.getProductLicense(Product.JIRA)).andReturn(jiraLicense).times(2);

        licenseStore.store(GOOD_LICENSE);
        expectLastCall();

        final Date date = new Date();

        expect(buildUtilsInfo.getCurrentBuildDate()).andReturn(date);
        if (isMaintenanceValidForBuildDate)
        {
            expect(jiraLicense.getMaintenanceExpiryDate()).andStubReturn(null);
        }
        else
        {
            expect(jiraLicense.getMaintenanceExpiryDate()).andStubReturn(new Date(date.getTime() - 10));
        }

        expect(applicationProperties.getOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE)).andStubReturn(isUsingOldLicenseToRunNewJiraBuild);

        if (isMaintenanceValidForBuildDate && isUsingOldLicenseToRunNewJiraBuild)
        {
            licenseStore.resetOldBuildConfirmation();
            expectLastCall();
        }

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        final LicenseDetails actualLicenseDetails = licenseManager.setLicense(GOOD_LICENSE);

        assertNotNull(actualLicenseDetails);
        assertTrue(actualLicenseDetails.isLicenseSet());
        assertEquals(GOOD_LICENSE, actualLicenseDetails.getLicenseString());
    }

    @Test
    public void testGetServerIdWithExistingServerId()
    {
        final String serverId = "A server ID";
        expect(licenseStore.retrieveServerId()).andReturn(serverId);

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        assertEquals(serverId, licenseManager.getServerId());
    }

    @Test
    public void testGetServerIdWithNonExistingServerId()
    {
        final String serverId = "A server ID";
        expect(licenseStore.retrieveServerId()).andReturn(null);
        expect(sidManager.generateSID()).andReturn(serverId);
        licenseStore.storeServerId(serverId); expectLastCall();

        JiraLicenseManager licenseManager = instantiate(JiraLicenseManagerImpl.class);
        assertEquals(serverId, licenseManager.getServerId());
    }
}
