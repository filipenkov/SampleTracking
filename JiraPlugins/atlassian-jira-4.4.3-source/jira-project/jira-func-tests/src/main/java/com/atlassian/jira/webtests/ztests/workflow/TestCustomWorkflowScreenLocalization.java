package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *  This func test verifies that the i18n of custom workflow screens is working.
 *  The i18n keys for some labels are retrieved from properties of the transition.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestCustomWorkflowScreenLocalization extends FuncTestCase
{

    public void setUpTest()
    {
        administration.restoreData("TestCustomWorkflowScreenLocalization.xml");
    }


    public void testSubmitButtonLabelIsTransitionName()
    {
        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");
    }

    public void testSubmitButtonLabelIsLocalized()
    {
        createWorkflowDraft("Workflow2");
        gotoWorkflowDraftSteps("Workflow2");

        tester.clickLinkWithText("Resolve");
        tester.clickLink("view_transition_properties");
        tester.setFormElement("attributeKey", "jira.i18n.submit");
        tester.setFormElement("attributeValue", "resolveissue.title");
        tester.submit();
        tester.assertTextPresent("jira.i18n.submit");
        tester.assertTextPresent("resolveissue.title");

        publishWorkflowDraft("Workflow2");

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve Issue");
    }


    public void testFallBackToTransitionName()
    {
        createWorkflowDraft("Workflow2");
        gotoWorkflowDraftSteps("Workflow2");

        tester.clickLinkWithText("Resolve");
        tester.clickLink("view_transition_properties");
        tester.setFormElement("attributeKey", "jira.i18n.submit");
        tester.setFormElement("attributeValue", "blah.doesnt.exist");
        tester.submit();
        tester.assertTextPresent("jira.i18n.submit");
        tester.assertTextPresent("blah.doesnt.exist");

        publishWorkflowDraft("Workflow2");

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");
    }

    public void testTransitionNameTitle()
    {
        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");
        tester.assertTitleEquals("Resolve [HMS-1] - Your Company JIRA ");
    }

    public void testLocalizedTitle()
    {
        createWorkflowDraft("Workflow2");
        gotoWorkflowDraftSteps("Workflow2");

        tester.clickLinkWithText("Resolve");
        tester.clickLink("view_transition_properties");
        tester.setFormElement("attributeKey", "jira.i18n.title");
        tester.setFormElement("attributeValue", "resolveissue.title");
        tester.submit();

        tester.assertTextPresent("jira.i18n.title");
        tester.assertTextPresent("resolveissue.title");

        publishWorkflowDraft("Workflow2");

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");

        tester.assertTitleEquals("Resolve Issue [HMS-1] - Your Company JIRA ");

    }


    public void testFixedDescription()
    {
        createWorkflowDraft("Workflow2");
        gotoWorkflowDraftSteps("Workflow2");

        tester.clickLinkWithText("Resolve");
        tester.clickLink("view_transition_properties");
        tester.setFormElement("attributeKey", "description");
        tester.setFormElement("attributeValue", "My Special description");
        tester.submit();

        tester.assertTextPresent("description");
        tester.assertTextPresent("My Special description");

        publishWorkflowDraft("Workflow2");

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");

        tester.assertTextPresent("My Special description");
    }


    // TODO: General workflow methods - move methods into frameworks API.

    public void publishWorkflowDraft(String workflowName)
    {
        gotoWorkflowDraftSteps(workflowName);
        tester.clickLink("publish_draft_workflow");
        tester.checkCheckbox("enableBackup", "false");
        tester.submit();
    }

    public void createWorkflowDraft(String workflowName)
    {
        navigation.gotoWorkflows();
        tester.assertLinkPresentWithText("Create Draft");
        tester.assertLinkPresent("createDraft_" + workflowName);
        tester.clickLink("createDraft_" + workflowName);
    }

    public void gotoWorkflowDraftSteps(String workflowName)
    {
        navigation.gotoWorkflows();
        tester.clickLink("steps_draft_" + workflowName);
    }

}
