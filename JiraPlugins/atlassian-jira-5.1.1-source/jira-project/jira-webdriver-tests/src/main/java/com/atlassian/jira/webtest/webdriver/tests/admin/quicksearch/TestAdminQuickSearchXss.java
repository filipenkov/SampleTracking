package com.atlassian.jira.webtest.webdriver.tests.admin.quicksearch;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import static com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers.hasMainLabel;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;

/**
 * Test Admin quicksearch against XSS vulerabilities.
 *
 * @since v5.0
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.ADMINISTRATION })
public class TestAdminQuickSearchXss extends BaseJiraWebTest
{
    @Test
    @Restore ("xml/TestAdminQuickSearch.xml")
    public void shouldNotGetXssAttackWhenFindingRecentProjectsByKey()
    {
        // Changing the window location means that waiting for the quicksearch result will fail
        backdoor.project().addProject("<script>window.location = '/';</script>", "XSS", "admin");

        final ProjectSummaryPageTab config = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, "XSS");
        final AutoComplete quicksearch = config.getHeader().getAdminQuickSearch();
        quicksearch.query("XSS");
        waitUntil(quicksearch.getTimedActiveSuggestion(), hasMainLabel("<script>window.location = '/';</script> (XSS)"));
    }

}
