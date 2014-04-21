package com.atlassian.jira.plugin.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import java.util.Map;

public class WorkflowUserPermissionValidatorPluginFactory extends AbstractWorkflowPermissionPluginFactory implements WorkflowPluginValidatorFactory
{
    private final SchemePermissions schemePermissions;

    public WorkflowUserPermissionValidatorPluginFactory(final SchemePermissions schemePermissions)
    {
        super(schemePermissions);
        this.schemePermissions = schemePermissions;
    }

    @Override
    protected void getVelocityParamsForInput(final Map<String, Object> velocityParams)
    {
        super.getVelocityParamsForInput(velocityParams);
        velocityParams.put("nullallowedoptions", EasyMap.build(Boolean.TRUE.toString(), "True", Boolean.FALSE.toString(), "False"));
    }

    @Override
    protected void getVelocityParamsForEdit(final Map<String, Object> velocityParams, final AbstractDescriptor descriptor)
    {
        getVelocityParamsForInput(velocityParams);

        if (!(descriptor instanceof ValidatorDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ValidatorDescriptor.");
        }

        final ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;

        @SuppressWarnings("unchecked")
        final Map<String, String> args = validatorDescriptor.getArgs();
        final String shortName = args.get("permission");
        final int permissionId = Permissions.getType(shortName);
        velocityParams.put("permission", new Integer(permissionId));
        velocityParams.put("vars-key", args.get("vars.key"));
        velocityParams.put("nullallowed", args.get("nullallowed"));
    }

    @Override
    protected void getVelocityParamsForView(final Map<String, Object> velocityParams, final AbstractDescriptor descriptor)
    {
        if (!(descriptor instanceof ValidatorDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ValidatorDescriptor.");
        }

        final ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;

        @SuppressWarnings("unchecked")
        final Map<String, String> args = validatorDescriptor.getArgs();
        final String shortName = args.get("permission");
        final int permissionId = Permissions.getType(shortName);
        velocityParams.put("permission", schemePermissions.getPermissionName(new Integer(permissionId)));
        velocityParams.put("vars-key", args.get("vars.key"));
        velocityParams.put("nullallowed", Boolean.valueOf(args.get("nullallowed")));
    }

    @Override
    protected Map<String, String> createMap(final Map<String, String> extractedParams)
    {
        if (extractedParams.containsKey("permission"))
        {
            final String value = extractedParams.get("permission");
            final int permissionId = Integer.parseInt(value);
            extractedParams.put("permission", Permissions.getShortName(permissionId));
        }

        return extractedParams;
    }

    public Map<String, ?> getDescriptorParams(final Map<String, Object> conditionParams)
    {
        return extractMultipleParams(conditionParams, CollectionBuilder.newBuilder("permission", "vars.key", "nullallowed").asList());
    }
}
