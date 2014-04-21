package com.atlassian.jira.webtest.selenium.favourites;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestIssueNavigatorFavourites extends JiraSeleniumTest
{
    private static final String FAV_IMG_10001 = "10001";
    private static final String FAV_LINK_ID_PREFIX = "fav_a_nav_SearchRequest_";

    public static Test suite()
    {
        return suiteFor(TestIssueNavigatorFavourites.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestIssueNavigatorFavourites.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testPublicFilter()
    {
        // logged out user
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10001", true);
        assertThat.elementNotPresent(FAV_IMG_10001);


        // Can see filter but not owner
        getNavigator().login("user", "user");
        getNavigator().gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10001", true);
        assertNotSet(FAV_LINK_ID_PREFIX, FAV_IMG_10001);

        toggleFavourite(FAV_IMG_10001);
        assertSet(FAV_LINK_ID_PREFIX, FAV_IMG_10001);

        getNavigator().gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10001", true);

        assertSet(FAV_LINK_ID_PREFIX, FAV_IMG_10001);

        // Is the owner of the filter
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        getNavigator().gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10001", true);
        toggleFavourite(FAV_IMG_10001);
        assertNotSet(FAV_LINK_ID_PREFIX, FAV_IMG_10001);

        getNavigator().gotoPage("secure/IssueNavigator.jspa?mode=show&requestId=10001", true);
        assertNotSet(FAV_LINK_ID_PREFIX, FAV_IMG_10001);

        toggleFavourite(FAV_IMG_10001);
        assertSet(FAV_LINK_ID_PREFIX, FAV_IMG_10001);

        getNavigator().gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10001", true);
        assertSet(FAV_LINK_ID_PREFIX, FAV_IMG_10001);

        getNavigator().gotoPage("secure/ManageFilters.jspa", true);
        assertSet("fav_a_mf_favourites_SearchRequest_", FAV_IMG_10001);

        getNavigator().gotoPage("secure/EditFilter!default.jspa", true);
        assertSet("fav_a_favourite", "");

        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        getNavigator().gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10001", true);
        toggleFavourite(FAV_IMG_10001);

        getNavigator().gotoPage("secure/EditFilter!default.jspa", true);
        assertNotSet("fav_a_favourite", "");
    }

    private void toggleFavourite(final String element)
    {
        client.click(FAV_LINK_ID_PREFIX + element);
        waitFor(2000);
    }

    private void assertNotSet(final String elemPrefix, final String elementId)
    {
        String element = elemPrefix + elementId;
        assertThat.attributeContainsValue(element, "class", "disabled");
        assertThat.attributeDoesntContainValue(element, "class", "enabled");
    }

    private void assertSet(final String elemPrefix, final String elementId)
    {
        String element = elemPrefix + elementId;
        assertThat.attributeContainsValue(element, "class", "enabled");
        assertThat.attributeDoesntContainValue(element, "class", "disabled");
    }
}