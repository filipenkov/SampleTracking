package com.atlassian.jira.web.action.admin.workflow;

import com.opensymphony.workflow.loader.WorkflowDescriptor;

import java.io.PrintWriter;

public class MockWorkflowDescriptor extends WorkflowDescriptor
{
    private String workflowXml;

    public MockWorkflowDescriptor(String workflowXml)
    {
        this.workflowXml = workflowXml;
    }

    public void writeXML(PrintWriter out, int indent)
    {
        out.write(workflowXml);
    }
}
