package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableMap;
import com.meterware.httpunit.WebTable;

import java.util.Map;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.JQL;
import static com.atlassian.jira.functest.framework.suite.Category.TIME_ZONES;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v4.4
 */
@WebTest ( { FUNC_TEST, JQL, TIME_ZONES })
public class TestJqlSearchWithTimeZones extends AbstractJqlFuncTest
{
    public static final String BERLIN_USER = "berlin";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestJqlSearchWithTimeZones.xml");
        administration.generalConfiguration().setDefaultUserTimeZone("Australia/Sydney");
    }

    public void testSearchResultsInIssueNavigatorSydneyTimeZone() throws Exception
    {
        String jqlString = "project = Bovine";
        navigation.login(ADMIN_USERNAME);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);

        assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "17/Apr/11", "20/Apr/11", "04/Apr/11", "20/Apr/11", "13/Apr/11", "12/Apr/11 2:00 AM")));
        assertThat(issueNavigatorRow(2), equalTo(row("COW-15", "19/Apr/11", "18/Apr/11", "21/Apr/11", "", "", "")));
    }

    public void testSearchResultsInIssueNavigatorBerlinTimeZone() throws Exception
    {
        String jqlString = "project = Bovine";
        navigation.login(BERLIN_USER);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);

        assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "16/Apr/11", "19/Apr/11", "04/Apr/11", "19/Apr/11", "13/Apr/11", "11/Apr/11 6:00 PM")));
        assertThat(issueNavigatorRow(2), equalTo(row("COW-15", "18/Apr/11", "17/Apr/11", "21/Apr/11", "", "", "")));
    }

    public void testSearchResultsInIssueNavigatorGMTMinus12TimeZone() throws Exception
    {
        String jqlString = "project = Bovine";
        navigation.login("gmtminus12");
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);

        assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "16/Apr/11", "19/Apr/11", "04/Apr/11", "19/Apr/11", "13/Apr/11", "11/Apr/11 4:00 AM")));
        assertThat(issueNavigatorRow(2), equalTo(row("COW-15", "18/Apr/11", "17/Apr/11", "21/Apr/11", "", "", "")));
    }

    public void testJqlForCreatedDateField() throws Exception
    {
        String jqlString = "project = Bovine and createdDate < \"2011-04-17\"";
        navigation.login(ADMIN_USERNAME);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);
        TableLocator tableLocator = new TableLocator(tester, "issuetable");
        WebTable table = tableLocator.getTable();
        assertNull(table);
        tester.assertTextPresent("No matching issues found.");

        navigation.login(BERLIN_USER);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);

        assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "16/Apr/11", "19/Apr/11", "04/Apr/11", "19/Apr/11", "13/Apr/11", "11/Apr/11 6:00 PM")));
    }

    public void testJqlForDueDateField() throws Exception
    {
        String jqlString = "project = Bovine and duedate = \"2011-04-04\"";

        navigation.login(ADMIN_USERNAME);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);
        assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "17/Apr/11", "20/Apr/11", "04/Apr/11", "20/Apr/11", "13/Apr/11", "12/Apr/11 2:00 AM")));

        navigation.login(BERLIN_USER);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);
        assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "16/Apr/11", "19/Apr/11", "04/Apr/11", "19/Apr/11", "13/Apr/11", "11/Apr/11 6:00 PM")));
    }

    public void testJqlForUpdatedField() throws Exception
    {
        String jqlString = "project = Bovine and updated < \"2011-04-20\"";
        navigation.login(ADMIN_USERNAME);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);

        assertThat(issueNavigatorRow(1), equalTo(row("COW-15", "19/Apr/11", "18/Apr/11", "21/Apr/11", "", "", "")));

        navigation.login(BERLIN_USER);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);

        assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "16/Apr/11", "19/Apr/11", "04/Apr/11", "19/Apr/11", "13/Apr/11", "11/Apr/11 6:00 PM")));
        assertThat(issueNavigatorRow(2), equalTo(row("COW-15", "18/Apr/11", "17/Apr/11", "21/Apr/11", "", "", "")));
    }

    public void testJqlForResolvedField() throws Exception
    {
        String jqlString = "project = Bovine and resolutiondate <= \"2011-04-20\"";
        navigation.login(ADMIN_USERNAME);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);
        TableLocator tableLocator = new TableLocator(tester, "issuetable");
        WebTable table = tableLocator.getTable();
        assertNull(table);
        tester.assertTextPresent("No matching issues found.");

        navigation.login(BERLIN_USER);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);

        assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "16/Apr/11", "19/Apr/11", "04/Apr/11", "19/Apr/11", "13/Apr/11", "11/Apr/11 6:00 PM")));
    }

    public void testJqlDatePickerCustomField() throws Exception
    {
        String jqlString = "\"Review date\" = \"2011-04-13\"";

        navigation.login(ADMIN_USERNAME);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);
       assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "17/Apr/11", "20/Apr/11", "04/Apr/11", "20/Apr/11", "13/Apr/11", "12/Apr/11 2:00 AM")));

        navigation.login(BERLIN_USER);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);

        assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "16/Apr/11", "19/Apr/11", "04/Apr/11", "19/Apr/11", "13/Apr/11", "11/Apr/11 6:00 PM")));
    }

    public void testJqlDateTimeCustomField() throws Exception
    {
        String jqlString = "expires < \"2011-04-12\"";
        navigation.login(ADMIN_USERNAME);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);
        TableLocator tableLocator = new TableLocator(tester, "issuetable");
        WebTable table = tableLocator.getTable();
        assertNull(table);
        tester.assertTextPresent("No matching issues found.");

        navigation.login(BERLIN_USER);
        navigation.issueNavigator().createSearch(jqlString + ORDER_BY_CLAUSE);

        assertThat(issueNavigatorRow(1), equalTo(row("COW-16", "16/Apr/11", "19/Apr/11", "04/Apr/11", "19/Apr/11", "13/Apr/11", "11/Apr/11 6:00 PM")));
    }

    ImmutableMap<String, String> issueNavigatorRow(int row)
    {
        TableLocator tableLocator = new TableLocator(tester, "issuetable");
        WebTable table = tableLocator.getTable();

        return ImmutableMap.<String, String>builder()
                .put("issueKey", table.getTableCell(row, 1).asText().trim())
                .put("createdDate", table.getTableCell(row, 8).asText().trim())
                .put("updatedDate", table.getTableCell(row, 9).asText().trim())
                .put("dueDate", table.getTableCell(row, 10).asText().trim())
                .put("resolvedDate", table.getTableCell(row, 11).asText().trim())
                .put("reviewDate", table.getTableCell(row, 12).asText().trim())
                .put("expires", table.getTableCell(row, 13).asText().trim())
                .build();
    }

    Map<String, String> row(String issueKey, String createdDate, String updatedDate, String dueDate, String resolvedDate, String reviewDate, String expires)
    {
        return ImmutableMap.<String, String>builder()
                .put("issueKey", issueKey)
                .put("createdDate", createdDate)
                .put("updatedDate", updatedDate)
                .put("dueDate", dueDate)
                .put("resolvedDate", resolvedDate)
                .put("reviewDate", reviewDate)
                .put("expires", expires)
                .build();
    }
}
