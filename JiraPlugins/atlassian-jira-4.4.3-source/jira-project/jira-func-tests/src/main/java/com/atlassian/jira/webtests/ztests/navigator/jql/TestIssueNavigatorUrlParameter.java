package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebResponse;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Func Test to verify the behaviour of the Issue Navigator when using URL Parameter and the Issue Navigator was previously in Simple or Advanced edit mode.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestIssueNavigatorUrlParameter extends AbstractJqlFuncTest
{
    private static final String JQL_NOT_PARSEABLE = "()&#)$&#(*$prject =&^$(&# homosapien";
    private static final String JQL_IN_VALID_DOESNOT_FIT = "component = \"New Component 5\" AND project = homosapien";
    private static final String JQL_IN_VALID_FITS = "reporter in membersOf(\"blub\")";
    private static final String JQL_VALID_DOESNOT_FIT = "component = \"New Component 2\"";
    private static final String JQL_VALID_AND_FITS = "component = \"New Component 2\" AND project = homosapien";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestIssueNavigatorUrlParameter.xml");
    }

    public void testIssueNavigatorRetainSimpleSearch() throws Exception
    {
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_NOT_PARSEABLE);
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_IN_VALID_DOESNOT_FIT);
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, true, "jqlQuery=" + JQL_IN_VALID_FITS);
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_VALID_DOESNOT_FIT);
        assertIssues("HSP-1");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, true, "jqlQuery=" + JQL_VALID_AND_FITS);
        assertIssues("HSP-1");

        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_NOT_PARSEABLE, "pid=123123");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_IN_VALID_DOESNOT_FIT, "pid=123123");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, true, "jqlQuery=" + JQL_IN_VALID_FITS, "pid=123123");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_VALID_DOESNOT_FIT, "pid=123123");
        assertIssues("HSP-1");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, true, "jqlQuery=" + JQL_VALID_AND_FITS, "pid=123123");
        assertIssues("HSP-1");

        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, true, "pid=10000");
        assertIssues("HSP-1");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "pid=123123");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "version=10002");
    }

    public void testIssueNavigatorRetainAdvancedSearch() throws Exception
    {
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_NOT_PARSEABLE);
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_IN_VALID_DOESNOT_FIT);
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_IN_VALID_FITS);
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_VALID_DOESNOT_FIT);
        assertIssues("HSP-1");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_VALID_AND_FITS);
        assertIssues("HSP-1");

        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_NOT_PARSEABLE, "pid=123123");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_IN_VALID_DOESNOT_FIT, "pid=123123");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_IN_VALID_FITS, "pid=123123");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_VALID_DOESNOT_FIT, "pid=123123");
        assertIssues("HSP-1");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "jqlQuery=" + JQL_VALID_AND_FITS, "pid=123123");
        assertIssues("HSP-1");

        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "pid=10000");
        assertIssues("HSP-1");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "pid=123123");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "version=10002");
    }

    public void testIssueNavigatorSearchRequestViews() throws Exception
    {
        try
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);

            executeIssueNavigatorSearchRequestView(400, "Error in the JQL Query: The character '#' is a reserved JQL character. You must enclose it in a string or use the escape '\\u0023' instead. (line 1, character 4)", "jqlQuery=" + JQL_NOT_PARSEABLE, "pid=123123");
            executeIssueNavigatorSearchRequestView(400, "The value 'New Component 5' does not exist for the field 'component'.", "jqlQuery=" + JQL_IN_VALID_DOESNOT_FIT, "pid=123123");
            executeIssueNavigatorSearchRequestView(400, "Function 'membersOf' can not generate a list of usernames for group 'blub'; the group does not exist.", "jqlQuery=" + JQL_IN_VALID_FITS, "pid=123123");
            executeIssueNavigatorSearchRequestView(200, "", "jqlQuery=" + JQL_VALID_DOESNOT_FIT, "pid=123123");

            Document doc = XMLUnit.buildControlDocument(tester.getDialog().getResponse().getText());
            XMLAssert.assertXpathExists("/rss/channel/item[key = 'HSP-1']", doc);

            executeIssueNavigatorSearchRequestView(200, "", "jqlQuery=" + JQL_VALID_AND_FITS, "pid=10001");

            doc = XMLUnit.buildControlDocument(tester.getDialog().getResponse().getText());
            XMLAssert.assertXpathExists("/rss/channel/item[key = 'HSP-1']", doc);
        }finally
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
        }
    }

    public void testIssueNavigatorSearchFilter() throws Exception
    {
        executeSearchFilter(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, 10001);
        executeSearchFilter(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, 10002);
        executeSearchFilter(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, 10003);
        assertIssues("HSP-1");
        executeSearchFilter(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, 10004);
        assertIssues("HSP-1");
    }

    private void executeSearchFilter(final IssueNavigatorNavigation.NavigatorEditMode startEditMode, final IssueNavigatorNavigation.NavigatorEditMode expectedEditMode, final int filterId)
    {
        if(navigation.issueNavigator().getCurrentEditMode() != startEditMode)
        {
           navigation.issueNavigator().displayAllIssues();
           navigation.issueNavigator().gotoEditMode(startEditMode);
           assertEquals(startEditMode, navigation.issueNavigator().getCurrentEditMode());
        }

        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId="+ filterId);

        if(navigation.issueNavigator().getCurrentEditMode() == null)
        {
            tester.clickLink("editfilter");
        }
        assertEquals(expectedEditMode, navigation.issueNavigator().getCurrentEditMode());
    }

    private void executeIssueNavigatorSearchRequestView(final int expectedResponseCode, final String expectedErrorMessage, final String... urlParameter)
            throws IOException, SAXException, ParserConfigurationException
    {
        final String encodedQueryString = encodeQueryString(urlParameter);
        tester.gotoPage("sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?" + encodedQueryString);
        final WebResponse response = tester.getDialog().getResponse();
        final int actualResponseCode = response.getResponseCode();

        assertEquals(expectedResponseCode, actualResponseCode);
        assertions.html().assertResponseContains(tester, expectedErrorMessage);
    }

    public void testIssueNavigatorAddParamsParameter() throws Exception
    {
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, false, "pid=10000");
        assertIssues("HSP-1");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, false, "pid=123123");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, false, "version=10002");

        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, false, "pid=10000");
        assertIssues("HSP-1");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, false, "pid=123123");
        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, false, "version=10002");
    }
}