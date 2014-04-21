package com.atlassian.jira.webtest.selenium.browseprojects;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Assert;
import junit.framework.Test;

import java.util.List;

/**
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //JS Errors - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestBrowseProjectControls extends AbstractTestBrowseProjects
{
    private static final List<String>
            PROJECT_DROPDOWNS = CollectionBuilder.newBuilder("reports", "filters", "more").asList(),
            COMPONENT_DROPDOWNS = CollectionBuilder.newBuilder("filters").asList(),
            VERSION_DROPDOWNS = CollectionBuilder.newBuilder("filters").asList();

    private static final String
            ACCORDION_FOLLOW_LINK = ".versionBanner-header a.versionBanner-name",
            ACCORDION_FOLLOW_LINK_XPATH = "//div[@class='versionBanner-header']/a[@class='versionBanner-name']",
            ACCORDION_EXPANDED_XPATH = "//ul[@class='versionBannerList']/li[1][contains(@class,'active expanded')]",
            ACCORDION_CONTENT_XPATH = "//ul[@class='versionBannerList']/li[1]/div[@class='versionBanner-content']",
            ACCORDION_HEADER_XPATH = "//ul[@class='versionBannerList']/li[1]/div[@class='versionBanner-header']",
            BODY_XPATH = "//body";
    /**
     * Registers selenium suite
     *
     * @return Test
     */
    public static Test suite()
    {
        return suiteFor(TestBrowseProjectControls.class);
    }

    /**
     * Sets up selenium test and navigates to the "homeosapien" project
     */
    public void onSetUp()
    {
        log("setting up TestBrowseProjectControls");
        super.onSetUp();
        log("setting up TestBrowseProjectControls complete");
    }

    public void testBrowseProjectControls()
    {
        loadAjaxTab(SUMMARY);
        showDropdowns(PROJECT_DROPDOWNS);
        loadAjaxTab(ROAD_MAP);
        toogleExpandoVisibility();
        loadAjaxTab(CHANGE_LOG);
        toogleExpandoVisibility();
        loadAjaxTab(SUMMARY);
        showDropdowns(PROJECT_DROPDOWNS);
        loadAjaxTab(ROAD_MAP);
        toogleExpandoVisibility();
        loggedOutRedirect(ACCORDION_HEADER_XPATH, getXsrfToken());
    }

    public void testBrowseVersionControls()
    {
        goToVersionsPage();
        loadAjaxTab(SUMMARY, getTriggerId(SUMMARY, VERSION_PREFIX));
        showDropdowns(VERSION_DROPDOWNS);
        loadAjaxTab(ISSUES, getTriggerId(ISSUES, VERSION_PREFIX));
        loadAjaxTab(SUMMARY, getTriggerId(SUMMARY, VERSION_PREFIX));
        showDropdowns(VERSION_DROPDOWNS);
    }

    public void testBrowseComponentControls()
    {
        goToComponentPage();
        loadAjaxTab(SUMMARY, getTriggerId(SUMMARY, COMPONENT_PREFIX));
        showDropdowns(COMPONENT_DROPDOWNS);
        loadAjaxTab(ROAD_MAP, getTriggerId(ROAD_MAP, COMPONENT_PREFIX));
        toogleExpandoVisibility();
        loadAjaxTab(CHANGE_LOG, getTriggerId(CHANGE_LOG, COMPONENT_PREFIX));
        toogleExpandoVisibility();
        loadAjaxTab(SUMMARY, getTriggerId(SUMMARY, COMPONENT_PREFIX));
        showDropdowns(COMPONENT_DROPDOWNS);
        loadAjaxTab(ROAD_MAP, getTriggerId(ROAD_MAP, COMPONENT_PREFIX));
        toogleExpandoVisibility();
    }

    public void testPersonalRoadMapControls()
    {
        goToPersonalRoadMapPage();
        toogleExpandoVisibility();
        loggedOutRedirect(ACCORDION_HEADER_XPATH, getXsrfToken());
    }

    private void validateExpandoAnchorClicks()
    {
        String targetURI = getLinkHref(ACCORDION_FOLLOW_LINK);
        client.click(ACCORDION_FOLLOW_LINK_XPATH, true);
        client.waitForPageToLoad(LOAD_WAIT);
        if (client.getLocation().equals(targetURI))
        {
            throw new RuntimeException("Expected browser location to be " + targetURI + " but recieved " + client.getLocation());
        }
        client.goBack();
        client.waitForPageToLoad(LOAD_WAIT);
    }

    private void toogleExpandoVisibility()
    {
        openAndContractExpando();
        validateExpandoAnchorClicks();
    }
    
    private void openAndContractExpando()
    {
        client.click(ACCORDION_HEADER_XPATH, false);

        // check that the fragment loaded
        assertThat.elementPresentByTimeout(ACCORDION_CONTENT_XPATH, LOAD_WAIT);
        assertThat.elementPresentByTimeout(ACCORDION_EXPANDED_XPATH, LOAD_WAIT);
        // check that the content area actually expanded
        int accordionHeight = client.getElementHeight(ACCORDION_CONTENT_XPATH).intValue();
        if (accordionHeight < 10)
        {
            throw new RuntimeException("Expected an element height greater then 10 but recieved " + accordionHeight);
        }
        // close it
        client.click(ACCORDION_HEADER_XPATH, false);
        // check that the content is hidden
        assertThat.elementNotPresentByTimeout(ACCORDION_EXPANDED_XPATH, LOAD_WAIT);
        waitFor(2000);
        accordionHeight = client.getElementHeight(ACCORDION_CONTENT_XPATH).intValue();
        // check that the content area actually contacted
        Assert.assertEquals("Element height", 0, accordionHeight);
    }

    private void showDropdowns(List<String> dropdowns)
    {
        for (String dropdown : dropdowns)
        {
            showDropdown(dropdown);
        }
    }

    /**
     * Clicks on button with supplied Id, then checks that the associated dropdown element becomes visible
     *
     * @param buttonId - HTML id of button
     */
    private void showDropdown (String buttonId)
    {
        client.click(buttonId, false);
        assertTrue(this.isVisible(buttonId + DROP_DOWN_ID_SUFFIX));
        client.click(BODY_XPATH, false);
        assertFalse(this.isVisible(buttonId + DROP_DOWN_ID_SUFFIX));
    }
}