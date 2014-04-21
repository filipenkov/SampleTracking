package com.atlassian.plugins.rest.module.util;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/dummy")
public class FooResource
{
    public FooResource()
    {
    }

    @Path("/sub")
    public Response subResource()
    {
        return null;
    }
}
