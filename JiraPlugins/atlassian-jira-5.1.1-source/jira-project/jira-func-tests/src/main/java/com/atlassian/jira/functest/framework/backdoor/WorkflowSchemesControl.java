package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import java.util.List;
import java.util.Map;

/**
 * Used to query workflows schemes during the functional tests.
 *
 * @since v5.1
 */
public class WorkflowSchemesControl extends BackdoorControl<WorkflowSchemesControl>
{
    private static final GenericType<List<WorkflowScheme>> LIST_GENERIC_TYPE = new GenericType<List<WorkflowScheme>>(){};

    public WorkflowSchemesControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public List<WorkflowScheme> getWorkflowSchemes()
    {
        return get(createWorkflowSchemeResource(), LIST_GENERIC_TYPE);
    }

    private WebResource createWorkflowSchemeResource()
    {
        return createResource().path("workflowschemes");
    }

    public static class WorkflowScheme
    {
        private String name;
        private String defaultWorkflow;
        private Map<String, String> mapping;

        public WorkflowScheme()
        {
        }

        public WorkflowScheme(String name, Map<String, String> mapping, String defaultWorkflow)
        {
            this.name = name;
            this.mapping = mapping;
            this.defaultWorkflow = defaultWorkflow;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getDefaultWorkflow()
        {
            return defaultWorkflow;
        }

        public void setDefaultWorkflow(String defaultWorkflow)
        {
            this.defaultWorkflow = defaultWorkflow;
        }

        public Map<String, String> getMapping()
        {
            return mapping;
        }

        public void setMapping(Map<String, String> mapping)
        {
            this.mapping = mapping;
        }
    }
}
