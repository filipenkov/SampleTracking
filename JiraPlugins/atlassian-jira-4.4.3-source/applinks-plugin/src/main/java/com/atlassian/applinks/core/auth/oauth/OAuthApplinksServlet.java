package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.ServletPathConstants;
import com.atlassian.applinks.core.auth.oauth.servlets.consumer.AddServiceProviderManuallyServlet;
import com.atlassian.applinks.core.util.WebResources;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import net.oauth.OAuth;
import net.oauth.OAuthProblemException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This servlet 'manages' the OAuth dance, consisting of the following steps:
 * 1) Obtain a request token from the remote application
 * 2) Redirect the user to the remote application and ask for authorization
 * 3) Wait for the redirect by the remote application and swap the request token for an access token afterwards
 * 4) Notify the user that authorization was successfully and he can continue with his operation.
 *
 * @since 3.0
 */
public class OAuthApplinksServlet extends HttpServlet
{
    private static final Logger LOG = LoggerFactory.getLogger(OAuthApplinksServlet.class);

    private final ApplicationLinkService applicationLinkService;
    private final ApplicationProperties applicationProperties;
    private final ConsumerTokenStoreService consumerTokenStoreService;
    private final OAuthTokenRetriever oAuthTokenRetriever;
    private final UserManager userManager;
    private final I18nResolver i18nResolver;
    private final WebResourceManager webResourceManager;
    private final TemplateRenderer templateRenderer;
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final ConsumerService consumerService;
    private final InternalHostApplication internalHostApplication;

    public static final String AUTHORIZE_PATH = "authorize";
    public static final String ACCESS_PATH = "access";
    private static final String APPLICATION_LINK_ID_PARAM = "applicationLinkID";
    private static final String REDIRECT_URL_PARAM = "redirectUrl";
    private static final String TEMPLATE = "auth/oauth/oauth_dance.vm";

    public OAuthApplinksServlet(final ApplicationLinkService applicationLinkService,
            final ApplicationProperties applicationProperties,
            final ConsumerTokenStoreService consumerTokenStoreService,
            final OAuthTokenRetriever oAuthTokenRetriever,
            final UserManager userManager,
            final I18nResolver i18nResolver,
            final WebResourceManager webResourceManager,
            final TemplateRenderer templateRenderer,
            final AuthenticationConfigurationManager authenticationConfigurationManager,
            final ConsumerService consumerService,
            final InternalHostApplication internalHostApplication)
    {
        this.applicationLinkService = applicationLinkService;
        this.applicationProperties = applicationProperties;
        this.consumerTokenStoreService = consumerTokenStoreService;
        this.oAuthTokenRetriever = oAuthTokenRetriever;
        this.userManager = userManager;
        this.i18nResolver = i18nResolver;
        this.webResourceManager = webResourceManager;
        this.templateRenderer = templateRenderer;
        this.authenticationConfigurationManager = authenticationConfigurationManager;
        this.consumerService = consumerService;
        this.internalHostApplication = internalHostApplication;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        checkNotNull(userManager.getRemoteUsername(), "You have to be logged in to use OAuth authentication.");
        String applicationLinkId = getApplicationLinkId(req);
        if (StringUtils.isBlank(applicationLinkId))
        {
            resp.sendError(400, i18nResolver.getText("auth.oauth.config.error.link.id.empty"));
            return;
        }
        final Map<String, Object> context = createVelocityContext(resp);
        final ApplicationLink applicationLink;
        try
        {
            applicationLink = applicationLinkService.getApplicationLink(new ApplicationId(applicationLinkId));
        }
        catch (TypeNotInstalledException e)
        {
            LOG.error("Failed to get application link", e);
            context.put("error", i18nResolver.getText("auth.oauth.config.error.link.type.not.loaded", applicationLinkId, e.getType()));
            templateRenderer.render(TEMPLATE, context, resp.getWriter());
            return;
        }
        
        if (applicationLink == null)
        {
            context.put("error", i18nResolver.getText("auth.oauth.config.error.link.id", applicationLinkId));
            templateRenderer.render(TEMPLATE, context, resp.getWriter());
            return;
        }

        if (!authenticationConfigurationManager.isConfigured(applicationLink.getId(), OAuthAuthenticationProvider.class))
        {
            context.put("error", i18nResolver.getText("auth.oauth.config.error.not.configured", applicationLink.toString()));
            templateRenderer.render(TEMPLATE, context, resp.getWriter());
            return;
        }

        final String token = getToken(req);
        try
        {
            if (StringUtils.isBlank(token) || req.getPathInfo().endsWith(AUTHORIZE_PATH))
            {
                obtainAndAuthorizeRequestToken(applicationLink, resp, req);
            }
            else if (req.getPathInfo().endsWith(ACCESS_PATH))
            {
                getAccessToken(token, applicationLink, req);
                final String redirectUrl = getRedirectUrl(req);
                if (StringUtils.isBlank(redirectUrl))
                {
                    templateRenderer.render(TEMPLATE, context, resp.getWriter());
                    return;
                }
                resp.sendRedirect(redirectUrl);
            }
        }
        catch (Exception e)
        {
            LOG.error("An error occurred when performing the oauth 'dance' for application link '" + applicationLink + "'", e);
            if (e.getCause() instanceof OAuthProblemException)
            {
                OAuthProblemException oAuthProblem = (OAuthProblemException) e.getCause();
                String problem = oAuthProblem.getProblem();
                if (problem.equals(OAuth.Problems.CONSUMER_KEY_UNKNOWN))
                {
                    context.put("error", i18nResolver.getText("auth.oauth.config.error.dance.oauth.problem.consumer.unknown", internalHostApplication.getName(), applicationLink.getName()));
                }
                else if (problem.equals(OAuth.Problems.TOKEN_REJECTED))
                {
                    context.put("error", i18nResolver.getText("auth.oauth.config.error.dance.oauth.problem.token.rejected"));
                }
                else
                {
                    context.put("error", i18nResolver.getText("auth.oauth.config.error.dance.oauth.problem", applicationLink.toString(), oAuthProblem.getProblem()));
                }
            }
            else
            {
                context.put("error", i18nResolver.getText("auth.oauth.config.error.dance", applicationLink.toString()));
            }

            String redirectUrl = getRedirectUrl(req);
            if (redirectUrl == null)
            {
                redirectUrl = "#";
            }

            context.put("redirectUrl", redirectUrl);
            templateRenderer.render(TEMPLATE, context, resp.getWriter());
            return;
        }
    }

