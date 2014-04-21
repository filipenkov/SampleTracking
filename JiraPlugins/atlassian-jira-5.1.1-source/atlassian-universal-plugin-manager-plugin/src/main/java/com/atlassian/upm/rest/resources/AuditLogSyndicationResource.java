package com.atlassian.upm.rest.resources;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import com.sun.syndication.feed.atom.Feed;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.Sys.isUpmDebugModeEnabled;
import static com.atlassian.upm.rest.MediaTypes.AUDIT_LOG_ENTRIES_JSON;
import static com.atlassian.upm.rest.MediaTypes.AUDIT_LOG_MAX_ENTRIES_JSON;
import static com.atlassian.upm.rest.MediaTypes.AUDIT_LOG_PURGE_AFTER_JSON;
import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static com.atlassian.upm.permission.Permission.GET_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.MANAGE_AUDIT_LOG;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

/**
 * Provides a REST resource for fetching the audit log feed
 */
@Path("/log/feed")
public class AuditLogSyndicationResource
{
    private final AuditLogService auditLogService;
    private final PermissionEnforcer permissionEnforcer;
    private final RepresentationFactory representationFactory;

    public AuditLogSyndicationResource(RepresentationFactory representationFactory, AuditLogService auditLogService, PermissionEnforcer permissionEnforcer)
    {
        this.representationFactory = representationFactory;
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.auditLogService = auditLogService;
    }

    @GET
    @Produces(MediaType.APPLICATION_ATOM_XML)
    public Response get(@Context Request request,
        @DefaultValue("25") @QueryParam("max-results") Integer maxResults,
        @DefaultValue("0") @QueryParam("start-index") Integer startIndex)
    {
        permissionEnforcer.enforcePermission(GET_AUDIT_LOG);
        Feed feed = auditLogService.getFeed(maxResults, startIndex);
        Response.ResponseBuilder builder = request.evaluatePreconditions(feed.getUpdated(), computeETag(feed));
        if (builder != null)
        {
            return builder.build();
        }
        return Response.ok(feed).lastModified(feed.getUpdated()).tag(computeETag(feed)).build();
    }

    /**
     * "fill" resource is only used to make testing the audit log easier
     */
    @PUT
    @Consumes(AUDIT_LOG_ENTRIES_JSON)
    public Response fill(FillEntriesRepresentation fillEntriesRepresentation)
    {
        permissionEnforcer.enforcePermission(MANAGE_AUDIT_LOG);
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }

        auditLogService.purgeLog();
        for (String entry : fillEntriesRepresentation.getEntries())
        {
            auditLogService.logI18nMessage(entry);
        }
        return Response.ok().build();
    }

    @GET
    @Path("purge-after")
    @Produces(AUDIT_LOG_PURGE_AFTER_JSON)
    public Response getPurgeAfter()
    {
        permissionEnforcer.enforcePermission(GET_AUDIT_LOG);
        return Response.ok(new PurgeAfterRepresentation(auditLogService.getPurgeAfter())).build();
    }

    @PUT
    @Path("purge-after")
    @Consumes(AUDIT_LOG_PURGE_AFTER_JSON)
    public Response setPurgeAfter(PurgeAfterRepresentation purgeAfterRepresentation)
    {
        permissionEnforcer.enforcePermission(MANAGE_AUDIT_LOG);
        if (purgeAfterRepresentation.getPurgeAfter() <= 0 || purgeAfterRepresentation.getPurgeAfter() > 100000)
        {
            return Response.status(BAD_REQUEST)
                .type(ERROR_JSON)
                .entity(representationFactory.createI18nErrorRepresentation("upm.auditLog.error.invalid.purgeAfter"))
                .build();
        }
        else
        {
            auditLogService.setPurgeAfter(purgeAfterRepresentation.getPurgeAfter());
            return Response.ok(new PurgeAfterRepresentation(auditLogService.getPurgeAfter()))
                .type(AUDIT_LOG_PURGE_AFTER_JSON)
                .build();
        }
    }

    @GET
    @Path("max-entries")
    @Produces(AUDIT_LOG_MAX_ENTRIES_JSON)
    public Response getMaxEntries()
    {
        permissionEnforcer.enforcePermission(GET_AUDIT_LOG);
        return Response.ok(new MaxEntriesRepresentation(auditLogService.getMaxEntries())).build();
    }

    @PUT
    @Path("max-entries")
    @Consumes(AUDIT_LOG_MAX_ENTRIES_JSON)
    public Response setMaxEntries(MaxEntriesRepresentation maxEntriesRepresentation)
    {
        permissionEnforcer.enforcePermission(MANAGE_AUDIT_LOG);
        if (maxEntriesRepresentation.getMaxEntries() < 0)
        {
            return Response.status(BAD_REQUEST)
                .type(ERROR_JSON)
                .entity(representationFactory.createI18nErrorRepresentation("upm.auditLog.error.invalid.maxEntries"))
                .build();
        }
        else
        {
            auditLogService.setMaxEntries(maxEntriesRepresentation.getMaxEntries());
            return Response.ok(new MaxEntriesRepresentation(auditLogService.getMaxEntries()))
                .type(AUDIT_LOG_MAX_ENTRIES_JSON)
                .build();
        }
    }

    public static final class PurgeAfterRepresentation
    {
        @JsonProperty private int purgeAfter;

        @JsonCreator
        public PurgeAfterRepresentation(@JsonProperty("purgeAfter") int purgeAfter)
        {
            this.purgeAfter = purgeAfter;
        }

        public int getPurgeAfter()
        {
            return purgeAfter;
        }
    }

    public static final class MaxEntriesRepresentation
    {
        @JsonProperty private int maxEntries;

        @JsonCreator
        public MaxEntriesRepresentation(@JsonProperty("maxEntries") int maxEntries)
        {
            this.maxEntries = maxEntries;
        }

        public int getMaxEntries()
        {
            return maxEntries;
        }
    }

    public static final class FillEntriesRepresentation
    {
        @JsonProperty private List<String> entries;

        @JsonCreator
        public FillEntriesRepresentation(@JsonProperty("entries") List<String> entries)
        {
            this.entries = checkNotNull(entries, "entries");
        }

        public List<String> getEntries()
        {
            return entries;
        }
    }

    private EntityTag computeETag(Feed feed)
    {
        return computeETag(feed.getUpdated());
    }

    private EntityTag computeETag(Date date)
    {
        return new EntityTag(Long.toString(date.getTime()));
    }
}
