package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.Navigable;

import java.util.Map;

/**
 * Represents 'Workflow schemes' administration section.
 *
 * @since v4.3
 */
public interface WorkflowSchemes extends Navigable<WorkflowSchemes>
{

    /**
     * Add new workflow scheme with given <tt>name</tt> and <tt>description</tt>.
     *
     * @param name name of the new scheme
     * @param description description of the new scheme
     * @return new workflow scheme ID
     */
    Integer addWorkflowScheme(String name, String description);


    /**
     * For given scheme identified by <tt>schemeName</tt>, assign <tt>workflowMappings</tt>, where keys represent
     * issue type names and values represent workflow name.
     * 
     * @param schemeName name of the scheme to edit
     * @param workflowMappings map representing mappings between issue types names and workflow names
     * @return this config instance
     * @see com.atlassian.jira.functest.framework.FunctTestConstants
     */
    WorkflowSchemes assignWorkflows(String schemeName, Map<String,String> workflowMappings);

    /**
     * For given scheme identified by <tt>schemeName</tt>, assign a new workflow mapping between issue identified by
     * <tt>issueTypeName</tt> and workflow identified by <tt>workflowName</tt>.
     *
     * @param schemeId id of the scheme to edit
     * @param issueTypeName name of the issue type to map
     * @param workflowName name of the workflow to map
     * @return this config instance
     * @see com.atlassian.jira.functest.framework.FunctTestConstants
     */
    WorkflowSchemes assignWorkflow(String schemeId, String issueTypeName, String workflowName);

    /**
     * For given scheme identified by <tt>schemeName</tt>, assign workflow identified by <tt>workflowName</tt> to
     * all issue types.
     *
     * @param schemeId id of the scheme to edit
     * @param workflowName name of the workflow to map
     * @return this config instance
     * @see com.atlassian.jira.functest.framework.FunctTestConstants#ISSUE_ALL
     * @see #assignWorkflow(String, String, String)
     */
    WorkflowSchemes assignWorkflowToAllIssueTypes(String schemeId, String workflowName);
}
