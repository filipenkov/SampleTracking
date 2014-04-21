package com.atlassian.applinks.core.rest.util;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.core.rest.model.ErrorListEntity;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.Status;
import com.atlassian.sal.api.message.I18nResolver;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.atlassian.applinks.core.util.URIUtil.concatenate;

public class RestUtil
{
    public static final String REST_APPLINKS_URL = "/rest/applinks/1.0/";

    public static Response ok()
    {
        return Response.ok().entity(Status.ok().build()).build();
    }

    public static Response ok(final String message)
    {
        return Response.ok().entity(Status.ok().message(message).build()).build();
    }

    public static Response ok(final Object entity)
    {
        return Response.ok(entity).build();
    }

    public static Response noContent()
    {
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * @param errors error message(s) to return
     * @return a 400 bad request error
     *         <p/>
     *         TODO Status should have a badRequest() builder
     */
    public static Response badRequest(final String... errors)
    {
        return Response.status(400).entity(new ErrorListEntity(400, errors)).build();
    }

    public static Response badFormRequest(final List<String> errors, List<String> fields)
    {
        return Response.status(400).entity(new ErrorListEntity(400, errors, fields)).build();
    }

    public static Response serverError(final String message)
    {
        return Response.status(500).entity(Status.error().message(message).build()).build();
    }

    public static Response notFound(final String message)
    {
        return Response.status(404).entity(Status.notFound().message(message).build()).build();
    }

    public static Response unauthorized(final String message)
    {
        return Response.status(401).entity(Status.unauthorized().message(message).build()).build();
    }

    public static Response credentialsRequired(final I18nResolver i18nResolver)
    {
        return unauthorized(i18nResolver.getText("applinks.remote.operation.failed.credentials.required"));
    }

    // TODO: the REST guidelines state that a 201 should contain a self link
    // in the HTTP headers (the "location" response header) and the full
    // entity in the body
    public static Response created(final Link link)
    {
        return Response.status(201).entity(Status.created(link).build()).build();
    }

    public static Response created()
    {
        return Response.status(201).build();
    }

    public static Response updated(final Link link)
    {
        return Response.ok(Status.ok().updated(link).build()).build();
    }

    public static Response updated(final Link link, final String message)
    {
        return Response.ok(Status.ok().updated(link).message(message).build()).build();
    }

    public static void checkParam(final String name, final Object value)
    {
        if (value == null)
        {
            throw new BadParameterException(name);
        }
    }

    public static URI getBaseRestUri(final ApplicationLink applicationLink)
    {
        return getBaseRestUri(applicationLink.getRpcUrl());
    }

    public static URI getBaseRestUri(final URI baseUri)
    {
        try
        {
            return concatenate(baseUri, "/rest/applinks/1.0");//todo hack - how should we determine this
        }
        catch (URISyntaxException e)
        {
            //URI is valid - this should never be thrown
            throw new RuntimeException(String.format("Failed to add REST base path to baseUri: %s",
                    baseUri.toASCIIString()), e);
        }
    }

    public static Response typeNotInstalled(final TypeId typeId)
    {
        return badRequest(String.format("No type with id %s installed", typeId));
    }
}
