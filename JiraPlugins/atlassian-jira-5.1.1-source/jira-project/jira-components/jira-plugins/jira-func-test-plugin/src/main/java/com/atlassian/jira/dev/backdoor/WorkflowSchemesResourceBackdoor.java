package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.dev.backdoor.util.CacheControl.never;

/**
 * Used to manipulate workflows during functional tests.
 *
 * @since v5.1
 */
@AnonymousAllowed
@Produces ({MediaType.APPLICATION_JSON})
@Consumes ({MediaType.APPLICATION_JSON})
@Path ("workflowschemes")
public class WorkflowSchemesResourceBackdoor
{
    private final WorkflowSchemeManager workflowSchemeManager;
    private final ConstantsManager constantsManager;

    public WorkflowSchemesResourceBackdoor(WorkflowSchemeManager workflowSchemeManager, ConstantsManager constantsManager)
    {
        this.workflowSchemeManager = workflowSchemeManager;
        this.constantsManager = constantsManager;
    }

    @GET
    public Response getWorkflows()
    {
        List<Scheme> schemeObjects = workflowSchemeManager.getSchemeObjects();
        List<WorkflowScheme> str = Lists.newArrayListWithCapacity(schemeObjects.size());

        for (Scheme scheme : schemeObjects)
        {
            String name = scheme.getName();
            Map<String, String> mappings = Maps.newHashMap();
            Collection<SchemeEntity> entities = scheme.getEntities();
            String defaultWorkflow =null;
            for (SchemeEntity entity : entities)
            {
                String parameter = entity.getParameter();
                if (parameter == null || "0".equals(parameter))
                {
                    defaultWorkflow = entity.getEntityTypeId().toString();
                }
                else
                {
                    IssueType object = constantsManager.getIssueTypeObject(parameter);
                    mappings.put(object.getName(), entity.getEntityTypeId().toString());
                }
            }
            if (defaultWorkflow == null)
            {
                defaultWorkflow = JiraWorkflow.DEFAULT_WORKFLOW_NAME;
            }

            str.add(new WorkflowScheme(name, mappings, defaultWorkflow));
        }
        return Response.ok(str).cacheControl(never()).build();
    }

    @JsonAutoDetect
    private static class WorkflowScheme
    {
        @JsonProperty
        private String name;

        @JsonProperty
        private String defaultWorkflow;

        @JsonProperty
        private Map<String, String> mapping;

        private WorkflowScheme(String name, Map<String, String> mapping, String defaultWorkflow)
        {
            this.name = name;
            this.mapping = mapping;
            this.defaultWorkflow = defaultWorkflow;
        }
    }
}
