package com.atlassian.upm.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.upm.Change;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.atlassian.upm.rest.MediaTypes.CHANGES_REQUIRING_RESTART_JSON;
import static com.google.common.base.Preconditions.checkNotNull;

@Path("/requires-restart")
public class ChangeRequiringRestartCollectionResource
{
    private final RepresentationFactory factory;
    private final PermissionEnforcer permissionEnforcer;
    private final PluginAccessorAndController pluginAccessorAndController;

    public ChangeRequiringRestartCollectionResource(PluginAccessorAndController pluginAccessorAndController,
        RepresentationFactory factory,
        PermissionEnforcer permissionEnforcer)
    {
        this.pluginAccessorAndController = pluginAccessorAndController;
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.factory = checkNotNull(factory, "factory");
    }

    @GET
    @Produces(CHANGES_REQUIRING_RESTART_JSON)
    public Response get()
    {
        Iterable<Change> filteredChanges = Iterables.filter(pluginAccessorAndController.getRestartRequiredChanges(), new Predicate<Change>()
        {
            public boolean apply(Change change)
            {
                return permissionEnforcer.hasPermission(change.getRequiredPermission());
            }
        });
        return Response.ok(factory.createChangesRequiringRestartRepresentation(filteredChanges)).build();
    }
}
