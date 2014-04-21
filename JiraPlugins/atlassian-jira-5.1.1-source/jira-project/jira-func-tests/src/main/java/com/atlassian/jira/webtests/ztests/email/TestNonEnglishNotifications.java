package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

/**
 * @since v3.13.4
 */
// introduced for some rudimentary testing around JRA-16611
// passing now
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestNonEnglishNotifications extends EmailFuncTestCase
{
    public void testNonEnglishNotifications() throws InterruptedException, MessagingException, IOException
    {
        // must be English to configure stuff
        navigation.login(ADMIN_USERNAME);
        administration.restoreData("TestTranslatedNotifications.xml");
        configureAndStartSmtpServer();

        // fred is french
        navigation.login(FRED_USERNAME);
        tester.gotoPage("/secure/ViewSubscriptions.jspa?filterId=10000");
        tester.clickLinkWithText("Ex\u00E9cuter maintenant");

        // must be English to flush mail queue
        navigation.login(ADMIN_USERNAME);
        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();
        assertEquals("Only one notification should have been sent", 1, messages.length);
        String body = messages[0].getContent().toString();

        // this is a poor man's validation that we translated stuff properly into French.
        // ideally we would use a TableLocator and compare that against a bunch of XPath assertions
        // but none of the works for the HTML email we're dealing with here.
        assertTrue(body.indexOf("Non attribu\u00E9e") > 0);
        assertTrue(body.indexOf("Non r\u00E9solu") > 0);
        assertTrue(body.indexOf("17/mars/09") > 0);
    }
}
