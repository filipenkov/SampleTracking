package com.atlassian.applinks.core.auth.trusted;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.auth.AbstractAuthServlet;
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
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsConfigurationManager;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @since v3.0
 */
abstract class AbstractTrustedAppsServlet extends AbstractAuthServlet
{
    public static final String TRUSTED_APPS_INCOMING_ID = "trustedapps.incoming.applicationId";
    protected static final String VM_TEMPLATE = "auth/trusted/config.vm";
    protected final TrustedApplicationsConfigurationManager trustedAppsManager;
    protected final AuthenticationConfigurationManager configurationManager;
    protected final TrustedApplicationsManager trustedApplicationsManager;
    protected final TrustConfigurator trustConfigurator;

    protected AbstractTrustedAppsServlet(final I18nResolver i18nResolver,
                                         final MessageFactory messageFactory,
                                         final TemplateRenderer templateRenderer,
                                         final WebResourceManager webResourceManager,
                                         final AdminUIAuthenticator adminUIAuthenticator,
                                         final ApplicationLinkService applicationLinkService,
                                         final InternalHostApplication internalHostApplication,
                                         final TrustedApplicationsManager trustedApplicationsManager,
                                         final AuthenticationConfigurationManager configurationManager,
                                         final TrustedApplicationsConfigurationManager trustedAppsManager,
                                         final TrustConfigurator trustConfigurator,
                                         final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                         final LoginUriProvider loginUriProvider,
                                         final DocumentationLinker documentationLinker,
                                         final XsrfTokenAccessor xsrfTokenAccessor,
                                         final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, applicationLinkService,
                adminUIAuthenticator, batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider,
                internalHostApplication, xsrfTokenAccessor, xsrfTokenValidator);
        this.trustedAppsManager = trustedAppsManager;
        this.trustedApplicationsManager = trustedApplicationsManager;
        this.configurationManager = configurationManager;
        this.trustConfigurator = trustConfigurator;
    }

    protected Action getAction(final HttpServletRequest request)
    {
        final String value = getRequiredParameter(request, "action");
        try
        {
            return Action.valueOf(value);
        }
        catch (IllegalArgumentException e)
        {
            throw new BadRequestException(messageFactory.newI18nMessage("auth.trusted.config.reciprocal.action.missing", value));
        }
    }

    @Override
    protected List<String> getRequiredWebResources()
    {
        return ImmutableList.of(WEB_RESOURCE_KEY + "trusted-auth");
    }

    protected boolean peerHasUAL(final HttpServletRequest request)
    {
        return !request.getServletPath().endsWith("-non-ual");   // UAL would end in "[inbound|outbound]-ual"
    }

    protected void render(final HttpServletRequest request,
                          final HttpServletResponse response,
                          final String consumer,
                          final String consumerAppType,
                          final String provider,
                          final String providerAppType,
                          final boolean enabled,
                          final Map<String, Object> renderContext)
            throws IOException
    {
        final Object view = renderContext.get("view");
        final String role = request.getServletPath().replaceFirst(".*/([^/?]+).*", "$1").startsWith("inbound") ? "provider" : "consumer";
        render( VM_TEMPLATE,
                new RendererContextBuilder(renderContext)
                        .put("stringUtils", new StringUtils())
                        .put("enabled", enabled)
                        .put("view", ObjectUtils.defaultIfNull(view, enabled ? "enabled" : "disabled"))
                        .put("nonUAL", !peerHasUAL(request))
                        .put("formLocation", request.getContextPath() + request.getServletPath() + request.getPathInfo())
                        .put("consumer", consumer)
                        .put("consumerAppType", consumerAppType)
                        .put("providerAppType", providerAppType)
                        .put("provider", provider)
                        .put("role", role)
                        .build(),
                request, response);
    }
}
