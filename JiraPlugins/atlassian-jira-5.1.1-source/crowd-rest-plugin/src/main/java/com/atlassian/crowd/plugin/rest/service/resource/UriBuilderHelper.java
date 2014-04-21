package com.atlassian.crowd.plugin.rest.service.resource;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

class UriBuilderHelper
{
    static UriBuilder buildBaseUserPath(UriInfo uriInfo)
    {
        final UriBuilder builder = uriInfo.getBaseUriBuilder();
        builder.path("user").path("{username}");
        return builder;
    }

    static UriBuilder buildUserWithAttributesPath(UriInfo uriInfo)
    {
        return buildBaseUserPath(uriInfo).path("attribute");
    }

    static UriBuilder buildBaseGroupPath(UriInfo uriInfo)
    {
        final UriBuilder builder = uriInfo.getBaseUriBuilder();
        builder.path("group").queryParam("{groupname}");
        return builder;
    }

    static UriBuilder buildGroupWithAttributesPath(UriInfo uriInfo)
    {
        return buildBaseGroupPath(uriInfo).path("attribute");
    }
}