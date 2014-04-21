package com.atlassian.crowd.embedded.admin.rest;

import com.atlassian.crowd.embedded.admin.rest.entities.DirectoryEntity;
import com.atlassian.crowd.embedded.admin.rest.entities.DirectoryList;
import com.atlassian.crowd.embedded.admin.rest.entities.DirectorySynchronisationInformationEntity;
import com.atlassian.crowd.embedded.admin.util.SimpleMessage;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationRoundInformation;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.Message;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * A resource to show properties of a directory.
 */
@Path("/")
public class DirectoryResource
{
    @Context
    private UriInfo uriInfo;
    private final CrowdDirectoryService crowdDirectoryService;
    private final I18nResolver i18nResolver;

    public DirectoryResource(CrowdDirectoryService crowdDirectoryService, I18nResolver i18nResolver)
    {
        this.crowdDirectoryService = crowdDirectoryService;
        this.i18nResolver = i18nResolver;
    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    @Path("/directory")
    public Response get()
    {
        final List<Directory> directories = crowdDirectoryService.findAllDirectories();

        DirectoryList list = new DirectoryList();
        for (Directory directory : directories)
            list.getDirectories().add(buildDirectoryEntity(directory));

        return Response.ok(list).build();
    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    @Path("/directory/{id}")
    public Response getDirectory(@PathParam("id") final Long id)
    {
        final Directory directory = crowdDirectoryService.findDirectoryById(id);
        if (directory != null)
        {
            return Response.ok(buildDirectoryEntity(directory)).build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private DirectoryEntity buildDirectoryEntity(Directory directory)
    {
        final DirectoryEntity entity = new DirectoryEntity();
        entity.setName(directory.getName());
        entity.getLinks().add(Link.self(getDirectoryUriBuilder().build(directory.getId())));

        final DirectorySynchronisationInformation syncInformation =
                crowdDirectoryService.getDirectorySynchronisationInformation(directory.getId());
        if (syncInformation != null)
        {
            final DirectorySynchronisationInformationEntity syncEntity = new DirectorySynchronisationInformationEntity();
            if (syncInformation.getLastRound() != null)
            {
                syncEntity.setLastSyncDurationInSeconds(syncInformation.getLastRound().getDurationMs() / 1000);
                syncEntity.setLastSyncStartTime(
                        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                .format(syncInformation.getLastRound().getStartTime()));
            }

            if (syncInformation.getActiveRound() != null)
            {
                syncEntity.setCurrentSyncStartTime(syncInformation.getActiveRound().getStartTime());
                syncEntity.setCurrentDurationInSeconds(
                        (System.currentTimeMillis() - syncInformation.getActiveRound().getStartTime()) / 1000
                );
            }
            else
            {
                syncEntity.setCurrentDurationInSeconds(0);
            }

            Message syncStatusMessage = getSyncStatusMessage(syncInformation);
            if (syncStatusMessage != null)
            {
                syncEntity.setSyncStatus(i18nResolver.getText(syncStatusMessage));
            }

            entity.setSync(syncEntity);
        }
        return entity;
    }

    protected UriBuilder getDirectoryUriBuilder()
    {
        return uriInfo.getBaseUriBuilder().path("directory").path("{id}");
    }

    private Message getSyncStatusMessage(DirectorySynchronisationInformation syncInfo)
    {
        final DirectorySynchronisationRoundInformation syncRound =
                syncInfo.isSynchronising() ? syncInfo.getActiveRound() : syncInfo.getLastRound();
        if (syncRound == null)
        {
            return null;
        }
        final String statusKey = syncRound.getStatusKey();
        if (statusKey == null)
        {
            return null;
        }

        final Serializable[] params = syncRound.getStatusParameters().toArray(new Serializable[0]);
        return SimpleMessage.instance("embedded.crowd." + statusKey, params);
    }
}
