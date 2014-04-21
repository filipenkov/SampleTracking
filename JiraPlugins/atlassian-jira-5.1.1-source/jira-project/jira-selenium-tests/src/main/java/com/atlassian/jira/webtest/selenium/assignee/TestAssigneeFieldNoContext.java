package com.atlassian.jira.webtest.selenium.assignee;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

/**
 *
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //Time out issue - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestAssigneeFieldNoContext extends JiraSeleniumTest
{
    public static Test suite()
    {
        return suiteFor(TestAssigneeFieldNoContext.class);
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestBlankProjectsPlusInactiveWorkflow.xml");
    }

    public void testWorkflowPostFunction()
    {
        getNavigator().login(ADMIN_USERNAME);
        // go directly to the transition page
        getNavigator().gotoPage("/secure/admin/workflows/ViewWorkflowTransition.jspa?workflowMode=live&descriptorTab=postfunctions&workflowStep=1&workflowTransition=4&workflowName=Update+Assignee+Field+Workflow", true);

        getNavigator().clickAndWaitForPageLoad("add_post_func");
        getNavigator().click("com.atlassian.jira.plugin.system.workflow:update-issue-field-function");
        getNavigator().clickAndWaitForPageLoad("Add");

        // Check pre-selected with unassigned disallowed
        assertThat.elementNotPresent("assignee_radio_unassigned_assignee");
        assertThat.elementPresent("assignee_radio_automatic_assignee");
        assertThat.attributeContainsValue("assignee_radio_automatic_assignee", "checked", "true");
        assertThat.elementPresent("assignee_radio_picker_assignee");


        // Enable Unassigned
        getNavigator().gotoPage("/secure/admin/jira/EditApplicationProperties!default.jspa", true);

        getNavigator().click("//input[@name='allowUnassigned'][@value='true']");
        getNavigator().clickAndWaitForPageLoad("id=edit_property");

        getNavigator().gotoPage("/secure/admin/workflows/ViewWorkflowTransition.jspa?workflowMode=live&descriptorTab=postfunctions&workflowStep=1&workflowTransition=4&workflowName=Update+Assignee+Field+Workflow", true);
        getNavigator().clickAndWaitForPageLoad("add_post_func");
        getNavigator().click("com.atlassian.jira.plugin.system.workflow:update-issue-field-function");
        getNavigator().clickAndWaitForPageLoad("id=add_submit");


        // Check Unassigned
        assertThat.elementPresent("assignee_radio_unassigned_assignee");
        assertThat.attributeContainsValue("assignee_radio_unassigned_assignee", "checked", "true");
        assertThat.elementPresent("assignee_radio_automatic_assignee");
        assertThat.elementPresent("assignee_radio_picker_assignee");
        getNavigator().clickAndWaitForPageLoad("id=add_submit");
        
        assertThat.elementHasText("//div[@class='highlighted-leaf']", "The Assignee of the issue will be cleared.");


        // Check Auto Assign
        getNavigator().clickAndWaitForPageLoad("//div[@class='highlighted-leaf']/a[1]");
        assertThat.elementPresent("assignee_radio_unassigned_assignee");
        assertThat.attributeContainsValue("assignee_radio_unassigned_assignee", "checked", "true");
        assertThat.elementPresent("assignee_radio_automatic_assignee");
        assertThat.elementPresent("assignee_radio_picker_assignee");

        assertThat.visibleByTimeout("assignee_radio_automatic_assignee");
        getNavigator().click("assignee_radio_automatic_assignee");
        getNavigator().clickAndWaitForPageLoad("id=update_submit");

        assertThat.elementHasText("//div[@class='highlighted-leaf']", "The Assignee of the issue will be set to -1.");

        // Check Unassigned again (different logic when radio selected)
        getNavigator().clickAndWaitForPageLoad("//div[@class='highlighted-leaf']/a[1]");
        assertThat.elementPresent("assignee_radio_unassigned_assignee");
        assertThat.elementPresent("assignee_radio_automatic_assignee");
        assertThat.attributeContainsValue("assignee_radio_automatic_assignee", "checked", "true");
        assertThat.elementPresent("assignee_radio_picker_assignee");

        assertThat.visibleByTimeout("assignee_radio_unassigned_assignee");
        getNavigator().click("assignee_radio_unassigned_assignee");
        getNavigator().clickAndWaitForPageLoad("id=update_submit");
        assertThat.elementHasText("//div[@class='highlighted-leaf']", "The Assignee of the issue will be cleared.");

        // Check blank username
        getNavigator().clickAndWaitForPageLoad("//div[@class='highlighted-leaf']/a[1]");
        assertThat.elementPresent("assignee_radio_unassigned_assignee");
        assertThat.attributeContainsValue("assignee_radio_unassigned_assignee", "checked", "true");
        assertThat.elementPresent("assignee_radio_automatic_assignee");
        assertThat.elementPresent("assignee_radio_picker_assignee");

        assertThat.visibleByTimeout("assignee_radio_picker_assignee");
        getNavigator().click("assignee_radio_picker_assignee");
        getNavigator().clickAndWaitForPageLoad("id=update_submit");
        //This may invalid if Unassigned users are disallowed.
        assertThat.elementHasText("//div[@class='highlighted-leaf']", "The Assignee of the issue will be cleared.");

        // Check blank username when click on text field
        getNavigator().clickAndWaitForPageLoad("//div[@class='highlighted-leaf']/a[1]");
        assertThat.elementPresent("assignee_radio_unassigned_assignee");
        assertThat.attributeContainsValue("assignee_radio_unassigned_assignee", "checked", "true");
        assertThat.elementPresent("assignee_radio_automatic_assignee");
        assertThat.elementPresent("assignee_radio_picker_assignee");

        assertThat.visibleByTimeout("assignee_userpicker_dummy_assignee");
        getNavigator().click("assignee_userpicker_dummy_assignee");
        getNavigator().clickAndWaitForPageLoad("id=update_submit");
        //This may invalid if Unassigned users are disallowed.
        assertThat.elementHasText("//div[@class='highlighted-leaf']", "The Assignee of the issue will be cleared.");


        // The unassigned should be checked
        getNavigator().clickAndWaitForPageLoad("//div[@class='highlighted-leaf']/a[1]");
        assertThat.elementPresent("assignee_radio_unassigned_assignee");
        assertThat.attributeContainsValue("assignee_radio_unassigned_assignee", "checked", "true");
        assertThat.elementPresent("assignee_radio_automatic_assignee");
        assertThat.elementPresent("assignee_radio_picker_assignee");

        // Check typed username
        assertThat.visibleByTimeout("assignee_radio_picker_assignee");
        getNavigator().click("assignee_radio_picker_assignee");
        client.typeWithFullKeyEvents("assignee_userpicker_dummy_assignee", "admin");

        getNavigator().clickAndWaitForPageLoad("id=update_submit");
        assertThat.elementHasText("//div[@class='highlighted-leaf']", "The Assignee of the issue will be set to admin.");

        //Check previous value
        getNavigator().clickAndWaitForPageLoad("//div[@class='highlighted-leaf']/a[1]");
        assertThat.elementPresent("assignee_radio_unassigned_assignee");
        assertThat.elementPresent("assignee_radio_automatic_assignee");
        assertThat.elementPresent("assignee_radio_picker_assignee");
        assertThat.attributeContainsValue("assignee_radio_picker_assignee", "checked", "true");
        assertThat.attributeContainsValue("assignee_userpicker_dummy_assignee", "value", "admin");
        getNavigator().clickAndWaitForPageLoad("id=update_submit");
        assertThat.elementHasText("//div[@class='highlighted-leaf']", "The Assignee of the issue will be set to admin.");


        //Check previous value
        getNavigator().clickAndWaitForPageLoad("//div[@class='highlighted-leaf']/a[1]");
        assertThat.elementPresent("assignee_radio_unassigned_assignee");
        assertThat.elementPresent("assignee_radio_automatic_assignee");
        assertThat.elementPresent("assignee_radio_picker_assignee");
        assertThat.attributeContainsValue("assignee_radio_picker_assignee", "checked", "true");
        assertThat.attributeContainsValue("assignee_userpicker_dummy_assignee", "value", "admin");

        assertThat.visibleByTimeout("assignee_userpicker_dummy_assignee");
        client.typeWithFullKeyEvents("assignee_userpicker_dummy_assignee", "adm");
        assertThat.elementPresentByTimeout("assignee_userpicker_dummy_assignee_i_admin", 10000);
        getNavigator().click("assignee_userpicker_dummy_assignee_i_admin");
        assertThat.attributeContainsValue("assignee_userpicker_dummy_assignee", "value", "admin");

        getNavigator().clickAndWaitForPageLoad("id=update_submit");
        assertThat.elementHasText("//div[@class='highlighted-leaf']", "The Assignee of the issue will be set to admin.");

        

    }
}
