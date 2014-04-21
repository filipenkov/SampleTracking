package com.atlassian.jira.projectconfig.workflow;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.util.lang.Pair;

/**
 * Helps the workflow panel work out which workflow to edit when the user asks to do so.
 *
 * @since v5.1
 */
public interface ProjectConfigWorkflowDispatcher
{
    /**
     * Called when the user tries to edit a workflow associated with the passed project. This method may try and create
     * a new workflow for that project.
     *
     * @param projectId the project to test.
     * @return an outcome with the name of the newly created workflow contained or null if the workflow was not created.
     *  Any errors that occur will returned in the outcome.
     */
    ServiceOutcome<Pair<String, Long>> editWorkflow(long projectId);
}
