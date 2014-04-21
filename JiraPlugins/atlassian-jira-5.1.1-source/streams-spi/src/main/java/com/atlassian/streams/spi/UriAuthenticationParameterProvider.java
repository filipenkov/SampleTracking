package com.atlassian.streams.spi;

import com.atlassian.streams.api.builder.StreamsFeedUriBuilder;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;

/**
 * Used by the {@link StreamsFeedUriBuilder} to add an authentication parameter to the feed URI.
 */
public interface UriAuthenticationParameterProvider
{
    /**
     * @return the authentication parameter key and value 
     */
    Option<Pair<String, String>> get();
}
