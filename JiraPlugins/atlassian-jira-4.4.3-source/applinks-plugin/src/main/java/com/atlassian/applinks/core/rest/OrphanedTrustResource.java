package com.atlassian.applinks.core.rest;

import com.atlassian.applinks.core.auth.OrphanedTrustCertificate;
import com.atlassian.applinks.core.auth.OrphanedTrustDetector;
import com.atlassian.applinks.core.rest.auth.AdminApplicationLinksInterceptor;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.rest.model.OrphanedTrustEntityList;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import org.springframework.beans.factory.annotation.Qualifier;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.applinks.core.rest.util.RestUtil.badRequest;
import static com.atlassian.applinks.core.rest.util.RestUtil.ok;

/**
 * REST resource for handling operations on {@link OrphanedTrustCertificate}s
 *
 * @since 3.0
 */
@Path("orphaned-trust")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@WebSudoRequired
@InterceptorChain({ContextInterceptor.class, AdminApplicationLinksInterceptor.class})
public class OrphanedTrustResource
{
    private final OrphanedTrustDetector orphanedTrustDetector;

    public OrphanedTrustResource(
            @Qualifier("delegatingOrphanedTrustDetector") final OrphanedTrustDetector orphanedTrustDetector
    )
    {
        this.orphanedTrustDetector = orphanedTrustDetector;
    }

    @GET
    public Response getIds()
    {
        return Response.ok(new OrphanedTrustEntityList(orphanedTrustDetector.findOrphanedTrustCertificates())).build();
    }

    @DELETE
    @Path("{type}/{id}")
    public Response delete(@PathParam("type") final String typeStr, @PathParam("id") final String id)
    {
        final OrphanedTrustCertificate.Type type;
        try
        {
            type = OrphanedTrustCertificate.Type.valueOf(typeStr);
        }
        catch (IllegalArgumentException e)
        {
            return badRequest("Invalid type parameter: " + typeStr);
        }

        orphanedTrustDetector.deleteTrustCertificate(id, type);
        return ok("Deleted certificate with id: " + id);
    }

}
