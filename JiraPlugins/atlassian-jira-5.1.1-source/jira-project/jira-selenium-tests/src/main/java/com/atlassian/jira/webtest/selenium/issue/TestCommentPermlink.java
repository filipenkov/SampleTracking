package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

@SkipInBrowser(browsers={Browser.IE}) //Pop-up problem - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestCommentPermlink extends JiraSeleniumTest
{
    private static final int[] COMMENT_IDS = new int[] {10000, 10001, 10002, 10003, 10004};

    public static Test suite()
    {
        return suiteFor(TestCommentPermlink.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestCommentPermlink.xml");
    }

    // tests that when viewing an issue regularly, no comments are highlighted
    public void testNoCommentsHighlighted()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoIssue("HSP-1");

        for (final int commentId : COMMENT_IDS)
        {
            assertThat.elementNotPresent("jquery=#comment-" + commentId + ".focused");
        }
    }

    // tests that when a permlink to a comment is clicked, it becomes highlighted
    public void testCommentsHighlighted()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoIssue("HSP-1");

        for (final int commentId : COMMENT_IDS)
        {
            final String commentLocator = "xpath=//div[@id='comment-" + commentId + "']";
            final String commentPermalinkLocator = commentLocator + "//span[text()='Permalink']";

            assertThat.elementPresentByTimeout(commentPermalinkLocator);
            getNavigator().clickAndWaitForPageLoad(commentPermalinkLocator);

            assertThat.elementPresent("jquery=#comment-" + commentId + ".focused .verbose");
            assertThat.elementPresent("jquery=#comment-" + commentId + ".focused .concise");
        }
    }

    // tests that when a comment is edited, the user is returned to the View Issue screen with the comment highlighted
    public void testEditCommentAnchorsAndHighlights()
    {
        getNavigator().gotoIssue("HSP-1");

        // click edit link 'edit_comment_10000'
        getNavigator().clickAndWaitForPageLoad("edit_comment_10000");

        // save comment
        getNavigator().clickAndWaitForPageLoad("comment-edit-submit");

        // assert that comment is highlighted
        assertThat.elementPresent("jquery=#comment-10000.focused .verbose");

        assertTrue("Asserting that the location has an anchor", client.getLocation().contains("#comment-10000"));
    }

    public void testThatPermLinkExpandsActivity()
    {
        getNavigator().gotoIssue("HSP-1");
 
        // make sure  the activity section is collapsed
        getNavigator().collapseContentSection("activitymodule");

        // click the permlink
        client.click("jquery=a.icon-perma:first");
        client.waitForPageToLoad();
        assertThat.elementNotPresent("jquery=#activitymodule.collapsed");
        assertThat.elementPresent("jquery=#activitymodule");

        // now go back to borowse the issue
        getNavigator().gotoIssue("HSP-1");
        assertThat.elementPresent("jquery=#activitymodule.collapsed");
    }

}
