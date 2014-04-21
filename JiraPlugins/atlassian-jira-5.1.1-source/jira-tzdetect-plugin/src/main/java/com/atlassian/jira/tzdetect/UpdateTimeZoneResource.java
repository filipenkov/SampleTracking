package com.atlassian.jira.tzdetect;


import com.atlassian.jira.timezone.TimeZoneInfo;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;

import static org.apache.commons.lang.StringUtils.isBlank;

@Path ("/update")
@Consumes({MediaType.APPLICATION_JSON})
public class UpdateTimeZoneResource
{
    private final BannerPreferences bannerPreferences;

    public UpdateTimeZoneResource(BannerPreferences bannerPreferences)
    {
        this.bannerPreferences = bannerPreferences;
    }


    @POST
    @Produces({MediaType.APPLICATION_JSON})
    public Response update(String id)
    {
        if (isBlank(id))
        {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        bannerPreferences.setUserTimeZonePreference(id);
        TimeZoneInfo userZone = bannerPreferences.getUserTimeZonePreference();
        return Response.ok(new TimeZoneInfoBean(userZone)).build();
    }

    private static class TimeZoneInfoBean
    {
        /**
         * The id of the timezone
         */
        @XmlElement
        private final String timeZoneId;

        /**
         * The i18n'ed display name for this timezone.
         */
        @XmlElement
        private final String displayName;

        /**
         * The GMT offset in the format (GMT[+|-]hh:mm)
         */
        @XmlElement
        private final String gmtOffset;

        /**
         * The name of the city for this timezone.
         */
        @XmlElement
        private final String city;

        /**
         * The key of the region
         */
        @XmlElement
        private final String regionKey;

        private TimeZoneInfoBean(TimeZoneInfo timeZoneInfo)
        {
            timeZoneId  = timeZoneInfo.getTimeZoneId();
            displayName = timeZoneInfo.getDisplayName();
            gmtOffset   = timeZoneInfo.getGMTOffset();
            city        = timeZoneInfo.getCity();
            regionKey   = timeZoneInfo.getRegionKey();
        }
    }
}