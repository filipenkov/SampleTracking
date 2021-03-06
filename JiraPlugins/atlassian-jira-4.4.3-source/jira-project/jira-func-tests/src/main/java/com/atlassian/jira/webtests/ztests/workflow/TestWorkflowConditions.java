package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.apache.oro.text.regex.MalformedPatternException;

@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowConditions extends JIRAWebTest
{
    public TestWorkflowConditions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void testProjectRoleWorkflowCondition() throws MalformedPatternException
    {
        administration.workflows().goTo().copyWorkflow("jira", "Copy of jira");
        clickLink("steps_live_Copy of jira");
        clickLinkWithText("Start Progress");
        clickLinkWithText("Add");
        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:isuserinprojectrole-condition");
        submit("Add");
        selectOption("jira.projectrole.id", "Users");
        submit("Add");
        assertTextSequence(new String[] { "Only users in project role ", "Users", " can execute this transition." });
        clickLinkWithText("workflow steps");
        clickLinkWithText("Stop Progress");
        clickLinkWithText("Add");
        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:isuserinprojectrole-condition");
        submit("Add");
        selectOption("jira.projectrole.id", "Developers");
        submit("Add");
        assertTextSequence(new String[] { "Only users in project role ", "Developers", " can execute this transition." });
        gotoPage("/secure/admin/workflows/EditWorkflowTransitionConditionParams!default.jspa?workflowStep=1&workflowTransition=4&count=2&workflowName=Copy+of+jira&workflowMode=live");

        selectOption("jira.projectrole.id", "Administrators");
        submit("Update");
        assertTextSequence(new String[] { "Only users in project role ", "Administrators", " can execute this transition." });
        clickLinkWithText("Delete");
        clickLink("project_role_browser");
        setFormElement("name", "");
        clickLink("view_Administrators");
        clickLinkWithText("Start Progress");
        assertTextSequence(new String[] { "Only users in project role ", "Administrators", " can execute this transition." });

    }

    public void testGroupCFAddCondition()
    {

        // Add a CF group picker called CF_GP1 and CF_GP2
        createGroupPickerCF("CF_GP1");
        createGroupPickerCF("CF_GP2");

        // add a WF with a Group CF
        administration.workflows().goTo().copyWorkflow("jira", "Copy of jira");
        clickLink("steps_live_Copy of jira");
        clickLinkWithText("Start Progress");
        clickLinkWithText("Add");
        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:isuseringroupcf-condition");
        submit("Add");

        // assert "groupcf" field has CF_GRP1 in it <option value="customfield_10000">CF_GP1</option>
        assertTextPresent("<select name=\"groupcf\">");
        assertTextPresent("<option value=\"customfield_10000\"");
        assertTextPresent("CF_GP1");
        assertTextPresent("<option value=\"customfield_10001\"");
        assertTextPresent("CF_GP2");

        submit("Add");

        // now edit it again
        gotoPage("secure/admin/workflows/EditWorkflowTransitionConditionParams!default.jspa?workflowStep=1&workflowTransition=4&count=2&workflowName=Copy+of+jira&workflowMode=live");

        // assert "groupcf" field has CF_GRP1 in it <option value="customfield_10000">CF_GP1</option>
        assertTextPresent("<select name=\"groupcf\">");
        assertTextPresent("<option value=\"customfield_10000\"");
        assertTextPresent("CF_GP1");
        assertTextPresent("<option value=\"customfield_10001\"");
        assertTextPresent("CF_GP2");

    }

    public void testPermissionCondition()
    {
        administration.workflows().goTo().copyWorkflow("jira", "Copy of jira");
        clickLink("steps_live_Copy of jira");
        clickLinkWithText("Start Progress");
        clickLinkWithText("Add");
        tester.checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:permission-condition");
        tester.submit("Add");
        // Select 'Resolve Issues' from select box 'permission'.
        tester.selectOption("permission", "Resolve Issues");
        tester.submit("Add");
        tester.assertTextPresent("Resolve Issues");
        tester.gotoPage("/secure/admin/workflows/EditWorkflowTransitionConditionParams!default.jspa?workflowMode=live&workflowStep=1&workflowTransition=4&count=2&workflowName=Copy+of+jira");
        tester.assertRadioOptionSelected("permission", "14"); // Resolve Issues
        tester.assertTextNotPresent("NullPointerException"); //JRA-15643

        // Select 'Create Issues' from select box 'permission'.
        tester.selectOption("permission", "Modify Reporter");
        tester.submit("Update");
        // now check that the edit operation succeeded
        tester.assertTextNotPresent("Resolve Issues");
        tester.assertTextPresent("Modify Reporter");
    }


    private void createGroupPickerCF(String fieldName)
    {
        navigation.gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("add_custom_fields");
        checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:grouppicker");
        submit("nextBtn");
        setFormElement("fieldName", fieldName);
        submit("nextBtn");
        submit("Update");
    }
}
