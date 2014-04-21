package com.atlassian.streams.api.builder;

/**
 * Factory for streams feed URI builders
 */
public interface StreamsFeedUriBuilderFactory
{
    /**
     * Get a builder for the given base URL
     *
     * @param baseUrl The base URL
     * @return The builder
     */
    StreamsFeedUriBuilder getStreamsFeedUriBuilder(String baseUrl);
}
