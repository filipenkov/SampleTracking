package com.sysbliss.jira.plugins.workflow.manager;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotation;

import java.io.IOException;
import java.util.List;

/**
 * Author: jdoklovic
 */
public interface WorkflowAnnotationManager {

    List<WorkflowAnnotation> getAnnotationsForWorkflow(JiraWorkflow workflow) throws Exception;
    void addAnnotationToWorkflow(JiraWorkflow workflow, WorkflowAnnotation annotation) throws Exception;
    void removeAnnotationFromWorkflow(JiraWorkflow workflow, String annotationId) throws Exception;
    void removeAnnotationFromWorkflow(JiraWorkflow workflow, WorkflowAnnotation annotation) throws Exception;
    boolean workflowHasAnnotation(JiraWorkflow workflow, String annotationId) throws Exception;
    boolean workflowHasAnnotation(JiraWorkflow workflow, WorkflowAnnotation annotation) throws Exception;
    void updateAnnotationForWorkflow(JiraWorkflow workflow, WorkflowAnnotation annotation) throws Exception;

    void copyAnnotationsForDraftWorkflow(String parentWorkflowName) throws Exception;

    void copyActiveAnnotations(String originalWorkflowName, String newWorkflowName) throws Exception;

    void publishDraftAnnotations(String parentWorkflowName) throws Exception;

}
