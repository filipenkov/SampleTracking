package com.atlassian.jira.webtests.ztests.license;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.LicenseKeys;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses the following xml files:
 *
 * TestLicenseMessagesNewBuildOldLicenseCommunity.xml
 * TestLicenseMessagesNewBuildOldLicenseDemo.xml
 * TestLicenseMessagesNewBuildOldLicenseFull.xml
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.LICENSING })
public class TestLicenseMessages extends FuncTestCase
{
    private static final String URL_WWW_ATLASSIAN_COM_ORDER = "http://www.atlassian.com/order";
    private static final String URL_WWW_ATLASSIAN_COM_EXPIRED_EVAL = "http://www.atlassian.com/software/jira/expiredevaluation.jsp";
    private static final String URL_MY_ATLASSIAN_COM = "http://my.atlassian.com/";
    private static final String URL_CONTACT_ATLASSIAN = "http://www.atlassian.com/contact";

    private static final List<LicenseInfo> switchableLicenses = new ArrayList<LicenseInfo>();
    private static final List<DataLicenseInfo> dataLicenses = new ArrayList<DataLicenseInfo>();
    private static final String[] NOT_EXPIRED_MESSAGES = new String[] {
            "JIRA support and updates for this license ended on ",
            "You are currently running a version of JIRA that was created after that date.",
            "Your evaluation period will expire in ",
            "If you wish to have access to support and updates, please "
    };
    private static final String[] EXPIRED_MESSAGES = new String[] {
            "JIRA support and updates for this license ended on ",
            "You are currently running a version of JIRA that was created after that date.",
            "Your evaluation period has expired.",
            "If you wish to have access to support and updates, please "
    };
    private static final String[] EXPIRED_MESSAGES_DEPRECATED = new String[] {
            "JIRA updates for this license ended on ",
            "You are currently running a version of JIRA that was created after that date.",
            "Your evaluation period has expired.",
            "If you wish to have access to support and updates, please "
    };
    private static final String[] NOT_EXPIRED_MESSAGES_DEPRECATED = new String[] {
            "JIRA updates for this license ended on ",
            "You are currently running a version of JIRA that was created after that date.",
            "Your evaluation period will expire in ",
            "If you wish to have access to support and updates, please "
    };

    static
    {
        switchableLicenses.add(new LicenseInfo(LicenseKeys.V2_EVAL_EXPIRED.getLicenseString(), "Evaluation",
                new String[] {
                        "(Your evaluation has expired.)",
                        "Your JIRA evaluation period expired on ",
                        "You are not able to create new issues in JIRA.",
                        "To reactivate JIRA, please "
                },
                new String[] { },
                new LicenseInfoUrl[] {
                        new LicenseInfoUrl("purchase JIRA", URL_WWW_ATLASSIAN_COM_EXPIRED_EVAL)
                }
        ));
        switchableLicenses.add(new LicenseInfo(LicenseKeys.V2_COMMERCIAL.getLicenseString(), "Commercial Server",
                new String[] {
                        "(Support and updates available until "
                },
                new String[] {
                        "JIRA support and updates for this license ended on ",
                        "JIRA updates created after ",
                        "are not valid for this license."
                },
                new LicenseInfoUrl[] { }
        ));
        switchableLicenses.add(createSupportedLicense(LicenseKeys.V2_COMMUNITY.getLicenseString(), "Community"));
        switchableLicenses.add(createSupportedLicense(LicenseKeys.V2_DEVELOPER.getLicenseString(), "Developer"));
        switchableLicenses.add(createUnsupportedLicense(LicenseKeys.V2_PERSONAL.getLicenseString(), "Personal"));
        switchableLicenses.add(createSupportedLicense(LicenseKeys.V2_OPEN_SOURCE.getLicenseString(), "Open Source"));
        switchableLicenses.add(createUnsupportedLicense(LicenseKeys.V2_DEMO.getLicenseString(), "Demonstration"));

        // note: these rely on the fact that the license embedded inside the data XML is old - i.e. its dateCreated
        // means that it is no longer "supported".
        dataLicenses.add(createCommercialDataLicense("Full"));
        dataLicenses.add(createNonCommercialDataLicense("Community"));
        dataLicenses.add(createDeprecatedDataLicense("Demo"));
    }

