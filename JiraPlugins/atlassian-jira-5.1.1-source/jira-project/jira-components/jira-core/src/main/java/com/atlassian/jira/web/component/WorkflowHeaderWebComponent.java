package com.atlassian.jira.web.component;

import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.model.WebPanel;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.Map;

public class WorkflowHeaderWebComponent
{
    private final WebInterfaceManager webInterfaceManager;
    private final ProjectWorkflowSchemeHelper helper;
    private final VelocityParamFactory velocityParamFactory;

    public WorkflowHeaderWebComponent(WebInterfaceManager webInterfaceManager, ProjectWorkflowSchemeHelper helper,
            VelocityParamFactory velocityParamFactory)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.helper = helper;
        this.velocityParamFactory = velocityParamFactory;
    }

    public String getHtml(JiraWorkflow jiraWorkflow, String helpPath)
    {
        WebPanel first = Iterables.getFirst(webInterfaceManager.getWebPanels("workflow.header"), null);
        if (first != null)
        {
            final Map<String, Object> context = Maps.newHashMap();
            context.put("jiraWorkflow", jiraWorkflow);
            context.put("active", true);
            context.put("helpUtil", HelpUtil.getInstance());
            context.put("sharedProjects", helper.getProjectsForWorkflow(jiraWorkflow.getName()));
            context.put("helpPath", HelpUtil.getInstance().getHelpPath(helpPath));

            return first.getHtml(velocityParamFactory.getDefaultVelocityParams(context));
        }
        else
        {
            return "";
        }
    }
}
