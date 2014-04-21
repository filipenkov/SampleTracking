package com.atlassian.jira.webtest.selenium.workflows;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.harness.util.Navigator;

/**
 * A test for JRA-15477
 */
@WebTest({Category.SELENIUM_TEST })
public class TestPublishDraftWorkflow extends JiraSeleniumTest
{
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestPublishDraftWorkflow.xml");
    }

    public void testUserHasToMakeAChoice() throws Exception
    {
        final Navigator navigator = getNavigator();

        navigator.gotoHome();
        navigator.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        navigator.gotoPage("secure/admin/workflows/ListWorkflows.jspa", true);
        assertThat.elementDoesNotContainText("id=workflows_table","backup of workflow");


        navigator.clickAndWaitForPageLoad("link=Create Draft");
        navigator.clickAndWaitForPageLoad("link=publish");

        // now press submit and it should error about not making a radio box selection
        navigator.clickAndWaitForPageLoad("id=publish-workflow-submit");
        assertThat.elementHasText("xpath=//div[@class='error']","Please select if you'd like to save a backup of the active workflow");


        // now make a selection but dont enter a file name
        client.click("id=publish-workflow-true");
        // the value should now be available because its enabled

        client.type("name=newWorkflowName","");
        navigator.clickAndWaitForPageLoad("id=publish-workflow-submit");
        assertThat.elementHasText("xpath=//div[@class='error']","You must specify a workflow name.");
        assertThat.elementHasText("name=newWorkflowName","");

        // ok set it for real now
        client.type("name=newWorkflowName","backup of workflow");
        navigator.clickAndWaitForPageLoad("id=publish-workflow-submit");

        // it should now be in our list
        assertThat.elementHasText("id=workflows_table","backup of workflow");
    }
}
