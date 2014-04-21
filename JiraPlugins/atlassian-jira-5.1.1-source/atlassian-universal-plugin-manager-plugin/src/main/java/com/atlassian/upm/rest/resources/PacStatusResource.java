package com.atlassian.upm.rest.resources;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.representations.LinkBuilder;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.upm.rest.MediaTypes.PAC_STATUS_JSON;
import static com.google.common.base.Preconditions.checkNotNull;

@Path("/pac-status")
public class PacStatusResource
{
    private final PacClient client;
    private final RepresentationFactory factory;
    private final PermissionEnforcer permissionEnforcer;

    private static final Logger log = LoggerFactory.getLogger(PacStatusResource.class);


    public PacStatusResource(final PacClient client, final RepresentationFactory factory, final PermissionEnforcer permissionEnforcer)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.client = checkNotNull(client, "client");
        this.factory = checkNotNull(factory, "factory");
    }


    @GET
    @Produces(PAC_STATUS_JSON)
    public Response get()
    {
        // Request a simple resource from PAC, to tell whether PAC is available.
        // Responses are: true, false, disabled
        // Always refresh the reachable state if it's been cached
        client.forgetPacReachableState();
        return Response.ok(factory.createPacStatusRepresentation(client.isPacDisabled(), client.isPacReachable())).build();
    }

    public static final class PacStatusRepresentation
    {
        @JsonProperty
        private boolean disabled;

        @JsonProperty
        private boolean reached;

        @JsonProperty
        private final Map<String, URI> links;

        @JsonCreator
        public PacStatusRepresentation(@JsonProperty("disabled") boolean disabled, @JsonProperty("reached") boolean reached,
                                       @JsonProperty("links") Map<String, URI> links)
        {
            this.disabled = disabled;
            this.reached = reached;
            this.links = ImmutableMap.copyOf(links);
        }

        public PacStatusRepresentation(boolean disabled, boolean reached, final UpmUriBuilder uriBuilder, final LinkBuilder linkBuilder)
        {
            this.disabled = disabled;
            this.reached = reached;
            this.links = linkBuilder.buildLinksFor(uriBuilder.buildPacStatusUri(), false).build();
        }

        public boolean isDisabled()
        {
            return disabled;
        }

        public boolean isReached()
        {
            return reached;
        }

        public URI getSelf()
        {
            return links.get("self");
        }
    }
}
