package com.atlassian.applinks.core.auth.trusted;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.TrustedAppsAuthenticationProvider;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsConfigurationManager;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * Servlet to configure Trusted Applications in the role as consumer (a.k.a.
 * outbound requests to a remote host).
 * </p>
 * <p>
 * Renders the the outbound trusted apps configuration screen, which contains a
 * checkbox to enable/disable trusted requests into the remote application.
 * </p>
 * <p>
 * When the user enables trusted apps for the Applink at hand, this servlet
 * will first redirect to the Trusted Apps Reciprocal Servlet on the peer which
 * will download the certificate from us and store it locally, or delete our
 * certificate if we sent the action=disable parameter. It will then redirect
 * the browser back to us here, indicating whether or not it was able to
 * perform the requested action by adding result=[success|failure] to the URL's
 * query parameters. If successful, we will set the flag in
 * {@link AuthenticationConfigurationManager} to indicate that Trusted Apps is
 * enabled for outbound requests (or remove the check if the action was to
 * disable).
 * Alternatively, if the peer reported failure, we will not make any local
 * state changes, but render an appropriate error.
 * </p>
 *
 * Registered under:
 * <ul>
 * <li>/plugins/servlet/applinks/auth/conf/trusted/outbound-ual/{application_id}</li>
 * <li>/plugins/servlet/applinks/auth/conf/trusted/outbound-non-ual/{application_id}</li>
 * </ul>
 *
 * @since v3.0
 */
public class ConsumerConfigurationServlet extends AbstractTrustedAppsServlet
{
    private final WebSudoManager webSudoManager;

    public ConsumerConfigurationServlet(final I18nResolver i18nResolver,
                                        final TemplateRenderer templateRenderer,
                                        final AdminUIAuthenticator adminUIAuthenticator,
                                        final WebResourceManager webResourceManager,
                                        final AuthenticationConfigurationManager configurationManager,
                                        final ApplicationLinkService applicationLinkService,
                                        final MessageFactory messageFactory,
                                        final TrustedApplicationsManager trustedApplicationsManager,
                                        final TrustedApplicationsConfigurationManager trustedAppsManager,
                                        final InternalHostApplication hostApplication,
                                        final TrustConfigurator trustConfigurator,
                                        final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                        final LoginUriProvider loginUriProvider,
                                        final DocumentationLinker documentationLinker,
                                        final WebSudoManager webSudoManager,
                                        final XsrfTokenAccessor xsrfTokenAccessor,
                                        final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager,
                adminUIAuthenticator, applicationLinkService, hostApplication,
                trustedApplicationsManager, configurationManager, trustedAppsManager,
                trustConfigurator, batchedJSONi18NBuilderFactory, loginUriProvider, documentationLinker, xsrfTokenAccessor, xsrfTokenValidator);
        this.webSudoManager = webSudoManager;
    }

    /**
     * Posted to by the form for outbound Trusted Applications with legacy
     * (non-UAL) peers.
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink link = getRequiredApplicationLink(request);
            trustConfigurator.configureOutboundTrust(link, getAction(request));
            render(link, request, response);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            if (peerHasUAL(request))
            {
                configureTrustAndRedirect(request, response);
            }
            else
            {
                /**
                 * Called when the user goes to outbound Trusted Applications configuration
                 * for a legacy (non-UAL) peer. The enable/disable button posts back to
                 * this servlet.
                 */
                render(getRequiredApplicationLink(request), request, response);
            }
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    private void configureTrustAndRedirect(final HttpServletRequest request,
                                           final HttpServletResponse response)
            throws ServletException, IOException
    {
        final String callbackUrl = getRequiredParameter(request, "callbackUrl");
        final Action action = getAction(request);
        final ApplicationLink applicationLink;
        try
        {
            applicationLink = getRequiredApplicationLink(request);
        }
        catch (NotFoundException ex)
        {
            response.sendRedirect(buildCallBackUrl(callbackUrl, action, true, null));
            return;
        }

        trustConfigurator.configureOutboundTrust(applicationLink, action);
        response.sendRedirect(buildCallBackUrl(callbackUrl, action, true, null));
    }

    private String buildCallBackUrl(final String callbackUrlBase,
                                    final Action action,
                                    final boolean success,
                                    final String message)
    {
        final StringBuilder buf = new StringBuilder(callbackUrlBase)
                .append(callbackUrlBase.contains("?") ? '&' : '?')
                .append("action=").append(action.name())
                .append('&')
                .append("result=").append(success ? "success" : "failure");
        if (!StringUtils.isBlank(message))
        {
            buf.append("&message=" + URIUtil.utf8Encode(message));
        }
        return buf.toString();
    }

    private void render(
            final ApplicationLink appLink,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws IOException
    {
        final boolean enabled = configurationManager.isConfigured(appLink.getId(), TrustedAppsAuthenticationProvider.class);
        final String consumer = internalHostApplication.getName();
        final String consumerAppType = i18nResolver.getText(internalHostApplication.getType().getI18nKey());
        final String provider = appLink.getName();
        final String providerAppType = i18nResolver.getText(appLink.getType().getI18nKey());

        render(request, response, consumer, consumerAppType, provider, providerAppType, enabled, emptyContext());
    }
}
