package com.atlassian.jira.webtest.selenium.assignee;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.impl.selenium.form.DirtyFormCheck;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;


/**
 *
 *
 * @since v4.3
 */
@WebTest({Category.SELENIUM_TEST })
public class TestAssignFormDirtyWarning extends JiraSeleniumTest
{
    private final static String WARNING = "You have entered new data in this dialog. If you navigate away from this dialog without first saving your data, the changes will be lost. Click cancel to return to the dialog.";
    
    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestHintsInDialogs.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testDirtyFormCheck() throws Exception
    {
        getNavigator().gotoIssue("MKY-1");
        DirtyFormCheck check = new DirtyFormCheck(context(), "Test 1", new DirtyFormCheck.Setup.None(), new DirtyFormCheck.DirtyFormDescriptor("#assign-issue", "#jira", ".aui-dialog-content-ready", "#assign-issue-submit", "#assign-issue-cancel", "a.shortcut-tip-trigger", WARNING, "assignee", true));
        check.run();
    }

 }
