package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.table.HtmlTable;
import org.xml.sax.SAXException;

/**
 * Used to be part of {@link TestPermissionSchemes}. Moved it out to make it clearer that this tests an upgrade task.
 *
 * @since v4.0
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.UPGRADE_TASKS })
public class TestUpgradeTask1_2 extends FuncTestCase
{
    public static final String DEFAULT_PERM_SCHEME = "Default Permission Scheme";

    public void testWorklogPermissionEmptyOnUpgrade() throws SAXException
    {
        log("Permission Schemes: Testing that worklog permissions remain empty on upgrade");
        administration.restoreData("BlankProjectsOldBuild.xml");
        navigation.gotoAdminSection("permission_schemes");
        tester.clickLinkWithText(DEFAULT_PERM_SCHEME);
        tester.assertTextPresent("Edit Permissions &mdash; " + DEFAULT_PERM_SCHEME);

        // Get the time tracking permissions table
        final HtmlTable timetrackingPermissionsTable = page.getHtmlTable("edit_timetracking_permissions");

        assertEquals(6, timetrackingPermissionsTable.getRowCount());

        assertTrue(timetrackingPermissionsTable.doesCellHaveText(1, 0, "Work On Issues"));
        assertTrue(timetrackingPermissionsTable.doesCellHaveText(2, 0, "Edit Own Worklogs"));
        assertNoRole(timetrackingPermissionsTable, 2);
        assertTrue(timetrackingPermissionsTable.doesCellHaveText(3, 0, "Edit All Worklogs"));
        assertNoRole(timetrackingPermissionsTable, 3);
        assertTrue(timetrackingPermissionsTable.doesCellHaveText(4, 0, "Delete Own Worklogs"));
        assertNoRole(timetrackingPermissionsTable, 4);
        assertTrue(timetrackingPermissionsTable.doesCellHaveText(5, 0, "Delete All Worklogs"));
        assertNoRole(timetrackingPermissionsTable, 5);
    }

    private void assertNoRole(HtmlTable permissionsTable, int row)
    {
        assertFalse(permissionsTable.doesCellHaveText(row, 1, "(Administrators)"));
        assertFalse(permissionsTable.doesCellHaveText(row, 1, "(Developers)"));
        assertFalse(permissionsTable.doesCellHaveText(row, 1, "(Users)"));
    }
}
