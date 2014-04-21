package com.atlassian.jira.plugin.workflow;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.module.ModuleFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;

public class WorkflowConditionModuleDescriptor extends AbstractWorkflowModuleDescriptor<WorkflowPluginConditionFactory>
{
    public WorkflowConditionModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            OSWorkflowConfigurator workflowConfigurator, final ComponentClassManager componentClassManager, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, workflowConfigurator, componentClassManager, moduleFactory);
    }

    @Override
    protected String getParameterName()
    {
        return "condition-class";
    }

    @Override
    public String getHtml(final String resourceName, final AbstractDescriptor descriptor)
    {
        if ((descriptor != null) && !(descriptor instanceof ConditionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ConditionDescriptor");
        }

        final ConditionDescriptor conditionDescriptor = (ConditionDescriptor) descriptor;

        final WorkflowPluginConditionFactory workflowConditionFactory = getModule();
        return super.getHtml(resourceName, workflowConditionFactory.getVelocityParams(resourceName, conditionDescriptor));
    }

    @Override
    public boolean isOrderable()
    {
        return false;
    }

    @Override
    public boolean isUnique()
    {
        return false;
    }

    @Override
    public boolean isDeletable()
    {
        return true;
    }

    @Override
    public boolean isAddable(final String actionType)
    {
        return true;
    }
}
