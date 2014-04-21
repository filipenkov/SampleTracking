package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class TestIssueCreationHelperBeanImpl extends MockControllerTestCase
{
    private JiraLicenseService jiraLicenseService;

    @Before
    public void setUp() throws Exception
    {
        jiraLicenseService = createMock(JiraLicenseService.class);
    }

    @Test
    public void testValidateLicenseNoLicense()
    {
        MockI18nBean i18n = new MockI18nBean();
        final JiraContactHelper jiraContactHelper = createMock(JiraContactHelper.class);
        expect(jiraContactHelper.getAdministratorContactMessage(i18n)).andReturn("please contact your JIRA administrators").anyTimes();
        final LicenseDetails licenseDetails = createMock(LicenseDetails.class);
        expect(licenseDetails.isLicenseSet()).andReturn(Boolean.FALSE).anyTimes();
        replay(licenseDetails, jiraContactHelper);

        final IssueCreationHelperBeanImpl issueCreationHelperBean = new IssueCreationHelperBeanImpl(null, null, null, jiraLicenseService, jiraContactHelper)
        {
            @Override
            LicenseDetails getLicenseDetails()
            {
                return licenseDetails;
            }
        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        issueCreationHelperBean.validateLicense(errorCollection, i18n);
        assertEquals("You are not able to create issues because your JIRA instance has an invalid license, please contact your JIRA administrators.",
                errorCollection.getErrorMessages().iterator().next());
        verify(licenseDetails);
    }

    @Test
    public void testValidateLicenseLicenseExpired()
    {
        MockI18nBean i18n = new MockI18nBean();
        final JiraContactHelper jiraContactHelper = createMock(JiraContactHelper.class);
        expect(jiraContactHelper.getAdministratorContactMessage(i18n)).andReturn("please contact your JIRA administrators").anyTimes();
        final LicenseDetails licenseDetails = createMock(LicenseDetails.class);
        expect(licenseDetails.isLicenseSet()).andReturn(Boolean.TRUE).anyTimes();
        expect(licenseDetails.isExpired()).andReturn(Boolean.TRUE).anyTimes();
        replay(licenseDetails, jiraContactHelper);


        final IssueCreationHelperBeanImpl issueCreationHelperBean = new IssueCreationHelperBeanImpl(null, null, null, jiraLicenseService, jiraContactHelper)
        {
            @Override
            LicenseDetails getLicenseDetails()
            {
                return licenseDetails;
            }
        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        issueCreationHelperBean.validateLicense(errorCollection, i18n);
        assertEquals(
                "You will not be able to create new issues because your JIRA evaluation period has expired, please contact your JIRA administrators.",
                errorCollection.getErrorMessages().iterator().next());
        verify(licenseDetails);
    }

    @Test
    public void testValidatePersonalLicenseUserLimitExceeded()
    {
        final UserUtil userUtil = createMock(UserUtil.class);
        expect(userUtil.hasExceededUserLimit()).andReturn(Boolean.TRUE);

        MockI18nBean i18n = new MockI18nBean();
        final JiraContactHelper jiraContactHelper = createMock(JiraContactHelper.class);
        expect(jiraContactHelper.getAdministratorContactMessage(i18n)).andReturn("please contact your JIRA administrators").anyTimes();
        final LicenseDetails licenseDetails = createMock(LicenseDetails.class);
        expect(licenseDetails.isLicenseSet()).andReturn(Boolean.TRUE).anyTimes();
        expect(licenseDetails.isExpired()).andReturn(Boolean.FALSE).anyTimes();
        replay(licenseDetails, jiraContactHelper, userUtil);

        final IssueCreationHelperBeanImpl issueCreationHelperBean = new IssueCreationHelperBeanImpl(userUtil, null, null, jiraLicenseService, jiraContactHelper)
        {
            @Override
            LicenseDetails getLicenseDetails()
            {
                return licenseDetails;
            }
        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        issueCreationHelperBean.validateLicense(errorCollection, i18n);
        assertEquals(
                "You will not be able to create new issues because the user limit for your JIRA instance has been exceeded, please contact your JIRA administrators.",
                errorCollection.getErrorMessages().iterator().next());

        verify(licenseDetails, userUtil);
    }

    @Test
    public void testValidateLicenseHappy()
    {
        MockI18nBean i18n = new MockI18nBean();
        final JiraContactHelper jiraContactHelper = createMock(JiraContactHelper.class);
        expect(jiraContactHelper.getAdministratorContactMessage(i18n)).andReturn("please contact your JIRA administrators").anyTimes();
        final LicenseDetails licenseDetails = createMock(LicenseDetails.class);
        expect(licenseDetails.isLicenseSet()).andReturn(Boolean.TRUE).anyTimes();
        expect(licenseDetails.isExpired()).andReturn(Boolean.FALSE).anyTimes();

        final UserUtil userUtil = createMock(UserUtil.class);
        expect(userUtil.hasExceededUserLimit()).andReturn(Boolean.FALSE).anyTimes();

        replay(licenseDetails, jiraContactHelper, userUtil);

        final IssueCreationHelperBeanImpl issueCreationHelperBean = new IssueCreationHelperBeanImpl(userUtil, null, null, jiraLicenseService, jiraContactHelper)
        {
            @Override
            LicenseDetails getLicenseDetails()
            {
                return licenseDetails;
            }
        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        issueCreationHelperBean.validateLicense(errorCollection, i18n);
        assertFalse(errorCollection.hasAnyErrors());

        verify(licenseDetails, userUtil);
    }

}
