/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.mock.workflow;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.workflow.AbstractJiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

public class MockJiraWorkflow extends AbstractJiraWorkflow
{
    private String name;

    public MockJiraWorkflow(WorkflowManager workflowManager, String filename) throws InvalidWorkflowDescriptorException, IOException, SAXException
    {
        super(workflowManager, getJiraWorkflow(filename));
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void reset()
    {
        // do nothing
    }

    public boolean isDraftWorkflow()
    {
        return false;
    }

    public static WorkflowDescriptor getJiraWorkflow(String filename) throws InvalidWorkflowDescriptorException, IOException, SAXException
    {
        InputStream isBrokenWorkflow = ClassLoaderUtils.getResourceAsStream(filename, WorkflowDescriptor.class);

         try
         {
             return WorkflowLoader.load(isBrokenWorkflow, true);
         }
         finally
         {
             if (isBrokenWorkflow != null)
                 isBrokenWorkflow.close();
         }
    }

}
