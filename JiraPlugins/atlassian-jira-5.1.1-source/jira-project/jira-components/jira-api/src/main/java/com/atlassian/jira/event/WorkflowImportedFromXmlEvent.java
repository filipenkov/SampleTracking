package com.atlassian.jira.event;

import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Event indicating a workflow has been imported via an XML file.
 *
 * @since v5.0
 */
public class WorkflowImportedFromXmlEvent extends AbstractWorkflowEvent
{
    public WorkflowImportedFromXmlEvent(JiraWorkflow workflow)
    {
        super(workflow);
    }
}
