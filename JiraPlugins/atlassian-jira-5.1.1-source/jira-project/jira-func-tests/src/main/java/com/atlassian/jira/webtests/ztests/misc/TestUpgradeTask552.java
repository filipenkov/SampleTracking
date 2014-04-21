package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;

/**
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS })
public class TestUpgradeTask552 extends FuncTestCase
{
    private static final String CUSTOM_FIELDS_ADMIN_TABLE_SELECTOR = "custom-fields";

    public void testUpgrade()
    {
         //this data has the following setup:
        // * 4 labels custom fields: 'Labels', 'Labels' (with a project context), 'Epic', and 'Tags'.
        // * default navigator columns with all 4 labels fields
        // * user navigator column default with 'Labels', 'Labels', and 'Epic' on it for user admin
        // * a filter ('custom field that becomes system field') navigator column layout with 'Labels', 'Labels', and 'Tags'
        // * a filter ('custom field that becomes system field') that should be renamed
        // * a filter ('custom field that remains a custom field') whose query shouldn't be renamed
        //
        // Once imported, the two 'Labels' custom fields should be merged into the 'Labels' system field.
        //
        // The data also has a custom screen config & custom field config.  Once upgraded, these should show the
        // new labels system field correctly.
        //don't want to refresh the caches here to make sure the upgrade task updates the caches properly!
        administration.restoreDataSlowOldWay("TestUpgradeTask552.xml");

        //check 'Labels' custom fields are gone
        navigation.gotoAdminSection("view_custom_fields");
        tester.assertTextPresent("Custom Fields");
        WebTable customFieldsTable = new TableLocator(tester, CUSTOM_FIELDS_ADMIN_TABLE_SELECTOR).getTable();
        assertEquals(3, customFieldsTable.getRowCount());
        text.assertTextPresent(new TableLocator(tester, CUSTOM_FIELDS_ADMIN_TABLE_SELECTOR), "Epic");
        text.assertTextPresent(new TableLocator(tester, CUSTOM_FIELDS_ADMIN_TABLE_SELECTOR), "Tags");

        //check the default issue navigator columns
        navigation.gotoAdminSection("issue_field_columns");
        tester.assertTextPresent("Issue Navigator Default Columns");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 0, 11), "Labels");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 0, 12), "Epic");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 0, 13), "Tags");

        //check the admin user's issue navigator columns
        navigation.issueNavigator().displayAllIssues();
        tester.assertTextPresent("Issue Navigator");
        tester.clickLinkWithText("Configure");
        tester.assertTextPresent("Issue Navigator Columns");
        text.assertTextSequence(new WebPageLocator(tester),
                "The table below shows issue fields in order of appearance in", "your", "Issue Navigator.");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 0, 11), "Labels");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 0, 12), "Epic");

        //check the filter columns have been converted for 'custom field that becomes system field' filter
        navigation.logout();
        navigation.login(FRED_USERNAME);
        navigation.issueNavigator().displayAllIssues();
        tester.clickLink("managefilters");
        tester.clickLink("filterlink_10011");
        tester.assertTextPresent("Issue Navigator");
        tester.assertTextPresent("custom field that becomes system field");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 0, 11), "Labels");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 0, 12), "Tags");

        // query renamed and same number of results
        navigation.issueNavigator().displayAllIssues();
        tester.clickLink("managefilters");
        tester.clickLink("edit_filter_10011");
        tester.assertTextPresent("custom field that becomes system field");
        tester.clickLink("editfilter");
        text.assertTextSequence(new WebPageLocator(tester), "1", "of", "1", "matching issues");
        // this assertion may be brittle since it is case sensitive (and subject to how JQL decides to stringify)
        tester.assertTextInElement("jqltext", "labels in (homer, marge) AND labels in (homer) AND tags not in (white)");

        // query not renamed and same number of results
        navigation.issueNavigator().displayAllIssues();
        tester.clickLink("managefilters");
        tester.clickLink("edit_filter_10010");
        tester.assertTextPresent("custom field that remains a custom field");
        tester.clickLink("editfilter");
        text.assertTextSequence(new WebPageLocator(tester), "2", "of", "2", "matching issues");
        tester.assertTextInElement("jqltext", "Epic in (apple, mango) and cf[10005] = blue");

        // can't test labels gadget since it isn't bundled and hasn't been adjusted to work with the new
        // core labels field.

        navigation.login(ADMIN_USERNAME);
        //check the custom labels screen only shows one epic and one labels field!
        navigation.gotoPage("/secure/admin/ConfigureFieldScreen.jspa?id=10000");
        tester.assertTextPresent("Custom LBL screen");
        final TableLocator fieldTableLocator = new TableLocator(tester, "field_table");
        text.assertTextPresent(fieldTableLocator, "Epic");
        text.assertTextPresent(fieldTableLocator, "Labels");
        //make sure there's only 5 rows!  The first column of the table shows the field number: 1., 2.,3.
        text.assertTextNotPresent(fieldTableLocator, "6.");

        //now check the custom field config
        navigation.gotoPage("/secure/admin/ConfigureFieldLayout!default.jspa?id=10000");
        tester.assertTextPresent("Req Labels Config");
        final TableLocator fieldConfigTableLocator = new TableLocator(tester, "field_table");
        assertEquals(20, fieldConfigTableLocator.getTable().getRowCount());
        text.assertTextPresent(fieldConfigTableLocator, "Epic");
        text.assertTextPresent(fieldConfigTableLocator, "Labels");
        text.assertTextPresent(fieldConfigTableLocator, "Tags");
    }

    public void testUpgradeNoSystemField()
    {
        //don't want to refresh the caches here to make sure the upgrade task updates the caches properly!
        administration.restoreDataSlowOldWay("TestUpgradeTask552NoSystemField.xml");

        navigation.gotoAdminSection("view_custom_fields");
        text.assertTextPresent("Epic/Theme");

        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent("Epic/Theme");
        text.assertTextSequence(new IdLocator(tester, "customfield_10000-val"), "This", "a", "is", "test");
    }

    public void testUpgradeSystemField()
    {
        //don't want to refresh the caches here to make sure the upgrade task updates the caches properly!
        administration.restoreDataSlowOldWay("TestUpgradeTask552SystemField.xml");

        navigation.issue().viewIssue("UPG-2");
        text.assertTextPresent("Labels");
        text.assertTextPresent("TestLabel");
        text.assertTextPresent(new IdLocator(tester, "labels-10001-value"), "two");
    }
}
