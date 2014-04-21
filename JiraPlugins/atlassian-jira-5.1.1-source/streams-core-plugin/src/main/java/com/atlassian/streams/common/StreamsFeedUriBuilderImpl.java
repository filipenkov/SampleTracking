package com.atlassian.streams.common;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.atlassian.streams.api.StreamsFilterType.Operator;
import com.atlassian.streams.api.builder.StreamsFeedUriBuilder;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.api.common.uri.Uris;
import com.atlassian.streams.spi.UriAuthenticationParameterProvider;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import static com.atlassian.streams.api.ActivityRequest.PROVIDERS_KEY;
import static com.atlassian.streams.api.ActivityRequest.USE_ACCEPT_LANG_KEY;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.STANDARD_FILTERS_PROVIDER_KEY;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

public class StreamsFeedUriBuilderImpl implements StreamsFeedUriBuilder
{
    private final String baseUrl;
    private final Multimap<String, String> parameters = ArrayListMultimap.create();
    private final UriAuthenticationParameterProvider authProvider;
    private final Set<String> providers = newHashSet();

    private Integer maxResults;
    private Integer timeout;

    public StreamsFeedUriBuilderImpl(final String baseUrl, UriAuthenticationParameterProvider authProvider)
    {
        this.baseUrl = baseUrl;
        this.authProvider = authProvider;
    }

    public URI getUri()
    {
        return buildUri("/activity");
    }

    public URI getServletUri()
    {
        return buildUri("/plugins/servlet/streams");
    }

    private URI buildUri(String path)
    {
        final StringBuilder url = new StringBuilder(baseUrl);
        url.append(path);
        String sep = "?";
        for (final Map.Entry<String, String> propEntry : parameters.entries())
        {
            url.append(sep).append(Uris.encode(propEntry.getKey())).append("=").append(Uris.encode(propEntry.getValue()));
            sep = "&";
        }
        if (!providers.isEmpty())
        {
            url.append(sep).append(PROVIDERS_KEY).append('=').append(Uris.encode(Joiner.on(' ').join(providers)));
        }
        if (maxResults != null)
        {
            url.append(sep).append("maxResults").append("=").append(maxResults);
        }
        if (timeout != null)
        {
            url.append(sep).append("timeout").append("=").append(timeout);
        }
        return URI.create(url.toString()).normalize();
    }

    /**
     * Set the max results
     */
    public StreamsFeedUriBuilder setMaxResults(final int maxResults)
    {
        this.maxResults = maxResults;
        return this;
    }

    public StreamsFeedUriBuilder setTimeout(final int timeout)
    {
        this.timeout = timeout;
        return this;
    }

    public StreamsFeedUriBuilder addApplication(String name)
    {
        parameters.put("application", name);
        return this;
    }

    public StreamsFeedUriBuilder addLocalOnly(boolean localOnly)
    {
        parameters.put("local", Boolean.toString(localOnly));
        return this;
    }

    public StreamsFeedUriBuilder addProviderFilter(String providerKey, String filterKey, Pair<Operator, Iterable<String>> filter)
    {
        parameters.put(providerKey, toString(filterKey, filter));
        return this;
    }

    public StreamsFeedUriBuilder addStandardFilter(String filterKey, Operator op, String value)
    {
        return addStandardFilter(filterKey, Pair.<Operator, Iterable<String>>pair(op, ImmutableList.of(value)));
    }

    public StreamsFeedUriBuilder addStandardFilter(String filterKey, Operator op, Date date)
    {
        return addStandardFilter(filterKey, Pair.<Operator, Iterable<String>>pair(op, ImmutableList.of(Long.toString(date.getTime()))));
    }

    public StreamsFeedUriBuilder addStandardFilter(String filterKey, Operator op, Iterable<String> values)
    {
        return addStandardFilter(filterKey, Pair.pair(op, values));
    }

    public StreamsFeedUriBuilder addStandardFilter(String filterKey, Pair<Operator, Iterable<String>> filter)
    {
        parameters.put(STANDARD_FILTERS_PROVIDER_KEY, toString(filterKey, filter));
        return this;
    }

    public StreamsFeedUriBuilder addAuthenticationParameterIfLoggedIn()
    {
        for (Pair<String, String> auth : authProvider.get())
        {
            parameters.put(auth.first(), auth.second());
        }

        return this;
    }

    private String toString(String filterKey, Pair<Operator, Iterable<String>> filter)
    {
        return filterKey + " " + filter.first() + " " + Joiner.on(' ').join(transform(filter.second(), escapeValue));
    }

    public StreamsFeedUriBuilder addLegacyFilterUser(String filterUser)
    {
        parameters.put("filterUser", filterUser);
        return this;
    }

    public StreamsFeedUriBuilder addLegacyKey(String key)
    {
        parameters.put("key", key);
        return this;
    }

    public StreamsFeedUriBuilder setLegacyMaxDate(long maxDate)
    {
        parameters.put("maxDate", Long.toString(maxDate));
        return this;
    }

    public StreamsFeedUriBuilder setLegacyMinDate(long minDate)
    {
        parameters.put("minDate", Long.toString(minDate));
        return this;
    }

    public StreamsFeedUriBuilder addProvider(String key)
    {
        providers.add(key);
        return this;
    }

    public StreamsFeedUriBuilder addUseAcceptLang(boolean useAcceptLang)
    {
        parameters.put(USE_ACCEPT_LANG_KEY, Boolean.toString(useAcceptLang));
        return this;
    }
    
    private static final Function<String, String> escapeValue = new Function<String, String>()
    {
        public String apply(String from)
        {
            // this is the same escaping defined in handleSpecialCases() in activity-streams-parent.js
            return from.replace("_", "\\_").replace(" ", "_");
        }
    };
}
