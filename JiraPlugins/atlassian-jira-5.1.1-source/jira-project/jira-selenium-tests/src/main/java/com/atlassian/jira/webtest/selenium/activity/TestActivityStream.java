package com.atlassian.jira.webtest.selenium.activity;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

@WebTest({Category.SELENIUM_TEST })
public class TestActivityStream extends JiraSeleniumTest
{
    private static final int TIMEOUT = 50000;

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestActivityStream.xml");
    }

    //JRA-18749
    public void testDeletedStatusWorks()
    {
        getNavigator().dashboard("10040").view();
        assertActivity("gadget-10060");

        getNavigator().gotoPage("/browse/HSP-1?page=com.atlassian.streams.streams-jira-plugin%3Aactivity-stream-issue-tab", true);
        assertActivity("gadget-0");

        getNavigator().gotoPage("secure/ViewProfile.jspa?name=admin", true);
        assertActivity("gadget-1");
    }

    private void assertActivity(String iframe)
    {
        client.selectFrame(iframe);

        assertThat.elementPresentByTimeout("jquery=div.activity-item-summary:first", TIMEOUT);
        String text = client.getText("jquery=div.activity-item-summary:first");
        text = text.replaceAll("\\s+", " ");
        assertTrue(text.contains("Administrator commented on HSP-1 - Stuff"));
        assertThat.textPresentByTimeout("Phone screen interview procedure.", TIMEOUT);

        //Some Status has been deleted.  The text should be coming from the change history of the issue.
        assertThat.textPresentByTimeout("changed the status to Some status", TIMEOUT);
        client.selectWindow(null);
    }
}
