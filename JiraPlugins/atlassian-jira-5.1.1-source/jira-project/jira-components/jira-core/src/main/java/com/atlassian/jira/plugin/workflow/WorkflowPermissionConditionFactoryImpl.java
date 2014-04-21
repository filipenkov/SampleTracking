package com.atlassian.jira.plugin.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;

import java.util.Map;

public class WorkflowPermissionConditionFactoryImpl extends AbstractWorkflowPermissionPluginFactory implements WorkflowPluginConditionFactory
{
    private final SchemePermissions schemePermissions;

    public WorkflowPermissionConditionFactoryImpl(SchemePermissions schemePermissions)
    {
        super(schemePermissions);
        this.schemePermissions = schemePermissions;
    }

    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor)
    {
        getVelocityParamsForInput(velocityParams);
        velocityParams.put("permission", getPermissionId(descriptor));
    }

    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor)
    {
        velocityParams.put("permission", schemePermissions.getPermissionName(getPermissionId(descriptor)));
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        // Process The map
        String value = extractSingleParam(conditionParams, "permission");
        final int permissionId = Integer.parseInt(value);
        return EasyMap.build("permission", Permissions.getShortName(permissionId));
    }

    private Integer getPermissionId(AbstractDescriptor descriptor)
    {
        if (!(descriptor instanceof ConditionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ConditionDescriptor.");
        }

        ConditionDescriptor conditionDescriptor = (ConditionDescriptor) descriptor;

        String shortName = (String) conditionDescriptor.getArgs().get("permission");
        int permissionId = Permissions.getType(shortName);
        return new Integer(permissionId);
    }
}
