package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Use this backdoor to manipulate Permission Schemes as part of setup for tests.
 *
 * This class should only be called by the
 * {@link com.atlassian.jira.functest.framework.backdoor.PermissionSchemesControl}.
 *
 * @since v5.0
 */
@Path ("permissionSchemes")
@Produces ({ MediaType.APPLICATION_JSON })
public class PermissionSchemesBackdoor
{
    private PermissionSchemeManager schemeManager;

    public PermissionSchemesBackdoor(PermissionSchemeManager schemeManager)
    {
        this.schemeManager = schemeManager;
    }

    @GET
    @AnonymousAllowed
    @Path("copyDefault")
    public Response copyDefault(@QueryParam ("schemeName") String newSchemeName)
    {
        Scheme defaultScheme = schemeManager.getDefaultSchemeObject();
        Scheme copyScheme = schemeManager.copyScheme(defaultScheme);

        // TODO - create new scheme blah blah immutable blah
        copyScheme.setName(newSchemeName);
        schemeManager.updateScheme(copyScheme);

        return Response.ok(copyScheme.getId()).build();
    }

    @GET
    @AnonymousAllowed
    @Path("entity/add")
    public Response addSchemeEntity(@QueryParam ("schemeId") long schemeId,
            @QueryParam ("permission") long permission,
            @QueryParam ("type") String type,
            @QueryParam ("parameter") String parameter)
    {
        try
        {
            GenericValue scheme = schemeManager.getScheme(schemeId);
            List<GenericValue> entities = schemeManager.getEntities(scheme, permission, type, parameter);
            if (!entities.isEmpty())
            {
                throw new IllegalStateException("PermissionScheme entity to be added already exists");
            }

            SchemeEntity entity = new SchemeEntity(type, parameter, permission);
            schemeManager.createSchemeEntity(scheme, entity);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }

        return Response.ok(null).build();
    }

    @GET
    @AnonymousAllowed
    @Path("entity/remove")
    public Response removeEntity(@QueryParam ("schemeId") long schemeId,
                                @QueryParam ("permission") long permission,
                                @QueryParam ("type") String type,
                                @QueryParam ("parameter") String parameter)
    {
        try
        {
            GenericValue scheme = schemeManager.getScheme(schemeId);
            List<GenericValue> entities = schemeManager.getEntities(scheme, permission, type, parameter);
            if (entities.isEmpty())
            {
                throw new IllegalStateException("PermissionScheme entity to be removed does not exist");
            }

            for (GenericValue entity : entities)
            {
                Long id = entity.getLong("id");
                schemeManager.deleteEntity(id);
            }
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }

        return Response.ok(null).build();
    }

    /**
     * Removes all matching entities for the given permission and type, and adds the entity with the given parameter.
     */
    @GET
    @AnonymousAllowed
    @Path("entity/replace")
    public Response replaceEntities(@QueryParam ("schemeId") long schemeId,
            @QueryParam ("permission") long permission,
            @QueryParam ("type") String type,
            @QueryParam ("parameter") String parameter)
    {
        try
        {
            GenericValue scheme = schemeManager.getScheme(schemeId);
            List<GenericValue> entities = schemeManager.getEntities(scheme, permission);

            for (GenericValue entity : entities)
            {
                Long id = entity.getLong("id");
                schemeManager.deleteEntity(id);
            }

            SchemeEntity entity = new SchemeEntity(type, parameter, permission);
            schemeManager.createSchemeEntity(scheme, entity);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }

        return Response.ok(null).build();
    }
}
