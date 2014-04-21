package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Lists;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.dev.backdoor.util.CacheControl.never;

/**
 * Used to query workflows during the functional tests.
 *
 * @since v5.1
 */
@AnonymousAllowed
@Produces ({MediaType.APPLICATION_JSON})
@Consumes ({MediaType.APPLICATION_JSON})
@Path ("workflows")
public class WorkflowResourceBackdoor
{
    private final WorkflowManager workflowManager;

    public WorkflowResourceBackdoor(WorkflowManager workflowManager)
    {
        this.workflowManager = workflowManager;
    }

    @GET
    public Response getWorkflows()
    {
        Collection<JiraWorkflow> workflows = workflowManager.getWorkflows();
        List<String> str = Lists.newArrayListWithCapacity(workflows.size());
        for (JiraWorkflow workflow : workflows)
        {
            str.add(workflow.getName());
        }
        return Response.ok(str).cacheControl(never()).build();
    }
}
