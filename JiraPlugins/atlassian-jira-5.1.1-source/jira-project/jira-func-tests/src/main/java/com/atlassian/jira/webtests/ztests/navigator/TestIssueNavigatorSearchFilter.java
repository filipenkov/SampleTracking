package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.w3c.dom.Attr;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Func tests around the Issue Navigator's Search Filter functionality:
 * View, New, Edit, Manage
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestIssueNavigatorSearchFilter extends FuncTestCase
{
    private static String EDIT_OPERATION              = "filtereditshares";
    private static String EDIT_INVALID_OPERATION      = "editinvalid";
    private static String SAVE_OPERATION              = "filtersave";
    private static String SAVE_AS_OPERATION           = "filtersaveas";
    private static String SAVE_NEW_OPERATION          = "filtersavenew";
    private static String RELOAD_OPERATION            = "reload";
    private static String VIEW_SUBSCRIPTION_OPERATION = "filterviewsubscriptions";
    private static String CREATE_NEW_OPERATION        = "copyasnewfilter";

    private static String VIEW_TAB   = "viewfilter";
    private static String EDIT_TAB   = "editfilter";
    private static String NEW_TAB    = "new_filter";
    private static String MANAGE_TAB = "managefilters";

    private static String SEARCH_REQUESTVIEWS          = "viewOptions";
    private static String TOOLS_OPTIONS                = "toolOptions";
    private static String PERMLINK                     = "permlink";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueNavigatorSearchFilter.xml");
    }

    public void testIssueNavigatorViewFilterModifiedAndValid() throws Exception
    {
        //Load filter "Issues in Homosapiens"
        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10000");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        tester.setWorkingForm("jqlform");
        final String jqlQuery = "project = homosapien and status = \"open\"";
        tester.setFormElement("jqlQuery", jqlQuery);
        tester.submit();
        tester.assertTextPresent("Filter modified since loading");

        navigation.issueNavigator().gotoViewMode();
        assertIDsPresent(EDIT_OPERATION, SAVE_OPERATION, SAVE_AS_OPERATION, RELOAD_OPERATION, VIEW_SUBSCRIPTION_OPERATION);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6");

        assertIDsPresent(SEARCH_REQUESTVIEWS, TOOLS_OPTIONS, PERMLINK);

        _assertPermlink(jqlQuery);

        assertIDsPresent(EDIT_OPERATION, SAVE_OPERATION, SAVE_AS_OPERATION, RELOAD_OPERATION, VIEW_SUBSCRIPTION_OPERATION);
        assertIDsPresent(EDIT_TAB, NEW_TAB, MANAGE_TAB);
    }

    public void testIssueNavigatorViewFilterModifiedAndInvalid() throws Exception
    {
        //Load filter "Issues in Homosapiens"
        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10000");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery","project = homosapien and status = \"blub\"");
        tester.submit();

        tester.assertTextPresent("The value &#39;blub&#39; does not exist for the field &#39;status&#39;.");
        assertIDsNotPresent(SEARCH_REQUESTVIEWS, TOOLS_OPTIONS, PERMLINK);
        tester.clickLink(VIEW_TAB);
        assertIDsPresent(VIEW_TAB, NEW_TAB, MANAGE_TAB);
    }

    public void testIssueNavigatorViewFilterNotModifiedAndValid() throws Exception
    {
        //Load filter "Issues in Homosapiens"
        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10000");

        navigation.issueNavigator().gotoViewMode();
        assertIDsPresent(EDIT_OPERATION, SAVE_AS_OPERATION, VIEW_SUBSCRIPTION_OPERATION);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");

        assertIDsPresent(SEARCH_REQUESTVIEWS, TOOLS_OPTIONS, PERMLINK);
        _assertPermlink("project = homosapien");

    }

    public void testIssueNavigatorViewFilterNotModifiedInvalid() throws Exception
    {
        administration.permissionSchemes().scheme("Copy of Default Permission Scheme").removePermission(10, "jira-administrators");

        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10000");
        assertIDsPresent(EDIT_TAB, NEW_TAB, MANAGE_TAB);

        assertIDsPresent(EDIT_INVALID_OPERATION, VIEW_SUBSCRIPTION_OPERATION);

        administration.permissionSchemes().scheme("Copy of Default Permission Scheme").grantPermissionToGroup(10, "jira-administrators");
    }

    public void testIssueNavigatorViewFilterNotOwnerModifiedAndValid() throws Exception
    {
        navigation.login(FRED_USERNAME);

        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10001");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "MKY-4", "MKY-3", "MKY-2", "MKY-1");

        assertIDsPresent(SEARCH_REQUESTVIEWS, TOOLS_OPTIONS, PERMLINK);
        _assertPermlink("project = monkey");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertIDsPresent(VIEW_TAB, NEW_TAB, MANAGE_TAB);

        tester.setWorkingForm("jqlform");
        final String jqlQuery = "project = monkey and status = \"open\"";
        tester.setFormElement("jqlQuery", jqlQuery);
        tester.submit();
        tester.assertTextPresent("Filter modified since loading");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6");

        assertIDsPresent(SEARCH_REQUESTVIEWS, TOOLS_OPTIONS, PERMLINK);
        _assertPermlink(jqlQuery);

        assertIDsPresent(CREATE_NEW_OPERATION);
        tester.clickLink(VIEW_TAB);
        assertIDsPresent(EDIT_TAB, NEW_TAB, MANAGE_TAB);
        assertIDsPresent(CREATE_NEW_OPERATION, RELOAD_OPERATION);
    }

    public void testIssueNavigatorViewFilterNotOwnerModifiedAndInvalid() throws Exception
    {
        navigation.login(FRED_USERNAME);

        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10001");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertIDsPresent(VIEW_TAB, NEW_TAB, MANAGE_TAB);

        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery","project = monkey and status = \"blub\"");
        tester.submit();

        tester.assertTextPresent("Filter modified since loading");

        assertIDsNotPresent(SEARCH_REQUESTVIEWS, TOOLS_OPTIONS, PERMLINK);
    }

    public void testIssueNavigatorViewFilterNotOwnerNotModifiedAndValid() throws Exception
    {
        navigation.login(FRED_USERNAME);
        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10001");
        assertIDsPresent(EDIT_TAB, NEW_TAB, MANAGE_TAB);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "MKY-4", "MKY-3", "MKY-2", "MKY-1");
        assertIDsPresent(CREATE_NEW_OPERATION);
        assertIDsPresent(SEARCH_REQUESTVIEWS, TOOLS_OPTIONS, PERMLINK);
        _assertPermlink("project = monkey");

        tester.clickLink(EDIT_TAB);
        assertIDsPresent(VIEW_TAB, NEW_TAB, MANAGE_TAB);
    }

    public void testIssueNavigatorViewFilterNotOwnerNotModifiedAndInvalid() throws Exception
    {
        navigation.login(FRED_USERNAME);
        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10000");

        assertIDsPresent(EDIT_TAB, NEW_TAB, MANAGE_TAB);
        assertIDsPresent(EDIT_INVALID_OPERATION);

        assertIDsNotPresent(SEARCH_REQUESTVIEWS, TOOLS_OPTIONS);

        tester.clickLink(EDIT_TAB);
        assertIDsPresent(VIEW_TAB, NEW_TAB, MANAGE_TAB);

        tester.assertTextPresent("A value with ID &#39;10000&#39; does not exist for the field &#39;project&#39;.");
    }

    public void testIssueNavigatorViewFilterNotLoadedModifiedAndValid() throws Exception
    {
        navigation.issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.setWorkingForm("jqlform");
        final String jqlQuery = "project = monkey";
        tester.setFormElement("jqlQuery", jqlQuery);
        tester.submit();
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "MKY-4", "MKY-3", "MKY-2", "MKY-1");
        assertIDsPresent(VIEW_TAB, NEW_TAB, MANAGE_TAB);
        assertIDsPresent(SAVE_NEW_OPERATION);
        assertIDsPresent(SEARCH_REQUESTVIEWS, TOOLS_OPTIONS, PERMLINK);
        _assertPermlink(jqlQuery);

        tester.clickLink(VIEW_TAB);
        assertIDsPresent(NEW_TAB, EDIT_TAB, MANAGE_TAB);
        assertIDsPresent(SAVE_NEW_OPERATION);
    }
    
    public void testIssueNavigatorViewFilterNotLoadedModifiedAndInvalid() throws Exception
    {
        navigation.issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery","project = monkey and status =\"blah\"");
        tester.submit();

        assertIDsPresent(VIEW_TAB, NEW_TAB, MANAGE_TAB);
        tester.clickLink(VIEW_TAB);
        assertIDsPresent(VIEW_TAB, NEW_TAB, MANAGE_TAB);
    }

    public void testIssueNavigatorNotLoggedIn() throws Exception
    {
        navigation.login(ADMIN_USERNAME);
        administration.permissionSchemes().scheme("Copy of Default Permission Scheme").grantPermissionToGroup(10, "");
        navigation.logout();
        tester.clickLinkWithText("Log in again.");

        navigation.issueNavigator().gotoViewMode();

        navigation.issueNavigator().createSearch("");
        assertIDsNotPresent(TOOLS_OPTIONS);
        assertIDsPresent(SEARCH_REQUESTVIEWS, PERMLINK);
    }

    private void assertIDsPresent(String... operations)
    {
        for (String operation : operations)
        {
            tester.assertLinkPresent(operation);
        }
    }

    private void _assertPermlink(final String jqlQuery) throws UnsupportedEncodingException
    {
        XPathLocator locator = new XPathLocator(tester, "//a[@id = 'permlink']");
        final Attr hrefAttribute = (Attr) locator.getNode().getAttributes().getNamedItem("href");
        final String link = hrefAttribute.getNodeValue();
        int startIndex = link.indexOf("jqlQuery") + 9;
        assertEquals(URLEncoder.encode(jqlQuery, "UTF8"), link.substring(startIndex));
    }

    private void assertIDsNotPresent(String... operations)
    {
        for (String operation : operations)
        {
            tester.assertLinkNotPresent(operation);
        }
    }

}
