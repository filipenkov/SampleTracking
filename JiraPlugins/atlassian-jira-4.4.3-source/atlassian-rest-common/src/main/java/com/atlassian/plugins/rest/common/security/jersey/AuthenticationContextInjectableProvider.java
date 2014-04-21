package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugins.rest.common.security.AuthenticationContext;
import com.google.common.base.Preconditions;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticationContextInjectableProvider extends SingletonTypeInjectableProvider<Context, AuthenticationContext>
{
    public AuthenticationContextInjectableProvider(AuthenticationContext authenticationContext)
    {
        super(AuthenticationContext.class, Preconditions.checkNotNull(authenticationContext));
    }
}
