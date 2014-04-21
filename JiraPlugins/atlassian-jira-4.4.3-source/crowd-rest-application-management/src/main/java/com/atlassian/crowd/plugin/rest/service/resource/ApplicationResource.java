package com.atlassian.crowd.plugin.rest.service.resource;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.manager.application.ApplicationManagerException;
import com.atlassian.crowd.plugin.rest.entity.ApplicationEntity;
import com.atlassian.crowd.plugin.rest.entity.RemoteAddressEntity;
import com.atlassian.crowd.plugin.rest.service.controller.ApplicationController;
import com.atlassian.crowd.plugin.rest.util.ApplicationLinkUriHelper;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.atlassian.crowd.plugin.rest.util.ApplicationLinkUriHelper.REMOTE_ADDRESSES_PATH_PARAM;
import static com.atlassian.crowd.plugin.rest.util.ApplicationLinkUriHelper.REMOTE_ADDRESS_QUERY_PARAM;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Represents an Application resource.
 *
 * @since 2.2
 */
@Path ("application")
@Consumes ({APPLICATION_XML, APPLICATION_JSON})
@Produces ({APPLICATION_XML, APPLICATION_JSON})
@AnonymousAllowed
public class ApplicationResource
{
    public final static String APPLICATION_NAME_QUERY_PARAM = "name";
    
    @Context
    protected UriInfo uriInfo;

    @Context
    protected HttpServletRequest request;

    private final ApplicationController applicationController;

    public ApplicationResource(final ApplicationController applicationController)
    {
        this.applicationController = applicationController;
    }

    /**
     * Returns all the applications or a specific application by name
     */
    @GET
    public Response getApplications(@QueryParam(APPLICATION_NAME_QUERY_PARAM) final String applicationName)
            throws ApplicationNotFoundException
    {
        if (applicationName == null)
        {
            return Response.ok(applicationController.getAllApplications(uriInfo.getBaseUri())).build();
        }
        else
        {
            return Response.ok(applicationController.getApplicationByName(applicationName, uriInfo.getBaseUri())).build();
        }
    }

    /**
     * Returns the specified application.
     *
     * @param applicationId ID of the application
     */
    @GET
    @Path("{applicationId}")
    public Response getApplicationById(@PathParam ("applicationId") final long applicationId)
            throws ApplicationNotFoundException
    {
        return Response.ok(applicationController.getApplicationById(applicationId, uriInfo.getBaseUri())).build();
    }

    /**
     * Adds a new application.
     *
     * @param applicationEntity new application entity
     */
    @POST
    public Response addApplication(@DefaultValue("false") @QueryParam("include-request-address") final boolean includeRequestAddress, final ApplicationEntity applicationEntity)
            throws DirectoryNotFoundException, InvalidCredentialException
    {
        final Link link;
        if (includeRequestAddress)
        {
            link = applicationController.addApplicationWithRequestAddress(applicationEntity, request, uriInfo.getBaseUri());
        }
        else
        {
            link = applicationController.addApplication(applicationEntity, uriInfo.getBaseUri());
        }
        return Response.created(link.getHref()).build();
    }

    /**
     * Removes the specified application.
     *
     * @param applicationId ID of the application
     */
    @DELETE
    @Path("{applicationId}")
    public Response removeApplication(@PathParam ("applicationId") final long applicationId)
            throws ApplicationManagerException
    {
        applicationController.removeApplication(applicationId);
        return Response.noContent().build();
    }

    /**
     * Updates the specified application.
     *
     * @param applicationId ID of the application
     */
    @PUT
    @Path("{applicationId}")
    public Response updateApplication(@PathParam ("applicationId") final long applicationId, final ApplicationEntity applicationEntity)
            throws ApplicationNotFoundException, DirectoryNotFoundException, ApplicationManagerException
    {
        if (applicationEntity.getId() == null || !applicationEntity.getId().equals(applicationId))
        {
            throw new IllegalArgumentException("The application ID of the resource location <" + uriInfo.getPath() + "> and the application entity passed in <" + applicationEntity.getId() + "> are not equal");
        }
        else
        {
            applicationController.updateApplication(applicationEntity);
            return Response.noContent().build();
        }
    }

    /**
     * Returns the remote addresses of the specified application.
     *
     * @param applicationId ID of the application
     */
    @GET
    @Path("{applicationId}/" + REMOTE_ADDRESSES_PATH_PARAM)
    public Response getRemoteAddresses(@PathParam ("applicationId") final long applicationId)
            throws ApplicationNotFoundException
    {
        final ApplicationEntity applicationEntity = applicationController.getApplicationById(applicationId, uriInfo.getBaseUri());
        return Response.ok(applicationEntity.getRemoteAddresses()).build();
    }

    /**
     * Adds the remote address to the specified application.
     *
     * @param applicationId ID of the application
     * @param remoteAddressEntity remote address entity
     */
    @POST
    @Path("{applicationId}/" + REMOTE_ADDRESSES_PATH_PARAM)
    public Response addRemoteAddress(@PathParam ("applicationId") final long applicationId, final RemoteAddressEntity remoteAddressEntity)
            throws ApplicationNotFoundException, DirectoryNotFoundException, ApplicationManagerException
    {
        applicationController.addRemoteAddress(applicationId, remoteAddressEntity);
        return Response.created(ApplicationLinkUriHelper.buildRemoteAddressUri(uriInfo.getBaseUri(), applicationId, remoteAddressEntity.getValue())).build();
    }

    /**
     * Removes the remote address of the specified application.
     *
     * @param applicationId ID of the application
     * @param remoteAddress remote address to remove
     */
    @DELETE
    @Path("{applicationId}/" + REMOTE_ADDRESSES_PATH_PARAM)
    public Response removeRemoteAddress(@PathParam ("applicationId") final long applicationId, @QueryParam (REMOTE_ADDRESS_QUERY_PARAM) final String remoteAddress)
            throws ApplicationNotFoundException, DirectoryNotFoundException, ApplicationManagerException
    {
        applicationController.removeRemoteAddress(applicationId, remoteAddress);
        return Response.noContent().build();
    }
}
