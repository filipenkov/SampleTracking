package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.dialogs.ShareDialog;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the new sharing inline dialog on both the view issue page and issue navigator.  This is not an end-to end test
 * since we already have separate e-mail funct tests for this functionality. Instead this test only ensures the
 * front-end controls are working correctly.
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore ("xml/blankprojects.xml")
public class TestShareIssueAndFilter extends BaseJiraWebTest
{
    @Test
    public void testShareDialogOnViewIssue()
    {
        final String issueKey = backdoor.issues().createIssue(10000, "A first test issue").key();

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue(issueKey);

        ShareDialog dialog = pageBinder.bind(ShareDialog.class);
        assertFalse(dialog.isTriggerPresent());

        backdoor.mailServers().addSmtpServer(1212);
        jira.goToViewIssue(issueKey);
        dialog = pageBinder.bind(ShareDialog.class);
        assertTrue(dialog.isTriggerPresent());

        dialog.open().
                addRecipient("admin").
                addRecipient("admin@example.com");

        assertEquals(CollectionBuilder.newBuilder("admin@example.com").asList(), dialog.getEmailRecipients());
        assertEquals(CollectionBuilder.newBuilder("admin").asList(), dialog.getRecipients());

        dialog.submit();
    }
    
    @Test
    public void testShareDialogWithKeyboard()
    {
        final String issueKey = backdoor.issues().createIssue(10000, "A first test issue").key();
        backdoor.mailServers().addSmtpServer(1212);

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue(issueKey);
        ShareDialog dialog = pageBinder.bind(ShareDialog.class);
        assertTrue(dialog.isTriggerPresent());

        dialog.openViaKeyboardShortcut().
                addRecipient("admin").
                addRecipient("admin@example.com").
                removeRecipient("admin");

        assertEquals(CollectionBuilder.newBuilder("admin@example.com").asList(), dialog.getEmailRecipients());
        assertEquals(Collections.emptyList(), dialog.getRecipients());

        dialog.submit();
    }

    @Test
    public void testShareDialogNoBrowsePermission()
    {
        backdoor.mailServers().addSmtpServer(1212);
        final String issueKey = backdoor.issues().createIssue(10000, "A first test issue").key();

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue(issueKey);
        ShareDialog dialog = pageBinder.bind(ShareDialog.class);
        assertTrue(dialog.isTriggerPresent());

        backdoor.permissions().removeGlobalPermission(27, "jira-developers");

        jira.goToViewIssue(issueKey);
        dialog = pageBinder.bind(ShareDialog.class);
        assertFalse(dialog.isTriggerPresent());
    }

    @Test
    public void testShareDialogOnIssueNav()
    {
        backdoor.issues().createIssue(10000, "A first test issue").key();

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        AdvancedSearch advancedSearch = pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("project = \"HSP\"")
                .submit();

        assertEquals(1, advancedSearch.getResults().getTotalCount());

        ShareDialog dialog = pageBinder.bind(ShareDialog.class);
        assertFalse(dialog.isTriggerPresent());

        backdoor.mailServers().addSmtpServer(1212);

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("project = \"HSP\"")
                .submit();

        dialog = pageBinder.bind(ShareDialog.class);
        assertTrue(dialog.isTriggerPresent());

        dialog.open().
                addRecipient("admin").
                addRecipient("admin@example.com");

        assertEquals(CollectionBuilder.newBuilder("admin@example.com").asList(), dialog.getEmailRecipients());
        assertEquals(CollectionBuilder.newBuilder("admin").asList(), dialog.getRecipients());

        dialog.submit();

        //empty query should also show share link
        pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("project = \"MKY\"")
                .submit();

        dialog = pageBinder.bind(ShareDialog.class);
        assertTrue(dialog.isTriggerPresent());
    }
}
