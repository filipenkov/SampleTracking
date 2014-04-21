package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import java.util.List;

/**
 * Hooks into the WorkflowResource within the func-test plugin.
 *
 * @since v5.1
 */
public class WorkflowsControl extends BackdoorControl<WorkflowsControl>
{
    private static final GenericType<List<String>> LIST_GENERIC_TYPE = new GenericType<List<String>>(){};

    public WorkflowsControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public List<String> getWorkflows()
    {
        return get(createWorkflowResource(), LIST_GENERIC_TYPE);
    }

    private WebResource createWorkflowResource()
    {
        return createResource().path("workflows");
    }
}