    private static DataLicenseInfo createDeprecatedDataLicense(String name)
    {
        return new DataLicenseInfo("TestLicenseMessagesNewBuildOldLicense" + name + ".xml",
                EXPIRED_MESSAGES_DEPRECATED,
                NOT_EXPIRED_MESSAGES_DEPRECATED,
                new LicenseInfoUrl[] { new LicenseInfoUrl("contact Atlassian", URL_CONTACT_ATLASSIAN) }
        );
    }

    private static DataLicenseInfo createNonCommercialDataLicense(String name)
    {
        return new DataLicenseInfo("TestLicenseMessagesNewBuildOldLicense" + name + ".xml",
                EXPIRED_MESSAGES,
                NOT_EXPIRED_MESSAGES,
                new LicenseInfoUrl[] { new LicenseInfoUrl("renew your maintenance", URL_MY_ATLASSIAN_COM) }
        );
    }

    private static DataLicenseInfo createCommercialDataLicense(String name)
    {
        return new DataLicenseInfo("TestLicenseMessagesNewBuildOldLicense" + name + ".xml",
                EXPIRED_MESSAGES,
                NOT_EXPIRED_MESSAGES,
                new LicenseInfoUrl[] { new LicenseInfoUrl("renew your maintenance", URL_WWW_ATLASSIAN_COM_ORDER) }
        );
    }

    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testSwitchableLicenses() throws Exception
    {
        for (LicenseInfo licenseInfo : switchableLicenses)
        {
            log("Testing license for " + licenseInfo.description);
            administration.switchToLicense(licenseInfo.license, licenseInfo.description);
            assertLicense(licenseInfo);

            if (licenseInfo.license.equals(LicenseKeys.V2_EVAL_EXPIRED.getLicenseString()))
            {
                // make sure we CANT create issues
                tester.gotoPage("secure/CreateIssue!default.jspa");
                assertCantCreateIssues();

                // make sure we CANT create issues even if they jump to the second step
                tester.gotoPage("secure/CreateIssue.jspa?pid=10000&issuetype=1");
                assertCantCreateIssues();
            }
        }
    }

    private void assertCantCreateIssues()
    {
        CssLocator locator = new CssLocator(tester, ".aui-message.error");
        text.assertTextPresent(locator, "You will not be able to create new issues because your JIRA evaluation period has expired, please contact your JIRA administrators.");
    }

    public void testNewBuildOldLicenses() throws Exception
    {
        File importDirectory = new File(administration.getJiraHomeDirectory(), "import");

        final long now = System.currentTimeMillis();
        String timestampNow = "" + now;
        String timestampExpired = "" + (now - 31 * DateUtils.DAY_MILLIS);

        for (DataLicenseInfo info : dataLicenses)
        {
            log("Testing license with file " + info.dataFile);

            // test expired
            modifyTimestampAndRestore(importDirectory, info.dataFile, timestampExpired);
            navigation.gotoAdminSection("license_details");
            assertDataLicense(info.expiredMessages, info.urls);

            // test not expired
            modifyTimestampAndRestore(importDirectory, info.dataFile, timestampNow);
            navigation.gotoAdminSection("license_details");
            assertDataLicense(info.notExpiredMessages, info.urls);
        }
    }

    private String replaceTokenWith(String s, String replacement)
    {
        if (s == null)
        {
            return null;
        }
        final String token = "TIMESTAMPTOCHANGE";
        final int index = s.indexOf(token);
        if (index < 0)
        {
            fail("Replacement token '" + token + "' not found");
        }
        return s.substring(0, index) + replacement + s.substring(index + token.length());
    }

