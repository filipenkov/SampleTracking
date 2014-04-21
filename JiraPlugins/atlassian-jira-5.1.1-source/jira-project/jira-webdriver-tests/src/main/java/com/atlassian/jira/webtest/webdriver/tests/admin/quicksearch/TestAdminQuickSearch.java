package com.atlassian.jira.webtest.webdriver.tests.admin.quicksearch;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.Suggestion;
import com.atlassian.jira.pageobjects.dialogs.AdministrationSearchDialog;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.admin.user.UserBrowserPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigErrorPage;
import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers.containsSuggestion;
import static com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers.hasMainLabel;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.4
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.IE_INCOMPATIBLE })
@RestoreOnce ("xml/TestAdminQuickSearch.xml")
public class TestAdminQuickSearch extends BaseJiraWebTest
{
    // NOTE: this is read-only test that restores data once per whole test class for speed (@RestoreOnce)
    // DO NOT put any backdoor data modification operations here
    // just use another test class for that

    @Inject
    private PageBinder binder;

    @Test
    public void testNavigation()
    {
        jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, "HSP");

        AutoComplete quickSearch = getAdminQuickSearchFromHeader();

        List<Suggestion> suggestions = quickSearch.query("Projects").getSuggestions();
        assertEquals(4, suggestions.size());
        quickSearch.acceptUsingMouse(quickSearch.getActiveSuggestion());

        ViewProjectsPage projectsPage = binder.bind(ViewProjectsPage.class);

        projectsPage.isAt(); // assert we have loaded the correct page

        quickSearch = getAdminQuickSearchFromHeader();

        quickSearch.query("edit user profile");

        quickSearch.acceptUsingKeyboard(quickSearch.getActiveSuggestion());
        binder.bind(UserBrowserPage.class);
    }

    @Test
    public void testNoHeaderQuickSearchOnNonAdminPages()
    {
        jira.gotoLoginPage().loginAsSysAdmin(AdvancedSearch.class);

        assertFalse("Expected Admin Quick Search NOT to be on issuenavigator",
                getAdminQuickSearchFromHeader().isPresent());

        jira.goToViewIssue("HSP-1");

        assertFalse("Expected Admin Quick Search NOT to be on view issue page",
                getAdminQuickSearchFromHeader().isPresent());
    }

    @Test
    public void testQuickSearchShortcutFocusesInput()
    {
        ProjectSummaryPageTab config = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, "HSP");
        AutoComplete quickSearch = pageBinder.bind(JiraHeader.class).getAdminQuickSearch();
        assertTrue("Expected Admin quick search label to be visible", quickSearch.getLabel().isVisible());
        assertEquals("Administration Quick Search", quickSearch.getLabel().getText());
        config.execKeyboardShortcut("/");
        assertFalse("Expected Admin quick search label NOT to be visible", quickSearch.getLabel().isVisible());
    }

    @Test
    public void testQuickSearchFromDialog()
    {
        ProjectSummaryPageTab config = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, "HSP");
        config.execKeyboardShortcut("g", "g");
        AdministrationSearchDialog adminSearchDialog = binder.bind(AdministrationSearchDialog.class);
        AutoComplete quickSearch = adminSearchDialog.getAdminQuickSearch();
        quickSearch.query("Pro");
        waitUntil(quickSearch.getTimedSuggestions(), containsSuggestion("Projects"));
        waitUntil(quickSearch.getTimedSuggestions(), containsSuggestion("homosapien (HSP)", "Projects, Recent Projects"));
        quickSearch.acceptUsingKeyboard(quickSearch.getActiveSuggestion());
        // TODO matches by main label are not prioritized - JRADEV-6356
        binder.bind(ProjectSummaryPageTab.class, "HSP");
    }

    @Test
    public void testQuickSearchFromDialogUsingKeywords()
    {
        ProjectSummaryPageTab config = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, "HSP");
        config.execKeyboardShortcut("g", "g");
        AdministrationSearchDialog adminSearchDialog = binder.bind(AdministrationSearchDialog.class);
        AutoComplete quickSearch = adminSearchDialog.getAdminQuickSearch();
        quickSearch.query("edit user profile");
        quickSearch.acceptUsingKeyboard(quickSearch.getActiveSuggestion());
        binder.bind(UserBrowserPage.class);
    }

    @Test
    public void testQuickSearchDialogNotShownForNormalUser()
    {
        ProjectConfigErrorPage config = jira.gotoLoginPage().login("fred", "fred", ProjectConfigErrorPage.class, "HSP");
        config.execKeyboardShortcut("g", "g");
        if (binder.delayedBind(AdministrationSearchDialog.class).inject().get().isOpen())
        {
            fail("Dialog should not be opened for non-admin user");
        }
    }

    @Test
    public void shouldFindRecentProjectsByName()
    {
        final ProjectSummaryPageTab config = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, "HSP");
        final AutoComplete quicksearch = config.getHeader().getAdminQuickSearch();
        quicksearch.query("homosap");
        waitUntil(quicksearch.getTimedActiveSuggestion(), hasMainLabel("homosapien (HSP)"));
    }

    @Test
    public void shouldFindRecentProjectsByKey()
    {
        final ProjectSummaryPageTab config = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, "HSP");
        final AutoComplete quicksearch = config.getHeader().getAdminQuickSearch();
        quicksearch.query("HSP");
        waitUntil(quicksearch.getTimedActiveSuggestion(), hasMainLabel("homosapien (HSP)"));
    }

    @Test
    public void shouldFindByMainSectionName()
    {
        final ProjectSummaryPageTab config = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, "HSP");
        final AutoComplete quicksearch = config.getHeader().getAdminQuickSearch();
        quicksearch.query("Users");
        final TimedQuery<List<Suggestion>> suggestionsQuery = quicksearch.getTimedSuggestions();
        waitUntil(suggestionsQuery, containsSuggestion("Groups", "Users"));
        waitUntil(suggestionsQuery, containsSuggestion("Roles", "Users"));
        waitUntil(suggestionsQuery, containsSuggestion("User Preferences", "Users"));
        waitUntil(suggestionsQuery, containsSuggestion("Global Permissions", "Users"));
    }

    private AutoComplete getAdminQuickSearchFromHeader()
    {
        return pageBinder.bind(JiraHeader.class).getAdminQuickSearch();
    }
}
