package com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.auth.oauth.servlets.AbstractOAuthConfigServlet;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * All servlets that provide functionality to register another application as a consumer should inherit from this servlet.
 *
 * @since 3.0
 */
public abstract class AbstractConsumerServlet extends AbstractOAuthConfigServlet
{
    protected static final String CONSUMER_KEY = "key";
    protected static final String CONSUMER_NAME = "consumerName";
    protected static final String CONSUMER_DESCRIPTION = "description";
    protected static final String CONSUMER_PUBLIC_KEY = "publicKey";
    protected static final String CONSUMER_CALLBACK = "callback";
    public static final String OAUTH_INCOMING_CONSUMER_KEY = "oauth.incoming.consumerkey";
    public static final String OAUTH_INCOMING_ENABLED = "oauth-incoming-enabled";

    protected final RequestFactory requestFactory;
    protected static final String ENABLED_CONTEXT_PARAM = "enabled";

    protected AbstractConsumerServlet(final I18nResolver i18nResolver, final MessageFactory messageFactory,
                                      final TemplateRenderer templateRenderer,
                                      final WebResourceManager webResourceManager,
                                      final ApplicationLinkService applicationLinkService,
                                      final AdminUIAuthenticator adminUIAuthenticator,
                                      final RequestFactory requestFactory,
                                      final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                      final DocumentationLinker documentationLinker,
                                      final LoginUriProvider loginUriProvider,
                                      final InternalHostApplication internalHostApplication,
                                      final XsrfTokenAccessor xsrfTokenAccessor,
                                      final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, applicationLinkService,
                adminUIAuthenticator, batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider,
                internalHostApplication, xsrfTokenAccessor, xsrfTokenValidator);
        this.requestFactory = requestFactory;
    }

    protected final URI getCallbackUri(final HttpServletRequest request, final Map<String, String> fieldErrorMessages)
    {
        String uriParam = request.getParameter(CONSUMER_CALLBACK);
        if (uriParam == null || isEmpty(uriParam))
        {
            return null;
        }

        final URI callback;
        try
        {
            if (!uriParam.endsWith("/"))
            {
                uriParam += "/";
            }
            callback = new URI(uriParam);
        }
        catch (URISyntaxException e)
        {
            fieldErrorMessages.put(CONSUMER_CALLBACK, i18nResolver.getText("auth.oauth.config.serviceprovider.invalid.uri"));
            return null;
        }
        if (!callback.isAbsolute())
        {
            fieldErrorMessages.put(CONSUMER_CALLBACK, i18nResolver.getText("auth.oauth.config.serviceprovider.callback.uri.must.be.absolute"));
            return null;
        }
        if (!"http".equals(callback.getScheme()) && !"https".equals(callback.getScheme()))
        {
            fieldErrorMessages.put(CONSUMER_CALLBACK, i18nResolver.getText("auth.oauth.config.serviceprovider.callback.uri.must.be.http.or.https"));
            return null;
        }
        return callback;
    }

    protected final PublicKey getPublicKey(final HttpServletRequest request, final Map<String, String> fieldErrorMessages)
    {
        final String publicKeyParam = checkRequiredFormParameter(request, CONSUMER_PUBLIC_KEY, fieldErrorMessages, "auth.oauth.config.serviceprovider.missing.public.key");
        if (publicKeyParam == null)
        {
            return null;
        }
        PublicKey publicKey = null;
        try
        {
            if (publicKeyParam.startsWith("-----BEGIN CERTIFICATE-----"))
            {
                publicKey = RSAKeys.fromEncodedCertificateToPublicKey(publicKeyParam);
            }
            else
            {
                publicKey = RSAKeys.fromPemEncodingToPublicKey(publicKeyParam);
            }
        }
        catch (GeneralSecurityException e)
        {
            fieldErrorMessages.put(CONSUMER_PUBLIC_KEY, i18nResolver.getText("auth.oauth.config.serviceprovider.invalid.public.key", e.getMessage()));
        }
        return publicKey;
    }

    protected void render(final String template, final Map<String, Object> params, HttpServletRequest request, final HttpServletResponse response, final ApplicationLink applicationLink)
            throws IOException
    {
        final RendererContextBuilder builder = new RendererContextBuilder(params);
        builder.put(ENABLED_CONTEXT_PARAM, applicationLink.getProperty(OAUTH_INCOMING_CONSUMER_KEY) != null);
        super.render(template, builder.build(), request, response);
    }
}
