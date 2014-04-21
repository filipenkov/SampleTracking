package com.atlassian.applinks.core.auth.trusted;

import com.atlassian.applinks.core.auth.AbstractAuthServlet;
import com.atlassian.applinks.core.auth.oauth.RequestUtil;
import com.atlassian.applinks.core.util.Holder;
import com.atlassian.applinks.host.spi.HostApplication;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.types.TrustedAppsAuthenticationProvider;
import com.atlassian.applinks.core.ServletPathConstants;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationException;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.spi.auth.AuthenticationDirection;
import com.atlassian.applinks.spi.auth.AuthenticationScenario;
import com.atlassian.applinks.spi.auth.AutoConfiguringAuthenticatorProviderPluginModule;
import com.atlassian.applinks.spi.auth.IncomingTrustAuthenticationProviderPluginModule;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import org.osgi.framework.Version;

import java.net.URI;
import javax.servlet.http.HttpServletRequest;

import static com.atlassian.applinks.ui.XsrfProtectedServlet.OVERRIDE_HEADER_NAME;
import static com.atlassian.applinks.ui.XsrfProtectedServlet.OVERRIDE_HEADER_VALUE;

/**
 *
 * @since v3.0
 */
public class TrustedAppsAuthenticationProviderPluginModule implements AutoConfiguringAuthenticatorProviderPluginModule, IncomingTrustAuthenticationProviderPluginModule
{
    public static final String CONSUMER_SERVLET_LOCATION_UAL =
            ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/trusted/outbound-ual/";
    public static final String CONSUMER_SERVLET_LOCATION_LEGACY =
            ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/trusted/outbound-non-ual/";

    public static final String PROVIDER_SERVLET_LOCATION_UAL =
            ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/trusted/inbound-ual/";
    public static final String PROVIDER_SERVLET_LOCATION_LEGACY =
            ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/trusted/inbound-non-ual/";

    public static final String AUTOCONFIGURE_SERVLET_LOCATION =
            ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/trusted/autoconfig/";

    private final HostApplication hostApplication;
    private final AuthenticationConfigurationManager configurationManager;
    private final TrustedApplicationsManager trustedApplicationsManager;
    private final RequestFactory requestFactory;
    private final TrustConfigurator trustConfigurator;

    public TrustedAppsAuthenticationProviderPluginModule(final HostApplication hostApplication,
                                                         final AuthenticationConfigurationManager configurationManager,
                                                         final TrustedApplicationsManager trustedApplicationsManager,
                                                         final RequestFactory requestFactory,
                                                         final TrustConfigurator trustConfigurator)
    {
        this.hostApplication = hostApplication;
        this.configurationManager = configurationManager;
        this.requestFactory = requestFactory;
        this.trustedApplicationsManager = trustedApplicationsManager;
        this.trustConfigurator = trustConfigurator;
    }

    public AuthenticationProvider getAuthenticationProvider(ApplicationLink link)
    {
        AuthenticationProvider provider = null;
        if (configurationManager.isConfigured(link.getId(), getAuthenticationProviderClass()))
        {
            provider = new TrustedAppsAuthenticationProvider ()
            {
                public ApplicationLinkRequestFactory getRequestFactory(final String username)
                {
                    return new TrustedApplicationsRequestFactory(
                            trustedApplicationsManager.getCurrentApplication(),
                            requestFactory, username);
                }
            };
        }
        return provider;
    }

