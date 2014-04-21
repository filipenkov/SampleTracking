package com.atlassian.jira.webtest.selenium.websudo;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.page.WebSudoBanner;
import com.atlassian.jira.webtest.framework.page.WebSudoLoginPage;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertFalseByDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertTrueByDefaultTimeout;

/*
 * @since 4.3
 *
 */
@WebTest({Category.SELENIUM_TEST })
public class TestWebSudo extends JiraSeleniumTest
{
    private static final String LOGIN_CANCEL = "login-cancel";
    private static final String HSP = "homosapien";
    private static final String SCREEN_SCHEME_PAGE = "/secure/project/SelectIssueTypeScreenScheme!default.jspa?projectId=10000";
    final String PAGE_THAT_REQUIRES_WEBSUDO_URL = "secure/admin/ViewAttachmentSettings.jspa";
    final String PAGE_THAT_DOES_NOT_REQUIRE_WEBSUDO_URL = "secure/IssueNavigator.jspa";

    public void onSetUp()
    {
        super.onSetUp();
        restoreBlankInstance();

        // turn websudo on
        getNavigator().gotoPage("rest/func-test/1.0/websudo?enabled=true", true);
    }

    public void onTearDown()
    {
        // turn websudo off
        getNavigator().gotoPage("rest/func-test/1.0/websudo?enabled=false", true);
    }

    private void assertProtectedLinkIsPresent(WebSudoBanner webSudoBanner)
    {
        assertTrueByDefaultTimeout("Protected link should be present", webSudoBanner.protectedLinkIsPresent());
    }

    private void assertProtectedLinkIsNotPresent(WebSudoBanner webSudoBanner)
    {
        assertFalseByDefaultTimeout("Protected link should not be present", webSudoBanner.protectedLinkIsPresent());
    }

    private void assertNormalLinkIsPresent(WebSudoBanner webSudoBanner)
    {   
        assertTrueByDefaultTimeout("Normal link should be present", webSudoBanner.normalLinkIsPresent());
    }

    private void assertNormalLinkIsNotPresent(WebSudoBanner webSudoBanner)
    {
        assertFalseByDefaultTimeout("Normal link should not be present", webSudoBanner.normalLinkIsPresent());
    }

    private void assertBannerIsPresent(WebSudoBanner webSudoBanner)
    {
        assertTrueByDefaultTimeout("WebSudo banner should be present", webSudoBanner.isPresent());
    }

    private void assertBannerIsNotPresent(WebSudoBanner webSudoBanner)
    {
        assertFalseByDefaultTimeout("WebSudo banner should not be present", webSudoBanner.isPresent());
    }

    private void assertBannerIsVisible(WebSudoBanner webSudoBanner)
    {
        assertTrueByDefaultTimeout("WebSudo banner should be visible", webSudoBanner.isVisible());
    }

    private void assertBannerIsNotVisible(WebSudoBanner webSudoBanner)
    {
        assertFalseByDefaultTimeout("WebSudo banner should not be visible", webSudoBanner.isVisible());
    }

    public void triggerWebSudo()
    {
        getNavigator().gotoPage(PAGE_THAT_REQUIRES_WEBSUDO_URL, true);

        WebSudoLoginPage webSudoLoginPage = context().getPageObject(WebSudoLoginPage.class);
        assertTrue(webSudoLoginPage != null);

        assertTrueByDefaultTimeout(webSudoLoginPage.isReady());

        webSudoLoginPage.setPassword(ADMIN_PASSWORD);
        webSudoLoginPage.submit();

        String location = context().client().getLocation();
        assertTrue(location.contains(PAGE_THAT_REQUIRES_WEBSUDO_URL));
    }

    public void testWebSudoLoginPageSkippedForNormalPages ()
    {
        getNavigator().gotoPage(PAGE_THAT_DOES_NOT_REQUIRE_WEBSUDO_URL, true);

        WebSudoLoginPage webSudoLoginPage = context().getPageObject(WebSudoLoginPage.class);
        assertTrue(webSudoLoginPage != null);

        assertFalseByDefaultTimeout(webSudoLoginPage.isReady());
    }

