package com.atlassian.jira.plugin.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import java.util.Map;

public class WorkflowPermissionValidatorPluginFactory extends AbstractWorkflowPermissionPluginFactory implements WorkflowPluginValidatorFactory
{
    private final SchemePermissions schemePermissions;

    public WorkflowPermissionValidatorPluginFactory(SchemePermissions schemePermissions)
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
        if (!(descriptor instanceof ValidatorDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ValidatorDescriptor.");
        }

        ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;

        String shortName = (String) validatorDescriptor.getArgs().get("permission");
        return new Integer(Permissions.getType(shortName));
    }
}
