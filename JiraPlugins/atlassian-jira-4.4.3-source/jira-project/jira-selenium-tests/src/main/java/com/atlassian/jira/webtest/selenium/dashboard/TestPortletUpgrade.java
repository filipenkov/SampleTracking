package com.atlassian.jira.webtest.selenium.dashboard;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.driver.admin.plugins.PluginsManagement;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;

/**
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestPortletUpgrade extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;
    private static final String PORTLET_UPGRADE_PROJECTS_DASHBOARD = "/secure/Dashboard.jspa?selectPageId=10011";
    private static final String PORTLET_UPGRADE_FILTERS_DASHBOARD = "/secure/Dashboard.jspa?selectPageId=10020";
    private static final String PORTLET_UPGRADE_ISSUES_DASHBOARD = "/secure/Dashboard.jspa?selectPageId=10021";
    private static final String PORTLET_UPGRADE_OTHERS_DASHBOARD = "/secure/Dashboard.jspa?selectPageId=10030";
    private static final String PROJECTS = "//optgroup[@label='Projects']/option[@selected='selected']";
    private static final String SELECTION = "//select[@id='<field>']/option[@selected='selected']";
    private static final String PROJECT_OR_FILTER_NAME = "//span[@id='filter_projectOrFilterId_name']";
    private static final String FILTER_NAME = "//span[@id='filter_filterId_name']";
    private static final String GADGET_TITLE = "//h3[@id='gadget-<number>-title']";
    private static final String INPUT_BY_ID = "//input[@id='<id>' and @value='<value>']";
    private static final String INPUT_CHECKBOX = "//input[@id='<id>' and @value='<value>' and @checked='checked']";
    private static final String BAMBOO_PORTLET_MSG = "Error: Portlets have been replaced by gadgets in this release. "
            + "You need to replace this Bamboo portlet with the equivalent gadget and upgrade your Bamboo instance to "
            + "2.3.2 or later, if you haven't already done so.";

    private PluginsManagement pluginsManagement;
    private Plugins plugins;

    public static Test suite()
    {
        return suiteFor(TestPortletUpgrade.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestPortletUpgrade.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        pluginsManagement = new PluginsManagement(globalPages());
        pluginsManagement.goToPlugins();
        plugins = pluginsManagement.plugins();
        getNavigator().gotoHome();
    }

    public void testPortletUpgrade()
    {
        _testPortletUpgradeBugzillaEnterprise();
        _testPortletUpgradeProjects();
        _testPortletUpgradeFilters();
        _testPortletUpgradeIssues();
        _testPortletUpgradeOthers();
        _testPortletUpgradeBugzillaProfessional();
        _testPortletUpgradeTextEnabled();
    }

    private void _testPortletUpgradeBugzillaEnterprise()
    {
        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10050"), "Bugzilla Issue ID Search");
        clickConfigureButtonFor("10050");
        //Check project category selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "bugzillaUrl").replace("<value>", "testBugzilla")));

        client.selectWindow(null);
    }

    private void _testPortletUpgradeProjects()
    {
        //Load the PortletUpgradeProjects dashboard
        getNavigator().gotoPage(PORTLET_UPGRADE_PROJECTS_DASHBOARD, true);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10020"), "Projects");
        clickConfigureButtonFor("10020");
        //Check project category selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "projectsOrCategories").replace("<value>", "cat10000")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "viewType-1").replace("<value>", "brief")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "cols-0").replace("<value>", "single-col")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10021"), "Projects");
        clickConfigureButtonFor("10021");
        //Check project category selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "projectsOrCategories").replace("<value>", "cat10001")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "viewType-0").replace("<value>", "collapsed")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "cols-2").replace("<value>", "three-col")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10022"), "Projects");
        clickConfigureButtonFor("10022");
        //Check project selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "projectsOrCategories").replace("<value>", "10000")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "viewType-1").replace("<value>", "brief")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "cols-0").replace("<value>", "single-col")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10023"), "Admin");

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10024"), "Introduction");
        client.selectFrame("gadget-10024");
        //Check introduction message
        assertThat.elementContainsText("css=div.view", "Jira is in ur base killing your issuez");

        client.selectWindow(null);

        //Check gadget name        
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10025"), "Quick Links");

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10060"), "Projects");
        clickConfigureButtonFor("10060");
        //Check project category selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "projectsOrCategories").replace("<value>", "allprojects")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "viewType-1").replace("<value>", "brief")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "cols-0").replace("<value>", "single-col")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10061"), "Projects");
        clickConfigureButtonFor("10061");
        //Check project selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "projectsOrCategories").replace("<value>", "catallCategories")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "viewType-0").replace("<value>", "collapsed")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "cols-1").replace("<value>", "two-col")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10062"), "Projects");
        clickConfigureButtonFor("10062");
        //Check project category selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "projectsOrCategories").replace("<value>", "allprojects")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "viewType-0").replace("<value>", "collapsed")));
        assertThat.elementPresent((INPUT_CHECKBOX.replace("<id>", "cols-0").replace("<value>", "single-col")));

        client.selectWindow(null);
    }

    private void _testPortletUpgradeFilters()
    {
        //Load the PortletUpgradeFilters dashboard        
        getNavigator().gotoPage(PORTLET_UPGRADE_FILTERS_DASHBOARD, true);
        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10031"), "Issue Statistics");
        clickConfigureButtonFor("10031");
        //Check gadget selections
        assertThat.elementContainsText(PROJECT_OR_FILTER_NAME, "TPD");
        assertThat.elementContainsText(SELECTION.replace("<field>", "statType"), "Status");
        assertThat.elementContainsText(SELECTION.replace("<field>", "sortBy"), "Natural");
        assertThat.elementContainsText(SELECTION.replace("<field>", "sortDirection"), "Descending");
        assertThat.elementContainsText(SELECTION.replace("<field>", "includeResolvedIssues"), "Yes");

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10032"), "Issue Statistics");
        clickConfigureButtonFor("10032");
        //Check gadget selections
        assertThat.elementContainsText(PROJECT_OR_FILTER_NAME, "Env");
        assertThat.elementContainsText(SELECTION.replace("<field>", "statType"), "Priority");
        assertThat.elementContainsText(SELECTION.replace("<field>", "sortBy"), "Total");
        assertThat.elementContainsText(SELECTION.replace("<field>", "sortDirection"), "Ascending");

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10033"), "Favourite Filters");
        clickConfigureButtonFor("10033");
        //Check gadget selection
        assertThat.elementContainsText(SELECTION.replace("<field>", "showCounts"), "Yes");

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10034"), "Filter Results: Done");
        clickConfigureButtonFor("10034");
        //Check gadget selections
        assertThat.elementContainsText(FILTER_NAME, "Done");
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "num").replace("<value>", "6")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10035"), "Two Dimensional Filter Statistics: All filter");
        clickConfigureButtonFor("10035");
        //Check gadget selections
        assertThat.elementContainsText(FILTER_NAME, "All filter");
        assertThat.elementContainsText(SELECTION.replace("<field>", "xstattype"), "Project");
        assertThat.elementContainsText(SELECTION.replace("<field>", "ystattype"), "Issue Type");
        assertThat.elementContainsText(SELECTION.replace("<field>", "sortBy"), "Natural");
        assertThat.elementContainsText(SELECTION.replace("<field>", "sortDirection"), "Ascending");
        assertThat.elementContainsText(SELECTION.replace("<field>", "showTotals"), "No");
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "numberToShow").replace("<value>", "3")));

        client.selectWindow(null);
    }

    private void _testPortletUpgradeIssues()
    {
        //Load the PortletUpgradeIssues dashboard                
        getNavigator().gotoPage(PORTLET_UPGRADE_ISSUES_DASHBOARD, true);
        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10026"), "Assigned to Me");
        clickConfigureButtonFor("10026");
        //Check gadget selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "num").replace("<value>", "3")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10027"), "Issues in progress");
        clickConfigureButtonFor("10027");
        //Check gadget selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "num").replace("<value>", "9")));


        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10028"), "Watched Issues");
        clickConfigureButtonFor("10028");
        //Check gadget selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "num").replace("<value>", "6")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10029"), "Voted Issues");
        clickConfigureButtonFor("10029");
        //Check gadget selections
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "num").replace("<value>", "8")));
        assertThat.elementPresent("//input[@name='showTotalVotes' and @checked='checked']");
        assertThat.elementNotPresent("//input[@name='showResolved' and @checked='checked']");

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10030"), "Road Map: Next 31 Days");
        clickConfigureButtonFor("10030");
        //Check gadget selections
        assertThat.elementContainsText(PROJECTS, "All Projects");
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "days").replace("<value>", "31")));
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "num").replace("<value>", "9")));

        client.selectWindow(null);
    }

    private void _testPortletUpgradeOthers()
    {
        //Load the PortletUpgradeOthers dashboard
        getNavigator().gotoPage(PORTLET_UPGRADE_OTHERS_DASHBOARD, true);
        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10040"), "FishEye Recent Changesets");
        client.selectFrame("gadget-10040");
        assertThat.textPresent("There are problems with the current configuration for this gadget.");
        waitFor(1000);

        client.selectWindow(null);

        clickConfigureButtonFor("10040");
        //Check gadget selection
//        assertThat.elementContainsText(SELECTION.replace("<field>", "instance"), "https://atlaseye.atlassian.com/");
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "rep").replace("<value>", "testRepo")));
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "path").replace("<value>", "testPath")));
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "numberToShow").replace("<value>", "9")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10041"), "FishEye Charts");
        clickConfigureButtonFor("10041");
        //Check gadget selection
//        assertThat.elementContainsText(SELECTION.replace("<field>", "instance"), "https://atlaseye.atlassian.com/");
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "repository").replace("<value>", "repoTest")));
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "path").replace("<value>", "pathTest")));
        assertThat.elementContainsText(SELECTION.replace("<field>", "charttype"), "Line");
        assertThat.elementContainsText(SELECTION.replace("<field>", "stacktype"), "Subdirectory");
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "authors").replace("<value>", "Admin")));
//        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "height").replace("<value>", "100")));
//        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "width").replace("<value>", "80")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10042"), "Crucible Charts");
        clickConfigureButtonFor("10042");
        //Check gadget selection
//        assertThat.elementContainsText(SELECTION.replace("<field>", "instance"), "https://atlaseye.atlassian.com/");
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "project").replace("<value>", "cruc")));
        assertThat.elementContainsText(SELECTION.replace("<field>", "charttype"), "Open Review Volume");
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "days").replace("<value>", "15")));
//        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "height").replace("<value>", "80")));
//        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "width").replace("<value>", "100")));

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10043"), "Bamboo Status");
        client.selectFrame("gadget-10043");
        waitFor(3000);
        assertThat.elementContainsText("//div[@id='portlet-content']/div", BAMBOO_PORTLET_MSG);

        client.selectWindow(null);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10044"), "Bamboo Plan Summary");
        client.selectFrame("gadget-10044");
        waitFor(3000);
        assertThat.elementContainsText("//div[@id='portlet-content']/div", BAMBOO_PORTLET_MSG);

        client.selectWindow(null);
    }

    private void _testPortletUpgradeBugzillaProfessional()
    {
        //restore data to upgrade from 3.13 professional
        restoreData("TestPortletUpgradeProfessional.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10015"), "Bugzilla Issue ID Search");
        clickConfigureButtonFor("10015");
        //Check project category selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "bugzillaUrl").replace("<value>", "testBugzilla")));

        client.selectWindow(null);
    }

    private void _testPortletUpgradeTextEnabled()
    {
        //restore data with Text portlet enabled
        restoreData("TestPortletUpgradeWithText.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        pluginsManagement.goToPlugins();
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.jira.plugin.system.portlets"), byDefaultTimeout());
        assertThat(pluginsManagement.systemPluginExistsAndEnabled("com.atlassian.jira.gadgets"), byDefaultTimeout());

        //Load the PortletUpgradeOthers dashboard
        getNavigator().gotoPage(PORTLET_UPGRADE_OTHERS_DASHBOARD, true);
        //Check gadget name
        assertThat.elementContainsText(GADGET_TITLE.replace("<number>", "10045"), "changedTitle");
        clickConfigureButtonFor("10045");
        //Check gadget selection
        assertThat.elementPresent((INPUT_BY_ID.replace("<id>", "title").replace("<value>", "changedTitle")));
        assertThat.elementContainsText("//textarea[@id='html']", "<em><strong>Text gadget is enabled!</strong></em>");

        client.selectWindow(null);
    }

    private boolean clickConfigureButtonFor(final String gadgetId)
    {
        // selects parent window
        getSeleniumClient().selectFrame("relative=top");

        // save current window
        String configButtonLocator = "css=#gadget-" + gadgetId + "-renderbox .configure a";

        if (client.isElementPresent(configButtonLocator))
        {
            client.click(configButtonLocator);
            client.selectFrame("gadget-" + gadgetId);
            assertThat.visibleByTimeout("//form[contains(@class, \"aui\")]", 10000);
            return true;
        }
        client.selectFrame("gadget-" + gadgetId);
        return false;
    }


}
