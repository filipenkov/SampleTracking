package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.elements.query.Poller;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests for creating an issue using quick create
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore ("xml/blankprojects.xml")
public class TestViewIssue extends BaseJiraWebTest
{
    @Test
    public void testEnableDisableInlineEdit()
    {
        String key1 = backdoor.issues().createIssue("HSP", "xxx").key;
        String key2 = backdoor.issues().createIssue("HSP", "yyy").key;

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, key1);

        Poller.waitUntilTrue("Dude, where's my kickass?", viewIssuePage.isIssueTypeEditable());

        backdoor.applicationProperties().setOption("jira.issue.inline.edit.disabled", true);

        viewIssuePage = jira.goTo(ViewIssuePage.class, key2);
        Poller.waitUntilFalse("Kickass was disabled but the issue page still is editable!?", viewIssuePage.isIssueTypeEditable());
    }

    @Test
    public void testAddCommentFromAnchor()
    {
        String key1 = backdoor.issues().createIssue("HSP", "xxx").key;
        String key2 = backdoor.issues().createIssue("HSP", "yyy").key;

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        //comment form isnt visible normally
        ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, key1);
        assertFalse("add comment dialog is active", viewIssuePage.isAddCommentModuleActive());

        //check that add comment is only visible when anchor is there
        ViewIssuePage viewIssuePage2 = jira.goTo(ViewIssuePage.class, key2, "add-comment");
        assertTrue("add comment dialog is not active", viewIssuePage2.isAddCommentModuleActive());

    }
}

