package com.atlassian.jira.webtest.selenium.browseprojects;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

import java.util.List;

/**
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //Pop-up not found - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestBrowseProjectNavigation extends AbstractTestBrowseProjects
{
    /**
     * Registers selenium suite
     *
     * @return Test
     */
    public static Test suite()
    {
        return suiteFor(TestBrowseProjectNavigation.class);
    }

    /**
     * Sets up selenium test and navigates to the "homeosapien" project
     */
    public void onSetUp()
    {
        log("setting up TestBrowseProjectNavigation");
        super.onSetUp();
        log("setting up TestBrowseProjectNavigation complete");
    }

    public void testProjectTabs() throws InterruptedException
    {
        navigatePanels(CollectionBuilder.newBuilder(ISSUES, ROAD_MAP, CHANGE_LOG, POPULAR_ISSUES, VERSIONS, COMPONENTS).asList());
        loggedOutRedirect(getTriggerId(SUMMARY), getXsrfToken());
    }

    public void testComponentTabs() throws InterruptedException
    {
        goToComponentPage();
        navigatePanels(CollectionBuilder.newBuilder(ISSUES, ROAD_MAP, CHANGE_LOG, POPULAR_ISSUES).asList(), COMPONENT_PREFIX);
        loggedOutRedirect(getTriggerId(SUMMARY, COMPONENT_PREFIX), getXsrfToken());
    }

    public void testVersionTabs() throws InterruptedException
    {
        goToVersionsPage();
        navigatePanels(CollectionBuilder.newBuilder(ISSUES, POPULAR_ISSUES).asList(), VERSION_PREFIX);
        loggedOutRedirect(getTriggerId(SUMMARY, VERSION_PREFIX), getXsrfToken());
    }

    /**
     * If server is down
     */
    public void testServerConnectionRedirect()
    {
        String contextPath = client.getEval("dom=selenium.browserbot.getCurrentWindow().contextPath");
        client.getEval("dom=selenium.browserbot.getCurrentWindow().jQuery('#" + PANELS.get(ISSUES) + "').attr('href','" + contextPath + "/givemea404')");
        client.click(PANELS.get(ISSUES), false);
        client.waitForPageToLoad(LOAD_WAIT);
        assertThat.textPresent("404");
    }

    public void testDeepLinking()
    {
        // test for user with javascript getting a non-ajax url, ajax navigation should work as normal
        getNavigator().gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project:roadmap-panel", true);
        // should not convert to hash onload
        if (!client.getLocation().contains("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project:roadmap-panel")) {
            throw new RuntimeException("Navigating to browse projects with a non-ajax url, should preserve that url "
                    + "struture, until another tab is clicked");
        }
        // should not append the hashed url, but rather replace the non-ajax url with a hashed version.
        loadAjaxTab(SUMMARY);
        // ajax version
        getNavigator().gotoFindIssues();
        waitFor(5000);
        getNavigator().gotoPage("/browse/HSP#selectedTab=com.atlassian.jira.plugin.system.project:roadmap-panel", true);
        assertThat.elementPresentByTimeout("browse-personal-roadmap", PAGE_LOAD_WAIT_TIME);
    }

    public void testLimitedPermissions() {
        getNavigator().logout(getXsrfToken());
        // as we a re logged out, can't use the page model
        getNavigator().gotoPage("secure/IssueNavigator.jspa", true);
        _assertWarningPanelPresent();
        getNavigator().gotoPage("secure/IssueNavigator!switchView.jspa?navType=advanced",true);
        _assertWarningPanelPresent();
    }

    private void _assertWarningPanelPresent()
    {
        assertThat.elementPresent("jquery=.aui-message.warning");
        assertThat.elementHasText("jquery=.aui-message.warning","You are not logged in");
    }

    private void paginatePanel () {
        loadAjaxTab("//a[@id='paging-upcoming']", "paging-all");
        loadAjaxTab("//a[@id='paging-all']", "paging-upcoming");
        client.goBack();
        assertIsTab("//a[@id='paging-upcoming']");
    }

    private void navigatePanels(List<String> panels) throws InterruptedException {
       navigatePanels(panels, null);

    }

    private void navigatePanels(List<String> panels, String panelPrefix) throws InterruptedException {
        for (String panel : panels)
        {
            if (client.isElementPresent("//a[@id='fragdueversions_more']"))
            {
                if (panelPrefix == null)
                {
                   client.click("fragdueversions_more", false);
                    assertIsTab(VERSIONS);
                }
                else
                {
                   client.click("fragdueversions_more", true);
                    assertIsTab(VERSIONS);
                    client.goBack();
                    client.waitForPageToLoad(LOAD_WAIT);
                }

            }
            loadAjaxTab(panel, getTriggerId(panel, panelPrefix));
            // this panel has pagination
            if (client.isElementPresent("//a[@id='paging-all']"))
            {
                paginatePanel();
            }
        }
        String lastTabURL = getWindowLocation();
        client.goBack();
        assertIsTab(panels.get(panels.size()-2));
        // navigate away from page
        client.click("find_link", true);
        // navigate back to browse projects with the hashed/ajax url
        client.open(lastTabURL);
        client.waitForPageToLoad(LOAD_WAIT);
        // verifies that deep linking and bookmarking will work
        assertIsTab(panels.get(panels.size()-1));
    }
}