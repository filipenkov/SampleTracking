package com.atlassian.applinks.core.auth.oauth.servlets.consumer;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.auth.oauth.servlets.AbstractOAuthConfigServlet;
import com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider.AddConsumerReciprocalServlet;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.Message;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * When acting as a consumer and using OAuth to make authenticated request to a service provider this servlet is used
 * to configure outbound OAuth configuration for third-party, non-applinks service provider that don't run our plugin (so we can't just
 * point to their inbound config servlet).
 * </p>
 * <p>
 * It asks the user to manually provide all oauth information required to
 * communicate with the service provider. This includes the Consumer Key that
 * is assigned to us, as well as the Shared Secret (asymmetric key signing
 * (RSA_SHA1) is not currently supported).
 * </p>
 * <p>
 * Bound under /applinks/auth/conf/oauth/outbound/3rdparty
 * </p>
 *
 * @since v3.0
 */
public class AddServiceProviderManuallyServlet extends AbstractOAuthConfigServlet
{
    private static final String CONSUMER_KEY_PARAMETER = "consumerKey";
    private static final String NAME_PARAMETER = "name";
    private static final String DESCRIPTION_PARAMETER = "description";
    private static final String SHARED_SECRET_PARAMETER = "sharedSecret";
    private static final String SERVICE_PROVIDER_REQUEST_TOKEN_URL_PARAMETER = "requestTokenUrl";
    private static final String SERVICE_PROVIDER_ACCESS_TOKEN_URL_PARAMETER = "accessTokenUrl";
    private static final String SERVICE_PROVIDER_AUTHORIZE_URL_PARAMETER = "authorizeUrl";

    private static final String TEMPLATE = "auth/oauth/outbound_nonapplinks.vm";

    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ConsumerService consumerService;
    private final WebSudoManager webSudoManager;
    public static final String CONSUMER_KEY_OUTBOUND = "consumerKey.outbound";
    public static final String SERVICE_PROVIDER_REQUEST_TOKEN_URL = "serviceProvider.requestTokenUrl";
    public static final String SERVICE_PROVIDER_ACCESS_TOKEN_URL = "serviceProvider.accessTokenUrl";
    public static final String SERVICE_PROVIDER_AUTHORIZE_URL = "serviceProvider.authorizeUrl";
    private static final String OUTGOING_ENABLED = "enabled";
    private static final Logger LOG = LoggerFactory.getLogger(AddServiceProviderManuallyServlet.class);
    private static final String OAUTH_OUTGOING_ENABLED_PARAM = "oauth-outgoing-enabled";

    protected AddServiceProviderManuallyServlet(
            final I18nResolver i18nResolver,
            final MessageFactory messageFactory,
            final TemplateRenderer templateRenderer,
            final WebResourceManager webResourceManager,
            final ApplicationLinkService applicationLinkService,
            final AdminUIAuthenticator adminUIAuthenticator,
            final AuthenticationConfigurationManager authenticationConfigurationManager,
            final ConsumerService consumerService,
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
        this.consumerService = consumerService;
        this.webSudoManager = webSudoManager;
    }

    /**
     * Displays the current oauth service provider configuration for this third-party application link, or a message
     * saying oauth is not configured. Also contains a button/link to configure oauth manually.
     */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(req);

            view(req, resp);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(req, resp);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(req);

