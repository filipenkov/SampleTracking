package com.atlassian.streams.spi;

import java.util.Locale;

/**
 * An implementation of {@link StreamsLocaleProvider} that provides the default {@link Locale}.
 * This {@code StreamsLocaleProvider} may be used for activity providers that are not localized.
 */
public class DefaultStreamsLocaleProvider implements StreamsLocaleProvider
{
    public Locale getApplicationLocale()
    {
        return Locale.getDefault();
    }
    
    public Locale getUserLocale()
    {
        return Locale.getDefault();
    }
}
