package com.sysbliss.jira.plugins.workflow.manager;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.sysbliss.jira.plugins.workflow.WorkflowDesignerConstants;
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotation;
import com.sysbliss.jira.plugins.workflow.model.layout.serialize.JSONAnnotationSerializer;
import com.sysbliss.jira.plugins.workflow.util.WorkflowDesignerPropertySet;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Author: jdoklovic
 */
public class WorkflowAnnotationManagerImpl implements WorkflowAnnotationManager {

    private WorkflowDesignerPropertySet workflowDesignerPropertySet;

    public WorkflowAnnotationManagerImpl(WorkflowDesignerPropertySet workflowDesignerPropertySet) {
        this.workflowDesignerPropertySet = workflowDesignerPropertySet;
    }

    public List<WorkflowAnnotation> getAnnotationsForWorkflow(JiraWorkflow workflow) throws Exception {

        return loadAnnotations(getPropertyKey(workflow));
    }

    public void addAnnotationToWorkflow(JiraWorkflow workflow, WorkflowAnnotation annotation) throws Exception {
        String propKey = getPropertyKey(workflow);
        List<WorkflowAnnotation> annotations = loadAnnotations(propKey);
        WorkflowAnnotation oldAnnotation = findAnnotationById(annotations,annotation.getId());

        if(oldAnnotation == null) {
            annotations.add(annotation);
        }

        saveAnnotations(propKey, annotations);
    }

    public void removeAnnotationFromWorkflow(JiraWorkflow workflow, String annotationId) throws Exception {
        String propKey = getPropertyKey(workflow);
        List<WorkflowAnnotation> annotations = loadAnnotations(propKey);
        Iterator<WorkflowAnnotation> i = annotations.iterator();

        WorkflowAnnotation annotation;
        while (i.hasNext()) {
            annotation = i.next();

            if(annotation.getId().equals(annotationId)) {
                i.remove();
            }
        }

        saveAnnotations(propKey, annotations);
    }

    public void removeAnnotationFromWorkflow(JiraWorkflow workflow, WorkflowAnnotation annotation) throws Exception {
        removeAnnotationFromWorkflow(workflow, annotation.getId());
    }

    public boolean workflowHasAnnotation(JiraWorkflow workflow, String annotationId) throws Exception {
        String propKey = getPropertyKey(workflow);
        List<WorkflowAnnotation> annotations = loadAnnotations(propKey);

        return (findAnnotationById(annotations,annotationId) != null);
    }

    public boolean workflowHasAnnotation(JiraWorkflow workflow, WorkflowAnnotation annotation) throws Exception {
        return workflowHasAnnotation(workflow, annotation.getId());
    }

    public void copyAnnotationsForDraftWorkflow(String parentWorkflowName) throws Exception {
        final String originalPropKey = WorkflowDesignerConstants.ANNOTATION_PREFIX.concat(parentWorkflowName);
        final String originalJson = workflowDesignerPropertySet.getProperty(originalPropKey);

        if (StringUtils.isNotBlank(originalJson)) {
            String propKey = WorkflowDesignerConstants.ANNOTATION_DRAFT_PREFIX.concat(parentWorkflowName);
            workflowDesignerPropertySet.setProperty(propKey, originalJson);
        }

    }

    public void copyActiveAnnotations(String originalWorkflowName, String newWorkflowName) throws Exception {
        final String originalPropKey = WorkflowDesignerConstants.ANNOTATION_PREFIX.concat(originalWorkflowName);
        final String originalJson = workflowDesignerPropertySet.getProperty(originalPropKey);

        if (StringUtils.isNotBlank(originalJson)) {
            String propKey = WorkflowDesignerConstants.ANNOTATION_PREFIX.concat(newWorkflowName);
            workflowDesignerPropertySet.setProperty(propKey, originalJson);
        }
    }

    public void publishDraftAnnotations(String parentWorkflowName) throws Exception {
        final String originalPropKey = WorkflowDesignerConstants.ANNOTATION_DRAFT_PREFIX.concat(parentWorkflowName);
        final String originalJson = workflowDesignerPropertySet.getProperty(originalPropKey);

        if (StringUtils.isNotBlank(originalJson)) {
            String propKey = WorkflowDesignerConstants.ANNOTATION_PREFIX.concat(parentWorkflowName);
            workflowDesignerPropertySet.setProperty(propKey, originalJson);
        }
    }

    public void updateAnnotationForWorkflow(JiraWorkflow workflow, WorkflowAnnotation annotation) throws Exception {
        String propKey = getPropertyKey(workflow);
        List<WorkflowAnnotation> annotations = loadAnnotations(propKey);
        WorkflowAnnotation oldAnnotation = findAnnotationById(annotations,annotation.getId());

        if(oldAnnotation != null) {
            oldAnnotation.setDescription(annotation.getDescription());
        }

        saveAnnotations(propKey, annotations);
    }

    private List<WorkflowAnnotation> loadAnnotations(String propKey) throws IOException {
        List<WorkflowAnnotation> annotations = new ArrayList<WorkflowAnnotation>();

        final String json = workflowDesignerPropertySet.getProperty(propKey);
        if (!StringUtils.isBlank(json)) {
            final JSONAnnotationSerializer deserializer = new JSONAnnotationSerializer();
            annotations = deserializer.deserialize(json);
        }

        return annotations;
    }

    private WorkflowAnnotation findAnnotationById(List<WorkflowAnnotation> annotations, String idToFind) {
        WorkflowAnnotation found = null;
        for(WorkflowAnnotation annotation : annotations) {
            if(annotation.getId().equals(idToFind)) {
                found = annotation;
                break;
            }
        }

        return found;
    }

    private String getPropertyKey(JiraWorkflow workflow) {
        String propKey;
        if(workflow.isDraftWorkflow()) {
            propKey = WorkflowDesignerConstants.ANNOTATION_DRAFT_PREFIX.concat(workflow.getName());
        } else {
            propKey = WorkflowDesignerConstants.ANNOTATION_PREFIX.concat(workflow.getName());
        }

        return propKey;
    }

    private void saveAnnotations(String propKey, List<WorkflowAnnotation> annotations) throws Exception {
        final JSONAnnotationSerializer serializer = new JSONAnnotationSerializer();
        final String json = serializer.serialize(annotations);
        workflowDesignerPropertySet.setProperty(propKey, json);
    }
}