            save(req, resp);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(req, resp);
        }
    }

    /**
     * Bound under GET /<APL_ID>/view
     * <p/>
     * Displays the form for manual configuration of all oauth parameters.
     */
    private void view(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        final ApplicationLink applicationLink = getRequiredApplicationLink(request);
        final RendererContextBuilder builder = createContextBuilder(applicationLink);
        if (authenticationConfigurationManager.isConfigured(applicationLink.getId(), OAuthAuthenticationProvider.class))
        {
            final Map<String, String> config = authenticationConfigurationManager.getConfiguration(applicationLink.getId(), OAuthAuthenticationProvider.class);
            if (config != null && config.containsKey(CONSUMER_KEY_OUTBOUND))
            {
                final String consumerKey = config.get(CONSUMER_KEY_OUTBOUND);
                final Consumer consumer = consumerService.getConsumerByKey(consumerKey);
                final String requestTokenUrl = config.get(SERVICE_PROVIDER_REQUEST_TOKEN_URL);
                final String accessTokenUrl = config.get(SERVICE_PROVIDER_ACCESS_TOKEN_URL);
                final String authorizeUrl = config.get(SERVICE_PROVIDER_AUTHORIZE_URL);
                if (consumer == null)
                {
                    LOG.warn("Failed to find information for service provider. No consumer with key '" + consumerKey + "' in OAuth store found. "
                            + "Application Link and OAuth store are out of sync. Has someone deleted this information?");
                }
                else
                {
                    builder.put(CONSUMER_KEY_PARAMETER, consumer.getKey())
                            .put(NAME_PARAMETER, consumer.getName())
                            .put(DESCRIPTION_PARAMETER, consumer.getDescription())
                            .put(SHARED_SECRET_PARAMETER, "")
                            .put(OUTGOING_ENABLED, true)
                            .put(SERVICE_PROVIDER_REQUEST_TOKEN_URL_PARAMETER, requestTokenUrl)
                            .put(SERVICE_PROVIDER_ACCESS_TOKEN_URL_PARAMETER, accessTokenUrl)
                            .put(SERVICE_PROVIDER_AUTHORIZE_URL_PARAMETER, authorizeUrl)
                            .put("success-msg", getMessage(request));
                }
            }
        }
        else
        {
            builder.put(OUTGOING_ENABLED, false).
                    put("success-msg", getMessage(request));
        }
        render(TEMPLATE, builder.build(), request, response);
    }

    /**
     * Bound under POST /<APL_ID>
     * <p/>
     * Saves new oauth provider configuration. If successful, redirects to "/<APL_ID>" to list the new configuration, or
     * re-renders the form with error messages.
     */
    private void save(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        final boolean enable = Boolean.parseBoolean(request.getParameter(OAUTH_OUTGOING_ENABLED_PARAM));
        final ApplicationLink applicationLink = getRequiredApplicationLink(request);
        final Map<String, Message> fieldErrorMessages = Maps.newHashMap();
        final String consumerKey = checkRequiredParameter(request, CONSUMER_KEY_PARAMETER, fieldErrorMessages, "auth.oauth.config.consumer.serviceprovider.key.is.required");
        final String name = checkRequiredParameter(request, NAME_PARAMETER, fieldErrorMessages, "auth.oauth.config.consumer.serviceprovider.name.is.required");
        final String description = request.getParameter(DESCRIPTION_PARAMETER);
        final String requestTokenUrl = checkRequiredParameter(request, SERVICE_PROVIDER_REQUEST_TOKEN_URL_PARAMETER, fieldErrorMessages, "auth.oauth.config.error.request.token.url");
        final String accessTokenUrl = checkRequiredParameter(request, SERVICE_PROVIDER_ACCESS_TOKEN_URL_PARAMETER, fieldErrorMessages, "auth.oauth.config.error.access.token.url");
        final String authorizeUrl = checkRequiredParameter(request, SERVICE_PROVIDER_AUTHORIZE_URL_PARAMETER, fieldErrorMessages, "auth.oauth.config.error.authorize.url");
        final String sharedSecret = checkRequiredParameter(request, SHARED_SECRET_PARAMETER, fieldErrorMessages, "auth.oauth.config.consumer.serviceprovider.shared.secret.is.required");

        if (!fieldErrorMessages.isEmpty() && enable)
        {
            final RendererContextBuilder builder = createContextBuilder(applicationLink)
                    .put("fieldErrorMessages", fieldErrorMessages)
                    .put(CONSUMER_KEY_PARAMETER, consumerKey)
                    .put(NAME_PARAMETER, name)
                    .put(DESCRIPTION_PARAMETER, description)
                    .put(SHARED_SECRET_PARAMETER, sharedSecret)
                    .put(SERVICE_PROVIDER_REQUEST_TOKEN_URL_PARAMETER, requestTokenUrl)
                    .put(SERVICE_PROVIDER_ACCESS_TOKEN_URL_PARAMETER, accessTokenUrl)
                    .put(SERVICE_PROVIDER_AUTHORIZE_URL_PARAMETER, authorizeUrl);
            render(TEMPLATE, builder.build(), request, response);
            return;
        }

        if (enable)
        {
            if (authenticationConfigurationManager.isConfigured(applicationLink.getId(), OAuthAuthenticationProvider.class))
            {
                final Map<String, String> config = authenticationConfigurationManager.getConfiguration(applicationLink.getId(), OAuthAuthenticationProvider.class);
                if (config != null && config.containsKey(CONSUMER_KEY_OUTBOUND))
                {
                    final String oldConsumerKey = config.get(CONSUMER_KEY_OUTBOUND);
                    consumerService.removeConsumerByKey(oldConsumerKey);
                }
            }
            else
            {
                //Check if this consumer key or service name is already in use.
                final Consumer existingConsumerDuplicatedServiceName = consumerService.getConsumer(name);
                final Consumer existingConsumerDuplicatedKey = consumerService.getConsumerByKey(consumerKey);
                if (existingConsumerDuplicatedServiceName != null)
                {
                    fieldErrorMessages.put(NAME_PARAMETER, messageFactory.newI18nMessage("auth.oauth.config.consumer.serviceprovider.service.name.exists", existingConsumerDuplicatedServiceName.getKey()));
                }
                if (existingConsumerDuplicatedKey != null)
                {
                    fieldErrorMessages.put(CONSUMER_KEY_PARAMETER, messageFactory.newI18nMessage("auth.oauth.config.consumer.serviceprovider.consumer.key.exists", existingConsumerDuplicatedKey.getName()));
                }
                if (!fieldErrorMessages.isEmpty())
                {
                    final RendererContextBuilder builder = createContextBuilder(applicationLink)
                            .put("fieldErrorMessages", fieldErrorMessages)
                            .put(CONSUMER_KEY_PARAMETER, consumerKey)
                            .put(NAME_PARAMETER, name)
                            .put(DESCRIPTION_PARAMETER, description)
                            .put(SHARED_SECRET_PARAMETER, sharedSecret)
                            .put(SERVICE_PROVIDER_REQUEST_TOKEN_URL_PARAMETER, requestTokenUrl)
                            .put(SERVICE_PROVIDER_ACCESS_TOKEN_URL_PARAMETER, accessTokenUrl)
                            .put(SERVICE_PROVIDER_AUTHORIZE_URL_PARAMETER, authorizeUrl);
                    render(TEMPLATE, builder.build(), request, response);
                    return;
                }
            }
            authenticationConfigurationManager.registerProvider(
                    applicationLink.getId(),
                    OAuthAuthenticationProvider.class,
                    ImmutableMap.of(CONSUMER_KEY_OUTBOUND, consumerKey,
                            SERVICE_PROVIDER_REQUEST_TOKEN_URL, requestTokenUrl,
                            SERVICE_PROVIDER_ACCESS_TOKEN_URL, accessTokenUrl,
                            SERVICE_PROVIDER_AUTHORIZE_URL, authorizeUrl));
            final Consumer consumer = Consumer.key(consumerKey)
                    .name(name)
                    .signatureMethod(Consumer.SignatureMethod.HMAC_SHA1)
                    .description(description)
                    .build();
            consumerService.add(name, consumer, sharedSecret);
        }
        else /* disable */
        {
            final Map<String, String> config = authenticationConfigurationManager.getConfiguration(applicationLink.getId(), OAuthAuthenticationProvider.class);
            if (config != null && config.containsKey(CONSUMER_KEY_OUTBOUND))
            {
                final String oldConsumerKey = config.get(CONSUMER_KEY_OUTBOUND);
                consumerService.removeConsumerByKey(oldConsumerKey);
            }
            authenticationConfigurationManager.unregisterProvider(applicationLink.getId(), OAuthAuthenticationProvider.class);
        }

        final String message;
        if (enable)
        {
            message = i18nResolver.getText("auth.oauth.config.consumer.serviceprovider.success");
        }
        else
        {
            message = i18nResolver.getText("auth.oauth.config.consumer.serviceprovider.deleted");
        }
        response.sendRedirect("./" + applicationLink.getId() + "?" + AddConsumerReciprocalServlet.MESSAGE_PARAM + "=" + URIUtil.utf8Encode(message));
    }

    // TODO: abstract-away into a base class

    protected final String checkRequiredParameter(final HttpServletRequest request, final String parameterName, final Map<String, Message> errorMessages, final String messageKey)
    {
        if (StringUtils.isBlank(request.getParameter(parameterName)))
        {
            errorMessages.put(parameterName, messageFactory.newI18nMessage(messageKey));
        }
        return request.getParameter(parameterName);
    }

}
