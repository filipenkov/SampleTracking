package com.atlassian.jira.webtest.webdriver.tests.admin.generalconfiguration;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.Suggestion;
import com.atlassian.jira.pageobjects.dialogs.AdministrationSearchDialog;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.admin.configuration.EditGeneralConfigurationPage;
import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.jira.pageobjects.pages.admin.user.UserBrowserPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigErrorPage;
import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.junit.Before;
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
public class TestGeneralConfiguration extends BaseJiraWebTest
{
    @Before
    public void setUp()
    {
        backdoor.restoreBlankInstance();
    }

    @Test
    public void testInlineEditNotPresentIfKickassDisabled()
    {
        // Verify present when plugin enabled
        ViewGeneralConfigurationPage viewPage = jira.gotoLoginPage().loginAsSysAdmin(ViewGeneralConfigurationPage.class);
        assertTrue(viewPage.isInlineEditPresent());

        EditGeneralConfigurationPage editPage = viewPage.edit();
        assertTrue(editPage.isInlineEditPresent());

        // Verify absent when plugin disabled
        backdoor.plugins().disablePlugin("com.atlassian.jira.jira-issue-nav-plugin");

        viewPage = jira.gotoLoginPage().loginAsSysAdmin(ViewGeneralConfigurationPage.class);
        assertFalse(viewPage.isInlineEditPresent());

        editPage = viewPage.edit();
        assertFalse(editPage.isInlineEditPresent());
    }
}
