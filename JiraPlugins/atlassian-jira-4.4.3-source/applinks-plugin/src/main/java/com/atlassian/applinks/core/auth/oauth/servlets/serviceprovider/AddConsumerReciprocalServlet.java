package com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.ServletPathConstants;
import com.atlassian.applinks.core.auth.oauth.ConsumerTokenStoreService;
import com.atlassian.applinks.core.auth.oauth.servlets.AbstractOAuthConfigServlet;
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
import com.atlassian.templaterenderer.TemplateRenderer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This servlet is the counterpart of
 * {@link com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider.AddConsumerByUrlServlet}
 * and
 * {@link com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider.AddConsumerManuallyServlet}.
 * </p>
 * <p>
 * Both those servlets redirect to this servlet on the other machine so oauth
 * can be added as an outbound authentication provider on this host. When this
 * servlet finishes, it redirects back to caller with a URL parameter to
 * signify success or failure (for instance when this is a one-way link and
 * the server id is not registered on this host -- which is possible when the
 * process was initiated by a user that is logged in on the remote host and
 * didn't realize).
 * </p>
 * <p>
 * This "cross-host dance" to establish an oauth link can fail for several
 * reasons:
 * <ul>
 * <li>the link is one-way and the server id isn't registered on this host</li>
 * <li>the user accessing this servlet does not have admin privileges</li>
 * <li>the server has no access at all and cannot even log in to this host</li>
 * </ul>
 * As a result, it's important the calling servlet does not make any local
 * state changes if the reciprocal operation failed.
 * </p>
 * <p>
 * This servlet takes to following url parameters:
 * <ul>
 * <li>callback=[absolute-url]</li>
 * </ul>
 * </p>
 * <p>
 * When redirecting back to <em>callback</em>, the following parameters are
 * sent:
 * <ul>
 * <li>success=[true|false]</li>
 * <li>message=[description] -- optional parameter used to describe the error</li>
 * </ul>
 * </p>
 * <p>
 * This servlet is bound under: [PUT|DELETE] /applinks/auth/conf/oauth/outbound/apl/<APL_ID>?callback=url
 * To enable oauth for outbound requests to the specified Application Links,
 * use PUT. To disable the oauth authentication provider, use DELETE.
 * </p>
 *
 * @since v3.0
 */
public class AddConsumerReciprocalServlet extends AbstractOAuthConfigServlet
{
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ConsumerTokenStoreService consumerTokenStoreService;
    private final WebSudoManager webSudoManager;
    public static final String ENABLE_OAUTH_AUTHENTICATION_PARAMETER = "enable-oauth";
    public static final String SUCCESS_PARAM = "success";
    public static final String CALLBACK_PARAM = "callback";
    private static final Logger LOG = LoggerFactory.getLogger(AddConsumerReciprocalServlet.class);

    public AddConsumerReciprocalServlet(
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
        this.webSudoManager = webSudoManager;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse resp) throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink;
            try
            {
                applicationLink = getRequiredApplicationLink(request);
            }
            catch(NotFoundException ex)
            {
                resp.sendRedirect(createRedirectUrl(request, true, null));
                return;
            }

            final boolean enable = Boolean.parseBoolean(request.getParameter(ENABLE_OAUTH_AUTHENTICATION_PARAMETER));
            try
            {
                if (enable)
                {
                    authenticationConfigurationManager.registerProvider(
                        applicationLink.getId(),
                        OAuthAuthenticationProvider.class,
                        Collections.<String, String>emptyMap());
                    resp.sendRedirect(createRedirectUrl(request, true, i18nResolver.getText("auth.oauth.config.serviceprovider.consumer.enabled")));
                }
                else
                {
                    if (authenticationConfigurationManager.isConfigured(applicationLink.getId(), OAuthAuthenticationProvider.class))
                    {
                        consumerTokenStoreService.removeAllConsumerTokens(applicationLink);
                    }
                    authenticationConfigurationManager.unregisterProvider(
                        applicationLink.getId(),
                        OAuthAuthenticationProvider.class);
                    resp.sendRedirect(createRedirectUrl(request, true, i18nResolver.getText("auth.oauth.config.serviceprovider.consumer.disabled")));
                }
            }
            catch (Exception e)
            {
                LOG.error("Error occurred when trying to " + (enable ? "enable" : "disable") + " OAuth authentication configuration for application link '" + applicationLink + "'", e);
                final String enableStr = (enable ? i18nResolver.getText("applinks.enable") : i18nResolver.getText("applinks.disable"));
                resp.sendRedirect(createRedirectUrl(request, false, i18nResolver.getText("auth.oauth.config.error.reciprocal.config", enableStr)));
            }
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, resp);
        }
    }

    public static String getReciprocalServletUrl(final URI baseUrl, final ApplicationId applicationId, final String callbackUrl, final String actionParamValue)
    {
        final URI enableOAuthURL = URIUtil.uncheckedConcatenate(baseUrl, ServletPathConstants.APPLINKS_CONFIG_SERVLET_PATH + "/oauth/outbound/apl/" + applicationId + "?callback=" + callbackUrl + "&" + AddConsumerReciprocalServlet.ENABLE_OAUTH_AUTHENTICATION_PARAMETER + "=" + actionParamValue);
        return enableOAuthURL.toString();
    }

    private String createRedirectUrl(final HttpServletRequest req, final boolean success, final String message)
    {
        String callbackUrl = getRequiredParameter(req, CALLBACK_PARAM);
        if (callbackUrl.indexOf("?") == -1)
        {
            callbackUrl += "?";
        }
        String redirectUrl = String.format("%s&" + SUCCESS_PARAM + "=%s", callbackUrl, success);
        if (!StringUtils.isBlank(message))
        {
            redirectUrl += "&" + MESSAGE_PARAM + "=" + URIUtil.utf8Encode(message);
        }
        return redirectUrl;
    }

}
