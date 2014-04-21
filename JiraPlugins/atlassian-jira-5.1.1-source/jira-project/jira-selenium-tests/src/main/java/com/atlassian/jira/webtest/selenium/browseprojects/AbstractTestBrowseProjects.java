package com.atlassian.jira.webtest.selenium.browseprojects;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;

/**
 * @since v4.0
 */
public abstract class AbstractTestBrowseProjects extends JiraSeleniumTest
{
    protected static final Map<String, String> PANELS = new HashMap<String, String>();
    protected static final String COMPONENT_PREFIX = "component";
    protected static final String VERSION_PREFIX = "version";
    protected static final String DROP_DOWN_ID_SUFFIX = "-dropdown";
    protected static final String SUMMARY = "Summary";
    protected static final String ISSUES = "Issues";
    protected static final String ROAD_MAP = "Road Map";
    protected static final String POPULAR_ISSUES = "Popular Issues";
    protected static final String VERSIONS = "Versions";
    protected static final String COMPONENTS = "Components";
    protected static final String CHANGE_LOG = "Change Log";
    protected static final int LOAD_WAIT = 10000;
    
    static
    {
        PANELS.put(SUMMARY, "summary-panel-panel");
        PANELS.put(ISSUES, "issues-panel-panel");
        PANELS.put(ROAD_MAP, "roadmap-panel-panel");
        PANELS.put(POPULAR_ISSUES, "popularissues-panel-panel");
        PANELS.put(VERSIONS, "versions-panel-panel");
        PANELS.put(COMPONENTS, "components-panel-panel");
        PANELS.put(CHANGE_LOG, "changelog-panel-panel");
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestBrowseProjects.xml");
        getNavigator().gotoBrowseProject("homosapien");
    }

    protected void loggedOutRedirect(String controlXPATH, final String xsrfToken)
    {
        Window.openAndSelect(client, "", "LogOutWindow");
        getNavigator().logout(xsrfToken);
        Window.close(client, "LogOutWindow");
        client.click(controlXPATH, true);
        client.waitForPageToLoad(LOAD_WAIT);
        assertThat.textPresent("You must log in to access this page.");
    }

    /**
     * Finds element by Id an checks wether it is displayed
     *
     * @param Id - HTML id of dropdown
     * @return Whether or not the element is visible, by checking the css <em>display</em> property is not equal to <em>none</em>
     */
    protected boolean isVisible(String Id)
    {
        return "true".equals(client.getEval("dom=this.browserbot.getUserWindow().jQuery(\"#" + Id + ":visible\").length === 1"));
    }


    protected String getLinkHref(String CSSselector)
    {
        return client.getEval("dom=this.browserbot.getUserWindow().jQuery('" + CSSselector + "').attr('href')");
    }

    /**
     * Gets current browser location
     *
     * @return url location of window
     */
    protected String getWindowLocation ()
    {
        return client.getEval("this.browserbot.getUserWindow().location.href");
    }

    protected void goToComponentPage ()
    {
        loadAjaxTab(COMPONENTS);
        client.click("component_10000", true);
        client.waitForPageToLoad(LOAD_WAIT);
    }

    protected void goToVersionsPage ()
    {
        loadAjaxTab(VERSIONS);
        client.click("version_10011", true);
        client.waitForPageToLoad(LOAD_WAIT);
    }

    protected void goToPersonalRoadMapPage ()
    {
        loadAjaxTab(ROAD_MAP);
        client.click("browse-personal-roadmap", true);
        client.waitForPageToLoad(LOAD_WAIT);
    }
    /**
     * Clicks tabs and ensures everything is working asyncrously by checking the url for change
     *
     * @param panelAssertion - HTML id of tab being clicked
     * @param panelLinkId -
     */
    protected void loadAjaxTab (String panelAssertion, String panelLinkId)
    {
        String url = client.getAttribute("jQuery=#" + panelLinkId + " @href");
        // click tab
        client.mouseDown(panelLinkId);
        // this gets fired during a mousedown also, need to check the page doesn't reload
        client.click(panelLinkId, false);
        assertThat(tabLoaded(panelLinkId), byDefaultTimeout());
        assertHashURL(url, panelAssertion);
    }

    private TimedCondition tabLoaded(String tabId)
    {
        return IsPresentCondition.forContext(context()).locator(loadedTabContainerLocator(tabId))
                .defaultTimeout(LOAD_WAIT).build();
    }

    private String loadedTabContainerLocator(String tabId)
    {
        return "css=li.active.loaded #" + tabId;
    }


    /**
     * Clicks tabs and ensures everything is working asyncrously by checking the url for change
     *
     * @param panel - HTML id of tab being clicked
     */
    protected void loadAjaxTab (String panel)
    {
        loadAjaxTab(panel, PANELS.get(panel));
    }

    protected void assertIsTab(String panelAssertion)
    {
        if (PANELS.containsKey(panelAssertion)) {
            panelAssertion = "//h2[text()='" + panelAssertion + "']";
        }
        assertThat.elementPresentByTimeout(panelAssertion, LOAD_WAIT);
    }

    protected String getTriggerId(String panel)
    {
        return getTriggerId(panel, null);
    }

    protected String getTriggerId(String panel, String prefix)
    {
        if (prefix == null)
        {
            return PANELS.get(panel);
        }
        else
        {
            return prefix + "-" + PANELS.get(panel);
        }
    }

    private void assertHashURL(String url, String panelAssertion)
    {
        Pattern pattern = Pattern.compile(".*\\?(.*)");
        Matcher matcher = pattern.matcher(url);
        matcher.find();
        String locationID = matcher.group(1);
        // wait for tab to load by checking for heading
        assertThat.elementPresentByTimeout("project-tab", LOAD_WAIT);
        assertIsTab(panelAssertion);
        // ensure this was an async request by comparing the url for change
        String windowLocation = getWindowLocation();
        Pattern hashPattern = Pattern.compile("#" + locationID + "$");
        Matcher hashMatcher = hashPattern.matcher(windowLocation);
        if (!hashMatcher.find() || !hashMatcher.group().equals("#" + locationID)) {
            throw new RuntimeException ("Expected url hash to be '" + "#" + locationID +
                    "' but received '" + windowLocation);
        }
    }
}
