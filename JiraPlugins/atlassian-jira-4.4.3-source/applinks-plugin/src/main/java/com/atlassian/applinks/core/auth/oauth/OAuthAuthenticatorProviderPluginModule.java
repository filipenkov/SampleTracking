package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.ServletPathConstants;
import com.atlassian.applinks.core.auth.AbstractAuthServlet;
import com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider.AddConsumerByUrlServlet;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationException;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.spi.auth.AuthenticationDirection;
import com.atlassian.applinks.spi.auth.AuthenticationScenario;
import com.atlassian.applinks.spi.auth.AutoConfiguringAuthenticatorProviderPluginModule;
import com.atlassian.applinks.spi.auth.IncomingTrustAuthenticationProviderPluginModule;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import com.atlassian.sal.api.user.UserManager;
import org.osgi.framework.Version;

import java.net.URI;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;

import static com.atlassian.applinks.ui.XsrfProtectedServlet.OVERRIDE_HEADER_NAME;
import static com.atlassian.applinks.ui.XsrfProtectedServlet.OVERRIDE_HEADER_VALUE;

/**
 * The OAuthAuthenticatorProviderPluginModule returns a {@link com.atlassian.applinks.core.auth.oauth.OAuthRequestFactoryImpl} to make authenticated requests
 * using OAuth and also contains the logic to determine the correct URL for the configuration UI.
 * There are four different UIs that can be displayed:
 * Incoming:
 * 1) If the remote application has the OAuth plugin installed or the UAL plugin, we can obtain the consumer information from the OAuth plugin and register this application as a
 *    consumer.
 *
 * 2) Otherwise we show a screen where the user can enter all the consumer details manually.
 *
 *
 * Outgoing:
 * 3) If the remote application has the OAuth plugin installed or the UAL plugin, we can enable OAuth locally and the remote application can read our consumer information
 *    via the OAuth plugin, thus this config UI displays only a enable/disable button.
 *
 * 4) Otherwise we show a screen where the user can enter all the service provider details manually.
 *
 * @since 3.0
 */
public class OAuthAuthenticatorProviderPluginModule implements AutoConfiguringAuthenticatorProviderPluginModule, IncomingTrustAuthenticationProviderPluginModule
{
    private static final String ADD_CONSUMER_MANUALLY_SERVLET_LOCATION =
            ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/oauth/add-consumer-manually/";
    private static final String ADD_CONSUMER_BY_URL_SERVLET_LOCATION =
            ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/oauth/add-consumer-by-url/";
    private static final String OUTBOUND_NON_APPLINKS_SERVLET_LOCATION =
            ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/oauth/outbound/3rdparty/";
    private static final String OUTBOUND_ATLASSIAN_SERVLET_LOCATION =
            ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/oauth/outbound/atlassian/";

    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ConsumerService consumerService;
    private final ConsumerTokenStoreService consumerTokenStoreService;
    private final InternalHostApplication hostApplication;
    private final RequestFactory requestFactory;
    private final UserManager userManager;
    private final ServiceProviderStoreService serviceProviderStoreService;

    public OAuthAuthenticatorProviderPluginModule(
            final AuthenticationConfigurationManager authenticationConfigurationManager,
            final ConsumerService consumerService,
            final ConsumerTokenStoreService consumerTokenStoreService,
            final InternalHostApplication hostApplication,
            final RequestFactory requestFactory,
            final UserManager userManager,
            final ServiceProviderStoreService serviceProviderStoreService)
    {
        this.authenticationConfigurationManager = authenticationConfigurationManager;
        this.consumerService = consumerService;
        this.consumerTokenStoreService = consumerTokenStoreService;
        this.hostApplication = hostApplication;
        this.requestFactory = requestFactory;
        this.userManager = userManager;
        this.serviceProviderStoreService = serviceProviderStoreService;
    }

    public AuthenticationProvider getAuthenticationProvider(final ApplicationLink link)
    {
        AuthenticationProvider provider = null;
        if (authenticationConfigurationManager.isConfigured(link.getId(), OAuthAuthenticationProvider.class))
        {
            provider = new OAuthAuthenticationProvider()
            {

                public ApplicationLinkRequestFactory getRequestFactory(final String username)
                {
                    return new OAuthRequestFactoryImpl(
                            link,
                            authenticationConfigurationManager,
                            consumerService,
                            consumerTokenStoreService,
                            requestFactory,
                            userManager,
                            hostApplication);
                }
            };
        }
        return provider;
    }

