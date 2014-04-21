package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.table.HtmlTable;
import org.xml.sax.SAXException;

/**
 * Used to be part of {@link com.atlassian.jira.webtests.ztests.admin.TestGlobalPermissions}. Moved it out to make it
 * clearer that this tests an upgrade task.
 *
 * @since v4.0
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.UPGRADE_TASKS })
public class TestUpgradeTask296 extends FuncTestCase
{
    public void testSystemAdminPermissionGetsPopulatedOnUpgrade() throws SAXException
    {
        administration.restoreData("BlankProjectsOldBuild.xml");
        navigation.gotoAdminSection("global_permissions");
        final HtmlTable globalPermissionsTable = page.getHtmlTable("global_perms");
        assertTrue(globalPermissionsTable.doesCellHaveText(1, 0, "JIRA System Administrators"));
        assertTrue(globalPermissionsTable.doesCellHaveText(1, 1, "jira-administrators"));
    }
}
