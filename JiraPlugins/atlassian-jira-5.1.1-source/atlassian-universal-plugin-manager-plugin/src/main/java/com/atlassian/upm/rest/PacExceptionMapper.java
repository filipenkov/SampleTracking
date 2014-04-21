package com.atlassian.upm.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.atlassian.plugins.PacException;
import com.atlassian.upm.rest.representations.RepresentationFactory;

import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static org.apache.commons.lang.StringUtils.isNumeric;

@Provider
public final class PacExceptionMapper implements ExceptionMapper<PacException>
{
    private static final int BAD_PROXY = 502;
    private final RepresentationFactory representationFactory;

    public PacExceptionMapper(RepresentationFactory representationFactory)
    {
        this.representationFactory = representationFactory;
    }

    public Response toResponse(PacException exception)
    {
        String message = exception.getMessage();
        int statusCode = isNumeric(message) ? Integer.parseInt(message) : BAD_PROXY;
        return Response.status(statusCode)
            .entity(representationFactory.createI18nErrorRepresentation("upm.pac.connection.error"))
            .type(ERROR_JSON)
            .build();
    }
}
