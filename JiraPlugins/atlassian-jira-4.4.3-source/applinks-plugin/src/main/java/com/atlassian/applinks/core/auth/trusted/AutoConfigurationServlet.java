package com.atlassian.applinks.core.auth.trusted;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
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

/**
 * <p>
 * Posted to by a peer that wants us to establish bidirectional trust.
 * </p>
 *
 * @since v3.0
 */
public class AutoConfigurationServlet extends AbstractTrustedAppsServlet
{
    private final WebSudoManager webSudoManager;

    public AutoConfigurationServlet(final I18nResolver i18nResolver,
                                    final InternalHostApplication host,
                                    final MessageFactory messageFactory,
                                    final TemplateRenderer templateRenderer,
                                    final WebResourceManager webResourceManager,
                                    final AdminUIAuthenticator adminUIAuthenticator,
                                    final ApplicationLinkService applicationLinkService,
                                    final TrustedApplicationsManager trustedApplicationsManager,
                                    final AuthenticationConfigurationManager configurationManager,
                                    final TrustedApplicationsConfigurationManager trustedAppsManager,
                                    final TrustConfigurator trustConfigurator,
                                    final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                    final LoginUriProvider loginUriProvider,
                                    final DocumentationLinker documentationLinker,
                                    final WebSudoManager webSudoManager,
                                    final XsrfTokenAccessor xsrfTokenAccessor,
                                    final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager,
                adminUIAuthenticator, applicationLinkService, host, trustedApplicationsManager,
                configurationManager, trustedAppsManager, trustConfigurator, batchedJSONi18NBuilderFactory,
                loginUriProvider, documentationLinker, xsrfTokenAccessor, xsrfTokenValidator);
        this.webSudoManager = webSudoManager;
    }

    @Override
    protected void doPut(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        final ApplicationLink link = getRequiredApplicationLink(request);
        try
        {
            trustConfigurator.issueInboundTrust(link);
            trustConfigurator.issueOutboundTrust(link);
        }
        catch (TrustConfigurator.ConfigurationException ce)
        {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to configure Trusted Applications: " + ce.getMessage());
        }
    }

    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink link = getRequiredApplicationLink(request);
            trustConfigurator.revokeInboundTrust(link);
            trustConfigurator.revokeOutboundTrust(link);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }
}
