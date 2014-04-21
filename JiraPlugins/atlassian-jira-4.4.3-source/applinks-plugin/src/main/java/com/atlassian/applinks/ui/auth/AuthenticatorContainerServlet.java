package com.atlassian.applinks.ui.auth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor;
import com.atlassian.applinks.core.util.Message;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.auth.AuthenticationDirection;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.applinks.ui.AbstractAppLinksAdminOnlyServlet;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor.BY_WEIGHT;

/**
 * Servlet for generating a collection of iframes targeting inbound/outbound
 * authenticator configuration servlets.
 *
 * @since v3.0
 */
public class AuthenticatorContainerServlet extends AbstractAppLinksAdminOnlyServlet
{
    /* query parameters */
    private static final String APPLICATION_ID = "applicationId";
    private static final String DIRECTION = "direction";

    /* templates */
    private static final String SUCCESS_TEMPLATE = "com/atlassian/applinks/ui/auth_container.vm";

    private final ApplicationLinkService applicationLinkService;
    private final ManifestRetriever manifestRetriever;
    private final PluginAccessor pluginAccessor;
    private final WebSudoManager webSudoManager;

    public AuthenticatorContainerServlet(final I18nResolver i18nResolver,
            final MessageFactory messageFactory,
            final TemplateRenderer templateRenderer,
            final WebResourceManager webResourceManager,
            final ApplicationLinkService applicationLinkService,
            final InternalHostApplication internalHostApplication,
            final ManifestRetriever manifestRetriever,
            final PluginAccessor pluginAccessor,
            final AdminUIAuthenticator adminUIAuthenticator,
            final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
            final LoginUriProvider loginUriProvider,
            final DocumentationLinker documentationLinker,
            final WebSudoManager webSudoManager,
            final XsrfTokenAccessor xsrfTokenAccessor,
            final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, adminUIAuthenticator,
                batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider, internalHostApplication,
                xsrfTokenAccessor, xsrfTokenValidator);
        this.applicationLinkService = applicationLinkService;
        this.manifestRetriever = manifestRetriever;
        this.pluginAccessor = pluginAccessor;
        this.webSudoManager = webSudoManager;
    }

    @Override
    protected List<String> getRequiredWebResources()
    {
        return ImmutableList.of(WEB_RESOURCE_KEY + "auth-container");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationId applicationId = new ApplicationId(getRequiredParameter(request, APPLICATION_ID));
            final ApplicationLink link;
            try
            {
                link = applicationLinkService.getApplicationLink(applicationId);
            }
            catch (TypeNotInstalledException e)
            {
                throw new IllegalStateException(String.format("Failed to load application %s as the %s type is not installed",
                                                              applicationId, e.getType()));
            }
            if (link == null)
            {
                logger.error("Couldn't find application link with id " + applicationId);
                throw new NotFoundException(messageFactory.newI18nMessage("auth.oauth.config.error.link.id", applicationId.toString()));
            }
            final AuthenticationDirection direction = AuthenticationDirection.valueOf(getRequiredParameter(request, DIRECTION));

            try
            {
                final Manifest manifest = manifestRetriever.getManifest(link.getRpcUrl(), link.getType());
                final Set<Class<? extends AuthenticationProvider>> authenticationProviderClasses;
                if (direction == AuthenticationDirection.INBOUND)
                {
                    authenticationProviderClasses = Sets.intersection(
                        Sets.<Class<? extends AuthenticationProvider>>newHashSet(internalHostApplication.getSupportedInboundAuthenticationTypes()),
                        manifest.getOutboundAuthenticationTypes());
                }
                else /* OUTBOUND */
                {
                    authenticationProviderClasses = Sets.intersection(
                        Sets.<Class<? extends AuthenticationProvider>>newHashSet(internalHostApplication.getSupportedOutboundAuthenticationTypes()),
                        manifest.getInboundAuthenticationTypes());
                }

                final List<ConfigTab> tabs = new ArrayList<ConfigTab>();

                final List<AuthenticationProviderModuleDescriptor> descriptors =
                    pluginAccessor.getEnabledModuleDescriptorsByClass(AuthenticationProviderModuleDescriptor.class);
                Collections.sort(descriptors, BY_WEIGHT);

                for (final AuthenticationProviderModuleDescriptor descriptor : descriptors)
                {
                    final AuthenticationProviderPluginModule module = descriptor.getModule();
                    if (authenticationProviderClasses.contains(module.getAuthenticationProviderClass()))
                    {
                        final String providerType = module.getAuthenticationProviderClass().getName();
                        final String cssClass = providerType.substring(providerType.lastIndexOf(".") + 1) ;
                        tabs.add(new ConfigTab(messageFactory.newI18nMessage(descriptor.getI18nNameKey()), module.getConfigUrl(link, manifest.getAppLinksVersion(), direction, request), descriptor.getKey(), cssClass));
                    }
                }

                final RendererContextBuilder contextBuilder = createContextBuilder(link);
                contextBuilder.put("tabs", tabs).put("direction", direction.name());
                render(SUCCESS_TEMPLATE, contextBuilder.build(), request, response);
            }
            catch (ManifestNotFoundException e)
            {
                logger.error("Failed to retrieve manifest for application link '" + link + "'.");
                throw new NotFoundException(messageFactory.newI18nMessage("auth.config.manifest.missing", link.getName()));
            }
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    public static class ConfigTab
    {

        private final Message name;
        private final String url;
        private final String id;
        private final String cssClass;

        public ConfigTab(final Message name, final String url, final String id, final String cssClass)
        {
            this.name = name;
            this.url = url;
            this.id = id;
            this.cssClass = cssClass;
        }

        public Message getName()
        {
            return name;
        }

        public String getUrl()
        {
            return url;
        }
        
        public String getId()
        {
            return id;
        }

        public String getCssClass()
        {
            return cssClass;
        }
    }
}
