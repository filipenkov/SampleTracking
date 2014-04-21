package com.atlassian.applinks.core.rest.exceptionmapper;

import com.atlassian.applinks.core.rest.util.BadParameterException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.atlassian.applinks.core.rest.util.RestUtil.badRequest;

@Provider
public class BadParameterExceptionMapper implements ExceptionMapper<BadParameterException>
{
    public Response toResponse(final BadParameterException e)
    {
        return badRequest(e.getMessage());
    }
}