    private Map<String, Object> createVelocityContext(final HttpServletResponse resp)
    {
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("i18n", i18nResolver);
        resp.setContentType("text/html");
        webResourceManager.requireResource("com.atlassian.applinks.applinks-plugin:oauth-dance");
        final StringWriter stringWriter = new StringWriter();
        webResourceManager.includeResources(stringWriter, UrlMode.RELATIVE);
        final WebResources webResources = new WebResources();
        webResources.setIncludedResources(stringWriter.getBuffer().toString());
        context.put("webResources", webResources);
        return context;
    }

    private String getToken(final HttpServletRequest req)
    {
        return req.getParameter(OAuth.OAUTH_TOKEN);
    }

    private void getAccessToken(String requestToken, final ApplicationLink applicationLink, final HttpServletRequest request)
            throws ResponseException
    {
        final String username = userManager.getRemoteUsername(request);
        final Map<String, String> config = authenticationConfigurationManager.getConfiguration(applicationLink.getId(), OAuthAuthenticationProvider.class);
        final ServiceProvider serviceProvider = ServiceProviderUtil.getServiceProvider(config, applicationLink);
        final String requestVerifier = request.getParameter(OAuth.OAUTH_VERIFIER);
        final String consumerKey = getConsumerKey(applicationLink);
        final ConsumerToken accessToken = oAuthTokenRetriever.getAccessToken(serviceProvider, requestToken, requestVerifier, consumerKey);
        consumerTokenStoreService.addConsumerToken(applicationLink, username, accessToken);
    }

    private void obtainAndAuthorizeRequestToken(final ApplicationLink applicationLink, final HttpServletResponse resp, final HttpServletRequest req)
            throws ResponseException, IOException
    {
        final Map<String, String> config = authenticationConfigurationManager.getConfiguration(applicationLink.getId(), OAuthAuthenticationProvider.class);
        final ServiceProvider serviceProvider = ServiceProviderUtil.getServiceProvider(config, applicationLink);
        final String consumerKey = getConsumerKey(applicationLink);
        final String redirectUrl = getRedirectUrl(req);
        URI baseUrl = RequestUtil.getBaseURLFromRequest(req, internalHostApplication.getBaseUrl());
        final String redirectToMeUrl = baseUrl + ServletPathConstants.APPLINKS_SERVLETS_PATH + "/oauth/login-dance/" + ACCESS_PATH
                + "?" + APPLICATION_LINK_ID_PARAM + "=" + applicationLink.getId() + (redirectUrl != null ? "&" + REDIRECT_URL_PARAM + "=" + URLEncoder.encode(redirectUrl, "UTF-8") : "");
        final ConsumerToken requestToken = oAuthTokenRetriever.getRequestToken(serviceProvider, consumerKey, redirectToMeUrl);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(OAuth.OAUTH_TOKEN, requestToken.getToken());
        parameters.put(OAuth.OAUTH_CALLBACK, redirectToMeUrl);
        resp.sendRedirect(serviceProvider.getAuthorizeUri() + "?" + OAuth.formEncode(parameters.entrySet()));
    }

    private String getConsumerKey(ApplicationLink applicationLink)
    {
        final Map<String, String> config = authenticationConfigurationManager.getConfiguration(applicationLink.getId(), OAuthAuthenticationProvider.class);
        if (config.containsKey(AddServiceProviderManuallyServlet.CONSUMER_KEY_OUTBOUND))
        {
            return config.get(AddServiceProviderManuallyServlet.CONSUMER_KEY_OUTBOUND);
        }
        return consumerService.getConsumer().getKey();
    }

    private String getApplicationLinkId(HttpServletRequest req)
    {
        return req.getParameter(APPLICATION_LINK_ID_PARAM);
    }

    private String getRedirectUrl(HttpServletRequest req)
    {
        return req.getParameter(REDIRECT_URL_PARAM);
    }

}