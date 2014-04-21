package com.atlassian.jira.webtest.selenium.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import junit.framework.Test;


/**
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestIssueNavigatorCollapsing extends JiraSeleniumTest
{
    public static Test suite()
    {
        return suiteFor(TestIssueNavigatorCollapsing.class);
    }

    public void testCollapsing()
    {
        restoreData("TestViewIssueCollapsing.xml");

        getNavigator().gotoFindIssuesSimple();

        assertThat.elementNotPresent("jquery=#issuenav.lhc-collapsed");

        client.click("jquery=.toggle-lhc");

        assertThat.elementPresentByTimeout("jquery=#issuenav.lhc-collapsed");

        getNavigator().gotoFindIssues();
        assertThat.elementPresentByTimeout("jquery=#issuenav.lhc-collapsed",1500);

        client.click("jquery=.toggle-lhc");
        assertThat.elementNotPresentByTimeout("jquery=#issuenav.lhc-collapsed");
        getNavigator().gotoFindIssues();
        assertThat.elementNotPresentByTimeout("jquery=#issuenav.lhc-collapsed",1500);


        client.click("id=issue-filter-submit", true);
        client.click("id=filtersavenew", true);

        client.click("jquery=.toggle-lhc");
        assertThat.elementPresentByTimeout("jquery=#issuenav.lhc-collapsed");

        getNavigator().gotoFindIssues();
        assertThat.elementPresentByTimeout("jquery=#issuenav.lhc-collapsed",1500);

        client.click("jquery=.toggle-lhc");
        assertThat.elementNotPresentByTimeout("jquery=#issuenav.lhc-collapsed");

        client.click("id=filtersavenew", true);
        assertThat.elementNotPresentByTimeout("jquery=#issuenav.lhc-collapsed",1500);

    }
//  Selenium wont react to the key press   
//    public void testCollapsingViaShortcutKey()
//    {
//        restoreData("TestViewIssueCollapsing.xml");
//
//        getNavigator().gotoFindIssues();
//
//        assertThat.elementNotPresent("jquery=#issuenav.lhc-collapsed");
//
//        pressCollapseKey();
//
//        assertThat.elementPresentByTimeout("jquery=#issuenav.lhc-collapsed");
//
//        getNavigator().gotoFindIssues();
//        assertThat.elementPresentByTimeout("jquery=#issuenav.lhc-collapsed",1500);
//
//        pressCollapseKey();
//        assertThat.elementNotPresentByTimeout("jquery=#issuenav.lhc-collapsed");
//        getNavigator().gotoFindIssues();
//        assertThat.elementNotPresentByTimeout("jquery=#issuenav.lhc-collapsed",1500);
//
//
//        pressCollapseKey();
//        client.click("id=filtersavenew", true);
//
//        pressCollapseKey();
//        assertThat.elementPresentByTimeout("jquery=#issuenav.lhc-collapsed");
//
//        getNavigator().gotoFindIssues();
//        assertThat.elementPresentByTimeout("jquery=#issuenav.lhc-collapsed",1500);
//
//        pressCollapseKey();
//        assertThat.elementNotPresentByTimeout("jquery=#issuenav.lhc-collapsed");
//
//        client.click("id=filtersavenew", true);
//        assertThat.elementNotPresentByTimeout("jquery=#issuenav.lhc-collapsed",1500);
//
//    }

    private void pressCollapseKey()
    {
        client.seleniumKeyPress("css=body", "\\93");
    }

    public void testCollapsingSections()
    {
        restoreData("TestViewIssueCollapsing.xml");

        getNavigator().gotoFindIssuesSimple();

        assertThat.elementPresentByTimeout("jquery=#navigator-filter-subheading-issueattributes-group.collapsed",1500);
        assertThat.notVisibleByTimeout("id=navigator-filter-subheading-issueattributes",1500);

        client.click("css=#navigator-filter-subheading-issueattributes-group legend span");
        assertThat.visibleByTimeout("id=navigator-filter-subheading-issueattributes",1500);
        assertThat.elementNotPresentByTimeout("jquery=#navigator-filter-subheading-issueattributes-group.collapsed");
        assertThat.elementPresentByTimeout("jquery=#navigator-filter-subheading-issueattributes-group.expanded");

        client.click("css=#navigator-filter-subheading-issueattributes-group legend span");
        assertThat.notVisibleByTimeout("id=navigator-filter-subheading-issueattributes",1500);

        client.click("css=#navigator-filter-subheading-issueattributes-group legend span");
        assertThat.visibleByTimeout("id=navigator-filter-subheading-issueattributes",1500);

        getNavigator().gotoFindIssuesSimple();

        assertThat.visibleByTimeout("id=navigator-filter-subheading-issueattributes",1500);
        client.click("css=#navigator-filter-subheading-issueattributes-group legend span");
        assertThat.notVisibleByTimeout("id=navigator-filter-subheading-issueattributes",1500);

        client.click("css=#navigator-filter-subheading-issueattributes-group legend span");
        assertThat.visibleByTimeout("id=navigator-filter-subheading-issueattributes",1500);

        getNavigator().gotoFindIssuesSimple();

        assertThat.visibleByTimeout("id=navigator-filter-subheading-issueattributes",1500);

    }

}