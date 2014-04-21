package com.atlassian.jira.webtest.selenium.auidialog.comment;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.AddCommentDialog;
import junit.framework.Test;

/**
 * Selenium test for comment dialog. In other words, another reason for Selenium to blow;)
 *
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestCommentDialog extends AbstractAuiDialogTest
{
    private AddCommentDialog commentDialog;

    public static Test suite()
    {
        return suiteFor(TestCommentDialog.class);
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        commentDialog = new AddCommentDialog(context());
        restoreData("TestCommentDialog.xml");
    }

    public void testSubmitsCommentInPreview()
    {
        getNavigator().gotoIssue("HSP-1");
        commentDialog.openFromViewIssue();
        commentDialog.assertIsInputMode();
        commentDialog.insertComment("test");
        commentDialog.togglePreview();
        commentDialog.submit();
        assertDialogNotOpen();
        assertThat.visibleByTimeout("id=comment-10000", DROP_DOWN_WAIT);
        assertThat.elementContainsText("id=comment-10000", "test");
    }

}