package com.atlassian.streams.spi;

import java.util.Locale;

/**
 * Provides the {@link Locale}s for the current user and the host application.
 */
public interface StreamsLocaleProvider
{

    /**
     * Gets the default locale for the application.  <em>Must not</em> return {@code null}.
     */
    Locale getApplicationLocale();
    
    /**
     * Gets the locale for the current user.  <em>Must not</em> return {@code null}.
     */
    Locale getUserLocale();
    
}
