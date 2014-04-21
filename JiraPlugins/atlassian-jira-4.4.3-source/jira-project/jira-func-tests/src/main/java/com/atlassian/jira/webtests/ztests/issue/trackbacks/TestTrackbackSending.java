package com.atlassian.jira.webtests.ztests.issue.trackbacks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import org.junit.Ignore;

/**
 * A test for trackback ping sending
 *
 * @since v3.13
 */
@Ignore ("Disabled pending more investigation --lmiranda")
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestTrackbackSending extends FuncTestCase
{
    private static final int MAX_WAIT_COUNT = 20;


    protected void setUpTest()
    {
        administration.restoreData("TestTrackbackSending.xml");
    }

    public void testCreateTrackBack() throws Exception
    {
        //
        // we can make JIRA trackback to itself IF we use a hostname other than
        // its configured baseURL host name.  So we use 127.0.0.1 which shuld be cool!
        //
        final String baseURL = get127_0_0_1_BaseURL();
        final String trackbackDesc = "This links back to " + baseURL + "/browse/TRACKBACK-3 with some russian \\u041a\\u043e\\u043c\\u043f\\u043e\\u043d\\u0435\\u043d\\u0442\\u044b just for good measure";
        
        navigation.issue().viewIssue("TRACKBACK-1");
        tester.clickLink("editIssue");
        tester.setFormElement("description", trackbackDesc);
        tester.submit("Update");

        /// flush mail queue
        navigation.gotoAdmin();
        tester.clickLink("mail_queue");
        tester.clickLinkWithText("Flush mail queue");

        navigation.issue().viewIssue("TRACKBACK-3");
        /// wait a number of times for the trackback to appear because it is an async process
        int waitCount = 0;
        while(waitCount < MAX_WAIT_COUNT) {
            // get down a specific on this
            XPathLocator locator = new XPathLocator(tester,"//div[@class='trackbackexcerpt']");
            String trackbackText = locator.getText();
            if (trackbackText.length() > 0) {
                text.assertTextPresent(trackbackText,trackbackDesc);
                break;
            }
            Thread.sleep(500);
            waitCount++;
        }
        if (waitCount >= MAX_WAIT_COUNT) {
            fail("The trackback did not turn up in time.  Its most likely the mail queue failed to flush.  email and trackbacks must be enabled!");
        }
    }

    private String get127_0_0_1_BaseURL()
    {
        final JIRAEnvironmentData data = getEnvironmentData();
        StringBuffer sb = new StringBuffer();
        sb.append(data.getProperty("jira.protocol"));
        String hostName = data.getProperty("jira.host");
        // just in case some one use 127.0.0.1 flip the hostname
        if (hostName.equals("127.0.0.1")) {
            hostName = "localhost";
        } else {
            hostName = "127.0.0.1";
        }
        sb.append("://");
        sb.append(hostName);
        sb.append(":");
        sb.append(data.getProperty("jira.port"));
        sb.append(data.getProperty("jira.context"));
        return sb.toString();
    }
}
