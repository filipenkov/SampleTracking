package com.atlassian.applinks.core.auth.oauth.servlets;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.auth.oauth.ConsumerTokenStoreService;
import com.atlassian.applinks.core.auth.oauth.OAuthHelper;
import com.atlassian.applinks.core.auth.oauth.ServiceProviderStoreService;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.oauth.Consumer;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * This servlet enables OAuth as an Outgoing and incoming authentication types. This request is initiated as part of the Auto configuration process, after an application links has been created.
 * The {@link com.atlassian.applinks.core.auth.oauth.OAuthAuthenticatorProviderPluginModule} will make a call to this servlet to ensure OAuth is configured in both applications.
 *
 * @since 3.0
 */
public class AutoConfigurationServlet extends AbstractOAuthConfigServlet
{
    private static final Logger LOG = LoggerFactory.getLogger(AutoConfigurationServlet.class.getName());
    private final ServiceProviderStoreService serviceProviderStoreService;
    private final ConsumerTokenStoreService consumerTokenStoreService;
    private final WebSudoManager webSudoManager;
    private final AuthenticationConfigurationManager configurationManager;

    protected AutoConfigurationServlet(
            final I18nResolver i18nResolver,
            final MessageFactory messageFactory,
            final TemplateRenderer templateRenderer,
            final WebResourceManager webResourceManager,
            final ApplicationLinkService applicationLinkService,
            final AdminUIAuthenticator adminUIAuthenticator,
            final ServiceProviderStoreService serviceProviderStoreService,
            final ConsumerTokenStoreService consumerTokenStoreService,
            final AuthenticationConfigurationManager configurationManager,
            final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
            final DocumentationLinker documentationLinker,
            final LoginUriProvider loginUriProvider,
            final InternalHostApplication internalHostApplication,
            final WebSudoManager webSudoManager,
            final XsrfTokenAccessor xsrfTokenAccessor,
            final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, applicationLinkService,
                adminUIAuthenticator, batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider,
                internalHostApplication, xsrfTokenAccessor, xsrfTokenValidator);
        this.serviceProviderStoreService = serviceProviderStoreService;
        this.configurationManager = configurationManager;
        this.consumerTokenStoreService = consumerTokenStoreService;
        this.webSudoManager = webSudoManager;
    }

    @Override
    protected void doPut(final HttpServletRequest request, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        final ApplicationLink applicationLink = getRequiredApplicationLink(request);
        try
        {
            final Consumer consumer = OAuthHelper.fetchConsumerInformation(applicationLink);
            serviceProviderStoreService.addConsumer(consumer, applicationLink);
            configurationManager.registerProvider(applicationLink.getId(), OAuthAuthenticationProvider.class, Collections.<String, String>emptyMap());
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        catch (Exception e)
        {
            LOG.error("Failed to auto-configure OAuth authentication for application link '" + applicationLink + "'", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink = getRequiredApplicationLink(request);

            consumerTokenStoreService.removeAllConsumerTokens(applicationLink);
            configurationManager.unregisterProvider(applicationLink.getId(), OAuthAuthenticationProvider.class);
            serviceProviderStoreService.removeConsumer(applicationLink);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, resp);
        }
    }
}
