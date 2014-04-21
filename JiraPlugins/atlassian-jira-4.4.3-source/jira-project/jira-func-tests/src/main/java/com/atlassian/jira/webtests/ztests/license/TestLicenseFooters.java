package com.atlassian.jira.webtests.ztests.license;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.LicenseKeys;

@WebTest ({ Category.FUNC_TEST, Category.LICENSING })
public class TestLicenseFooters extends JIRAWebTest
{
    public TestLicenseFooters(String name)
    {
        super(name);
    }

    public void testEnterpriseCommunityLicense()
    {
        restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_COMMUNITY.getLicenseString());
        assertTextPresentBeforeText("Powered by a free Atlassian", "community license for Atlassian.");

        assertTextNotPresent("site is for non-production use only.");
        assertTextNotPresent("open source license for Atlassian.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
    }

    public void testEnterpriseCommunityLicenseLoggedOut()
    {
        try
        {
            restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_COMMUNITY.getLicenseString());
            logout();
            assertTextPresentBeforeText("Powered by a free Atlassian", "community license for Atlassian.");

            assertTextNotPresent("site is for non-production use only.");
            assertTextNotPresent("open source license for Atlassian.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testEnterpriseDeveloperLicense()
    {
        restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_DEVELOPER.getLicenseString());
        assertTextPresentBeforeText("This", "site is for non-production use only.");

        assertTextNotPresent("Powered by a free Atlassian");
        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("open source license for Atlassian.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
    }

    public void testEnterpriseDeveloperLicenseLoggedOut()
    {
        try
        {
            restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_DEVELOPER.getLicenseString());
            logout();
            assertTextPresentBeforeText("This", "site is for non-production use only.");

            assertTextNotPresent("Powered by a free Atlassian");
            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("open source license for Atlassian.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testEnterprisePersonalLicense()
    {
        restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_PERSONAL.getLicenseString());
        assertTextSequence(new String[] { "A", "free bug tracker", "for up to three users? Try", "JIRA Personal", "Edition." });

        assertTextNotPresent("Powered by a free Atlassian");
        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("open source license for Atlassian.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        assertTextNotPresent("site is for non-production use only.");
    }

    public void testEnterprisePersonalLicenseLoggedOut()
    {
        try
        {
            restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_PERSONAL.getLicenseString());
            logout();
            assertTextSequence(new String[] { "A", "free bug tracker", "for up to three users? Try", "JIRA Personal", "Edition." });

            assertTextNotPresent("Powered by a free Atlassian");
            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("open source license for Atlassian.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
            assertTextNotPresent("site is for non-production use only.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testEnterpriseOpenSourceLicense()
    {
        restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_OPEN_SOURCE.getLicenseString());
        assertTextPresentBeforeText("Powered by a free Atlassian", "open source license for Atlassian.");

        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("site is for non-production use only.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
    }

    public void testEnterpriseOpenSourceLicenseLoggedOut()
    {
        try
        {
            restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_OPEN_SOURCE.getLicenseString());
            logout();
            assertTextPresentBeforeText("Powered by a free Atlassian", "open source license for Atlassian.");

            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("site is for non-production use only.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }

    }

    public void testEnterpriseDemonstrationLicense()
    {
        restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_DEMO.getLicenseString());
        assertTextPresentBeforeText("This JIRA site is for demonstration purposes only.", "bug tracking software for your team.");

        assertTextNotPresent("Powered by a free Atlassian");
        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("site is for non-production use only.");
        assertTextNotPresent("open source license for Atlassian.");
    }

    public void testEnterpriseDemonstrationLicenseLoggedOut()
    {
        try
        {
            restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_DEMO.getLicenseString());
            logout();
            assertTextPresentBeforeText("This JIRA site is for demonstration purposes only.", "bug tracking software for your team.");

            assertTextNotPresent("Powered by a free Atlassian");
            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("site is for non-production use only.");
            assertTextNotPresent("open source license for Atlassian.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testEnterpriseLicense()
    {
        restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_COMMERCIAL.getLicenseString());

        assertTextNotPresent("Powered by a free Atlassian");
        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("site is for non-production use only.");
        assertTextNotPresent("Powered by a free Atlassian");
        assertTextNotPresent("open source license for Atlassian.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
    }

    public void testEnterpriseLicenseLoggedOut()
    {
        try
        {
            restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_COMMERCIAL.getLicenseString());
            logout();
            assertTextNotPresent("Powered by a free Atlassian");
            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("site is for non-production use only.");
            assertTextNotPresent("open source license for Atlassian.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testEnterpriseEvaluationLicense()
    {
        restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_EVAL_EXPIRED.getLicenseString());
        navigation.gotoAdminSection("license_details");
        assertTextPresent("(Your evaluation has expired.)");

        navigation.gotoPage("secure/BrowseProjects.jspa");

        assertTextSequence(new String[] { "Powered by a free Atlassian", "JIRA evaluation license", "Please consider", "purchasing it", "today" });
        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("site is for non-production use only.");
        assertTextNotPresent("open source license for Atlassian.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        assertTextNotPresent("for up to three users? Try");
    }

    public void testEnterpriseEvaluationLicenseLoggedOut()
    {
        try
        {
            restoreDataWithLicense("blankprojects.xml", LicenseKeys.V2_EVAL_EXPIRED.getLicenseString());
            logout();
            assertTextSequence(new String[] { "Powered by a free Atlassian", "JIRA evaluation license", "Please consider", "purchasing it", "today" });
            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("site is for non-production use only.");
            assertTextNotPresent("open source license for Atlassian.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
            assertTextNotPresent("for up to three users? Try");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }
}
