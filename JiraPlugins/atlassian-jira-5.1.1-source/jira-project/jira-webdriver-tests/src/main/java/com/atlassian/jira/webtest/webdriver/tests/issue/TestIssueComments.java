package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.AddCommentSection;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Comment;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.openqa.selenium.Keys;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test adding comments on the view issue page
 *
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestIssueComments extends BaseJiraWebTest
{
    private static final String SHORT_COMMENT = "all ok";
    private static final String TOO_LONG_COMMENT = "This is too long comment";

    /**
     * JRADEV-8225: adding a mention clears the rest of the line
     */
    @Test
    public void testMentionDoesNotClearLine()
    {
        backdoor.restoreBlankInstance();
        IssueCreateResponse issueCreateResponse = backdoor.issues().createIssue(10001L, "Monkeys everywhere :(");

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        ViewIssuePage viewIssuePage = jira.goToViewIssue(issueCreateResponse.key());
        final AddCommentSection commentSection = viewIssuePage.comment();
        final String line1 = "This is the remainder of the line\n";
        final String line2 = "This is the next line";
        commentSection.typeComment(line1 + line2).typeComment(Keys.UP);
        //go to the beginning of the line. Keys.HOME doesn't work on all platforms :(
        for (int i = 0; i < line1.length(); i++)
        {
            commentSection.typeComment(Keys.LEFT);
        }
        String comment = commentSection.typeComment("@fred").selectMention("fred").getComment();

        assertEquals(comment, "[~fred]" + line1 + line2);

        //add the comment here to make sure the next test that runs avoids the dirty form warning.
        commentSection.addAndWait();
    }

    @Test
    public void testCommentExceedsLimit()
    {
        backdoor.restoreBlankInstance();
        backdoor.advancedSettings().setTextFieldCharacterLengthLimit(10);
        IssueCreateResponse issueCreateResponse = backdoor.issues().createIssue(10001L, "Monkeys everywhere :(");
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        final AddCommentSection commentSection = jira.goToViewIssue(issueCreateResponse.key()).comment();
        final String errors = commentSection.typeComment(TOO_LONG_COMMENT).addWithErrors();
        assertEquals("The entered text is too long. It exceeds the allowed limit of 10 characters.", errors);
        commentSection.closeErrors().cancel();
    }

    @Test
    public void testCommentShorterThanLimit()
    {
        backdoor.restoreBlankInstance();
        backdoor.advancedSettings().setTextFieldCharacterLengthLimit(10);
        IssueCreateResponse issueCreateResponse = backdoor.issues().createIssue(10001L, "Monkeys everywhere :(");

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        final AddCommentSection commentSection = jira.goToViewIssue(issueCreateResponse.key()).comment();
        commentSection.typeComment(SHORT_COMMENT).addAndWait();

        final List<Comment> comments = backdoor.issues().getIssue(issueCreateResponse.key()).getComments();
        final Comment comment = Iterables.getOnlyElement(comments);
        assertEquals(SHORT_COMMENT, comment.body);
    }

}
