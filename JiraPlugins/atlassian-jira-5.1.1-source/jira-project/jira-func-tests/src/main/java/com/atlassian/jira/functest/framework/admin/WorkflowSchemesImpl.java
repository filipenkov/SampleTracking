package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import java.util.Map;

/**
 * Default implementation of {@link WorkflowSchemes}.
 *
 * @since v4.3
 */
public class WorkflowSchemesImpl extends AbstractFuncTestUtil implements WorkflowSchemes
{
    private static final String WORKFLOW_SCHEMES_LINK_ID = "workflow_schemes";
    private static final String ADD_WORKFLOWSCHEME_LINK_ID = "add_workflowscheme";
    private static final String EDIT_WORKFLOWS_LINK_ID_PREFIX = "edit_workflows_";
    private static final String ISSUE_TYPE_SELECT_NAME = "type";
    private static final String WORKFLOW_SELECT_NAME = "workflow";

    public WorkflowSchemesImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
    }

    @Override
    public WorkflowSchemes goTo()
    {
        navigation().gotoAdminSection(WORKFLOW_SCHEMES_LINK_ID);
        return this;
    }

    @Override
    public Integer addWorkflowScheme(String name, String description)
    {
        tester.clickLink(ADD_WORKFLOWSCHEME_LINK_ID);
//        tester.setWorkingForm("add-workflow-scheme");
        tester.setFormElement("name", name);
        tester.setFormElement("description", description);
        tester.submit("Add");
        return Integer.valueOf(locators.id("workflow-scheme-id").getNode().getAttributes().getNamedItem("value").getNodeValue());
    }

    @Override
    public WorkflowSchemes assignWorkflows(String schemeId, Map<String, String> workflowMappings)
    {
        goToEditWorkflows(schemeId);
        for (Map.Entry<String,String> mapping : workflowMappings.entrySet())
        {
            assignWorkflowFromEditWorkflowsPage(mapping.getKey(), mapping.getValue());
        }
        return this;
    }

    @Override
    public WorkflowSchemes assignWorkflow(String schemeName, String issueTypeId, String workflowName)
    {
        goToEditWorkflows(schemeName);
        assignWorkflowFromEditWorkflowsPage(issueTypeId, workflowName);
        return this;
    }

    @Override
    public WorkflowSchemes assignWorkflowToAllIssueTypes(String schemeId, String workflowName)
    {
        return assignWorkflow(schemeId, FunctTestConstants.ISSUE_TYPE_ALL, workflowName);
    }

    private void goToEditWorkflows(String schemeId)
    {
        tester.clickLink(EDIT_WORKFLOWS_LINK_ID_PREFIX + schemeId);
    }

    private void assignWorkflowFromEditWorkflowsPage(String issueType, String workflowName)
    {
        tester.clickLink("assign-workflow");
        tester.selectOption(ISSUE_TYPE_SELECT_NAME, issueType);
        tester.selectOption(WORKFLOW_SELECT_NAME, workflowName);
        tester.submit("Assign");
    }
}
