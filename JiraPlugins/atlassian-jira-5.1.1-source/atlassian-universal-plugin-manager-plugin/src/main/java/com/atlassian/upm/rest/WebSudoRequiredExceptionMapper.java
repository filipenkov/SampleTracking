package com.atlassian.upm.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.atlassian.plugins.rest.common.sal.websudo.WebSudoRequiredException;
import com.atlassian.upm.rest.representations.RepresentationFactory;

import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Provider
public final class WebSudoRequiredExceptionMapper implements ExceptionMapper<WebSudoRequiredException>
{
    private final RepresentationFactory representationFactory;

    public WebSudoRequiredExceptionMapper(RepresentationFactory representationFactory)
    {
        this.representationFactory = representationFactory;
    }

    public Response toResponse(WebSudoRequiredException exception)
    {
        return Response.status(UNAUTHORIZED).entity(representationFactory.createI18nErrorRepresentation("upm.websudo.error")).type(ERROR_JSON).build();
    }
}