package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.UserProfile;
import com.atlassian.jira.functest.framework.UserProfileImpl;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.LicenseKeys;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.SimpleStoredMessage;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Collection;

/**
 * Tests submitting a Support Request via the admin section.
 * <p/>
 * Tests sending and receiving of the support request using GreenMail.
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING, Category.LICENSING })
public class TestSupportRequest extends EmailFuncTestCase
{
    private static final String SUPPORT_REQUEST_HEADER =
            "---------------------------------------------------" + newline
                    + "This is an automated support request sent from JIRA" + newline
                    + "---------------------------------------------------";
    private static final String SUPPORT_REQUEST_PROBLEM = "Problem Description:" + newline + newline;
    private static final String CONTACT_NAME = "admin";
    private static final String CONTACT_EMAIL = "admin@localhost";
    private static final String CONTACT_NUMBER = "1234567890";
    private static final String SUPPORT_REQUEST_CONTACT =
            "Contact Information" + newline
                    + newline
                    + "Name:   " + CONTACT_NAME + newline
                    + "Email:  " + CONTACT_EMAIL + newline
                    + "Phone:  " + CONTACT_NUMBER + newline;
    private static final String LOG_ZIP = "atlassian-jira.log.zip";
    private static final String LOG_CONTENT_TYPE = "application/zip";

    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSupportRequest.xml");
    }

    public void testSupportRequestDenied() throws Exception
    {
        switchLicenseAndAssertSupportRequestDenied(LicenseKeys.V2_PERSONAL);
        switchLicenseAndAssertSupportRequestDenied(LicenseKeys.V2_DEMO);
    }

    public void testSupportRequestNotDenied() throws Exception
    {
        switchLicenseAndAssertSupportRequestNotDenied(LicenseKeys.V2_COMMUNITY);
        switchLicenseAndAssertSupportRequestNotDenied(LicenseKeys.V2_DEVELOPER);
        switchLicenseAndAssertSupportRequestNotDenied(LicenseKeys.V2_OPEN_SOURCE);
    }

    private void switchLicenseAndAssertSupportRequestDenied(LicenseKeys.License license) throws SAXException
    {
        administration.switchToLicense(license);
        gotoSupportRequestAdminPage();
        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester), new String[] {
                "Please note that your license", "does not entitle you to support.",
                "If you are experiencing problems, please see the JIRA",
                "The Atlassian Team" });
        assertions.getLinkAssertions().assertLinkLocationEndsWith("forums", "http://forums.atlassian.com/");
        assertions.getLinkAssertions().assertLinkLocationEndsWith("purchasing", "http://www.atlassian.com/order");
    }

    private void switchLicenseAndAssertSupportRequestNotDenied(LicenseKeys.License license) throws SAXException
    {
        administration.switchToLicense(license);
        gotoSupportRequestAdminPage();
        tester.assertTextNotPresent("does not entitle you to support.");
        tester.assertTextNotPresent("If you are experiencing problems, please see the JIRA");
    }

    private void gotoSupportRequestAdminPage()
    {
        tester.gotoPage("/secure/admin/JiraSupportRequest!default.jspa");
    }

    private void assertLinkWithTextUrlStartsWith(String name, String url)
    {
        try
        {

            WebLink link = tester.getDialog().getResponse().getLinkWith(name);
            assertTrue(link.getURLString().indexOf(url) >= 0);
        }
        catch (SAXException e)
        {
            fail("Failed to get the link with name: " + name);
        }
    }

    /**
     * Check that all mandatory fields produce the correct error message.
     */
    public void testMandatoryFields()
    {
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();
        tester.setFormElement("to", "");
        tester.setFormElement("cc", "admin@example.com");
        tester.setFormElement("name", "");
        tester.setFormElement("email", "");
        tester.setFormElement("phone", "");
        tester.submit("Submit");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify at least one 'to' address.");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify a subject.");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify a description.");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify a name.");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify a contact email address.");
    }

    /**
     * Check that all e-mail fields validate the entered address.
     */
    public void testInvalidEmailAddresses()
    {
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();
        tester.setFormElement("to", "test@@test.com");
        tester.setFormElement("cc", "invalidemail.com");
        tester.setFormElement("email", "anotherivalid^#$%^#$^");
        tester.submit("Submit");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify a valid 'to' address(es).");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify a valid 'cc' address(es).");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify a valid 'contact' address(es).");
    }

    /**
     * Check invalid e-mail addresses if multiple addresses are entered
     */
    public void testInvalidMultipleEmailAddresses()
    {
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();
        tester.setFormElement("to", "test@test.com, test@@test.com");
        tester.setFormElement("cc", "invalidemail.com, valid@email.com");
        tester.setFormElement("email", "email@example.com, anotherivalid^#$%^#$^");
        tester.submit("Submit");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify a valid 'to' address(es).");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify a valid 'cc' address(es).");
        assertions.getJiraFormAssertions().assertFieldErrMsg("You must specify a valid 'contact' address(es).");
    }

    public void testBaseUrlIsSentInTheSupportRequest()
            throws MessagingException, IOException, InterruptedException, SAXException, FolderException
    {
        //configure the mail server so we can goto the support request page
        String from = "sender@base.url.com";
        String prefix = "[TEST-BaseUrl]";
        configureAndStartSmtpServer(from, prefix);

        //edit the base URL to something we know
        String baseUrl = "http://example.url.com:8090";
        administration.generalConfiguration().setBaseUrl(baseUrl);
        assertBaseUrlnSupportRequest("change1@blah.com", baseUrl, from, prefix, 1);

        //edit the base url again
        baseUrl = "http://a.new.base.url/test/jira";
        administration.generalConfiguration().setBaseUrl(baseUrl);
        assertBaseUrlnSupportRequest("change2@random.email", baseUrl, from, prefix, 2);

        //edit the base url and once more
        baseUrl = "https://third.time.lucky";
        administration.generalConfiguration().setBaseUrl(baseUrl);
        assertBaseUrlnSupportRequest("change3@last.test", baseUrl, from, prefix, 3);
    }

    public void testSupportRequestContainsLicenseInfo() throws Exception
    {
        //configure the mail server so we can goto the support request page
        String from = "sender@base.url.com";
        String prefix = "[TEST-BaseUrl]";
        configureAndStartSmtpServer(from, prefix);

        final LicenseKeys.License license = LicenseKeys.V2_DEVELOPER_LIMITED;
        administration.switchToLicense(license);

        //first check the license info is shown in the support request page
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();

        final String licenseDesc = license.getDescription();

        final WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, new String[] { "License Type", licenseDesc });
        tester.assertTextPresent("Maintenance Period End Date");
        text.assertTextSequence(locator, new String[] { "Maintenance Status", "Supported" });
        tester.assertTextPresent("Date Purchased");
        text.assertTextSequence(locator, "Support Entitlement Number", license.getSen());
        text.assertTextSequence(locator, "User Limit", String.valueOf(license.getMaxUsers()));

        sendSupportRequestAndVerify("legendarysupport@example.com", "", false, false, from, prefix, 1, true);
        //verify the sent support request (email) content has the base url
        MailFolder userInbox = getUserInbox("legendarysupport@example.com");
        assertEquals(1, userInbox.getMessageCount());
        final MimeMessage email = ((SimpleStoredMessage) userInbox.getMessages().get(0)).getMimeMessage();
        assertEmailBodyContains(email, "License Type");
        assertEmailBodyContains(email, licenseDesc);
        assertEmailBodyContains(email, "Maintenance Period End Date");
        assertEmailBodyContains(email, "Maintenance Status");
        assertEmailBodyContains(email, "Supported");
        assertEmailBodyContains(email, "Date Purchased");
        assertEmailBodyContains(email, "JIRA Home");
        assertEmailBodyContains(email, "User Limit");
        assertEmailBodyContains(email, "Support Entitlement Number");
    }

    public void testSupportRequestContainsNoSecurityBlacklistInfo() throws Exception
    {
        //configure the mail server so we can goto the support request page
        String from = "sender@base.url.com";
        String prefix = "[TEST-BaseUrl]";
        configureAndStartSmtpServer(from, prefix);

        final LicenseKeys.License license = LicenseKeys.V2_DEVELOPER_LIMITED;
        administration.switchToLicense(license);

        //first check the license info is shown in the support request page
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();

        tester.assertTextNotPresent("License Hash 1");
        tester.assertTextNotPresent("License Hash 1 Text");
        tester.assertTextNotPresent("License Message");
        tester.assertTextNotPresent("License Message Text");
        tester.assertTextNotPresent("License20");
        tester.assertTextNotPresent("jira.sid.key");
        tester.assertTextNotPresent("org.apache.shindig.common.crypto.BlobCrypter:key");

        sendSupportRequestAndVerify("legendarysupport@example.com", "", false, false, from, prefix, 1, true);
        //verify the sent support request (email) content has the base url
        MailFolder userInbox = getUserInbox("legendarysupport@example.com");
        assertEquals(1, userInbox.getMessageCount());
        final MimeMessage email = ((SimpleStoredMessage) userInbox.getMessages().get(0)).getMimeMessage();
        assertEmailBodyDoesntContain(email, "License Hash 1");
        assertEmailBodyDoesntContain(email, "License Hash 1 Text");
        assertEmailBodyDoesntContain(email, "License Message");
        assertEmailBodyDoesntContain(email, "License Message Text");
        assertEmailBodyDoesntContain(email, "License20");
        assertEmailBodyDoesntContain(email, "jira.sid.key");
        assertEmailBodyDoesntContain(email, "org.apache.shindig.common.crypto.BlobCrypter:key");

    }

    public void testSupportRequestContainsMemoryAndInputArgsInfo()
            throws MessagingException, IOException, InterruptedException, SAXException, FolderException
    {
        //configure the mail server so we can goto the support request page
        String from = "sender@base.url.com";
        String prefix = "[TEST-BaseUrl]";
        configureAndStartSmtpServer(from, prefix);

        //first check the license info is shown in the support request page
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();

        sendSupportRequestAndVerify("legendarysupport@example.com", "", false, false, from, prefix, 1, true);
        //verify the sent support request (email) content has the base url
        MailFolder userInbox = getUserInbox("legendarysupport@example.com");
        assertEquals(1, userInbox.getMessageCount());
        final MimeMessage email = ((SimpleStoredMessage) userInbox.getMessages().get(0)).getMimeMessage();
        assertEmailBodyContains(email, "Used PermGen Memory");
        assertEmailBodyContains(email, "Free PermGen Memory");
        assertEmailBodyContains(email, "Memory Pools:");
        assertEmailBodyContains(email, "JVM Input Arguments");
        // Make sure the warning message are not present
        assertEmailBodyDoesntContain(email, "Unable to determine, this requires running JDK 1.5 and higher.");
    }

    public void testSupportRequestContainsTimezoneInfo()
            throws MessagingException, IOException, InterruptedException, SAXException, FolderException
    {
        //configure the mail server so we can goto the support request page
        String from = "sender@base.url.com";
        String prefix = "[TEST-BaseUrl]";
        configureAndStartSmtpServer(from, prefix);

        //first check the license info is shown in the support request page
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();

        sendSupportRequestAndVerify("legendarysupport@example.com", "", false, false, from, prefix, 1, true);
        //verify the sent support request (email) content has the base url
        MailFolder userInbox = getUserInbox("legendarysupport@example.com");
        assertEquals(1, userInbox.getMessageCount());
        final MimeMessage email = ((SimpleStoredMessage) userInbox.getMessages().get(0)).getMimeMessage();
        assertEmailBodyContains(email, "User Timezone");
        assertEmailBodyContainsLine(email, "\\s*System Time: \\d{1,2}:\\d{2}:\\d{2} [+-]\\d{4}\\s*");
    }

    public void testSupportRequestContainsUserLimitLicenseInfo()
            throws MessagingException, IOException, InterruptedException, SAXException, FolderException
    {
        //switch to the enterprise hosted license.
        administration.switchToLicense(LicenseKeys.V2_HOSTED);

        //configure the mail server so we can goto the support request page
        String from = "sender@base.url.com";
        String prefix = "[TEST-BaseUrl]";
        configureAndStartSmtpServer(from, prefix);

        //first check the license info is shown in the support request page
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();
        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester), new String[] { "License Type", "JIRA Enterprise: Hosted" });
        tester.assertTextPresent("Maintenance Period End Date");
        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester), new String[] { "Maintenance Status", "Supported" });
        tester.assertTextPresent("Date Purchased");
        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester), new String[] { "User Limit", "200", "(1 currently active)" });

        sendSupportRequestAndVerify("legendarysupport@example.com", "", false, false, from, prefix, 1, true);
        //verify the sent support request (email) content has the base url
        MailFolder userInbox = getUserInbox("legendarysupport@example.com");
        assertEquals(1, userInbox.getMessageCount());
        final MimeMessage email = ((SimpleStoredMessage) userInbox.getMessages().get(0)).getMimeMessage();
        assertEmailBodyContains(email, "License Type: JIRA Enterprise: Hosted");
        assertEmailBodyContains(email, "Maintenance Period End Date");
        assertEmailBodyContains(email, "Maintenance Status");
        assertEmailBodyContains(email, "Supported");
        assertEmailBodyContains(email, "Date Purchased");
        assertEmailBodyContains(email, "User Limit: 200 (1 currently active)");
    }

    private void assertBaseUrlnSupportRequest(String to, String baseUrl, String from, String prefix, int expectTotalNumOfEmails)
            throws SAXException, InterruptedException, MessagingException, IOException, FolderException
    {
        //verify the send support request (HTML) page has the base url.
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();
        WebTable environmentTable = tester.getDialog().getResponse().getTableWithID("support_req_env");
        assertions.getTableAssertions().assertTableContainsRow(environmentTable, new String[] { "Base URL", baseUrl });

        sendSupportRequestAndVerify(to, "", false, false, from, prefix, expectTotalNumOfEmails, true);

        //verify the sent support request (email) content has the base url
        MailFolder userInbox = getUserInbox(to);
        assertEquals(1, userInbox.getMessageCount());
        final MimeMessage email = ((SimpleStoredMessage) userInbox.getMessages().get(0)).getMimeMessage();
        assertEmailBodyContains(email, "Base URL: " + baseUrl);
    }

    public void testSupportRequestPageTranslated()
    {
        //configure the mail server so we can goto the support request page
        String from = "sender@support.request.com";
        String prefix = "[TEST-PREFIX]";
        configureAndStartSmtpServer(from, prefix);

        // change language to German
        administration.generalConfiguration().setJiraLocale("German (Germany)");
        UserProfile userProfile = new UserProfileImpl(tester, getEnvironmentData(), navigation);
        userProfile.changeUserLanguage("Deutsch (Deutschland) [Default]");

        // goto support request page and ensure translation is applied
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();
        tester.assertTextPresent("Supportanforderung");
        tester.assertTextPresent("Betreff");
        tester.assertTextPresent("Beschreibung");
        tester.assertTextPresent("Existierende Supportanfrage");
    }

    public void testSupportRequestEmailNotTranslated()
            throws MessagingException, IOException, InterruptedException, FolderException
    {
        // Standardsprache = Default Language
        //configure the mail server so we can goto the support request page
        String from = "sender@support.request.com";
        String prefix = "[TEST-PREFIX]";
        configureAndStartSmtpServer(from, prefix);

        // change language to German
        administration.generalConfiguration().setJiraLocale("German (Germany)");
        UserProfile userProfile = new UserProfileImpl(tester, getEnvironmentData(), navigation);
        userProfile.changeUserLanguage("Deutsch (Deutschland) [Default]");

        sendSupportRequestAndVerify("test@localhost", "", false, false, from, prefix, 1, false);
    }

    /**
     * Sends a support request and verifies that the support request was sent to the right recipients and asserts
     * various known values of the support request.
     *
     * @param to comma-seperated list of email addresses to send to
     * @param cc comma-seperated list of email addresses to cc to
     * @param isAttachExport whether to attach export (checkbox)
     * @param isAttachLog whether to attach jira logs
     * @param from email address the support request was sent from
     * @param prefix email subject prefix
     * @param expectTotalNumOfEmails the total expected number of emails sent out
     * @param useEnglish whether or not to use English or German
     * @throws InterruptedException interrupted
     * @throws MessagingException message error
     * @throws IOException IO error
     * @throws FolderException folder error
     */
    private void sendSupportRequestAndVerify(String to, String cc, boolean isAttachExport, boolean isAttachLog, String from, String prefix, int expectTotalNumOfEmails, final boolean useEnglish)
            throws InterruptedException, MessagingException, IOException, FolderException
    {
        int expectedNumberOfParts = 2;
        if (isAttachExport)
        {
            expectedNumberOfParts++;//verify there is another part to the email for the export
        }
        if (isAttachLog)
        {
            expectedNumberOfParts++;//verify there is another part to the email for the logs
        }

        String subject = "New Support Request Test";
        String description = "test sending a support req";

        //send the support request
        if (useEnglish)
        {
            sendSupportRequestInEnglish(to, cc, subject, description, isAttachExport, isAttachLog);
        }
        else
        {
            sendSupportRequestInGerman(to, cc, subject, description, isAttachExport, isAttachLog);
        }

        //verify the support request

        //no need to flush as we send support requests right away
        waitForMail(1);
        //verify that the server recieved only one email
        assertEquals(expectTotalNumOfEmails, getGreenMail().getReceivedMessages().length);

        final MimeMessage[] receivedEmails = getGreenMail().getReceivedMessages();
        final MimeMessage email = receivedEmails[expectTotalNumOfEmails - 1];

        Collection toEmailAddresses = parseEmailAddresses(to);
        Collection ccEmailAddresses = parseEmailAddresses(cc);

        //verify each recipient got the same email.
        Address[] recipients = email.getAllRecipients();
        for (Address recipient : recipients)
        {
            String recipientEmailAddress = recipient.toString();

            //verify that the user with 'to' address recieved the email
            MailFolder userInbox = getUserInbox(recipientEmailAddress);
            assertEquals(1, userInbox.getMessageCount());
            assertEquals(email, ((SimpleStoredMessage) userInbox.getMessages().iterator().next()).getMimeMessage());

            assertEmailToEquals(email, toEmailAddresses);
            assertEmailCcEquals(email, ccEmailAddresses);
            assertEmailFromEquals(email, from);
            assertEmailSubjectEquals(email, prefix + " [JIRA Support Request] " + subject);
            String body = GreenMailUtil.getBody(email);
            assertEmailBodyContains(body, SUPPORT_REQUEST_HEADER);
            assertEmailBodyContains(body, SUPPORT_REQUEST_PROBLEM + description);

            assertEmailBodyDoesntContain(email, "Kaufdatum");
            assertEmailBodyDoesntContain(email, "Lizenztyp");
            assertEmailBodyContains(body, "Date Purchased");
            assertEmailBodyContains(body, "License Type");

            assertEmailBodyContains(body, "Environment Information:");
            assertEmailBodyContains(body, "Language Information:");
            assertEmailBodyContains(body, "Database Statistics");
            assertEmailBodyContains(body, "Listeners:");
            assertEmailBodyContains(body, "Services:");
            assertEmailBodyContains(body, "Plugins:");
            assertEmailBodyContains(body, SUPPORT_REQUEST_CONTACT);
            assertEmailBodyContains(body, "System Properties");
        }
    }

    /**
     * Sends a JIRA support request with all the passed in values. Note: this requires a mail server to be setup.
     *
     * @param to comma-separated list of email addresses to send to
     * @param cc comma-separated list of email addresses to cc to
     * @param subject subject of the support request
     * @param description description of the support request
     * @param isAttachExport whether to attach export (checkbox)
     * @param isAttachLog whether to attach jira logs
     */
    private void sendSupportRequestInEnglish(String to, String cc, String subject, String description, boolean isAttachExport, boolean isAttachLog)
    {
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();
        tester.setFormElement("to", to);
        tester.setFormElement("subject", subject);
        tester.setFormElement("description", description);
        tester.setFormElement("cc", cc);
        tester.setFormElement("name", CONTACT_NAME);
        tester.setFormElement("email", CONTACT_EMAIL);
        tester.setFormElement("phone", CONTACT_NUMBER);
        tester.submit("Submit");

        tester.assertTextPresent("Your support request email has been sent");
        tester.assertTextPresent("(Please note that if there is a problem delivering this email, the Atlassian support staff may not receive your support request.)");
    }

    /**
     * Sends a JIRA support request with all the passed in values. Note: this requires a mail server to be setup.
     *
     * @param to comma-separated list of email addresses to send to
     * @param cc comma-separated list of email addresses to cc to
     * @param subject subject of the support request
     * @param description description of the support request
     * @param isAttachExport whether to attach export (checkbox)
     * @param isAttachLog whether to attach jira logs
     */
    private void sendSupportRequestInGerman(String to, String cc, String subject, String description, boolean isAttachExport, boolean isAttachLog)
    {
        navigation.gotoAdmin();
        gotoSupportRequestAdminPage();
        tester.setFormElement("to", to);
        tester.setFormElement("subject", subject);
        tester.setFormElement("description", description);
        tester.setFormElement("cc", cc);
        tester.setFormElement("name", CONTACT_NAME);
        tester.setFormElement("email", CONTACT_EMAIL);
        tester.setFormElement("phone", CONTACT_NUMBER);
        tester.submit("Senden");

        tester.assertTextPresent("Supportanfrage gesendet");
        tester.assertTextPresent("Ihre E-Mail-Supportanfrage wurde gesendet.");
    }
}
