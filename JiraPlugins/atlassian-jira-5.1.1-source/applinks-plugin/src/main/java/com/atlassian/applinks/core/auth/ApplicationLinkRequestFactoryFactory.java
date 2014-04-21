package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.sal.api.net.RequestFactory;

public interface ApplicationLinkRequestFactoryFactory
{
    /**
     * @param link the {@link ApplicationLink} to create a {@link RequestFactory} for
     * @return a {@link RequestFactory} configured for the specified {@link ApplicationLink}
     */
    ApplicationLinkRequestFactory getApplicationLinkRequestFactory(ApplicationLink link);

    /**
     * @param link the {@link ApplicationLink} to create a {@link RequestFactory} for
     * @param provider the {@link AuthenticationProvider} to bind the {@link RequestFactory} to
     * @return a {@link RequestFactory} configured for the specified {@link ApplicationLink}, or null if the specified
     * provider is not configured for the specified {@link ApplicationLink}
     */
    ApplicationLinkRequestFactory getApplicationLinkRequestFactory(ApplicationLink link, Class<? extends AuthenticationProvider> provider);
}
