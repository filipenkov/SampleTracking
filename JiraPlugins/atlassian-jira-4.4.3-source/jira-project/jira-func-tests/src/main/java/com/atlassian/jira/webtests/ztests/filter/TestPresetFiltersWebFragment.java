package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT, Category.FILTERS })
public class TestPresetFiltersWebFragment extends JIRAWebTest
{
    public TestPresetFiltersWebFragment(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("TestWebFragment.xml");
    }

    public void tearDown()
    {
        navigation.login(ADMIN_USERNAME);
        administration.restoreBlankInstance();
        super.tearDown();
    }

    /**
     * test that the preset filter links on the filter portlet and browse project page
     * is visible with correct permissions.
     * <p/>
     * NOTE: it tests that the links are visbile, but does not check if the links return correct results/search query
     */
    public void testPresetFiltersWebFragment()
    {
        navigation.logout(); //do not have the Browse condition yet, cant see the portlet/project
        assertStandardFiltersNotVisible();
        assertUserFiltersNotVisible();

        navigation.login(ADMIN_USERNAME);
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(BROWSE, "");

        navigation.browseProject(PROJECT_HOMOSAP_KEY);
        assertStandardFiltersVisible(PROJECT_HOMOSAP_KEY);
        assertUserFiltersVisible(PROJECT_HOMOSAP_KEY);

        //check as a non-logged in user
        navigation.logout();

        navigation.browseProject(PROJECT_HOMOSAP_KEY);//check the browse project page
        assertStandardFiltersVisible(PROJECT_HOMOSAP_KEY);
        assertUserFiltersNotVisible();
    }

    public void testVersionLinksPresent()
    {
        administration.restoreData("TestVersionAndComponentOpenIssueSummary.xml");
        //check all links on the project page are correct
        navigation.browseProject(PROJECT_HOMOSAP_KEY);
        assertStandardFiltersVisible(PROJECT_HOMOSAP_KEY);
        assertUserFiltersVisible(PROJECT_HOMOSAP_KEY);

        //check the links on the versions page.
        tester.gotoPage("/browse/HSP/fixforversion/10001");
        final String baseJql = "&jqlQuery=fixVersion+%3D+%22New+Version+4%22+AND+project+%3D+" + PROJECT_HOMOSAP_KEY;
        assertLinkPresentWithURL("filter_all", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql);
        assertLinkPresentWithURL("filter_outstanding", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+resolution+%3D+Unresolved+ORDER+BY+updated+DESC");
        tester.assertLinkNotPresent("filter_unscheduled");
        assertLinkPresentWithURL("filter_mostimportant", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+resolution+%3D+Unresolved+ORDER+BY+priority+DESC");
        assertLinkPresentWithURL("filter_resolvedrecently", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+resolutiondate+%3E%3D-1w+ORDER+BY+updated+DESC");
        assertLinkPresentWithURL("filter_addedrecently", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+created%3E%3D-1w+ORDER+BY+created+DESC");
        assertLinkPresentWithURL("filter_updatedrecently", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+updated%3E%3D-1w+ORDER+BY+updated+DESC");
        assertLinkPresentWithURL("filter_assignedtome", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+assignee+%3D+currentUser%28%29+AND+resolution+%3D+Unresolved");
        assertLinkPresentWithURL("filter_reportedbyme", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+reporter+%3D+currentUser%28%29");
    }

    public void testComponentLinksPresent()
    {
        administration.restoreData("TestVersionAndComponentOpenIssueSummary.xml");
        //check all links on the project page are correct
        navigation.browseProject(PROJECT_HOMOSAP_KEY);
        assertStandardFiltersVisible(PROJECT_HOMOSAP_KEY);
        assertUserFiltersVisible(PROJECT_HOMOSAP_KEY);

        //check the links on the components page.
        tester.gotoPage("/browse/HSP/component/10000");
        final String baseJql = "&jqlQuery=component+%3D+%22New+Component+1%22+AND+project+%3D+" + PROJECT_HOMOSAP_KEY;
        assertLinkPresentWithURL("filter_all", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql);
        assertLinkPresentWithURL("filter_outstanding", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+resolution+%3D+Unresolved+ORDER+BY+updated+DESC");
        assertLinkPresentWithURL("filter_unscheduled", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+resolution+%3D+Unresolved+AND+fixVersion+is+EMPTY+ORDER+BY+priority+DESC");
        assertLinkPresentWithURL("filter_mostimportant", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+resolution+%3D+Unresolved+ORDER+BY+priority+DESC");
        assertLinkPresentWithURL("filter_resolvedrecently", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+resolutiondate+%3E%3D-1w+ORDER+BY+updated+DESC");
        assertLinkPresentWithURL("filter_addedrecently", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+created%3E%3D-1w+ORDER+BY+created+DESC");
        assertLinkPresentWithURL("filter_updatedrecently", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+updated%3E%3D-1w+ORDER+BY+updated+DESC");
        assertLinkPresentWithURL("filter_assignedtome", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+assignee+%3D+currentUser%28%29+AND+resolution+%3D+Unresolved");
        assertLinkPresentWithURL("filter_reportedbyme", "/secure/IssueNavigator.jspa?reset=true&mode=hide" + baseJql + "+AND+reporter+%3D+currentUser%28%29");
    }

    //--------------------------------------------------------------------------------------------------- helper methods
    private void assertUserFiltersNotVisible()
    {
        tester.assertLinkNotPresent("filter_assignedtome");
        tester.assertLinkNotPresent("filter_reportedbyme");
    }

    private void assertUserFiltersVisible(String projectKey)
    {
        assertLinkPresentWithURL("filter_assignedtome", "/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+" + projectKey + "+AND+assignee+%3D+currentUser%28%29+AND+resolution+%3D+Unresolved");
        assertLinkPresentWithURL("filter_reportedbyme", "/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+" + projectKey + "+AND+reporter+%3D+currentUser%28%29");
    }

    private void assertStandardFiltersVisible(String projectKey)
    {
        assertLinkPresentWithURL("filter_all", "/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+" + projectKey);
        assertLinkPresentWithURL("filter_outstanding", "/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+" + projectKey + "+AND+resolution+%3D+Unresolved+ORDER+BY+updated+DESC");
        assertLinkPresentWithURL("filter_unscheduled", "/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+" + projectKey + "+AND+resolution+%3D+Unresolved+AND+fixVersion+is+EMPTY+ORDER+BY+priority+DESC");
        assertLinkPresentWithURL("filter_mostimportant", "/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+" + projectKey + "+AND+resolution+%3D+Unresolved+ORDER+BY+priority+DESC");
        assertLinkPresentWithURL("filter_resolvedrecently", "/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+" + projectKey + "+AND+resolutiondate+%3E%3D-1w+ORDER+BY+updated+DESC");
        assertLinkPresentWithURL("filter_addedrecently", "/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+" + projectKey + "+AND+created%3E%3D-1w+ORDER+BY+created+DESC");
        assertLinkPresentWithURL("filter_updatedrecently", "/secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+" + projectKey + "+AND+updated%3E%3D-1w+ORDER+BY+updated+DESC");
    }

    private void assertStandardFiltersNotVisible()
    {
        tester.assertLinkNotPresent("filter_all");
        tester.assertLinkNotPresent("filter_outstanding");
        tester.assertLinkNotPresent("filter_unscheduled");
        tester.assertLinkNotPresent("filter_mostimportant");
        tester.assertLinkNotPresent("filter_resolvedrecently");
        tester.assertLinkNotPresent("filter_addedrecently");
        tester.assertLinkNotPresent("filter_updatedrecently");
    }
}
