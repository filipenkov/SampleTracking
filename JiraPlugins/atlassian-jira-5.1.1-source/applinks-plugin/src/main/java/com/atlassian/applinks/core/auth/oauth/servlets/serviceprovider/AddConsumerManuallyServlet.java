package com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.auth.oauth.ServiceProviderStoreService;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.RequestFactory;
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
import java.net.URI;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * This servlet allows to register an oauth consumer. The following information about the consumer is required:
 * key, name, description (optional), public key, callback URL (optional).
 *
 * @since 3.0
 */
public class AddConsumerManuallyServlet extends AbstractConsumerServlet
{
    private final ServiceProviderStoreService providerStoreService;
    private final WebSudoManager webSudoManager;
    private static final Logger LOG = LoggerFactory.getLogger(AddConsumerManuallyServlet.class);

    private static final String INCOMING_NON_APPLINKS_TEMPLATE = "auth/oauth/incoming_nonapplinks.vm";
    private static final String CONSUMER = "consumer";
    private static final String PUBLIC_KEY = "publicKey";

    protected AddConsumerManuallyServlet(
            final I18nResolver i18nResolver,
            final MessageFactory messageFactory,
            final TemplateRenderer templateRenderer,
            final WebResourceManager webResourceManager,
            final ApplicationLinkService applicationLinkService,
            final AdminUIAuthenticator adminUIAuthenticator,
            final RequestFactory requestFactory,
            final ServiceProviderStoreService providerStoreService,
            final InternalHostApplication internalHostApplication,
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
        this.providerStoreService = providerStoreService;
        this.webSudoManager = webSudoManager;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink = getRequiredApplicationLink(request);

            final Consumer consumer = providerStoreService.getConsumer(applicationLink);
            final RendererContextBuilder builder = createContextBuilder(applicationLink);
            builder.put("contextPath", request.getContextPath());
            builder.put("message", getMessage(request));
            if (consumer != null)
            {
                builder.put(CONSUMER, consumer);
                final String publicKey = RSAKeys.toPemEncoding(consumer.getPublicKey());
                builder.put(PUBLIC_KEY, publicKey);
            }
            render(INCOMING_NON_APPLINKS_TEMPLATE, builder.build(), request, response, applicationLink);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink = getRequiredApplicationLink(request);

            final Map<String, String> fieldErrorMessages = new HashMap<String, String>();

            final boolean enabled = Boolean.parseBoolean(checkRequiredFormParameter(request, OAUTH_INCOMING_ENABLED, fieldErrorMessages, "auth.oauth.config.error.enable"));

            addOrRemoveConsumer(request, applicationLink, fieldErrorMessages, enabled);

            if (fieldErrorMessages.isEmpty())
            {
                final String message = enabled ? i18nResolver.getText("auth.oauth.config.serviceprovider.consumer.enabled") : i18nResolver.getText("auth.oauth.config.serviceprovider.consumer.disabled");
                response.sendRedirect("./" + applicationLink.getId() + "?" + MESSAGE_PARAM + "=" + URIUtil.utf8Encode(message));
            }
            else
            {
                final FormFields formFields = new FormFields(request);
                final RendererContextBuilder builder = createContextBuilder(applicationLink);
                builder.put("contextPath", request.getContextPath());
                builder.put(CONSUMER, formFields);
                builder.put(PUBLIC_KEY, formFields.getPublicKey());
                builder.put("fieldErrorMessages", fieldErrorMessages);
                render(INCOMING_NON_APPLINKS_TEMPLATE, builder.build(), request, response, applicationLink);
            }
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    private void addOrRemoveConsumer(final HttpServletRequest request, final ApplicationLink applicationLink, final Map<String, String> fieldErrorMessages, final boolean enabled)
            throws IOException
    {
        if (enabled)
        {
            final String key = checkRequiredFormParameter(request, CONSUMER_KEY, fieldErrorMessages, "auth.oauth.config.serviceprovider.missing.consumer.key");
            final String name = checkRequiredFormParameter(request, CONSUMER_NAME, fieldErrorMessages, "auth.oauth.config.serviceprovider.missing.consumer.name");
            final String description = request.getParameter(CONSUMER_DESCRIPTION);
            final PublicKey publicKey = getPublicKey(request, fieldErrorMessages);
            final URI callback = getCallbackUri(request, fieldErrorMessages);
            if (!fieldErrorMessages.isEmpty())
            {
                return;
            }
            try
            {
                final Consumer consumer = Consumer.key(key).name(name).publicKey(publicKey).description(description).callback(callback).build();
                providerStoreService.addConsumer(consumer, applicationLink);
            }
            catch (Exception e)
            {
                LOG.error("Failed to store consumer key", e);
                fieldErrorMessages.put("communication", i18nResolver.getText("auth.oauth.config.error.consumer.add", e.getMessage()));
            }
        }
        else
        {
            try
            {
                providerStoreService.removeConsumer(applicationLink);
            }
            catch (Exception e)
            {
                LOG.error("Failed to disable OAuth outgoing, when trying to remove the consumer for application link '" + applicationLink + "'", e);
                fieldErrorMessages.put("communication", i18nResolver.getText("auth.oauth.config.error.consumer.remove", e.getMessage()));
            }
        }
    }

    public static class FormFields
    {
        public String key;
        public String name;
        public String description;
        public String publicKey;
        public String callback;

        public FormFields(final HttpServletRequest request)
        {
            key = request.getParameter(CONSUMER_KEY);
            name = request.getParameter(CONSUMER_NAME);
            description = request.getParameter(CONSUMER_DESCRIPTION);
            publicKey = request.getParameter(CONSUMER_PUBLIC_KEY);
            callback = request.getParameter(CONSUMER_CALLBACK);
        }

        public String getKey()
        {
            return key;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getPublicKey()
        {
            return publicKey;
        }

        public String getCallback()
        {
            return callback;
        }
    }

}
