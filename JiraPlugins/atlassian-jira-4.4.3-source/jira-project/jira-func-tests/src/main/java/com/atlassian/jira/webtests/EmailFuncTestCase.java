package com.atlassian.jira.webtests;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.icegreen.greenmail.AbstractServer;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.opensymphony.util.TextUtils;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class extends FuncTestCase by adding methods to test emails being sent from JIRA.
 */
public class EmailFuncTestCase extends FuncTestCase implements FunctTestConstants
{
    public static final String DEAFULT_FROM_ADDRESS = "jiratest@atlassian.com";
    public static final String DEFAULT_SUBJECT_PREFIX = "[JIRATEST]";
    public static final String newline = "\r\n";

    private GreenMail greenMail;

    public void tearDownTest()
    {
        if (greenMail != null)
        {
            greenMail.stop();
            greenMail = null;
        }
    }

    public GreenMail getGreenMail()
    {
        if (greenMail == null)
        {
            fail("GreenMail has not been setup.");
        }
        return greenMail;
    }

    /**
     * Use this method to start a {@link com.icegreen.greenmail.smtp.SmtpServer}. <p> This will also configure JIRA to
     * use this SMTP server in the admin section. You should call this after your data import. This will override any
     * existing mail servers setup already. </p> <p> A simple SMTP server proxy is started by first attempting to start
     * on a default port number. If this port is already used we try that port number plus one and so on for 10
     * attempts. this allows for multiple tests running in Bamboo concurrently, and also for a particular test machine
     * maybe using that port already. </p> <p> The tearDown() method will close the TCP socket. </p>
     *
     * @return A {@link com.icegreen.greenmail.util.GreenMail} to query for mails
     */
    protected GreenMail configureAndStartSmtpServer()
    {
        return configureAndStartSmtpServer(DEAFULT_FROM_ADDRESS, DEFAULT_SUBJECT_PREFIX);
    }

    protected GreenMail configureAndStartSmtpServer(String from, String prefix)
    {
        //check if mail sending is disabled
        assertSendingMailIsEnabled();

        configureAndStartGreenMailSmtp();

        setupJiraMailServer(from, prefix, String.valueOf(greenMail.getSmtp().getPort()));

        return greenMail;
    }

    /**
     * Given a comma seperated list of email addresses, returns a collection of the email addresses.
     *
     * @param emails comma seperated list of email addresses
     * @return collection of individual email address
     */
    protected Collection<String> parseEmailAddresses(String emails)
    {
        StringTokenizer st = new StringTokenizer(emails, ",");
        Collection<String> emailList = new ArrayList<String>();
        while (st.hasMoreTokens())
        {
            String email = st.nextToken().trim();
            if (TextUtils.stringSet(email))
            {
                emailList.add(email.trim());
            }
        }
        return emailList;
    }

    protected void assertRecipientsHaveMessages(Collection /*<String>*/ recipients) throws MessagingException
    {
        for (Iterator iterator = recipients.iterator(); iterator.hasNext();)
        {
            String recipient = (String) iterator.next();
            assertFalse("Recipient '" + recipient + "' did not receive any messages", getMessagesForRecipient(recipient).isEmpty());
        }
    }

    protected List<MimeMessage> getMessagesForRecipient(String recipient) throws MessagingException
    {
        MimeMessage[] messages = getGreenMail().getReceivedMessages();
        List<MimeMessage> ret = new ArrayList<MimeMessage>();

        for (int i = 0; i < messages.length; i++)
        {
            MimeMessage message = messages[i];
            if (Arrays.asList(message.getHeader("To")).contains(recipient))
            {
                ret.add(message);
            }
        }

        return ret;
    }

