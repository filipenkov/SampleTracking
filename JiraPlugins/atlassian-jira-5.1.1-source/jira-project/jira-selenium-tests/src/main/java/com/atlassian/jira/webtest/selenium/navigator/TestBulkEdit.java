package com.atlassian.jira.webtest.selenium.navigator;

import com.atlassian.jira.functest.framework.backdoor.PermissionSchemesControl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.*;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertTrueByDefaultTimeout;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.jQuery;
import static com.atlassian.jira.webtests.Permissions.ASSIGNABLE_USER;

/**
 * @since v4.0.1
 */
@SkipInBrowser(browsers={Browser.IE}) //JS Error - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestBulkEdit extends JiraSeleniumTest
{
    @Override
    protected void onTearDown() throws Exception
    {
        backdoor.darkFeatures().disableForSite("jira.no.frother.reporter.field");
        super.onTearDown();
    }

    public void testAssigneeControl()
    {
        // 0. Setup
        restoreBlankInstance();

        getBackdoor().usersAndGroups().addUserToGroup("jiradev", Groups.ADMINISTRATORS);
        PermissionSchemesControl permissionSchemes = getBackdoor().permissionSchemes();

        Long schemeId = permissionSchemes.copyDefaultScheme("Admin-Only Scheme");
        permissionSchemes.replaceGroupPermissions(schemeId, ASSIGNABLE_USER, Groups.ADMINISTRATORS);
        getBackdoor().project().setPermissionScheme(MKY_PROJECT_ID, schemeId);

        getBackdoor().issues().createIssue(HSP_PROJECT_ID, "HSP Issue 1", "admin");
        getBackdoor().issues().createIssue(HSP_PROJECT_ID, "HSP Issue 2", "admin");
        getBackdoor().issues().createIssue(MKY_PROJECT_ID, "MKY Issue 1", "admin");

        // 1. Test the UI
        getNavigator().findAllIssues();     // find all 3 issues across both HSP and MKY projects
        client.click("//a[@id='toolOptions']/span");
        client.click("bulkedit_all");       // bulk edit
        client.waitForPageToLoad();
        client.click("name=all");                // select all 3 issues
        client.clickButton("Next >>", true);
        client.check("operation", "bulk.edit.operation.name");
        client.clickButton("Next >>", true);

        // TODO - convert this to use a FrotherControlComponent instance
        client.typeWithFullKeyEvents("assignee-field", "j");
        assertTrueByDefaultTimeout(
            "JIRA Developer should be the one and only search result",
            jQuery("#assignee-suggestions li", this.context()).element().isVisible()
        );
        assertTrueByDefaultTimeout(jQuery(".aui-list-item-li-jira-developer", this.context()).element().isVisible());
    }

}
