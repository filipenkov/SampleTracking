package com.atlassian.streams.spi;

import org.joda.time.DateTimeZone;

/**
 * Provides format preferences for the host application.
 *
 * @since v5.0
 */
public interface FormatPreferenceProvider
{
    /**
     * Returns the application's time format preference.
     * 
     * @return the application's time format preference.
     */
    String getTimeFormatPreference();

    /**
     * Returns the application's time format preference.
     *
     * @return the application's time format preference.
     */
    String getDateFormatPreference();

    /**
     * Returns the application's date time format preference.
     *
     * @return the application's date time format preference.
     */
    String getDateTimeFormatPreference();

    /**
     * Returns the time zone of the logged in user.
     * NB: This is guaranteed to return a non-null value.
     * If no user is logged in (anonymous user) or the system doesn't support time zone configuration or no specific
     * time zone is configured, it should still return a time zone. (e.g. the default time zone for the system).
     *
     * @return the user's time zone. Should never return null.
     */
    DateTimeZone getUserTimeZone();
}
