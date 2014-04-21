package com.atlassian.applinks.core;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.spi.auth.AuthenticationDirection;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import org.osgi.framework.Version;

import javax.servlet.http.HttpServletRequest;

public class MockAuthenticationProviderPluginModule implements AuthenticationProviderPluginModule
{
    private final AuthenticationProvider module;

    public MockAuthenticationProviderPluginModule(final AuthenticationProvider module)
    {
        this.module = module;
    }

    public String getKey()
    {
        throw new UnsupportedOperationException();
    }

    public String getI18nKey()
    {
        throw new UnsupportedOperationException();
    }

    public AuthenticationProvider getAuthenticationProvider(final ApplicationLink link)
    {
        return module;
    }

    public String getConfigUrl(final ApplicationLink link, final Version applicationLinksVersion, final AuthenticationDirection direction, final HttpServletRequest httpServletRequest)
    {
        throw new UnsupportedOperationException();
    }

    public Class<? extends AuthenticationProvider> getAuthenticationProviderClass()
    {
        return module.getClass();
    }
}
