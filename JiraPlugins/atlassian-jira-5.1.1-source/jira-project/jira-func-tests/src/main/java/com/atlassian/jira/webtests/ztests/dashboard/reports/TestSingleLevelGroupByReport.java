package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 * Tests the single level group by report
 */
@WebTest ({ Category.FUNC_TEST, Category.REPORTS, Category.USERS_AND_GROUPS, Category.FIELDS, Category.SCHEMES })
public class TestSingleLevelGroupByReport extends FuncTestCase
{
    public void testRunReportLoggedIn()
    {
        administration.restoreData("TestFullAnonymousPermissions.xml");
        //login user
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        runReport();
    }

    public void testRunReportAnonymously()
    {
        administration.restoreData("TestFullAnonymousPermissions.xml");
        //logout user (to test anonymous access)
        navigation.logout();
        tester.clickLinkWithText("Log in again.");
        runReport();
    }

    public void runReport()
    {
        navigation.runReport(10001L, "com.atlassian.jira.plugin.system.reports:singlelevelgroupby");

        //ensure all report components are present (JRA-11661)
        text.assertTextPresent("Select a filter to display");
        text.assertTextPresent("Select a field to group by");
        tester.setFormElement("filterid", "10000"); // ALL
        tester.submit("Next");
        //ensure report is correct
        text.assertTextPresent("Single Level Group By Report");
        text.assertTextPresent("MKY-2");
        text.assertTextPresent("MKY-1");
        text.assertTextPresent("0 of 2 issues have been resolved");
    }

    public void testVersionIsEncoded()
    {
        administration.restoreData("TestVersionAndComponentsWithHTMLNames.xml");

        gotoSingleLevelGroupByReportAllFixForVersions();
        text.assertTextPresent("&quot;version&lt;input &gt;");
        text.assertTextNotPresent("\"version<input >");
    }

    public void testFieldVisibility()
    {
        administration.restoreData("TestVersionAndComponentsWithHTMLNames.xml");

        gotoSingleLevelGroupByReportAllFixForVersions();
        assertFieldsVisible(true, true);

        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Priority");
        gotoSingleLevelGroupByReportAllFixForVersions();
        assertFieldsVisible(false, true);

        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Resolution");
        gotoSingleLevelGroupByReportAllFixForVersions();
        assertFieldsVisible(false, false);

        administration.fieldConfigurations().defaultFieldConfiguration().showFields("Priority");
        gotoSingleLevelGroupByReportAllFixForVersions();
        assertFieldsVisible(true, false);

        administration.fieldConfigurations().defaultFieldConfiguration().showFields("Resolution");
        gotoSingleLevelGroupByReportAllFixForVersions();
        assertFieldsVisible(true, true);

        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, ISSUE_TYPE_IMPROVEMENT);
        tester.setFormElement("summary", "This is a test to see if field is shown");
        tester.submit();

        // create field configuration
        navigation.gotoAdmin();
        tester.clickLink("field_configuration");
        tester.clickLink("add-field-configuration");
        tester.setFormElement("fieldLayoutName", "Many Hidden Fields");
        tester.submit("Add");
        // i didnt do this mess but I at least made comments about ewhat is does
        tester.clickLink("hide_4");     // hide Components
        tester.clickLink("hide_8");     // hide Fix Versions
        tester.clickLink("hide_13");    // hide Priority
        tester.clickLink("hide_15");    // hide Resolution

        // create field config scheme
        tester.clickLink("issue_fields");
        tester.clickLink("add-field-configuration-scheme");
        tester.setFormElement("fieldLayoutSchemeName", "All Fields Hidden Scheme");
        tester.submit("Add");

        tester.clickLink("add-issue-type-field-configuration-association");
        tester.selectOption("issueTypeId", "Bug");
        tester.selectOption("fieldConfigurationId", "Many Hidden Fields");
        tester.submit("Add");

