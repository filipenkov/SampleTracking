package com.atlassian.streams.common;

import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.spi.OptionalService;
import com.atlassian.streams.spi.UriAuthenticationParameterProvider;

import static com.google.common.base.Preconditions.checkNotNull;

public class SwitchingUriAuthenticationParameterProvider
        extends OptionalService<UriAuthenticationParameterProvider> implements UriAuthenticationParameterProvider
{
    private final UriAuthenticationParameterProvider defaultProvider;

    public SwitchingUriAuthenticationParameterProvider(UriAuthenticationParameterProvider defaultProvider)
    {
        super(UriAuthenticationParameterProvider.class);
        this.defaultProvider = checkNotNull(defaultProvider, "defaultProvider");
    }

    public Option<Pair<String, String>> get()
    {
        return getService().getOrElse(defaultProvider).get();
    }
}