    public String getConfigUrl(final ApplicationLink link, final Version applicationLinksVersion, AuthenticationDirection direction, final HttpServletRequest request)
    {
        final boolean peerHasUAL = applicationLinksVersion != null; // TODO: check for >= 3.0
        switch (direction)
        {
            case INBOUND:
                /**
                 * Inbound configuration is always rendered locally.
                 * The only difference between ual- and non-ual peer is that
                 * with the latter we don't make a reciprocal call.
                 */
                return URIUtil
                        .uncheckedConcatenate(RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl()),
                                (peerHasUAL ? PROVIDER_SERVLET_LOCATION_UAL : PROVIDER_SERVLET_LOCATION_LEGACY) +
                                        link.getId().toString())
                        .toString();
            default:
                /**
                 * Render the inbound servlet from the remote machine so that it
                 * requires the user to log in as admin and we can display
                 * trust restrictions.
                 * For non-ual peers we render the consumer UI locally (cannot
                 * display ip/url restrictions).
                 */
                if (peerHasUAL)
                {
                    return URIUtil.uncheckedConcatenate(link.getDisplayUrl(),
                            PROVIDER_SERVLET_LOCATION_UAL +
                                    hostApplication.getId().toString()) + "?" + AbstractAuthServlet.HOST_URL_PARAM + "=" + URIUtil.utf8Encode(RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl()));
                }
                else
                {
                    return URIUtil.uncheckedConcatenate(RequestUtil.getBaseURLFromRequest(request, hostApplication.getBaseUrl()),
                            CONSUMER_SERVLET_LOCATION_LEGACY +
                                    link.getId().toString())
                            .toString();
                }
        }
    }

    public Class<? extends AuthenticationProvider> getAuthenticationProviderClass()
    {
        return TrustedAppsAuthenticationProvider.class;
    }

    public void enable(final RequestFactory authenticatedRequestFactory,
                       final ApplicationLink applicationLink)
            throws AuthenticationConfigurationException
    {
        enableRemoteTrust(authenticatedRequestFactory, applicationLink);
        try
        {
            trustConfigurator.issueInboundTrust(applicationLink);
            trustConfigurator.issueOutboundTrust(applicationLink);
        }
        catch (TrustConfigurator.ConfigurationException ce)
        {
            throw new AuthenticationConfigurationException(
                    "Error configuring Trusted Applications: " + ce.getMessage(), ce);
        }
    }

    private void enableRemoteTrust(final RequestFactory<Request<Request<?, Response>,Response>> requestFactory,
                                   final ApplicationLink applicationLink)
            throws AuthenticationConfigurationException
    {
        configureRemoteTrust(requestFactory, applicationLink, Request.MethodType.PUT);
    }

    private void disableRemoteTrust(final RequestFactory requestFactory, final ApplicationLink applicationLink)
            throws AuthenticationConfigurationException
    {
        configureRemoteTrust(requestFactory, applicationLink, Request.MethodType.DELETE);
    }

    /**
     * @param action    either {@link com.atlassian.sal.api.net.Request.MethodType#PUT} or
     * {@link com.atlassian.sal.api.net.Request.MethodType#DELETE}.
     */
    private void configureRemoteTrust(final RequestFactory<Request<Request<?, Response>,Response>> requestFactory,
                                      final ApplicationLink applicationLink,
                                      final Request.MethodType action)
            throws AuthenticationConfigurationException
    {
        final Holder<Boolean> success = new Holder<Boolean>(false);
        final Holder<String> errorMessage = new Holder<String>();
        final URI autoConfigUrl = URIUtil.uncheckedConcatenate(applicationLink.getRpcUrl(),
                AUTOCONFIGURE_SERVLET_LOCATION + hostApplication.getId().toString());
        try
        {
            final Request<Request<?, Response>, Response> request = requestFactory.createRequest(action, autoConfigUrl.toString());
            request.addHeader(OVERRIDE_HEADER_NAME, OVERRIDE_HEADER_VALUE);
            request.execute(new ResponseHandler<Response>()
            {
                public void handle(final Response response) throws ResponseException
                {
                    if (response.isSuccessful())
                    {
                        success.set(true);
                    }
                    else
                    {
                        errorMessage.set(String.format("Response code: %d: %s",
                                response.getStatusCode(), response.getResponseBodyAsString()));
                    }
                }
            });
        }
        catch (ResponseException re)
        {
            errorMessage.set("Communication error: " + re.getMessage());
        }
        if (!success.get())
        {
            throw new AuthenticationConfigurationException(
                    "Error configuring peer: " + errorMessage.get());
        }
    }

    public boolean isApplicable(final AuthenticationScenario authenticationScenario, final ApplicationLink applicationLink)
    {
        return authenticationScenario.isCommonUserBase() && authenticationScenario.isTrusted();
    }

    public void disable(final RequestFactory authenticatedRequestFactory, final ApplicationLink applicationLink)
            throws AuthenticationConfigurationException {

        trustConfigurator.revokeInboundTrust(applicationLink);
        trustConfigurator.revokeOutboundTrust(applicationLink);
        disableRemoteTrust(authenticatedRequestFactory, applicationLink);
    }

    public boolean incomingEnabled(final ApplicationLink applicationLink)
    {
        return trustConfigurator.inboundTrustEnabled(applicationLink);
    }
}