        // associate with project
        tester.gotoPage("/plugins/servlet/project-config/" + "HSP" + "/fields");
        tester.clickLink("project-config-fields-scheme-change");
        tester.selectOption("schemeId", "All Fields Hidden Scheme");
        tester.submit("Associate");

        // verify that bug's fields are hidden, new feature's are visible
        gotoSingleLevelGroupByReportAllFixForVersions();
        assertFieldsVisibleEnt();
    }

    public void testFilterIdRequired()
    {
        administration.restoreBlankInstance();
        navigation.runReport (10001L, "com.atlassian.jira.plugin.system.reports:singlelevelgroupby");
        // don't choose a filter
        tester.submit("Next");
        text.assertTextPresent("Filter is a required field");
    }

    private void gotoSingleLevelGroupByReportAllFixForVersions()
    {
        navigation.runReport(10001L, "com.atlassian.jira.plugin.system.reports:singlelevelgroupby");
        tester.setFormElement("filterid", "10000"); // All
        tester.selectOption("mapper", "Fix For Versions (all)");
        tester.submit("Next");
    }

    private void assertFieldsVisibleEnt()
    {
        try
        {
            final WebTable table = tester.getDialog().getResponse().getTableWithID("single_groupby_report_table");

            final int rowCount = table.getRowCount();
            assertEquals(9, rowCount); // there are 3 versions (2 rows each)

            for (int i = 2; i < rowCount; i+=2) // every second row starting from 2
            {
                final TableCell priorityCell = table.getTableCell(i, 4);
                if (i == 8) // last one is new feature
                {
                    assertTableCellContainsPriorityIcon(priorityCell);
                }
                else
                {
                    assertTableCellContainsNoPriorityIcon(priorityCell);
                }

                final TableCell resolutioCell = table.getTableCell(i, 2);
                if (i == 8) // last one is new feature
                {
                    assertTrue(resolutioCell.asText().contains("Unresolved"));
                }
                else
                {
                    assertFalse(resolutioCell.asText().contains("Unresolved"));
                }
            }
        }
        catch (SAXException e)
        {
            fail("SAX Exception:" + e.getMessage());
        }
    }

    private void assertFieldsVisible(boolean priorityVisible, boolean resolutionVisible)
    {
        try
        {
            final WebTable table = tester.getDialog().getResponse().getTableWithID("single_groupby_report_table");

            final int rowCount = table.getRowCount();
            assertEquals(7, rowCount); // there are 3 versions (2 rows each)

            for (int i = 2; i < rowCount; i+=2) // every second row starting from 2
            {
                final TableCell priorityCell = table.getTableCell(i, 4);
                if (priorityVisible)
                {
                    assertTableCellContainsPriorityIcon(priorityCell);
                }
                else
                {
                    assertTableCellContainsNoPriorityIcon(priorityCell);
                }

                final TableCell resolutionCell = table.getTableCell(i, 2);
                if (resolutionVisible)
                {
                    assertTrue(resolutionCell.asText().contains("Unresolved"));
                }
                else
                {
                    assertFalse(resolutionCell.asText().contains("Unresolved"));
                }
            }
        }
        catch (SAXException e)
        {
            fail("SAX Exception:" + e.getMessage());
        }
    }

    // based on code copied form JIRAWebTest.java
    private void assertTableCellContainsPriorityIcon(TableCell tableCell)
    {
        // assert priority cell contains an icon - always
        final WebImage[] images = tableCell.getImages();
        assertNotNull(images);
        assertEquals(1, images.length);

        WebImage icon = images[0];
        assertTrue(icon.getSource().contains("/images/icons"));
    }

    // based on code copied form JIRAWebTest.java
    private void assertTableCellContainsNoPriorityIcon(TableCell tableCell)
    {
        // assert priority cell contains an icon - always
        final WebImage[] images = tableCell.getImages();
        assertTrue(images == null || images.length == 0);
    }
}
