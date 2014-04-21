package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

@SkipInBrowser (browsers = { Browser.IE }) //Element not found - Responsibility: JIRA Team
@WebTest ({ Category.SELENIUM_TEST })
public class TestOpsBar extends JiraSeleniumTest
{
    public static Test suite()
    {
        return suiteFor(TestOpsBar.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestOpsBar.xml");
        backdoor.plugins().disablePlugin("com.atlassian.jira.jira-issue-nav-plugin");
    }

    public void testOpsBarScrolling()
    {
        getNavigator().gotoIssue("HSP-1");

//        assertThat.elementPresent("jquery=div.command-bar");
        assertThat.elementNotPresent("jquery=#stalker.detached");
        final Number origBar = client.getElementPositionTop("jquery=#stalker");

        assertTrue(origBar.intValue() > 0);
        client.runScript("window.scrollBy(0,400);");

        assertThat.elementPresentByTimeout("jquery=#stalker.detached");
        Number newTop = client.getElementPositionTop("jquery=#stalker.detached");

        assertTrue(newTop.intValue() == 0);

        client.runScript("window.scrollBy(0,200);");

        assertThat.elementPresentByTimeout("jquery=#stalker.detached");
        newTop = client.getElementPositionTop("jquery=#stalker.detached");

        assertTrue(newTop.intValue() == 0);

        client.runScript("window.scrollBy(0,200);");

        assertThat.elementPresentByTimeout("jquery=#stalker.detached");
        newTop = client.getElementPositionTop("jquery=#stalker.detached");

        assertTrue(newTop.intValue() == 0);

        client.runScript("window.scrollTo(0,0);");

        assertThat.elementNotPresentByTimeout("jquery=#stalker.detached");
        final Number newOrigBar = client.getElementPositionTop("jquery=#stalker");
        assertTrue(origBar.intValue() == newOrigBar.intValue());

    }

    public void testAnchorLinks()
    {
        getNavigator().gotoPage("/browse/HSP-1?focusedCommentId=10000&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-10000", true);
        assertThatElementIsBelowOpsBar("#comment-10000");
    }

    private void assertThatElementIsBelowOpsBar(String jQueryElemSelector)
    {
        Number commentYOffset = client.getElementPositionTop("jquery=" + jQueryElemSelector);
        Number scrollPosition = Integer.parseInt(client.getEval("this.browserbot.getCurrentWindow().jQuery('html,body').scrollTop()"), 10);
        Number stalkerHeight = client.getElementHeight("jquery=#stalker.detached");
        if (scrollPosition.intValue() + stalkerHeight.intValue() > commentYOffset.intValue())
        {
            throw new RuntimeException("Expected scroll position to be anchor element top position PLUS stalker bar height");
        }
    }

    public void testOpsBarControls()
    {
        getNavigator().gotoIssue("HSP-1");
        assertThat.elementPresent("jquery=div.command-bar");
        assertThat.elementPresent("edit-issue");
        assertThat.elementNotPresent("issue-edit");
        client.click("jquery=#opsbar-operations_more:first");
        assertThat.visibleByTimeout("jquery=#delete-issue");
    }

    public void testSaveComment()
    {
        getNavigator().gotoIssue("HSP-1");
        client.click("comment-issue");
        assertThat.elementPresentByTimeout("jquery=#stalker.action");
        client.typeWithFullKeyEvents("comment", "This is test comment");
        client.click("issue-comment-add-submit", true);
        client.waitForPageToLoad();
        client.getEval("window.jQuery(window).scroll()"); // Firefox 3.5 (which selenium runs in - needs this)
        assertThat.elementPresentByTimeout("jquery=#stalker.detached");
    }

}
