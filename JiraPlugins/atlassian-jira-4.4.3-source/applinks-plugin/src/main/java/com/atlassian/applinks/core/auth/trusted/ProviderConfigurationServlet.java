package com.atlassian.applinks.core.auth.trusted;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.auth.AbstractAuthServlet;
import com.atlassian.applinks.core.auth.oauth.RequestUtil;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
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
import com.atlassian.security.auth.trustedapps.IPAddressFormatException;
import com.atlassian.security.auth.trustedapps.RequestConditions;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsConfigurationManager;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * Servlet to configure Trusted Applications in the role as provider (a.k.a.
 * inbound requests from a remote host).
 * </p>
 * This servlet has two faces. The first is as the counterpart servlet for
 * {@link ConsumerConfigurationServlet}. When
 * the latter is used to enable incoming Trusted Apps requests, it will first
 * redirect the browser to this servlet (running on the peer) which will
 * attempt to register the caller as a trusted application in the Trusted
 * Application's plugin by downloading the certificate from the caller.
 * </p>
 * <p>
 * If this servlet is successful in performing the requested action (either
 * installing or removing te Trusted Apps certificate for the peer), it will
 * redirect the browser back to the
 * {@link ConsumerConfigurationServlet} running
 * on the peer, passing along the {@code result=[success|failure]} query
 * parameter. This redirection is done using the {@code callback} URL parameter
 * that the caller
 * ({@link ConsumerConfigurationServlet}) passed
 * in the URL string.
 * </p>
 * <p>
 * Registered under:
 * /plugins/servlet/applinks/auth/conf/trusted/inbound-ual/{application_id}?action=[ENABLE|DISABLE]&callback=http://full/url
 * </p>
 * <p>
 * The second task of this servlet is to locally configure Trusted Applications
 * as a service provider for a non-UAL peer. In this scenario the servlet
 * behaves like the old Trusted Applications configuration servlets that take a
 * URL and download and install the peer's public key. In this mode will the
 * servlet actually render output, while the former mode will always end in a
 * redirect to the peer.
 * </p>
 * <p>
 * Registered under:
 * /plugins/servlet/applinks/auth/conf/trusted/inbound-non-ual/{application_id}
 * </p>
 *
 * @since v3.0
 */
public class ProviderConfigurationServlet extends AbstractTrustedAppsServlet
{
    private final WebSudoManager webSudoManager;

