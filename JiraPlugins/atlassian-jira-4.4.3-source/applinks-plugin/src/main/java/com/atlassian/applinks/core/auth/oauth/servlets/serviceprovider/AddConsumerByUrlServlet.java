package com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.ServletPathConstants;
import com.atlassian.applinks.core.auth.oauth.OAuthHelper;
import com.atlassian.applinks.core.auth.oauth.RequestUtil;
import com.atlassian.applinks.core.auth.oauth.ServiceProviderStoreService;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.auth.AuthenticationDirection;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.oauth.Consumer;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * When registering an oauth consumer this servlet allows to register a consumer that is an atlassian application
 * and has the oauth plugin installed. Because it has this plugin installed we are able to obtain all information about the consumer
 * by querying a specific endpoint provided by the oauth plugin. Thus it's enough to know the URL of the application.
 * <p/>
 * When registering a consumer and this consumer has also the applinks plugin installed, then adding this consumer will also enable oauth for
 * outgoing authentication in the other application.
 *
 * @since 3.0
 */
public class AddConsumerByUrlServlet extends AbstractConsumerServlet
{
    private final ManifestRetriever manifestRetriever;
    private final ServiceProviderStoreService serviceProviderStoreService;
    private final WebSudoManager webSudoManager;
    private static final String INCOMING_APPLINKS_TEMPLATE = "auth/oauth/incoming_applinks.vm";
    private static final String COMMUNICATION_CONTEXT_PARAM = "communication";
    private static final String FIELD_ERROR_MESSAGES_CONTEXT_PARAM = "fieldErrorMessages";
    private static final String REMOTE_URL_CONTEXT_PARAM = "remoteURL";
    private static final String SUCCESS_MSG_CONTEXT_PARAM = "success-msg";
    public static final String UI_POSITION = "uiposition";
    private static final String ENABLE_DISABLE_OAUTH_PARAM = "ENABLE_DISABLE_OAUTH_PARAM";
    private static final Logger LOG = LoggerFactory.getLogger(AddConsumerByUrlServlet.class);

