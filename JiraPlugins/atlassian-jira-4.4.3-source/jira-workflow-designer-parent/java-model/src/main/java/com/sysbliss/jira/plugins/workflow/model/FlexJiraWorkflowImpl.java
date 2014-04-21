/**
 *
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author jdoklovic
 */
public class FlexJiraWorkflowImpl extends AbstractFlexWorkflowObject implements FlexJiraWorkflow, Serializable {


    private static final long serialVersionUID = -2234285987097338369L;
    private boolean isEditable;
    private boolean isSystemWorkflow;
    private boolean isDraftWorkflow;
    private boolean hasDraftWorkflow;
    private boolean isActive;
    private boolean isLoaded;
    private List allSteps;
    private List allActions;
    private List globalActions;
    private List initialActions;
    private List unlinkedStatuses;
    private List<WorkflowAnnotation> workflowAnnotations;
    private boolean hasSchemes;

    public boolean getIsEditable() {
        return this.isEditable;
    }

    public void setIsEditable(final boolean canEdit) {
        this.isEditable = canEdit;

    }

    /**
     * {@inheritDoc}
     */
    public boolean getHasDraftWorkflow() {
        return hasDraftWorkflow;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getIsActive() {
        return isActive;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getIsLoaded() {
        return isLoaded;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getIsDraftWorkflow() {
        return isDraftWorkflow;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getIsSystemWorkflow() {
        return isSystemWorkflow;
    }

    /**
     * {@inheritDoc}
     */
    public void setHasDraftWorkflow(final boolean b) {
        this.hasDraftWorkflow = b;

    }

    /**
     * {@inheritDoc}
     */
    public void setIsActive(final boolean b) {
        this.isActive = b;

    }

    /**
     * {@inheritDoc}
     */
    public void setIsLoaded(final boolean b) {
        this.isLoaded = b;

    }

    /**
     * {@inheritDoc}
     */
    public void setIsDraftWorkflow(final boolean b) {
        this.isDraftWorkflow = b;

    }

    /**
     * {@inheritDoc}
     */
    public void setIsSystemWorkflow(final boolean b) {
        this.isSystemWorkflow = b;

    }

    /**
     * {@inheritDoc}
     */
    public List getAllSteps() {
        return allSteps;
    }

    /**
     * {@inheritDoc}
     */
    public List getAllActions() {
        return allActions;
    }

    /**
     * {@inheritDoc}
     */
    public List getGlobalActions() {
        return globalActions;
    }

    /**
     * {@inheritDoc}
     */
    public List getInitialActions() {
        return initialActions;
    }

    /**
     * {@inheritDoc}
     */
    public void setAllSteps(final List steps) {
        this.allSteps = steps;

    }

    /**
     * {@inheritDoc}
     */
    public void setAllActions(final List actions) {
        this.allActions = actions;

    }

    /**
     * {@inheritDoc}
     */
    public void setGlobalActions(final List actions) {
        this.globalActions = actions;

    }

    /**
     * {@inheritDoc}
     */
    public void setInitialActions(final List actions) {
        this.initialActions = actions;

    }

    public List getUnlinkedStatuses() {
        return unlinkedStatuses;
    }

    public void setUnlinkedStatuses(final List statuses) {
        this.unlinkedStatuses = statuses;

    }

    public boolean getHasSchemes() {
        return hasSchemes;
    }

    public void setHasSchemes(final boolean b) {
        this.hasSchemes = b;
    }

    public List<WorkflowAnnotation> getWorkflowAnnotations() {
        return workflowAnnotations;
    }

    public void setWorkflowAnnotations(List<WorkflowAnnotation> workflowAnnotations) {
        this.workflowAnnotations = workflowAnnotations;
    }
}