    public ProviderConfigurationServlet(final I18nResolver i18nResolver,
                                        final TemplateRenderer templateRenderer,
                                        final AdminUIAuthenticator adminUIAuthenticator,
                                        final WebResourceManager webResourceManager,
                                        final ApplicationLinkService applicationLinkService,
                                        final MessageFactory messageFactory,
                                        final TrustedApplicationsConfigurationManager trustedAppsManager,
                                        final AuthenticationConfigurationManager configurationManager,
                                        final TrustedApplicationsManager trustedApplicationsManager,
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
                trustedApplicationsManager,
                configurationManager, trustedAppsManager, trustConfigurator,
                batchedJSONi18NBuilderFactory, loginUriProvider, documentationLinker,
                xsrfTokenAccessor, xsrfTokenValidator);
        this.webSudoManager = webSudoManager;
    }

    /**
     * Unfortunately we have to support GET, because this servlet is invoked
     * from a 302 redirect.
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink link = getRequiredApplicationLink(request);
            if (!StringUtils.isBlank(request.getParameter("result")))
            {
                processPeerResponse(request, response, link);
            }
            else
            {
                // render local inbound configuration
                render(getRequiredApplicationLink(request), request, response, emptyContext());
            }
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    private void processPeerResponse(final HttpServletRequest request, final HttpServletResponse response, final ApplicationLink link)
            throws IOException
    {
        // returned from peer
        final RendererContextBuilder contextBuilder = new RendererContextBuilder();
        if (!peerWasSuccessful(request))
        {
            contextBuilder.put("error",
                    messageFactory.newI18nMessage("auth.trusted.config.consumer.save.peer.failed", request.getParameter("message")));
        }
        render(link, request, response, contextBuilder.build());
    }

    private boolean peerWasSuccessful(final HttpServletRequest request)
    {
        return "success".equals(getRequiredParameter(request, "result").toLowerCase());
    }

    /**
     * Posted to from the local inbound form to enable/disable inbound TA.
     * This will also redirect to the peer to enable reciprocal outbound trust.
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
            final RendererContextBuilder contextBuilder = new RendererContextBuilder();
            boolean success = false;
            try
            {
                configureLocalTrust(request, link);

                if (peerHasUAL(request))
                {
                    response.sendRedirect(createRedirectURL(request, link));
                    return; // nothing to render
                }
                success = true;
            }
            catch (InputValidationException ive)
            {
                contextBuilder.put(ive.getField(), ive.getMessage());
            }
            catch (TrustConfigurator.ConfigurationException ce)
            {
                contextBuilder.put("error", ce.getMessage());
            }
            if (!success && getAction(request) == Action.ENABLE)
            {
                contextBuilder.put("view", "edit");
            }
            render(link, request, response, contextBuilder.build());
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    private void configureLocalTrust(final HttpServletRequest request, final ApplicationLink link)
            throws TrustConfigurator.ConfigurationException
    {
        if (Action.ENABLE == getAction(request))
        {
            issueLocalTrust(request, link);
        }
        else
        {
            trustConfigurator.revokeInboundTrust(link);
        }

    }

    /**
     * @throws InputValidationException when input validation failed.
     * @throws TrustConfigurator.ConfigurationException when a communication
     * error occured talking to the peer.
     */
    private void issueLocalTrust(final HttpServletRequest request, final ApplicationLink link)
            throws TrustConfigurator.ConfigurationException, InputValidationException
    {
        final RequestConditions.RulesBuilder rulesBuilder = RequestConditions.builder();
        final String ipPatternsInput = request.getParameter("ipPatternsInput");
        final String urlPatternsInput = request.getParameter("urlPatternsInput");
        final String timeoutInput = request.getParameter("timeoutInput");

        if (!StringUtils.isBlank(ipPatternsInput))
        {
            try
            {
                rulesBuilder.addIPPattern(StringUtils.split(ipPatternsInput, "\n\r"));
            }
            catch (IPAddressFormatException e)
            {
                throw new InputValidationException(i18nResolver.getText("auth.trusted.config.error.ip.patterns", "<br>\"192.168.*.*<br>127.0.0.1\"")
                        , "ipPatternsInputErrorHtml");
            }
        }
        if (!StringUtils.isBlank(urlPatternsInput))
        {
            try
            {
                rulesBuilder.addURLPattern(StringUtils.split(urlPatternsInput, "\n\r"));
            }
            catch (IllegalArgumentException e)
            {
                throw new InputValidationException(i18nResolver.getText("auth.trusted.config.error.url.patterns"), "urlPatternsInputError");
            }
        }
        if (!StringUtils.isBlank(timeoutInput))
        {
            try
            {
                rulesBuilder.setCertificateTimeout(Long.parseLong(timeoutInput));
            }
            catch (IllegalArgumentException iae)
            {
                throw new InputValidationException(i18nResolver.getText("auth.trusted.config.error.timeout"), "timeoutInputError");
            }
        }
        else
        {
            rulesBuilder.setCertificateTimeout(TrustConfigurator.DEFAULT_CERTIFICATE_TIMEOUT);
        }
        trustConfigurator.updateInboundTrust(link, rulesBuilder.build());
    }

    private String createRedirectURL(final HttpServletRequest request, final ApplicationLink link)
            throws IOException
    {
        final URI remoteDisplayUrl = (!StringUtils.isEmpty(request.getParameter(HOST_URL_PARAM))) ?  URI.create(request.getParameter(HOST_URL_PARAM)) : link.getDisplayUrl();

        // URL pointing back to ourselves. The peer will append: "&action=[ENABLE|DISABLE]&result=[success|failure][&message=ErrorDescription]
        final String callbackUrl = URIUtil.uncheckedConcatenate(RequestUtil.getBaseURLFromRequest(request, internalHostApplication.getBaseUrl()),
                request.getServletPath(), request.getPathInfo()) + "?" + HOST_URL_PARAM + "=" + URIUtil.utf8Encode(remoteDisplayUrl);

        final URI targetBase = URIUtil.uncheckedConcatenate(
                                remoteDisplayUrl,
                                TrustedAppsAuthenticationProviderPluginModule.CONSUMER_SERVLET_LOCATION_UAL + internalHostApplication.getId());

        return String.format("%s?callbackUrl=%s&action=%s",
                targetBase.toString(),
                URIUtil.utf8Encode(callbackUrl),
                getAction(request).name());
    }

    /**
     * Renders the local inbound configuration.
     */
    private void render(
            final ApplicationLink appLink,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Map<String, Object> renderContext)
            throws IOException
    {
        final String trustedAppsId = (String) appLink.getProperty(TRUSTED_APPS_INCOMING_ID);
        final boolean enabled = null != trustedAppsId;
        final String consumer = appLink.getName();
        final String consumerAppType = i18nResolver.getText(appLink.getType().getI18nKey());
        final String provider = internalHostApplication.getName();
        final String providerAppType = i18nResolver.getText(internalHostApplication.getType().getI18nKey());

        final RendererContextBuilder contextBuilder = new RendererContextBuilder(renderContext)
                .put("urlPatternsInput", request.getParameter("urlPatternsInput"))
                .put("ipPatternsInput", request.getParameter("ipPatternsInput"))
                .put("timeoutInput", request.getParameter("timeoutInput"))
                .put("hostUrl", request.getParameter(AbstractAuthServlet.HOST_URL_PARAM));

        if (enabled)
        {
            final RequestConditions conditions = trustedApplicationsManager
                    .getTrustedApplication(trustedAppsId)
                    .getRequestConditions();
            contextBuilder
                    .put("urlPatterns", join(conditions.getURLPatterns(), '\n'))
                    .put("ipPatterns", join(conditions.getIPPatterns(), '\n'))
                    .put("timeout", Long.toString(conditions.getCertificateTimeout()));
        }
        render(request, response, consumer, consumerAppType, provider, providerAppType, enabled, contextBuilder.build());
    }

    private String join(final Iterable<String> iterable, final char delimiter)
    {
        return StringUtils.join(iterable.iterator(), delimiter);
    }

    private static class InputValidationException extends RuntimeException
    {
        private final String field;

        private InputValidationException(final String message, final String field)
        {
            super(message);
            this.field = field;
        }

        public String getField()
        {
            return field;
        }
    }
}
