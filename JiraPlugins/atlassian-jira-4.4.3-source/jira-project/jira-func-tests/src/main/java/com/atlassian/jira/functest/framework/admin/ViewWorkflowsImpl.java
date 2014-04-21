package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link com.atlassian.jira.functest.framework.admin.ViewWorkflows}
 *
 * @since v4.3
 */
public class ViewWorkflowsImpl extends AbstractFuncTestUtil implements ViewWorkflows
{
    private static final String NEW_WORKFLOW_NAME_INPUT_NAME = "newWorkflowName";
    private static final String NEW_WORKFLOW_DESCRIPTION_INPUT_NAME = "description";
    private static final String SUBMIT_BUTTON_NAME = "Add";
    private static final String COPY_BUTTON_NAME = "Update";
    private static final String COPY_LINK_PREFIX = "copy_";
    private static final String STEPS_LINK_PREFIX = "steps_live_";

    private final Navigation navigation;
    private final WorkflowSteps steps;

    public ViewWorkflowsImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel, final Navigation navigation)
    {
        super(tester, environmentData, logIndentLevel);
        this.navigation = navigation;
        this.steps = new WorkflowStepsImpl(tester, environmentData, childLogIndentLevel());
    }


    @Override
    public ViewWorkflows goTo()
    {
        navigation.gotoWorkflows();
        return this;
    }

    @Override
    public ViewWorkflows addWorkflow(String name, String description)
    {
        notNull("name", name);
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        tester.setFormElement(NEW_WORKFLOW_NAME_INPUT_NAME, name);
        if (description != null)
        {
            tester.setFormElement(NEW_WORKFLOW_DESCRIPTION_INPUT_NAME, description);
        }
        tester.submit(SUBMIT_BUTTON_NAME);
        return this;
    }

    @Override
    public ViewWorkflows copyWorkflow(String nameToCopy, String newWorkflowName)
    {
        tester.clickLink(COPY_LINK_PREFIX + nameToCopy);
        tester.setFormElement(NEW_WORKFLOW_NAME_INPUT_NAME, newWorkflowName);
        tester.submit(COPY_BUTTON_NAME);
        return this;
    }

    @Override
    public WorkflowSteps workflowSteps(String workflowName)
    {
        tester.clickLink(STEPS_LINK_PREFIX + workflowName);
        return steps;
    }

    @Override
    public ViewWorkflows launchDesigner(String workflowName)
    {
        tester.clickLink("designer_" + workflowName);
        tester.assertTextPresent("Workflow Designer");

        return this;
    }
}
