package com.sysbliss.jira.plugins.workflow.manager;

import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * Author: jdoklovic
 */
public class WorkflowImageParams {

    private JiraWorkflow workflow;
    private int stepId;
    private boolean showLabels;

    private WorkflowImageParams(JiraWorkflow workflow, int stepId, boolean showLabels) {
        this.workflow = workflow;
        this.stepId = stepId;
        this.showLabels = showLabels;
    }

    public JiraWorkflow getWorkflow() {
        return workflow;
    }

    public int getStepId() {
        return stepId;
    }

    public boolean showLabels() {
        return showLabels;
    }

    public static class Builder {
        private JiraWorkflow workflow;
        private int stepId;
        private boolean showLabels;

        public Builder(JiraWorkflow workflow) {
            this.workflow = workflow;
            this.stepId = -1;
            this.showLabels = false;
        }

        public Builder setStepId(int stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder setShowLabels(boolean showLabels) {
            this.showLabels = showLabels;
            return this;
        }

        public WorkflowImageParams build() {
            return new WorkflowImageParams(workflow,stepId,showLabels);
        }
    }
}
