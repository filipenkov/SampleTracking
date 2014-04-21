package com.atlassian.applinks.core.rest.client;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.link.ReciprocalActionException;
import com.atlassian.applinks.core.rest.EntityLinkResource;
import com.atlassian.applinks.core.rest.model.EntityLinkEntity;
import com.atlassian.applinks.core.rest.util.RestUtil;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;

import java.net.URI;

import static com.atlassian.applinks.spi.application.TypeId.getTypeId;

/**
 * @since v3.0
 */
public class EntityLinkClient
{
    private final InternalHostApplication internalHostApplication;
    private final RestUrlBuilder restUrlBuilder;

    public EntityLinkClient(final InternalHostApplication internalHostApplication,
                            final RestUrlBuilder restUrlBuilder)
    {
        this.internalHostApplication = internalHostApplication;
        this.restUrlBuilder = restUrlBuilder;
    }

    public void createEntityLinkFrom(final EntityLink entityLink, final EntityType localType, final String localKey)
            throws ReciprocalActionException, CredentialsRequiredException
    {
        createEntityLinkFrom(entityLink, localType, localKey, entityLink.getApplicationLink().createAuthenticatedRequestFactory());
    }

    /**
     * @param requestFactory    the {@link com.atlassian.applinks.api.ApplicationLinkRequestFactory}
     * that is used.
     */
    public void createEntityLinkFrom(final EntityLink entityLink, final EntityType localType, final String localKey, final ApplicationLinkRequestFactory requestFactory)
            throws ReciprocalActionException, CredentialsRequiredException
    {
        final EntityReference localEntity = internalHostApplication.toEntityReference(localKey, localType.getClass());
        final EntityLinkEntity linkBack = new EntityLinkEntity(
                internalHostApplication.getId(),
                localKey,
                getTypeId(localType),
                localEntity.getName(),
                null,   // displayUrl not relevant during the linking process
                null,   // iconUrl not relevant during the linking process
                false);

        final URI baseUri = RestUtil.getBaseRestUri(entityLink.getApplicationLink());

        final Request createLinkBackRequest = requestFactory
                .createRequest(Request.MethodType.PUT, restUrlBuilder
                        .getUrlFor(baseUri, EntityLinkResource.class)
                        .createEntityLink(getTypeId(entityLink.getType()), entityLink.getKey(), false, linkBack)
                        .toString());

        createLinkBackRequest.setEntity(linkBack);

        try
        {
            createLinkBackRequest.execute(new ResponseHandler<Response>()
            {
                public void handle(final Response createLinkBackResponse) throws ResponseException
                {
                    if (createLinkBackResponse.getStatusCode() == 201)
                    {
                        // cool! created reciprocal link, continue
                    }
                    else
                    {
                        throw new ResponseException(String.format("Received %s - %s",
                                createLinkBackResponse.getStatusCode(),
                                createLinkBackResponse.getStatusText()
                        ));
                    }
                }
            });
        }
        catch (final ResponseException e)
        {
            throw new ReciprocalActionException(e);
        }
    }


    public void deleteEntityLinkFrom(final EntityLink remoteEntity, final EntityType localType,
                                     final String localKey) throws ReciprocalActionException, CredentialsRequiredException
    {
        final ApplicationLink applicationLink = remoteEntity.getApplicationLink();

        final URI baseUri = RestUtil.getBaseRestUri(applicationLink);

        final String url = restUrlBuilder
                .getUrlFor(baseUri, EntityLinkResource.class)
                .deleteApplicationEntityLink(getTypeId(remoteEntity.getType()), remoteEntity.getKey(),
                        getTypeId(localType), localKey, internalHostApplication.getId().get(), true)
                .toString();

        final StringBuilder deletionUri = new StringBuilder(url);
        deletionUri.append(String.format("?typeId=%s&key=%s&applicationId=%s",
                getTypeId(localType), localKey, internalHostApplication.getId()));

        final Request deleteReciprocalLinkRequest = applicationLink.createAuthenticatedRequestFactory()
                .createRequest(Request.MethodType.DELETE, deletionUri.toString());

        try
        {
            deleteReciprocalLinkRequest.execute(new ResponseHandler<Response>()
            {
                public void handle(final Response response) throws ResponseException
                {
                    if (response.getStatusCode() == 200)
                    {
                        // deleted reciprocal link, continue
                    }
                    else
                    {
                        throw new ResponseException(String.format("Received %s - %s",
                                response.getStatusCode(),
                                response.getStatusText()
                        ));
                    }
                }
            });
        }
        catch (ResponseException e)
        {
            throw new ReciprocalActionException(e);
        }

    }

}
