package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.DeleteIssueLinkDialog;
import com.atlassian.jira.webtest.selenium.framework.dialogs.SubmittableDialog;
import com.atlassian.jira.webtest.selenium.framework.model.CancelType;
import com.atlassian.jira.webtest.selenium.framework.model.SubmitType;

@WebTest({Category.SELENIUM_TEST })
public class TestDeleteIssueLink extends JiraSeleniumTest
{

    private static final String HSP_1 = "HSP-1";
    private static final String ISSUE_LINK_DELETE_ID = "del_10020_10010";

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestDeleteIssueLinkDialog.xml");
    }

    public void testDelete()
    {
        getNavigator().gotoIssue(HSP_1);
        SubmittableDialog deleteIssueDialog = new DeleteIssueLinkDialog(context(), ISSUE_LINK_DELETE_ID);
        deleteIssueDialog.open();
        deleteIssueDialog.cancel(CancelType.BY_ESCAPE);
        deleteIssueDialog.open();
        deleteIssueDialog.submit(SubmitType.BY_CLICK);
        assertThat.elementNotPresent(ISSUE_LINK_DELETE_ID);
    }

}
