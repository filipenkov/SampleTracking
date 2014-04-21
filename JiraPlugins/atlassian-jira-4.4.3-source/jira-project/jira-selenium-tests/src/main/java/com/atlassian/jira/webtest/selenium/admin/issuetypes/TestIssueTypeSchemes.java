package com.atlassian.jira.webtest.selenium.admin.issuetypes;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * Test creating, copying and editing an issue type scheme for JIRA professional and enterprise editions.
 * This is done here because of the javascript configure issue type scheme page which allows users to drag and drop
 * issue types to schemes. Delete and Aasociate to project is done as functional test.
 */
@SkipInBrowser(browsers={Browser.IE}) //Correct item not being selected - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestIssueTypeSchemes extends JiraSeleniumTest
{
    private static final String HSP_PID = "10000";

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestIssueTypeSchemes.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void onTearDown()
    {
        restoreBlankInstance();
        //super.tearDown();
    }

    public void testIssueTypeSchemeCreate()
    {
        String initSchemeName = "new selenium scheme";
        String initSchemeDesc = "new scheme added by selenium test";
        String finalSchemeName = "new selenium added scheme";
        String finalSchemeDesc = "description of new scheme added by selenium";
        String newIssueTypeName = "new selenium add type";
        String newIssueTypeDesc = "description for selenium add type";

        gotoIssueTypeSchemes();
        //check that the new schemes to be created dont exist already
        assertThat.textNotPresent(initSchemeName);
        assertThat.textNotPresent(initSchemeDesc);
        assertThat.textNotPresent(finalSchemeName);
        assertThat.textNotPresent(finalSchemeDesc);

        client.type("name", initSchemeName);
        client.type("description", initSchemeDesc);
        getNavigator().clickAndWaitForPageLoad("Add");
        assertThat.textPresent("Add Issue Types Scheme");
        assertConfigureSchemeOptions(initSchemeName, initSchemeDesc, finalSchemeName, finalSchemeDesc, newIssueTypeName, newIssueTypeDesc);

        //check that the new scheme created is by the correct name & desc
        assertThat.textNotPresent(initSchemeName);
        assertThat.textNotPresent(initSchemeDesc);
        assertThat.textPresent(finalSchemeName);
        assertThat.textPresent(finalSchemeDesc);
    }

    public void testIssueTypeSchemeCopy()
    {
        String defaultSchemeName = "Default Issue Type Scheme";
        String initSchemeName = "Copy of " + defaultSchemeName;
        String initSchemeDesc = "Default issue type scheme is the list of global issue types. All newly created issue types will automatically be added to this scheme.";
        String finalSchemeName = "selenium copied default scheme";
        String finalSchemeDesc = "description of selenium copied scheme";
        String newIssueTypeName = "new selenium copy type";
        String newIssueTypeDesc = "description for selenium copy type";

        gotoIssueTypeSchemes();
        //check that the scheme to copy exists and the to be copied scheme dont
        assertThat.textPresent(defaultSchemeName);
        assertThat.textPresent(initSchemeDesc);
        assertThat.textNotPresent(finalSchemeName);
        assertThat.textNotPresent(finalSchemeDesc);

        getNavigator().clickAndWaitForPageLoad("copy_10000");//copy default scheme
        assertThat.textPresent("Add Issue Types Scheme");
        assertConfigureSchemeOptions(initSchemeName, initSchemeDesc, finalSchemeName, finalSchemeDesc, newIssueTypeName, newIssueTypeDesc);

        //check that both the copied and the original schemes exist
        assertThat.textPresent(defaultSchemeName);
        assertThat.textPresent(initSchemeDesc);
        assertThat.textPresent(finalSchemeName);
        assertThat.textPresent(finalSchemeDesc);
    }

    public void testIssueTypeSchemeEdit()
    {
        String initSchemeName = "Test Issue Type Scheme";
        String initSchemeDesc = "Description for test issue type scheme";
        String finalSchemeName = "selenium edited scheme";
        String finalSchemeDesc = "description of scheme edited by selenium";
        String newIssueTypeName = "new selenium edit type";
        String newIssueTypeDesc = "description for selenium edit type";

        gotoIssueTypeSchemes();
        //check that the scheme to be edited exists and the to be edited name dont
        assertThat.textPresent(initSchemeName);
        assertThat.textPresent(initSchemeDesc);
        assertThat.textNotPresent(finalSchemeName);
        assertThat.textNotPresent(finalSchemeDesc);

        getNavigator().clickAndWaitForPageLoad("edit_10010");//copy default scheme
        assertThat.textPresent("Modify Issue Types Scheme");
        assertConfigureSchemeOptions(initSchemeName, initSchemeDesc, finalSchemeName, finalSchemeDesc, newIssueTypeName, newIssueTypeDesc);

        //check that the original scheme is renamed
        assertThat.textNotPresent(initSchemeName);
        assertThat.textNotPresent(initSchemeDesc);
        assertThat.textPresent(finalSchemeName);
        assertThat.textPresent(finalSchemeDesc);
    }

    //--------------------------------------------------------------------------------------------------- Helper Methods
    private void gotoIssueTypeSchemes()
    {
        getNavigator().gotoAdmin();
        getNavigator().clickAndWaitForPageLoad("issue_types");
        getNavigator().clickAndWaitForPageLoad("link=Issue Types Scheme");
    }

    private void assertConfigureSchemeOptions(String initSchemeName, String initSchemeDesc, String finalSchemeName, String finalSchemeDesc, String newIssueTypeName, String newIssueTypeDesc)
    {
        //verify that the name and description field is prepopulated from the previous submit
        assertThat.formElementEquals("name", initSchemeName);
        assertThat.formElementEquals("description", initSchemeDesc);
        client.type("name", finalSchemeName);
        client.type("description", finalSchemeDesc);

        //remove all issue types so we can add new ones only
        client.click("link=Remove all");//javascript
        getNavigator().clickAndWaitForPageLoad("Save");
        assertThat.textPresent("You must select at least one option");
        // drag from available options to selected options list
        client.dragAndDropToObject("availableOptions_1","selectedOptions");
        // check that the drag was successful
        assertEquals("selectedOptions", getParentId("availableOptions_1"));
        //add a new issue type
        client.type("constantName", newIssueTypeName);
        client.type("constantDescription", newIssueTypeDesc);

        getNavigator().clickAndWaitForPageLoad("Add");

        //verify new selenium type is selectable from the select list
        client.selectOption("defaultOption_select", newIssueTypeName);

        getNavigator().clickAndWaitForPageLoad("Save");
        assertThat.textNotPresent("Add Issue Types Scheme");
        assertThat.textPresent(finalSchemeName);
        assertThat.textPresent(finalSchemeDesc);
        assertThat.textPresent(newIssueTypeName);

        getNavigator().clickAndWaitForPageLoad("link=Global Issue Types");
        assertThat.textPresent(newIssueTypeName);
        assertThat.textPresent(newIssueTypeDesc);
        assertThat.elementPresent("link=" + finalSchemeName);

        //go back to the issue types schemes page
        getNavigator().clickAndWaitForPageLoad("link=Issue Types Scheme");
    }

    private String getParentId(String childId)
    {
        return client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\"#" + childId + "\").parent().attr(\"id\")");
    }
}
