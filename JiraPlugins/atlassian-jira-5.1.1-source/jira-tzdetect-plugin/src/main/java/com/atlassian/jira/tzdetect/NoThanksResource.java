package com.atlassian.jira.tzdetect;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.apache.commons.lang.StringUtils.isBlank;

@Path ("/nothanks")
@Consumes("application/json")
public class NoThanksResource
{
    private final BannerPreferences bannerPreferences;

    public NoThanksResource(BannerPreferences bannerPreferences)
    {
        this.bannerPreferences = bannerPreferences;
    }

    @POST
    public void noThanks(String id)
    {
        if (isBlank(id))
        {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        bannerPreferences.setNoThanksTimeZone(id);
    }
}
