package com.atlassian.jira.workflows.layout.persistence;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * A builder for creating the workflow layout property keys to be used to persist a workflow layout
 * in a {@link com.sysbliss.jira.plugins.workflow.util.WorkflowDesignerPropertySet}.
 */
public abstract class WorkflowLayoutPropertyKeyBuilder
{
    private String workflowName;

    private WorkflowState workflowState;

    protected WorkflowState getWorkflowState()
    {
        return workflowState;
    }

    protected String getWorkflowName()
    {
        return workflowName;
    }

    public static WorkflowLayoutPropertyKeyBuilder newBuilder()
    {
        return new WorkflowLayoutPropertyKeyBuilder.MD5();
    }

    public WorkflowLayoutPropertyKeyBuilder setWorkflowName(final String workflowName)
    {
        this.workflowName = workflowName;
        return this;
    }

    public WorkflowLayoutPropertyKeyBuilder setWorkflowState(final WorkflowState workflowState)
    {
        this.workflowState = workflowState;
        return this;
    }

    public abstract String build();

    private static class MD5 extends WorkflowLayoutPropertyKeyBuilder
    {
        @Override
        public String build()
        {
            return join(getWorkflowState().keyPrefix() + md5Hex(getWorkflowName()));
        }
    }

    public enum WorkflowState
    {
        LIVE
                {
                    @Override
                    String keyPrefix()
                    {
                        return "jira.workflow.layout:";
                    }
                },
        DRAFT
                {
                    @Override
                    String keyPrefix()
                    {
                        return "jira.workflow.draft.layout:";
                    }
                };

        abstract String keyPrefix();
    }
}
