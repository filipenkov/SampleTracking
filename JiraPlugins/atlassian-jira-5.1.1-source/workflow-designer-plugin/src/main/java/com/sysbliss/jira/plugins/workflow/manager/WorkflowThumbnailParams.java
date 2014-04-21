package com.sysbliss.jira.plugins.workflow.manager;

import com.atlassian.jira.workflow.JiraWorkflow;

import java.security.InvalidParameterException;

/**
 * Author: jdoklovic
 */
public class WorkflowThumbnailParams {
    private JiraWorkflow workflow;
    private int stepId;
    private boolean showLabels;
    private int width;
    private int height;
    private boolean maintainAspect;

    private WorkflowThumbnailParams(JiraWorkflow workflow, int stepId, int width, int height, boolean maintainAspect, boolean showLabels) {
        this.workflow = workflow;
        this.stepId = stepId;
        this.width = width;
        this.height = height;
        this.maintainAspect = maintainAspect;
        this.showLabels = showLabels;
    }

    public JiraWorkflow getWorkflow() {
        return workflow;
    }

    public int getStepId() {
        return stepId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean maintainAspect() {
        return maintainAspect;
    }

    public boolean showLabels() {
        return showLabels;
    }

    public static class Builder {
        private JiraWorkflow workflow;
        private int stepId;
        private int width;
        private int height;
        private boolean maintainAspect;
        private boolean showLabels;

        public Builder(JiraWorkflow workflow) {
            this.workflow = workflow;
            this.stepId = -1;
            this.width = 0;
            this.height = 0;
            this.maintainAspect = false;
            this.showLabels = false;
        }

        public Builder setStepId(int stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setMaintainAspect(boolean maintainAspect) {
            this.maintainAspect = maintainAspect;
            return this;
        }

        public Builder setShowLabels(boolean showLabels) {
            this.showLabels = showLabels;
            return this;
        }

        public WorkflowThumbnailParams build() {
            if(width < 1 || height < 1) {
                throw new InvalidParameterException("WIDTH and HEIGHT must be greater than 0");
            }
            return new WorkflowThumbnailParams(workflow, stepId, width, height, maintainAspect, showLabels);
        }
    }
}
