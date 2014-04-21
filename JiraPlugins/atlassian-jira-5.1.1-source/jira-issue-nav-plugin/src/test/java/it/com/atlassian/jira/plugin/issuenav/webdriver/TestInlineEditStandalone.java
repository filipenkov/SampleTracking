package it.com.atlassian.jira.plugin.issuenav.webdriver;

import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailComponent;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.jira.plugin.issuenav.pageobjects.util.KickassLoginUtil;
import org.junit.Test;

public class TestInlineEditStandalone extends TestInlineEdit
{
    @Test
    public void testClone()
    {
        // TODO: this should be in JIRA core
        IssueDetailComponent issue = goToIssue("XSS-17");
        IssueDetailComponent newIssue = issue.opsBar().cloneIssueExpectingKey("XSS-18");

        assertEquals("XSS-18", newIssue.getIssueKey());
    }

    @Test
    public void testWindowTitleUpdates()
    {
        IssueDetailComponent issue = goToIssue("XSS-17");
        issue.summary().editSaveWait("Window Title Updates");
        assertTrue(issue.getWindowTitle().contains("Window Title Updates"));
    }

   @Override
    protected IssueDetailComponent goToIssue(final String issueKey, String username, String password, String query)
    {
        IssueDetailPage issueDetailPage = KickassLoginUtil.kickAssWith(product, username, password, IssueDetailPage.class, issueKey);
        return issueDetailPage.details();
    }
}