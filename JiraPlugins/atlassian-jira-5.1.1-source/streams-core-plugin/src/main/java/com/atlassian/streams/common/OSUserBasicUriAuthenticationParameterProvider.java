package com.atlassian.streams.common;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.spi.UriAuthenticationParameterProvider;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.google.common.base.Preconditions.checkNotNull;

public final class OSUserBasicUriAuthenticationParameterProvider implements UriAuthenticationParameterProvider
{
    private final UserManager userManager;

    public OSUserBasicUriAuthenticationParameterProvider(UserManager userManager)
    {
        this.userManager = checkNotNull(userManager, "userManager");
    }

    Pair<String, String> param = pair("os_authType", "basic");

    public Option<Pair<String, String>> get()
    {
        if (userManager.getRemoteUsername() != null)
        {
            return some(param);
        }
        else
        {
            return none();
        }
    }
}