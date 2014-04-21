package com.atlassian.applinks.core.auth.oauth.servlets.consumer;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.auth.oauth.ConsumerTokenStoreService;
import com.atlassian.applinks.core.auth.oauth.servlets.AbstractOAuthConfigServlet;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
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
import com.atlassian.templaterenderer.TemplateRenderer;

/**
 * When acting as a consumer and using OAuth to authenticate to the service provider,
 * this servlet allows to register another Atlassian application that has the OAuth plugin installed as a service provider.
 * After enabling OAuth for outgoing authentication, the other application has to register "us" as a consumer.
 *
 * @since 3.0
 */
public class AddAtlassianServiceProviderServlet extends AbstractOAuthConfigServlet
{
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ConsumerTokenStoreService consumerTokenStoreService;
    private final InternalHostApplication internalHostApplication;
    private final WebSudoManager webSudoManager;
    private static final String OUTGOING_ENABLED = "enabled";
    private static final String TEMPLATE = "auth/oauth/outbound_oauth_plugin_installed.vm";
    private static final String OAUTH_OUTGOING_ENABLED = "outgoing-enabled";

    public AddAtlassianServiceProviderServlet(
             final I18nResolver i18nResolver,
             final MessageFactory messageFactory,
             final TemplateRenderer templateRenderer,
             final WebResourceManager webResourceManager,
             final ApplicationLinkService applicationLinkService,
             final AdminUIAuthenticator adminUIAuthenticator,
             final AuthenticationConfigurationManager authenticationConfigurationManager,
             final ConsumerTokenStoreService consumerTokenStoreService,
             final InternalHostApplication internalHostApplication,
             final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
             final LoginUriProvider loginUriProvider,
             final DocumentationLinker documentationLinker,
             final WebSudoManager webSudoManager,
             final XsrfTokenAccessor xsrfTokenAccessor,
             final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, applicationLinkService,
                adminUIAuthenticator, batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider,
                internalHostApplication, xsrfTokenAccessor, xsrfTokenValidator);
        this.authenticationConfigurationManager = authenticationConfigurationManager;
        this.consumerTokenStoreService = consumerTokenStoreService;
        this.internalHostApplication = internalHostApplication;
        this.webSudoManager = webSudoManager;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink = getRequiredApplicationLink(request);
            final RendererContextBuilder builder = createContextBuilder(applicationLink);
            final boolean isConfigured = authenticationConfigurationManager.isConfigured(applicationLink.getId(), OAuthAuthenticationProvider.class);
            builder.put(OUTGOING_ENABLED, isConfigured);
            render(TEMPLATE, builder.build(), request, response);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink = getRequiredApplicationLink(request);
            final RendererContextBuilder builder = createContextBuilder(applicationLink);
            final boolean enabled = Boolean.parseBoolean(request.getParameter(OAUTH_OUTGOING_ENABLED));
            builder.put(OUTGOING_ENABLED, enabled);
            if (enabled)
            {
                authenticationConfigurationManager.registerProvider(applicationLink.getId(), OAuthAuthenticationProvider.class, Collections.<String, String>emptyMap());
                builder.put("message", i18nResolver.getText("auth.oauth.config.consumer.atlassian.serviceprovider.message.enabled", internalHostApplication.getName(), applicationLink.getName(), internalHostApplication.getBaseUrl()));
            }
            else
            {
                consumerTokenStoreService.removeAllConsumerTokens(applicationLink);
                authenticationConfigurationManager.unregisterProvider(applicationLink.getId(), OAuthAuthenticationProvider.class);
                builder.put("message", i18nResolver.getText("auth.oauth.config.consumer.atlassian.serviceprovider.message.disabled"));
            }
            render(TEMPLATE, builder.build(), request, response);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

}