    public String getConfigUrl(final ApplicationLink link, final Version applicationLinksVersion, AuthenticationDirection direction, final HttpServletRequest request)
    {
        final String configUri;
        final boolean supportsAppLinks = applicationLinksVersion != null;   // TODO: maybe safer to check for < 3.0

        //If the application is has the OAuth Plugin installed, we can use the same screen as for applications that have UAL installed.
        final boolean oAuthPluginInstalled = OAuthHelper.isOAuthPluginInstalled(link);

        if (direction == AuthenticationDirection.OUTBOUND)
        {
            if (supportsAppLinks)
            {
                // render the peer's inbound servlet
                configUri = link.getDisplayUrl() +
                        ADD_CONSUMER_BY_URL_SERVLET_LOCATION +
                        hostApplication.getId() + "?" + AddConsumerByUrlServlet.UI_POSITION + "=remote&" + AbstractAuthServlet.HOST_URL_PARAM + "=" + URIUtil.utf8Encode(RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl())).toString();
            }
            else if (oAuthPluginInstalled)
            {
                configUri = RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl()) +
                        OUTBOUND_ATLASSIAN_SERVLET_LOCATION + link.getId().toString();
            } else
            {
                configUri = RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl()) +
                        OUTBOUND_NON_APPLINKS_SERVLET_LOCATION +
                        link.getId().toString();
            }
        }
        else
        {
            if (supportsAppLinks || oAuthPluginInstalled)
            {
                configUri = RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl()) +
                        ADD_CONSUMER_BY_URL_SERVLET_LOCATION +
                        link.getId().toString() + "?" + AddConsumerByUrlServlet.UI_POSITION + "=local";
            }
            else
            {
                configUri = RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl()) +
                        ADD_CONSUMER_MANUALLY_SERVLET_LOCATION +
                        link.getId().toString();
            }
        }
        return configUri;
    }

    public Class<? extends AuthenticationProvider> getAuthenticationProviderClass()
    {
        return OAuthAuthenticationProvider.class;
    }

    public boolean isApplicable(final AuthenticationScenario authenticationScenario, final ApplicationLink applicationLink)
    {
        return authenticationScenario.isTrusted();
    }

    public void enable(final RequestFactory authenticatedRequestFactory,
                       final ApplicationLink applicationLink)
            throws AuthenticationConfigurationException
    {
        enableRemoteTrust(authenticatedRequestFactory, applicationLink);
        try
        {
            authenticationConfigurationManager.registerProvider(applicationLink.getId(), OAuthAuthenticationProvider.class, Collections.<String, String>emptyMap());
            final Consumer consumer = OAuthHelper.fetchConsumerInformation(applicationLink);
            serviceProviderStoreService.addConsumer(consumer, applicationLink);
        }
        catch (Exception e)
        {
            throw new AuthenticationConfigurationException("Failed to " +
                    "auto-configure OAuth authentication locally for " +
                    "application link '" + applicationLink + "'", e);
        }
    }

    public void disable(final RequestFactory authenticatedRequestFactory,
                        final ApplicationLink applicationLink)
            throws AuthenticationConfigurationException
    {
        consumerTokenStoreService.removeAllConsumerTokens(applicationLink);
        authenticationConfigurationManager.unregisterProvider(applicationLink.getId(), OAuthAuthenticationProvider.class);
        serviceProviderStoreService.removeConsumer(applicationLink);
        disableRemoteTrust(authenticatedRequestFactory, applicationLink);
    }

    private void enableRemoteTrust(final RequestFactory<Request<Request<?, Response>,Response>> requestFactory,
                                   final ApplicationLink applicationLink)
            throws AuthenticationConfigurationException
    {
        configureRemoteTrust(requestFactory, applicationLink, Request.MethodType.PUT);
    }

    private void disableRemoteTrust(final RequestFactory<Request<Request<?, Response>,Response>> requestFactory,
                                    final ApplicationLink applicationLink)
            throws AuthenticationConfigurationException
    {
        configureRemoteTrust(requestFactory, applicationLink, Request.MethodType.DELETE);
    }

    private void configureRemoteTrust(final RequestFactory<Request<Request<?, Response>,Response>> authenticatedRequestFactory,
                                      final ApplicationLink applicationLink,
                                      final Request.MethodType action)
            throws AuthenticationConfigurationException
    {
        final URI autoConfigUrl = URIUtil.uncheckedConcatenate(applicationLink.getRpcUrl(),
                ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/oauth/autoconfig/" + hostApplication.getId().toString());
        final Request<Request<?,Response>, Response> request = authenticatedRequestFactory.createRequest(action, autoConfigUrl.toString());
        request.addHeader(OVERRIDE_HEADER_NAME, OVERRIDE_HEADER_VALUE);
        boolean autoConfigSuccessful;
        try
        {
            autoConfigSuccessful = request.executeAndReturn(new ReturningResponseHandler<Response,Boolean>()
            {
                public Boolean handle(final Response response) throws ResponseException
                {
                    return response.isSuccessful();
                }
            });
        }
        catch (ResponseException e)
        {
            autoConfigSuccessful = false;
        }

        if (!autoConfigSuccessful)
        {
            throw new AuthenticationConfigurationException(
                    "Failed to auto-configure OAuth authentication in remote " +
                            "application. Application link '" + applicationLink + "'");
        }
    }

    public boolean incomingEnabled(final ApplicationLink applicationLink)
    {
        return serviceProviderStoreService.getConsumer(applicationLink) != null;
    }
}