    private void modifyTimestampAndRestore(final File importDirectory, final String fileName, final String timestamp) throws Exception
    {
        // read in the XML
        log("Modifying timestamp for backup data at " + fileName);
        File data = new File(getEnvironmentData().getXMLDataLocation(), fileName);
        FileReader fileReader = null;
        char[] buff = null;
        try
        {
            fileReader = new FileReader(data);
            int length = (int) data.length();
            buff = new char[length];
            fileReader.read(buff);
        }
        finally
        {
            if (fileReader != null)
            {
                fileReader.close();
            }
        }

        // modify the timestamp
        String xml = replaceTokenWith(new String(buff), timestamp);
        File newData = null;
        try
        {
            // write new data to temp file
            newData = File.createTempFile(fileName, ".xml", importDirectory);
            FileWriter of = new FileWriter(newData);
            of.write(xml);
            of.close();

            // import the data
            log("Restoring data '" + newData.getAbsolutePath() + "'");
            tester.gotoPage("secure/admin/XmlRestore!default.jspa");
            tester.setWorkingForm("jiraform");
            tester.setFormElement("filename", newData.getName());
            tester.submit();
            administration.waitForRestore();
            tester.assertTextPresent("Your project has been successfully imported");
            navigation.disableWebSudo();
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
            administration.generalConfiguration().setBaseUrl(getEnvironmentData().getBaseUrl().toString());
        }
        finally
        {
            if (newData != null)
            {
                if (newData.exists())
                {
                    newData.delete();
                }
            }
        }
    }

    private void assertLicense(LicenseInfo licenseInfo)
    {
        final Locator pageLocator;
        pageLocator = new WebPageLocator(tester);
        text.assertTextSequence(pageLocator, licenseInfo.messages);
        for (int i = 0; i < licenseInfo.notMessages.length; i++)
        {
            String notMessage = licenseInfo.notMessages[i];
            text.assertTextNotPresent(pageLocator, notMessage);
        }
        for (int i = 0; i < licenseInfo.urls.length; i++)
        {
            LicenseInfoUrl licenseInfoUrl = licenseInfo.urls[i];
            assertions.getLinkAssertions().assertLinkLocationEndsWith(licenseInfoUrl.text, licenseInfoUrl.url);
        }
    }

    private void assertDataLicense(String[] messages, LicenseInfoUrl[] urls)
    {
        final Locator pageLocator;
        pageLocator = new WebPageLocator(tester);
        text.assertTextSequence(pageLocator, messages);
        for (int i = 0; i < urls.length; i++)
        {
            LicenseInfoUrl licenseInfoUrl = urls[i];
            assertions.getLinkAssertions().assertLinkLocationEndsWith(licenseInfoUrl.text, licenseInfoUrl.url);
        }
    }

    private static LicenseInfo createSupportedLicense(String key, String description)
    {
        return new LicenseInfo(key, description,
                new String[] {
                        "(Support and updates available until "
                },
                new String[] {
                        "JIRA support and updates for this license ended on ",
                        "JIRA support and updates created after ",
                        "are not valid for this license."
                },
                new LicenseInfoUrl[] { }
        );
    }

    private static LicenseInfo createUnsupportedLicense(String key, String description)
    {
        return new LicenseInfo(key, description,
                new String[] {
                        "(Updates available until "
                },
                new String[] {
                        "JIRA updates for this license ended on ",
                        "JIRA updates created after ",
                        "are not valid for this license."
                },
                new LicenseInfoUrl[] { }
        );
    }

    private static class LicenseInfo
    {
        final String license;
        final String description;
        final String[] messages;
        final String[] notMessages;
        final LicenseInfoUrl[] urls;

        private LicenseInfo(final String license, final String description, final String[] messages, final String[] notMessages, final LicenseInfoUrl[] urls)
        {
            this.license = license;
            this.description = description;
            this.messages = messages;
            this.notMessages = notMessages;
            this.urls = urls;
        }
    }

    private static class DataLicenseInfo
    {
        final String dataFile;
        final String[] expiredMessages;
        final String[] notExpiredMessages;
        final LicenseInfoUrl[] urls;

        private DataLicenseInfo(final String dataFile, final String[] expiredMessages, final String[] notExpiredMessages, final LicenseInfoUrl[] urls)
        {
            this.dataFile = dataFile;
            this.expiredMessages = expiredMessages;
            this.notExpiredMessages = notExpiredMessages;
            this.urls = urls;
        }
    }

    private static class LicenseInfoUrl
    {
        String text;
        String url;

        private LicenseInfoUrl(final String text, final String url)
        {
            this.text = text;
            this.url = url;
        }
    }
}
