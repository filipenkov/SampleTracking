package com.atlassian.jira.webtest.webdriver.tests.websudo;

/**
 * @since v5.0
 */

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoBanner;
import com.atlassian.jira.pageobjects.config.EnableWebSudo;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.AdminSummaryPage;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoPage;
import com.atlassian.jira.pageobjects.pages.admin.ViewAttachmentsSettingsPage;
import com.atlassian.jira.pageobjects.project.DeleteProjectPage;
import com.atlassian.jira.pageobjects.project.screens.ScreensPageTab;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@WebTest ({ Category.WEBDRIVER_TEST })
public class TestWebSudo  extends BaseJiraWebTest
{
    private static final String PROJECT_KEY = "HSP";
    private String passwordForWebSudo;

    @Before
    public void setup()
    {
        backdoor.restoreBlankInstance();

        JiraLoginPage jiraLoginPage = jira.gotoLoginPage();
        passwordForWebSudo = jiraLoginPage.PASSWORD_ADMIN;
        jiraLoginPage.loginAsSysAdmin(DashboardPage.class);
    }

    private void triggerWebSudo()
    {
        jira.visitDelayed(ViewAttachmentsSettingsPage.class);
        JiraWebSudoPage websudoPage = pageBinder.bind(JiraWebSudoPage.class);

        assertTrue(websudoPage.isAt().now());

        ViewAttachmentsSettingsPage viewAttachmentSettings = websudoPage.confirm(passwordForWebSudo, ViewAttachmentsSettingsPage.class);

        assertTrue(viewAttachmentSettings.isAt().now());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoDoesNotRedirectToParameterReturnUrl()
    {
        jira.visitDelayed(DeleteProjectPage.class, 10000L);
        JiraWebSudoPage websudoPage = pageBinder.bind(JiraWebSudoPage.class);

        assertTrue(websudoPage.isAt().now());

        DeleteProjectPage deleteProjectPage = websudoPage.confirm(passwordForWebSudo, DeleteProjectPage.class);

        assertTrue(deleteProjectPage.isAt().now());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoLoginPageAppearsOnlyOnceForProtectedPages ()
    {
        triggerWebSudo();
        ViewAttachmentsSettingsPage viewAttachmentSettings = jira.visit(ViewAttachmentsSettingsPage.class);
        assertTrue(viewAttachmentSettings.isAt().now());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoLoginPageSkippedForNormalPages ()
    {
        AdminSummaryPage adminSummary = jira.visit(AdminSummaryPage.class);
        assertTrue(adminSummary.isAt().now());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoBannerDisappearsAfterDropOnNormalPages ()
    {
        triggerWebSudo();

        jira.visit(AdminSummaryPage.class);

        JiraWebSudoBanner webSudoBanner = pageBinder.bind(JiraWebSudoBanner.class);
        assertTrue(webSudoBanner.isShowing());
        assertTrue(webSudoBanner.hasNormalDropLink());
        assertFalse(webSudoBanner.hasProdectedDropLink());

        String oldLocation = jira.getTester().getDriver().getCurrentUrl();

        webSudoBanner.dropWebSudo(AdminSummaryPage.class);

        assertEquals("The old location and the current location should match.", oldLocation,
                jira.getTester().getDriver().getCurrentUrl());

        assertFalse(webSudoBanner.isShowing());

        jira.visit(DashboardPage.class);
        webSudoBanner = pageBinder.bind(JiraWebSudoBanner.class);

        assertFalse(webSudoBanner.isShowing());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoCancelRedirect ()
    {
        jira.visit(ProjectSummaryPageTab.class, "HSP");

        jira.visitDelayed(ViewAttachmentsSettingsPage.class);
        JiraWebSudoPage websudoPage = pageBinder.bind(JiraWebSudoPage.class);

        assertTrue(websudoPage.isAt().now());

        ProjectSummaryPageTab projectSummaryTab = websudoPage.cancel(ProjectSummaryPageTab.class, PROJECT_KEY);
        assertTrue(projectSummaryTab.isAt().now());

        ScreensPageTab screensPageTab = jira.visit(ScreensPageTab.class, PROJECT_KEY);
        websudoPage = screensPageTab.gotoSelectScheme(JiraWebSudoPage.class);

        assertTrue(websudoPage.isAt().now());

        screensPageTab = websudoPage.cancel(ScreensPageTab.class, PROJECT_KEY);

        assertTrue(screensPageTab.isAt().now());

        jira.logout();
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        AdminSummaryPage adminSummaryPage = pageBinder.navigateToAndBind(JiraWebSudoPage.class)
                .cancel(AdminSummaryPage.class);

        assertTrue(adminSummaryPage.isAt().now());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoBannerRedirectsAfterDropOnProtectedPages ()
    {
        triggerWebSudo();

        jira.visit(ViewAttachmentsSettingsPage.class);
        JiraWebSudoBanner webSudoBanner = pageBinder.bind(JiraWebSudoBanner.class);

        assertTrue(webSudoBanner.isShowing());
        assertTrue(webSudoBanner.hasProdectedDropLink());
        assertFalse(webSudoBanner.hasNormalDropLink());

        DashboardPage dashboard = webSudoBanner.dropWebSudo(DashboardPage.class);

        assertTrue(dashboard.isAt().now());

        webSudoBanner = pageBinder.bind(JiraWebSudoBanner.class);
        assertFalse(webSudoBanner.isShowing());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoDoesNotRedirectExternally()
    {
        jira.visit(JiraWebSudoPage.class, "http://google.com");

        JiraWebSudoPage jiraWebSudoPage = pageBinder.bind(JiraWebSudoPage.class);
        jiraWebSudoPage.authenticate(AdminSummaryPage.class);

        AdminSummaryPage adminSummaryPage = pageBinder.bind(AdminSummaryPage.class);
        assertTrue(adminSummaryPage.isAt().now());
    }
}