    protected AddConsumerByUrlServlet(
            final I18nResolver i18nResolver,
            final MessageFactory messageFactory,
            final TemplateRenderer templateRenderer,
            final WebResourceManager webResourceManager,
            final ApplicationLinkService applicationLinkService,
            final AdminUIAuthenticator adminUIAuthenticator,
            final RequestFactory requestFactory,
            final ManifestRetriever manifestRetriever,
            final InternalHostApplication internalHostApplication,
            final ServiceProviderStoreService serviceProviderStoreService,
            final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
            final LoginUriProvider loginUriProvider,
            final DocumentationLinker documentationLinker,
            final WebSudoManager webSudoManager,
            final XsrfTokenAccessor xsrfTokenAccessor,
            final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, applicationLinkService,
                adminUIAuthenticator, requestFactory, batchedJSONi18NBuilderFactory, documentationLinker,
                loginUriProvider, internalHostApplication, xsrfTokenAccessor, xsrfTokenValidator);
        this.manifestRetriever = manifestRetriever;
        this.serviceProviderStoreService = serviceProviderStoreService;
        this.webSudoManager = webSudoManager;
    }

    /**
     * Invoke with the name of the application ID as the last elements of the URL path.
     * <p/>
     * /plugins/servlet/applinks/auth/conf/oauth/{application_id} /rest/
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse resp) throws IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink = getRequiredApplicationLink(request);

            final RendererContextBuilder builder = createContextBuilder(applicationLink);

            final String parameter = request.getParameter(AddConsumerReciprocalServlet.SUCCESS_PARAM);
            if (parameter != null)
            {
                final boolean successfulEnabledOAuthInRemoteApp = Boolean.parseBoolean(parameter);
                if (successfulEnabledOAuthInRemoteApp)
                {
                    final Map<String, String> fieldErrorMessages = new HashMap<String, String>();
                    final boolean enabled = Boolean.parseBoolean(request.getParameter(OAUTH_INCOMING_ENABLED));
                    addOrRemoveConsumer(applicationLink, enabled, fieldErrorMessages);
                    final String message = getMessage(request);
                    if (!StringUtils.isEmpty(message) && fieldErrorMessages.isEmpty())
                    {
                        builder.put(SUCCESS_MSG_CONTEXT_PARAM, message);
                    }

                    if (!fieldErrorMessages.isEmpty())
                    {
                        builder.put(FIELD_ERROR_MESSAGES_CONTEXT_PARAM, fieldErrorMessages);
                    }
                }
                else
                {
                    final Map<String, String> fieldErrorMessages = new HashMap<String, String>();
                    fieldErrorMessages.put(COMMUNICATION_CONTEXT_PARAM, getMessage(request));
                    builder.put(FIELD_ERROR_MESSAGES_CONTEXT_PARAM, fieldErrorMessages);
                }
            }
            else
            {
                final String message = getMessage(request);
                if (!StringUtils.isEmpty(message))
                {
                    builder.put(SUCCESS_MSG_CONTEXT_PARAM, message);
                }
            }

            final String uiPosition = request.getParameter(UI_POSITION);
            final String remoteURL = getRemoteURL(applicationLink, uiPosition, request);
            if (remoteURL != null)
            {
                builder.put(REMOTE_URL_CONTEXT_PARAM, remoteURL);
            }
            builder.put(UI_POSITION, uiPosition);
            render(INCOMING_APPLINKS_TEMPLATE, builder.build(), request, resp, applicationLink);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, resp);
        }
    }

    /* (non-Javadoc)
     * We will only receive a POST if the remote application does not have UAL installed.
     * If UAL is installed we will redirect to the remote application and this will redirect back to us and
     * we will add this application as a consumer in our doGet method.
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink = getRequiredApplicationLink(request);

            final boolean enabled = Boolean.parseBoolean(request.getParameter(OAUTH_INCOMING_ENABLED));

            final Map<String, String> fieldErrorMessages = new HashMap<String, String>();
            addOrRemoveConsumer(applicationLink, enabled, fieldErrorMessages);
            final String uiPosition = request.getParameter(UI_POSITION);
            if (fieldErrorMessages.isEmpty())
            {
                final String message = enabled ? i18nResolver.getText("auth.oauth.config.serviceprovider.consumer.enabled") : i18nResolver.getText("auth.oauth.config.serviceprovider.consumer.disabled");
                response.sendRedirect("./" + applicationLink.getId() + "?" + MESSAGE_PARAM + "=" + URIUtil.utf8Encode(message) + "&uiposition=" + uiPosition);
            }
            else
            {
                final RendererContextBuilder builder = createContextBuilder(applicationLink);
                builder.put(FIELD_ERROR_MESSAGES_CONTEXT_PARAM, fieldErrorMessages);
                builder.put(UI_POSITION, uiPosition);
                render(INCOMING_APPLINKS_TEMPLATE, builder.build(), request, response, applicationLink);
            }
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    private String getRemoteURL(final ApplicationLink applicationLink, final String uiPosition, final HttpServletRequest request)
    {
        try
        {
            final Manifest manifest = manifestRetriever.getManifest(applicationLink.getRpcUrl(), applicationLink.getType());
            if (manifest.getAppLinksVersion() != null)
            {
                //This is the URL of the remote application.
                final URI displayUrl = (!StringUtils.isEmpty(request.getParameter(HOST_URL_PARAM))) ?  URI.create(request.getParameter(HOST_URL_PARAM)) : applicationLink.getDisplayUrl();

                // redirect URL to the remote application for enabling oauth
                final String callbackUrl = RequestUtil.getBaseURLFromRequest(request, internalHostApplication.getBaseUrl()) +
                        ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/oauth/add-consumer-by-url/" +
                        applicationLink.getId() + "/" + AuthenticationDirection.INBOUND.name() + "?" +
                        OAUTH_INCOMING_ENABLED + "=" +
                        ENABLE_DISABLE_OAUTH_PARAM + "&" +
                        UI_POSITION + "=" + uiPosition + "&" +
                        HOST_URL_PARAM + "=" + URIUtil.utf8Encode(displayUrl);
                final String encodedCallbackUrl = URIUtil.utf8Encode(callbackUrl);

                return AddConsumerReciprocalServlet.getReciprocalServletUrl(displayUrl, internalHostApplication.getId(), encodedCallbackUrl, ENABLE_DISABLE_OAUTH_PARAM);
            }
        }
        catch (Exception e)
        {
            LOG.warn("An Error occurred when building the URL to the '" + AddConsumerReciprocalServlet.class + "' servlet of the remote application.", e);
        }
        return null;
    }

    private void addOrRemoveConsumer(final ApplicationLink applicationLink, final boolean enabled, final Map<String, String> fieldErrorMessages)
            throws IOException
    {
        if (enabled)
        {
            try
            {
                final Consumer consumer = OAuthHelper.fetchConsumerInformation(applicationLink);
                serviceProviderStoreService.addConsumer(consumer, applicationLink);
            }
            catch (ResponseException e)
            {
                LOG.error("Error occurred when trying to fetch the consumer information from the remote application for application link '" + applicationLink + "'", e);
                fieldErrorMessages.put(COMMUNICATION_CONTEXT_PARAM, i18nResolver.getText("auth.oauth.config.error.communication.consumer", e.getMessage()));
            }
            catch (Exception e)
            {
                LOG.error("Error occurred when trying to store consumer information for application link '" + applicationLink + "'", e);
                fieldErrorMessages.put(COMMUNICATION_CONTEXT_PARAM, i18nResolver.getText("auth.oauth.config.error.consumer.add", e.getMessage()));
            }
        }
        else
        {
            try
            {
                serviceProviderStoreService.removeConsumer(applicationLink);
            }
            catch (Exception e)
            {
                LOG.error("Error occurred when trying to remove consumer from application link '" + applicationLink + "'.", e);
                fieldErrorMessages.put(COMMUNICATION_CONTEXT_PARAM, i18nResolver.getText("auth.oauth.config.error.consumer.remove", e.getMessage()));
            }
        }
    }

}
