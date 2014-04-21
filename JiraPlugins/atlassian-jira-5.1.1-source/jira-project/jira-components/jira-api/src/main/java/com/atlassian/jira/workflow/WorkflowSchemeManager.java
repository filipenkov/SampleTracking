/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 29, 2004
 * Time: 6:03:54 PM
 */
package com.atlassian.jira.workflow;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface WorkflowSchemeManager extends SchemeManager
{
    public String getSchemeEntityName();

    public String getEntityName();

    public String getAssociationType();

    public String getSchemeDesc();

    GenericValue getWorkflowScheme(GenericValue project) throws GenericEntityException;

    GenericValue getDefaultEntity(GenericValue scheme) throws GenericEntityException;

    List<GenericValue> getNonDefaultEntities(GenericValue scheme) throws GenericEntityException;

    /**
     * @return A collection of all workflow names currently active (ie assigned to schemes & associated with projects).
     */
    Collection<String> getActiveWorkflowNames() throws GenericEntityException, WorkflowException;

    void addWorkflowToScheme(GenericValue scheme, String workflowName, String issueTypeId)
            throws GenericEntityException;

    /**
     * Updates Workflow Schemes's such that schemes asscoiated to the workflow with name oldWorkflowName will be changed
     * to newWorkflowName.
     * <p/>
     * Note: There is no validation performed by this method to determine if the provided oldWorkflowName or
     * newWorkflowName are valid workflow names or if the workflow is active/inactive. These validations must be done by
     * the caller.
     *
     * @param oldWorkflowName name of the workflow to re-assign all its associated schemes from
     * @param newWorkflowName name of the workflow to assign all the schemes associated to targetWorkflow
     */
    void updateSchemesForRenamedWorkflow(String oldWorkflowName, String newWorkflowName);

    Collection<GenericValue> getSchemesForWorkflow(JiraWorkflow workflow);

    public void clearWorkflowCache();

    /**
     * Returns a map representation of a workflow scheme for a passed project. The returned map stores {issuetype ->
     * workflowName}. A null issuetype points out the default workflow for the scheme.
     *
     * @param project the project whose scheme should be returned.
     * @return the map representation of a workflow scheme. Each key represents an issuetype which its associated value
     *         the name of the workflow assigned to that issue type. A null issuetype points out the default workflow
     *         for that scheme.
     */
    Map<String, String> getWorkflowMap(Project project);

    /**
     * Get the name of the workflow associated with the passed project and issue type.
     *
     * @param project the project used in the search.
     * @param issueType the issue type used in the search.
     * @return the name of the workflow associated with the passed project and issue type.
     */
    String getWorkflowName(Project project, String issueType);

    /**
     * Get the name of the workflow from the passed scheme associated with the passed issue type.
     *
     * @param scheme the scheme to search.
     * @param issueType the issue type used in the search.
     * @return the name of the workflow associated with the scheme and issue type.
     */
    String getWorkflowName(GenericValue scheme, String issueType);

    /**
     * Tells the caller if the passed project is using the default workflow scheme.
     *
     * @param project the project to check.
     * @return true if the passed project is using the default scheme, false otherwise.
     */
    boolean isUsingDefaultScheme(Project project);
}