    public void testWebSudoLoginPageAppearsOnlyOnceForProtectedPages ()
    {
        triggerWebSudo();

        String oldLocation = context().client().getLocation();

        getNavigator().gotoPage(PAGE_THAT_REQUIRES_WEBSUDO_URL, true);

        assertLocationNotChanged(oldLocation);
    }

    public void testWebSudoBannerDisappearsAfterDropOnNormalPages ()
    {
        triggerWebSudo();

        getNavigator().gotoPage(PAGE_THAT_DOES_NOT_REQUIRE_WEBSUDO_URL, true);

        WebSudoBanner webSudoBanner = context().getPageObject(WebSudoBanner.class);
        assertTrue(webSudoBanner != null);

        assertBannerIsPresent(webSudoBanner);
        assertBannerIsVisible(webSudoBanner);
        assertNormalLinkIsPresent(webSudoBanner);
        assertProtectedLinkIsNotPresent(webSudoBanner);

        String oldLocation = context().client().getLocation();

        webSudoBanner.dropWebSudo();

        assertLocationNotChanged(oldLocation);

        assertBannerIsPresent(webSudoBanner);
        assertBannerIsNotVisible(webSudoBanner);
        assertNormalLinkIsPresent(webSudoBanner);
        assertProtectedLinkIsNotPresent(webSudoBanner);
    }

    public void testWebSudoCancelRedirect ()
    {
        getNavigator().gotoPage("/plugins/servlet/project-config/HSP/summary", true);
        getNavigator().gotoPage(PAGE_THAT_REQUIRES_WEBSUDO_URL, true);
        client.click(LOGIN_CANCEL, true);
        assertOnSummaryPage(HSP);

        getNavigator().gotoPage("/plugins/servlet/project-config/HSP/screens", true);
        client.click("project-config-screens-scheme-change", true);
        client.click(LOGIN_CANCEL, true);
        assertOnScreensPage(HSP);

        getNewSessionWithWebSudo();
        getNavigator().gotoPage(SCREEN_SCHEME_PAGE, true);
        client.click(LOGIN_CANCEL, true);
        assertThat.elementContainsText("admin-page-heading", "Administration");
        
    }

    private void getNewSessionWithWebSudo ()
    {
        getNavigator().logout(getXsrfToken()).login("admin", "admin");
        getNavigator().gotoPage("rest/func-test/1.0/websudo?enabled=true", true);
    }

    private void assertOnScreensPage (String project)
    {
        assertThat.elementContainsText("project-config-header-name", project);
        assertThat.elementPresentByTimeout("css=.active-tab #view_project_screens_tab");
    }

    private void assertOnSummaryPage (String project)
    {
        assertThat.elementContainsText("project-config-header-name", project);
        assertThat.elementPresentByTimeout("css=.active-tab #view_project_summary_tab");
    }

    private void assertLocationNotChanged(String oldLocation)
    {
        final String newLocation = context().client().getLocation();
        
        assertTrue("Location should not have changed [oldLocation=" + oldLocation + ", newLocation=" + newLocation + "]",
                   newLocation.equals(oldLocation));
    }

    public void testWebSudoBannerRedirectsAfterDropOnProtectedPages ()
    {
        triggerWebSudo();

        getNavigator().gotoPage(PAGE_THAT_REQUIRES_WEBSUDO_URL, true);

        WebSudoBanner webSudoBanner = context().getPageObject(WebSudoBanner.class);
        assertTrue(webSudoBanner != null);
        assertBannerIsPresent(webSudoBanner);
        assertBannerIsVisible(webSudoBanner);
        assertProtectedLinkIsPresent(webSudoBanner);
        assertNormalLinkIsNotPresent(webSudoBanner);

        webSudoBanner.dropWebSudo();

        assertTrueByDefaultTimeout(globalPages().dashboard().isAt());
        assertBannerIsNotPresent(webSudoBanner);
    }
}
