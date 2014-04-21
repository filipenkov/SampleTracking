package com.atlassian.streams.api;

import com.atlassian.streams.api.StreamsFilterType.Operator;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.api.common.uri.Uri;

import com.google.common.collect.Multimap;

/**
 * A request for activity
 */
public interface ActivityRequest
{
    String PROVIDERS_KEY = "providers";
    String MAX_RESULTS = "maxResults";
    String TIMEOUT = "timeout";
    String USE_ACCEPT_LANG_KEY = "use-accept-lang";

    /**
     * The maximum number of results to return
     *
     * @return The maximum number of results
     */
    int getMaxResults();

    /**
     * The base URI of the request
     * @return the Base URI of the request
     */
    Uri getUri();

    Multimap<String, Pair<Operator, Iterable<String>>> getStandardFilters();

    Multimap<String, Pair<Operator, Iterable<String>>> getProviderFilters();

    /**
     * Get the value of the {@code Accept-Language} HTTP header.
     *
     *  @return the value of the {@code Accept-Language} HTTP header, or {@code null} if not set.
     */
    String getRequestLanguages();
}
