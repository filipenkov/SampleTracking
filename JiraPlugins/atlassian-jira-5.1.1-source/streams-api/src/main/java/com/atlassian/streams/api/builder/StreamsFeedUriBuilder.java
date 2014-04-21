package com.atlassian.streams.api.builder;

import java.net.URI;
import java.util.Date;

import com.atlassian.streams.api.StreamsFilterType.Operator;
import com.atlassian.streams.api.common.Pair;

/**
 * A builder for constructing feed URIs
 */
public interface StreamsFeedUriBuilder
{
    /**
     * Get the shortened URI
     *
     * @return The shortened URI
     */
    URI getUri();

    /**
     * Get the complete servlet URI
     *
     * @return The complete servlet URI
     */
    URI getServletUri();

    /**
     * Set the max results
     *
     * @param maxResults the maximum number of results to return
     */
    StreamsFeedUriBuilder setMaxResults(int maxResults);

    /**
     * Set the timeout
     *
     * @param timeout the maximum number of milliseconds to allow for a response
     */
    StreamsFeedUriBuilder setTimeout(int timeout);

    /**
     * Add a standard filter parameter to the query string.  Standard filter parameters have the query key of
     * 'com.atlassian.streams'.  The query string value will be '{filterKey} {op} {value}'.
     *
     * @param filterKey Key of the thing to filter on
     * @param op Operator to use when filtering
     * @param value Value to use in the filtering operation
     * @return this URI builder
     */
    StreamsFeedUriBuilder addStandardFilter(String filterKey, Operator op, String value);

    /**
     * Add a standard filter parameter to the query string, with a date value.  Standard filter parameters have the
     * query key of 'com.atlassian.streams'.  The query string value will be '{filterKey} {op} {date.inMillis}'.  The
     * date will be converted to milliseconds since epoch.
     *
     * @param filterKey Key of the thing to filter on
     * @param op Operator to use when filtering
     * @param date Date to convert to millis and use as the filter value
     * @return this URI builder
     */
    StreamsFeedUriBuilder addStandardFilter(String filterKey, Operator op, Date date);

    /**
     * Add a standard filter parameter to the query string.  Standard filter parameters have the
     * query key of 'com.atlassian.streams'.  The query string value will be
     * '{filterKey} {op} {value0} {value1} ... {valueN}'.
     *
     * @param filterKey Key of the thing to filter on
     * @param filter Operator and values to be used in the filter operation
     * @return this URI builder
     */
    StreamsFeedUriBuilder addStandardFilter(String filterKey, Pair<Operator, Iterable<String>> filter);

    /**
     * Add a standard filter parameter to the query string.  Standard filter parameters have the
     * query key of 'com.atlassian.streams'.  The query string value will be
     * '{filterKey} {op} {value0} {value1} ... {valueN}'.
     *
     * @param filterKey Key of the thing to filter on
     * @param op Operator to use when filtering
     * @param values Values to use in the filtering operation
     * @return this URI builder
     */
    StreamsFeedUriBuilder addStandardFilter(String filterKey, Operator op, Iterable<String> values);

    /**
     * Add a provider specific filter parameter to the query string.  The provider key will be used as the query key.
     * The query string value will be
     * '{filterKey} {op} {value0} {value1} ... {valueN}'.
     *
     * @param filterKey Key of the thing to filter on
     * @param filter Operator and values to be used in the filter operation
     * @return this URI builder
     */
    StreamsFeedUriBuilder addProviderFilter(String providerKey, String filterKey, Pair<Operator, Iterable<String>> filter);

    /**
     * Add the local flag to the query string, specifying that only local content should be used to build the response.
     *
     * @param localOnly Whether to use only local content to build the response
     * @return this URI builder
     */
    StreamsFeedUriBuilder addLocalOnly(boolean localOnly);

    /**
     * Add the given key (project key, space key etc)
     *
     * @param key The key
     */
    StreamsFeedUriBuilder addLegacyKey(String key);

    /**
     * Add the given user to filter by
     *
     * @param filterUser the user
     */
    StreamsFeedUriBuilder addLegacyFilterUser(String filterUser);

    /**
     * Add a flag specifying that the provider should respect the Accept-Language header.
     *
     * @param useAcceptLang Whether the activity provider should respect the Accept-Language header
     * @return this URI builder
     */
    StreamsFeedUriBuilder addUseAcceptLang(boolean useAcceptLang);

    /**
     * Set the minimum date bound
     *
     * @param minDate the minimum date bound
     */
    StreamsFeedUriBuilder setLegacyMinDate(long minDate);

    /**
     * Set the maximum date bound
     *
     * @param maxDate the maximum date bound
     */
    StreamsFeedUriBuilder setLegacyMaxDate(long maxDate);

    /**
     * Adds the local authentication parameter to the URI if the user is logged in.  For instance, in JIRA this would
     * add 'os_authType=basic' while on Fe/Cru it would add the Fisheye auth token.
     *
     * @return this builder
     */
    StreamsFeedUriBuilder addAuthenticationParameterIfLoggedIn();

    /**
     * Adds the provider to the list of providers that should be used to load data.  If none are added, all providers
     * will be used.
     *
     * @return this builder
     */
    StreamsFeedUriBuilder addProvider(String key);
}
