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

    public static final String WORKFLOW_NAME = "Workflow2";

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
        administration.workflows().goTo().createDraft(WORKFLOW_NAME);
        administration.workflows().goTo().edit(WORKFLOW_NAME).textView().goTo();

        tester.clickLinkWithText("Resolve");
        tester.clickLink("view_transition_properties");
        tester.setFormElement("attributeKey", "jira.i18n.submit");
        tester.setFormElement("attributeValue", "resolveissue.title");
        tester.submit();
        tester.assertTextPresent("jira.i18n.submit");
        tester.assertTextPresent("resolveissue.title");

        administration.workflows().goTo().publishDraft(WORKFLOW_NAME).publish();

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve Issue");
    }


    public void testFallBackToTransitionName()
    {
        administration.workflows().goTo().createDraft(WORKFLOW_NAME);
        administration.workflows().goTo().edit(WORKFLOW_NAME).textView().goTo();

        tester.clickLinkWithText("Resolve");
        tester.clickLink("view_transition_properties");
        tester.setFormElement("attributeKey", "jira.i18n.submit");
        tester.setFormElement("attributeValue", "blah.doesnt.exist");
        tester.submit();
        tester.assertTextPresent("jira.i18n.submit");
        tester.assertTextPresent("blah.doesnt.exist");

        administration.workflows().goTo().publishDraft(WORKFLOW_NAME).publish();

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
        tester.assertTitleEquals("Resolve [HMS-1] - Your Company JIRA");
    }

    public void testLocalizedTitle()
    {
        administration.workflows().goTo().createDraft(WORKFLOW_NAME);
        administration.workflows().goTo().edit(WORKFLOW_NAME).textView().goTo();

        tester.clickLinkWithText("Resolve");
        tester.clickLink("view_transition_properties");
        tester.setFormElement("attributeKey", "jira.i18n.title");
        tester.setFormElement("attributeValue", "resolveissue.title");
        tester.submit();

        tester.assertTextPresent("jira.i18n.title");
        tester.assertTextPresent("resolveissue.title");

        administration.workflows().goTo().publishDraft(WORKFLOW_NAME).publish();

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");

        tester.assertTitleEquals("Resolve Issue [HMS-1] - Your Company JIRA");

    }


    public void testFixedDescription()
    {
        administration.workflows().goTo().createDraft(WORKFLOW_NAME);
        administration.workflows().goTo().edit(WORKFLOW_NAME).textView().goTo();

        tester.clickLinkWithText("Resolve");
        tester.clickLink("view_transition_properties");
        tester.setFormElement("attributeKey", "description");
        tester.setFormElement("attributeValue", "My Special description");
        tester.submit();

        tester.assertTextPresent("description");
        tester.assertTextPresent("My Special description");

        administration.workflows().goTo().publishDraft(WORKFLOW_NAME).publish();

        navigation.issue().viewIssue("HMS-1");
        tester.clickLink("action_id_11");
        tester.setWorkingForm("issue-workflow-transition");
        assertions.assertSubmitButtonPresentWithText("issue-workflow-transition-submit", "Resolve");

        tester.assertTextPresent("My Special description");
    }

}
