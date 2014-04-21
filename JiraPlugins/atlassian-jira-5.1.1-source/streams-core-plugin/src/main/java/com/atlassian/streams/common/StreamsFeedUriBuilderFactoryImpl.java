package com.atlassian.streams.common;

import com.atlassian.streams.api.builder.StreamsFeedUriBuilder;
import com.atlassian.streams.api.builder.StreamsFeedUriBuilderFactory;
import com.atlassian.streams.spi.UriAuthenticationParameterProvider;

import org.springframework.beans.factory.annotation.Qualifier;

import static com.google.common.base.Preconditions.checkNotNull;

public class StreamsFeedUriBuilderFactoryImpl implements StreamsFeedUriBuilderFactory
{
    private final UriAuthenticationParameterProvider authProvider;

    public StreamsFeedUriBuilderFactoryImpl(
            @Qualifier("uriAuthParamProvider") UriAuthenticationParameterProvider authProvider)
    {
        this.authProvider = checkNotNull(authProvider, "authProvider");
    }

    public StreamsFeedUriBuilder getStreamsFeedUriBuilder(String baseUrl)
    {
        return new StreamsFeedUriBuilderImpl(baseUrl, authProvider);
    }
}
