package com.atlassian.upm.rest.resources;

import com.atlassian.plugins.PacException;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.pac.PluginVersionPair;
import com.atlassian.upm.rest.representations.RepresentationFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static com.atlassian.upm.rest.MediaTypes.PAC_DETAILS;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * This resource will provide the "details" link for the plugin and any plugin available update info.
 * it has been extracted from PluginRepresentation because it involves a call to PAC.
 */
@Path("/pac-details/{pluginKey}/{pluginVersion}")
public class PacPluginDetailsResource
{
    private final PacClient client;
    private final RepresentationFactory representationFactory;

    private static final Logger log = LoggerFactory.getLogger(PacPluginDetailsResource.class);

    public PacPluginDetailsResource(PacClient client, RepresentationFactory representationFactory)
    {
        this.client = checkNotNull(client, "client");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
    }

    /**
     * Returns the 'details' link of the plugin passed in parameter
     * @param key
     * @param version
     * @return Response representing the PAC details or a serverError() if PAC returned an exception
     */
    @GET
    @Produces(PAC_DETAILS)
    public Response get(@PathParam("pluginKey") String key, @PathParam("pluginVersion") String version)
    {
        try
        {   
            PluginVersionPair pluginVersionPair = client.getSpecificAndLatestAvailablePluginVersions(key, version);
            return Response.ok(representationFactory.createPacDetailsRepresentation(key, pluginVersionPair)).build();
        }
        catch (PacException e)
        {
            log.warn(String.format("Could not get the plugin version from PAC (%s, %s): %s", key, version, e.toString()));
            return Response.serverError().build();
        }
    }
}
