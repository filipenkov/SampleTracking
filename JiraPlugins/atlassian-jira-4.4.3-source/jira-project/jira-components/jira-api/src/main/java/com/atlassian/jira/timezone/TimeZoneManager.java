package com.atlassian.jira.timezone;

import java.util.TimeZone;

/**
 * The TimeZoneManager can be used to retrieve the time zone of the logged in user.
 *
 * @since v4.4
 */
public interface TimeZoneManager
{

    /**
     * Return the time zone of the user who is currently logged in.
     *
     * @return the time zone.
     */
    TimeZone getLoggedInUserTimeZone();
}
