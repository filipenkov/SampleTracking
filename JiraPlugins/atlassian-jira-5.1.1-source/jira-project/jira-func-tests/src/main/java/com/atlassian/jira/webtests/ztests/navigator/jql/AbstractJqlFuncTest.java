package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Some common methods for the tests which are testing Jql.
 *
 * @since v4.0
 */
public abstract class AbstractJqlFuncTest extends FuncTestCase
{
    public static final String ORDER_BY_CLAUSE = " ORDER BY key DESC";

    void assertFitsFilterForm(final String jqlQuery, final IssueNavigatorAssertions.FilterFormParam... formParams)
    {
        assertions.getIssueNavigatorAssertions().assertJqlFitsInFilterForm(jqlQuery, formParams);
    }

    void assertTooComplex(final String jqlQuery)
    {
        log("Asserting too complex: '" + jqlQuery + "'");
        navigation.issueNavigator().createSearch(jqlQuery);
        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();
    }

    void assertFilterFormValue(IssueNavigatorAssertions.FilterFormParam formParam)
    {
        tester.setWorkingForm("issue-filter");
        assertSameElements(formParam.getValues(), tester.getDialog().getForm().getParameterValues(formParam.getName()));
    }

    private static void assertSameElements(String[] a, String[] b)
    {
        Set<String> as = (a == null || a.length == 0) ? null : new HashSet<String>(Arrays.asList(a));
        Set<String> bs = (b == null || b.length == 0) ? null : new HashSet<String>(Arrays.asList(b));
        assertEquals(as, bs);
    }

    void assertIssues(final String... keys)
    {
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(keys);
    }

    protected void executeIssueNavigatorURL(final IssueNavigatorNavigation.NavigatorEditMode startEditMode, final IssueNavigatorNavigation.NavigatorEditMode expectedEditMode, final boolean resetQuery, final String... urlParameter) throws UnsupportedEncodingException
    {
        if(navigation.issueNavigator().getCurrentEditMode() != startEditMode)
        {
           navigation.issueNavigator().displayAllIssues();
           navigation.issueNavigator().gotoEditMode(startEditMode);
           assertEquals(startEditMode, navigation.issueNavigator().getCurrentEditMode());
        }

        String params = encodeQueryString(urlParameter);

        if(resetQuery)
        {
            params = params.concat("&reset=true");
        }
        else
        {
            params = params.concat("&addParams=true");
        }

        System.out.println("URL=" + "secure/IssueNavigator.jspa?" + params +  "&runQuery=true");

        tester.gotoPage("secure/IssueNavigator.jspa?" + params +  "&runQuery=true");
        assertEquals(expectedEditMode, navigation.issueNavigator().getCurrentEditMode());
    }

    protected String encodeQueryString(final String... urlParameter) throws UnsupportedEncodingException
    {
        StringBuilder params = new StringBuilder();
        for (String parameter : urlParameter)
        {
            final int index = parameter.indexOf("=");
            final String parameterName = parameter.substring(0, index);
            final String parameterValue = parameter.substring(index + 1);

            params.append("&").append(parameterName).append("=").append(URLEncoder.encode(parameterValue, "UTF8"));
        }
        return params.substring(1);
    }

    /**
     * Runs the specified search WITH AN APPENDED ORDER BY CLAUSE.
     *
     * @param jqlString the jql to search
     * @param issueKeys the issue keys to assert in the result
     */
    protected void assertSearchWithResults(String jqlString, String... issueKeys)
    {
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);
        assertIssues(issueKeys);
    }

    /**
     * Runs the specified search exactly as passed in - it is your responsibility to provide an order by cluase in the
     * query if ordering is important.
     *
     * @param jqlString the jql to search
     * @param issueKeys the issue keys to assert in the result
     */
    protected void assertExactSearchWithResults(String jqlString, String... issueKeys)
    {
         navigation.issueNavigator().createSearch(jqlString);
         assertIssues(issueKeys);
    }

    protected void assertSearchWithError(String jqlString, String error)
    {
        navigation.issueNavigator().createSearch(jqlString);
        assertions.getIssueNavigatorAssertions().assertJqlErrors(error);
    }

    protected void assertSearchWithWarning(String jqlString, String warning)
    {
        navigation.issueNavigator().createSearch(jqlString);
        assertions.getIssueNavigatorAssertions().assertJqlWarnings(warning);
    }

    protected static IssueNavigatorAssertions.FilterFormParam createFilterFormParam(final String name, final String... values)
    {
        return new IssueNavigatorAssertions.FilterFormParam(name, values);
    }

    protected void assertJqlQueryInTextArea(final String expectedJQL)
    {
        final XPathLocator locator = new XPathLocator(tester, "//textarea[@id='jqltext']");
        text.assertTextPresent(locator, expectedJQL);
    }
}