    protected void assertSendingMailIsEnabled()
    {
        navigation.gotoAdmin();
        tester.clickLink("mail_queue");

        try
        {
            final String responseText = tester.getDialog().getResponse().getText();
            if (responseText.indexOf("Sending mail is disabled") != -1)
            {
                fail("Mail sending is disabled. Please restart your server without -Datlassian.mail.senddisabled=true.");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void setupJiraMailServer(String from, String prefix, String smtpPort)
    {
        navigation.gotoAdmin();
        tester.clickLink("mail_servers");
        if (tester.getDialog().isLinkPresent("deleteSMTP"))
        {
            tester.clickLink("deleteSMTP");
            tester.submit("Delete");
        }
        tester.clickLinkWithText("Configure new SMTP mail server");
        tester.setFormElement("name", "Local Test Server");
        tester.setFormElement("from", from);
        tester.setFormElement("prefix", prefix);
        tester.setFormElement("serverName", "localhost");
        log("Setting SMTP server to 'localhost:" + smtpPort + "'");
        tester.setFormElement("port", smtpPort);
        tester.submit("Add");
        tester.assertLinkNotPresentWithText("Configure new SMTP mail server");
        tester.assertTextPresent("Local Test Server");
    }

    protected void setupJiraImapPopServer()
    {
        navigation.gotoAdmin();
        tester.clickLink("mail_servers");
        tester.clickLinkWithText("Configure new POP / IMAP mail server");
        tester.setFormElement("name", "Local Test Pop/Imap Server");
        tester.setFormElement("serverName", "localhost");
        tester.setFormElement("username", ADMIN_USERNAME);
        tester.setFormElement("password", ADMIN_USERNAME);
        tester.submit("Add");
    }

    protected void setupPopService()
    {
        setupPopService("project=MKY, issue=1, createusers=true");
    }

    protected void setupPopService(String handlerParameters)
    {
        String popPort = String.valueOf(getGreenMail().getPop3().getPort());

        navigation.gotoAdmin();
        tester.clickLink("services");
        tester.setFormElement("name", "pop");
        tester.setFormElement("clazz", "com.atlassian.jira.service.services.pop.PopService");
        tester.setFormElement("delay", "1");
        tester.submit("Add Service");
        tester.setFormElement("handler.params", handlerParameters);
        tester.setFormElement("port", popPort);
        tester.setFormElement("delay", "1");
        tester.submit("Update");

    }

    protected void setupImapService()
    {
        setupImapService("project=MKY, issue=1, createusers=true");
    }

    protected void setupImapService(String handlerParameters)
    {
        String imapPort = String.valueOf(getGreenMail().getImap().getPort());

        navigation.gotoAdmin();
        tester.clickLink("services");
        tester.setFormElement("name", "imap");
        tester.setFormElement("clazz", "com.atlassian.jira.service.services.imap.ImapService");
        tester.setFormElement("delay", "1");
        tester.submit("Add Service");
        tester.setFormElement("handler.params", handlerParameters);
        tester.setFormElement("port", imapPort);
        tester.setFormElement("delay", "1");
        tester.submit("Update");
    }

    protected GreenMail configureAndStartGreenMailSmtp()
    {
        return configureAndStartGreenMail(new JIRAServerSetup[] { JIRAServerSetup.SMTP });
    }

    protected GreenMail configureAndStartGreenMail(JIRAServerSetup[] serverSetups)
    {
        return configureAndStartGreenMail(new JIRAGreenMailSetup(serverSetups));
    }

    private GreenMail configureAndStartGreenMail(JIRAGreenMailSetup jiraGreenMailSetup)
    {
        log("Configuring and starting JIRA green mail server");

        greenMail = null;
        int setupRetries = 0;
        boolean hasFailedService = false;
        while (setupRetries < 10)
        {
            greenMail = new GreenMail(jiraGreenMailSetup.getServerSetups());
            greenMail.start();

            try
            {
                //wait for the servers to start up
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            Collection<AbstractServer> servers = new ArrayList<AbstractServer>();
            servers.add(greenMail.getSmtp());
            servers.add(greenMail.getPop3());
            servers.add(greenMail.getImap());

            //for each server, check if any has failed to start
            for (Iterator iterator = servers.iterator(); iterator.hasNext() && !hasFailedService;)
            {
                AbstractServer server = (AbstractServer) iterator.next();
                hasFailedService = !isServerRunning(server);
            }

            //if any service has failed to start than restart with incremented port number
            if (hasFailedService)
            {
                log("Some servers did not start properly. Incrementing ports and trying again... '" + setupRetries + "'");
                setupRetries++;
                hasFailedService = false;
                greenMail.stop();
                jiraGreenMailSetup.incrementPorts();
            }
            else
            {
                log("Successfully started green mail server");
                break;
            }
        }
        assertTrue("Error: Could not start green mail server. See log for details.", !hasFailedService);
        return greenMail;
    }

    /**
     * Checks if the server is not null that it is alive and running.
     *
     * @param server abstract server
     * @return true if the server thread is alive, false otherwise
     */
    private boolean isServerRunning(AbstractServer server)
    {
        if (server != null)
        {
            if (server.isAlive())
            {
                log("Running '" + server.getProtocol() + "' server on port '" + server.getPort() + "'.");
            }
            else
            {
                log("Error trying to start '" + server.getProtocol() + "' server on port '" + server.getPort() + "'.");
                return false;
            }
        }
        return true;
    }

    /**
     * Get the {@link com.icegreen.greenmail.imap.ImapConstants#INBOX_NAME inbox} for the user with email userEmail.
     * NOTE: if the user does not exist, one will be created.
     *
     * @param userEmail the email address of the user
     * @return MailFolder of the user with the given email address
     * @throws FolderException if the user doesn't have an inbox on this server
     */
    protected MailFolder getUserInbox(String userEmail) throws FolderException
    {
        GreenMailUser mailUser = getGreenMail().setUser(userEmail, "password");
        return getGreenMail().getManagers().getImapHostManager().getInbox(mailUser);
    }

    /**
     * This is useful for writing func tests that test that the correct notifications are being sent. It goest to the
     * admin section mail-queue and flushes the queue and waits till it recieves emailCount number of emails before
     * timeout. If the timeout is reached before the expected number of emails arrives will fail.
     *
     * @param emailCount number of expected emails to wait to receive
     * @throws InterruptedException if interrupted
     */
    protected void flushMailQueueAndWait(int emailCount) throws InterruptedException
    {
        flushMailQueueAndWait(emailCount, 500);
    }

    /**
     * Does the same as {@link #flushMailQueueAndWait(int)} but allows the user to specify the wait period in case a lot
     * of e-mails are being sent.
     *
     * @param emailCount number of expected emails to wait to receive
     * @param waitPeriodMillis The amout of time to wait in millis until the e-mails should have arrived.
     * @throws InterruptedException if interrupted
     */
    protected void flushMailQueueAndWait(int emailCount, int waitPeriodMillis) throws InterruptedException
    {
        //flush mail queue
        navigation.gotoAdmin();
        tester.clickLink("mail_queue");
        tester.clickLinkWithText("Flush mail queue");
        log("Flushed mail queue. Waiting for '" + waitPeriodMillis + "' ms...");
        // Sleep for a small while - just to be sure the mail is received.
        final boolean receivedAllMail = greenMail.waitForIncomingEmail(waitPeriodMillis, emailCount);
        String msg = "Did not recieve all expected emails (" + emailCount + ") within the timeout.";
        if (greenMail.getReceivedMessages() != null)
        {
            msg += " Only received " + greenMail.getReceivedMessages().length + " message(s).";
        }
        assertTrue(msg, receivedAllMail);
    }

    protected void waitForMail(int emailCount) throws InterruptedException
    {
        final int waitPeriodMillis = 500;
        assertTrue("Did not recieve all expected emails within the timeout", greenMail.waitForIncomingEmail(waitPeriodMillis, emailCount));
    }

    /**
     * Asserts that the given email's body contains the bodySubString using indexOf.
     *
     * @param email email to extract the content body from
     * @param bodySubString expected substring of the email body
     * @throws MessagingException Message error
     * @throws IOException IO error
     * @see GreenMailUtil#getBody(javax.mail.Part)
     */
    protected void assertEmailBodyContains(MimeMessage email, String bodySubString)
            throws MessagingException, IOException
    {
        final String emailBody = GreenMailUtil.getBody(email);
        assertTrue("The string '" + bodySubString + "' was not found in the e-mail body [" + emailBody + "]",
                emailBody.indexOf(bodySubString) != -1);
    }

    /**
     * Asserts that the given email's body contains a line which matches the given string or pattern.
     *
     * @param email email to extract the content body from
     * @param linePattern expected line or line pattern
     * @throws MessagingException Message error
     * @throws IOException IO error
     * @see GreenMailUtil#getBody(javax.mail.Part)
     */
    protected void assertEmailBodyContainsLine(MimeMessage email, String linePattern)
            throws MessagingException, IOException
    {
        final String emailBody = GreenMailUtil.getBody(email);
        String[] lines = emailBody.split("\\n");
        boolean match = false;
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            match = line.matches(linePattern);
            if (match)
            {
                break;
            }
        }
        assertTrue("The line '" + linePattern + "' was not found in the e-mail body [" + emailBody + "]", match);
    }

    /**
     * Asserts that the given email's body does not contain the bodySubString using indexOf.
     *
     * @param email email to extract the content body from
     * @param bodySubString string to not occur in body
     * @throws MessagingException Message error
     * @throws IOException IO error
     * @see GreenMailUtil#getBody(javax.mail.Part)
     */
    protected void assertEmailBodyDoesntContain(MimeMessage email, String bodySubString)
            throws MessagingException, IOException
    {
        final String emailBody = GreenMailUtil.getBody(email);
        assertTrue("The string '" + bodySubString + "' was found (shouldn't exist) in the e-mail body [" + emailBody + "]",
                emailBody.indexOf(bodySubString) == -1);
    }

    /**
     * Assert that the String emailBody contains bodySubString
     *
     * @param emailBody body
     * @param bodySubString expected substring
     * @throws MessagingException message error
     * @throws IOException IO error
     */
    protected void assertEmailBodyContains(String emailBody, String bodySubString)
            throws MessagingException, IOException
    {
        assertTrue("Expected '" + bodySubString + "' to be present in email body '" + emailBody + "'", emailBody.indexOf(bodySubString) != -1);
    }

    protected void assertEmailHasNumberOfParts(MimeMessage email, int expectedNumOfParts)
            throws MessagingException, IOException
    {
        Object emailContent = email.getContent();
        if (emailContent instanceof Multipart)
        {
            Multipart multiPart = (Multipart) emailContent;
            assertEquals(expectedNumOfParts, multiPart.getCount());
        }
        else
        {
            fail("Cannot assert number of parts for email. Email is not a multipart type.");
        }
    }

    /**
     * Assert that the email was addressed to the expectedTo
     *
     * @param email email to assert the value of the to header
     * @param expectedTo the single or comma seperated list of expected email addresses
     * @throws MessagingException meesage error
     * @see #assertEmailToEquals(javax.mail.internet.MimeMessage, java.util.Collection)
     */
    protected void assertEmailToEquals(MimeMessage email, String expectedTo) throws MessagingException
    {
        assertEmailToEquals(email, parseEmailAddresses(expectedTo));
    }

    /**
     * Assert that the email was addressed to each and everyone of the expectedAddresses
     *
     * @param email email to assert the value of the to header
     * @param expectedToAddresses collection of expected email addresses
     * @throws MessagingException meesage error
     */
    protected void assertEmailToEquals(MimeMessage email, Collection expectedToAddresses) throws MessagingException
    {
        String[] toHeader = email.getHeader("to");
        assertEquals(1, toHeader.length);
        Collection actualAddresses = parseEmailAddresses(toHeader[0]);
        assertEmailsEquals(expectedToAddresses, actualAddresses);
    }

    protected void assertEmailCcEquals(MimeMessage email, Collection expectedCcAddresses) throws MessagingException
    {
        String[] ccHeader = email.getHeader("cc");
        if (ccHeader != null)
        {
            assertEquals(1, ccHeader.length);
            Collection actualAddresses = parseEmailAddresses(ccHeader[0]);
            assertEmailsEquals(expectedCcAddresses, actualAddresses);
        }
        else
        {
            //if there is no Cc header, assert that we were not expecting any emails.
            assertTrue("Expected Cc address but was null", expectedCcAddresses.isEmpty());
        }
    }

    private void assertEmailsEquals(Collection expectedAddresses, Collection actualAddresses)
    {
        assertEquals("Expected '" + expectedAddresses.size() + "' email addresses but only found '" + actualAddresses.size() + "'", expectedAddresses.size(), actualAddresses.size());
        assertEquals(expectedAddresses, actualAddresses);
    }

    protected void assertEmailFromEquals(MimeMessage email, String expectedTo) throws MessagingException
    {
        String[] addresses = email.getHeader("from");
        assertEquals(1, addresses.length);
        assertEquals(expectedTo, addresses[0]);
    }

    protected void assertEmailSubjectEquals(MimeMessage email, String subject) throws MessagingException
    {
        assertEquals(subject, email.getSubject());
    }

    protected void assertEmailSent(String recipient, String subject, String issueComment)
            throws MessagingException, IOException
    {
        final List emails = getMessagesForRecipient(recipient);
        assertEquals("Incorrect number of e-mails received for '" + recipient + "'", 1, emails.size());
        final MimeMessage emailMessage = (MimeMessage) emails.get(0);
        assertEmailBodyContains(emailMessage, issueComment);
        assertEmailSubjectEquals(emailMessage, subject);
    }

    protected void assertCorrectNumberEmailsSent(int numOfMessages)
            throws MessagingException
    {
        final MimeMessage[] messages = getGreenMail().getReceivedMessages();
        if (messages.length != numOfMessages)
        {
            for (int i = 0; i < messages.length; i++)
            {
                MimeMessage message = messages[i];
                log("Mail sent to '" + message.getHeader("to")[0] + "' with SUBJECT '" + message.getSubject() + "'");
            }
            fail("Invalid number of e-mails received.  Was " + messages.length + " but should have been " + numOfMessages + ".");
        }
    }
}
