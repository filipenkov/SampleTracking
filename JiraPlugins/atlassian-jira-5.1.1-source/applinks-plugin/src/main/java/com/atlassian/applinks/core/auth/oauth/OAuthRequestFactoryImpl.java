package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.AuthorisationAdminURIGenerator;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.rest.context.CurrentContext;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.HostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.user.UserManager;

import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static com.atlassian.applinks.core.util.URIUtil.utf8Encode;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * * atlassian-plugin.xml excerpt: {@code <applinks-authentication-provider class="com.atlassian.applinks.core.auth.oauth.OAuthRequestFactoryImpl">
 * </applinks-authentication-provider>
 *
 * @since 3.0
 */
public class OAuthRequestFactoryImpl implements ApplicationLinkRequestFactory, AuthorisationAdminURIGenerator
{
    // Note that this URI path belongs to the OAuth plugin, not the Applinks plugin.
    // It would be better to discover it programmatically from the OAuth plugin; it's
    // hard-coded here to avoid introducing a dependency on a new OAuth plugin API in
    // Applinks 3.6, which is otherwise compatible with OAuth 1.2.
    private static final String OAUTH_ACCESS_TOKENS_ADMIN_SERVLET_LOCATION =
            "/plugins/servlet/oauth/users/access-tokens";
    
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ApplicationLink applicationLink;
    private final ConsumerService consumerService;
    private final ConsumerTokenStoreService consumerTokenStoreService;
    private final RequestFactory requestFactory;
    private final UserManager userManager;
    private final HostApplication hostApplication;

    public OAuthRequestFactoryImpl(final ApplicationLink applicationLink,
                                   final AuthenticationConfigurationManager authenticationConfigurationManager,
                                   final ConsumerService consumerService, final ConsumerTokenStoreService consumerTokenStoreService,
                                   final RequestFactory requestFactory, final UserManager userManager,
                                   final HostApplication hostApplication)
    {
        this.applicationLink = checkNotNull(applicationLink);
        this.authenticationConfigurationManager = checkNotNull(authenticationConfigurationManager);
        this.consumerService = checkNotNull(consumerService);
        this.consumerTokenStoreService = checkNotNull(consumerTokenStoreService);
        this.requestFactory = checkNotNull(requestFactory);
        this.userManager = checkNotNull(userManager);
        this.hostApplication = checkNotNull(hostApplication);
    }

    public ApplicationLinkRequest createRequest(final Request.MethodType methodType, final String uri) throws CredentialsRequiredException
    {
        final Map<String, String> config = authenticationConfigurationManager
                .getConfiguration(applicationLink.getId(), OAuthAuthenticationProvider.class);

        final ServiceProvider serviceProvider = ServiceProviderUtil.getServiceProvider(config, applicationLink);

        if (config == null)
        {
            throw new IllegalStateException(String.format(
                    "OAuth Authentication is not configured for application link %s", applicationLink));
        }
        final Request request = requestFactory.createRequest(methodType, uri);
        final String username = checkNotNull(userManager.getRemoteUsername(), "You have to be logged in to use oauth authentication.");
        return new OAuthRequest(uri, methodType, request, serviceProvider, consumerService, retrieveConsumerToken(username), consumerTokenStoreService, applicationLink.getId(), username);
    }

    private ConsumerToken retrieveConsumerToken(final String username) throws CredentialsRequiredException
    {
        final ConsumerToken consumerToken = consumerTokenStoreService.getConsumerToken(applicationLink, username);
        // token should never be a request token, we only store access tokens.
        if (consumerToken == null || consumerToken.isRequestToken())
        {
            throw new CredentialsRequiredException(this, "You do not have an authorized access token for the remote resource.");
        }
        return consumerToken;
    }

    public URI getAuthorisationURI()
    {
        final HttpServletRequest request = CurrentContext.getHttpServletRequest();
        URI baseUrl;
        if (request != null)
        {
            baseUrl = RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl());
        }
        else
        {
           baseUrl = hostApplication.getBaseUrl();
        }
        return URIUtil.uncheckedConcatenate(baseUrl,
                "/plugins/servlet/applinks/oauth/login-dance/authorize?applicationLinkID=" + utf8Encode(applicationLink.getId().get()));
    }
    
    public URI getAuthorisationURI(final URI callback)
    {
        return URIUtil.uncheckedToUri(getAuthorisationURI().toString() +
                "&redirectUrl=" + utf8Encode(checkNotNull(callback)));
    }

    public URI getAuthorisationAdminURI()
    {
        return URIUtil.uncheckedConcatenate(applicationLink.getDisplayUrl(),
                                            OAUTH_ACCESS_TOKENS_ADMIN_SERVLET_LOCATION);
    }
}