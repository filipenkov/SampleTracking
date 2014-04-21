package com.atlassian.applinks.core.auth.basic;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider;
import com.atlassian.applinks.core.auth.ApplicationLinkRequestAdaptor;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;

import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * atlassian-plugin.xml excerpt:
 * {@code
 * <applinks-authentication-provider
 * class="com.atlassian.applinks.core.auth.basic.BasicAuthRequestFactoryImpl">
 * </applinks-authentication-provider>
 * }
 */
public class BasicAuthRequestFactoryImpl implements ApplicationLinkRequestFactory
{
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";

    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ApplicationLink applicationLink;
    private final RequestFactory requestFactory;

    public BasicAuthRequestFactoryImpl(final AuthenticationConfigurationManager authenticationConfigurationManager,
                              final ApplicationLink applicationLink, final RequestFactory requestFactory)
    {
        this.authenticationConfigurationManager = checkNotNull(authenticationConfigurationManager);
        this.applicationLink = checkNotNull(applicationLink);
        this.requestFactory = checkNotNull(requestFactory);
    }

    public ApplicationLinkRequest createRequest(final Request.MethodType methodType, final String s)
    {
        final Map<String, String> config = authenticationConfigurationManager
                .getConfiguration(applicationLink.getId(), BasicAuthenticationProvider.class);

        if (config == null)
        {
            throw new IllegalStateException(String.format(
                    "Basic HTTP Authentication is not configured for application link %s", applicationLink));
        }
        else
        {
            return new ApplicationLinkRequestAdaptor(
                    requestFactory.createRequest(methodType, s))
                            .addBasicAuthentication(config.get(USERNAME_KEY), config.get(PASSWORD_KEY));
        }

    }

    public URI getAuthorisationURI()
    {
        return null;
    }

    public URI getAuthorisationURI(final URI callback)
    {
        return null;
    }

}