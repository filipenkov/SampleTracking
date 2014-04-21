package com.atlassian.applinks;

import java.io.PrintWriter;
import java.net.URI;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.auth.basic.BasicServlet;
import com.atlassian.applinks.core.auth.oauth.ConsumerTokenStoreService;
import com.atlassian.applinks.core.auth.oauth.servlets.consumer.AddAtlassianServiceProviderServlet;
import com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider.AddConsumerReciprocalServlet;
import com.atlassian.applinks.core.auth.trusted.AutoConfigurationServlet;
import com.atlassian.applinks.core.auth.trusted.ConsumerConfigurationServlet;
import com.atlassian.applinks.core.auth.trusted.ProviderConfigurationServlet;
import com.atlassian.applinks.core.auth.trusted.TrustConfigurator;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.ListApplicationLinksServlet;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.applinks.ui.auth.AuthenticatorContainerServlet;
import com.atlassian.applinks.ui.velocity.VelocityContextFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsConfigurationManager;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class AdminOnlyServletsTest
{
    @DataPoints public static TestServlet[] testServlets = TestServlet.values();

    static I18nResolver i18nResolver;
    static MessageFactory messageFactory;
    static TemplateRenderer templateRenderer;
    static WebResourceManager webResourceManager;
    static ApplicationLinkService applicationLinkService;
    static ManifestRetriever manifestRetriever;
    static PluginAccessor pluginAccessor;
    static AdminUIAuthenticator adminUIAuthenticator;
    static InternalHostApplication internalHostApplication;
    static BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory;
    static DocumentationLinker documentationLinker;
    static LoginUriProvider loginUriProvider;
    static WebSudoManager webSudoManager;
    static VelocityContextFactory velocityContextFactory;
    static TrustedApplicationsManager trustedApplicationsManager;
    static AuthenticationConfigurationManager configurationManager;
    static TrustedApplicationsConfigurationManager trustedAppsManager;
    static TrustConfigurator trustConfigurator;
    static ConsumerTokenStoreService consumerTokenStoreService;
    static XsrfTokenAccessor xsrfTokenAccessor;
    static XsrfTokenValidator xsrfTokenValidator;

    HttpServletRequest request;
    HttpServletResponse response;
    HttpSession session;
    PrintWriter writer;

    @Before
    public void setUp()
    {
        i18nResolver = mock(I18nResolver.class);
        messageFactory = mock(MessageFactory.class);
        templateRenderer = mock(TemplateRenderer.class);
        webResourceManager = mock(WebResourceManager.class);
        applicationLinkService = mock(ApplicationLinkService.class);
        manifestRetriever = mock(ManifestRetriever.class);
        pluginAccessor = mock(PluginAccessor.class);
        adminUIAuthenticator = mock(AdminUIAuthenticator.class);
        internalHostApplication = mock(InternalHostApplication.class);
        batchedJSONi18NBuilderFactory = mock(BatchedJSONi18NBuilderFactory.class);
        documentationLinker = mock(DocumentationLinker.class);
        loginUriProvider = mock(LoginUriProvider.class);
        webSudoManager = mock(WebSudoManager.class);
        velocityContextFactory = mock(VelocityContextFactory.class);
        trustedApplicationsManager = mock(TrustedApplicationsManager.class);
        configurationManager = mock(AuthenticationConfigurationManager.class);
        trustedAppsManager = mock(TrustedApplicationsConfigurationManager.class);
        trustConfigurator = mock(TrustConfigurator.class);
        consumerTokenStoreService = mock(ConsumerTokenStoreService.class);
        xsrfTokenAccessor = mock(XsrfTokenAccessor.class);
        xsrfTokenValidator = mock(XsrfTokenValidator.class);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        writer = mock(PrintWriter.class);

        when(request.getPathInfo()).thenReturn("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
        when(request.getServletPath()).thenReturn("servlet-path");
        when(xsrfTokenValidator.validateFormEncodedToken(request)).thenReturn(true);
    }

    @Theory
    public void verifyThatAdminAccessIsCheckedForAdminOnlyServlet(TestServlet servlet) throws Exception
    {
        when(request.getMethod()).thenReturn(servlet.getMethod());
        whenUserIsLoggedInAsAdmin();
        servlet.getServlet().service(request, response);

        verify(adminUIAuthenticator).canAccessAdminUI(request);
    }

    @Theory
    public void verifyThatWebSudoRequestIsExecutedForAdminOnlyServlet(TestServlet servlet) throws Exception
    {
        when(request.getMethod()).thenReturn(servlet.getMethod());
        whenUserIsLoggedInAsAdmin();

        servlet.getServlet().service(request, response);

        verify(webSudoManager).willExecuteWebSudoRequest(request);
    }

    @Theory
    public void verifyWebSudoGivenControlWhenRequestRequiresSudoAuth(TestServlet servlet) throws Exception
    {
        when(request.getMethod()).thenReturn(servlet.getMethod());
        whenUserIsLoggedInAsAdmin();
        doThrow(new WebSudoSessionException("blah")).when(webSudoManager).willExecuteWebSudoRequest(request);
        servlet.getServlet().service(request, response);

        verify(webSudoManager).enforceWebSudoProtection(request, response);
    }

    enum TestServlet
    {
        LIST_APPLICATION_LINKS_SERVLET_GET
        {
            @Override
            HttpServlet getServlet()
            {
                return new ListApplicationLinksServlet(i18nResolver, messageFactory, templateRenderer, webResourceManager,
                        adminUIAuthenticator, internalHostApplication, batchedJSONi18NBuilderFactory, documentationLinker,
                        loginUriProvider, velocityContextFactory, webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "GET";
            }
        },

        AUTHENTICATOR_CONTAINER_SERVLET_GET
        {
            @Override
            HttpServlet getServlet()
            {
                return new AuthenticatorContainerServlet(i18nResolver, messageFactory, templateRenderer, webResourceManager,
                        applicationLinkService, internalHostApplication, manifestRetriever, pluginAccessor,
                        adminUIAuthenticator, batchedJSONi18NBuilderFactory, loginUriProvider, documentationLinker,
                        webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "GET";
            }
        },

        TRUSTED_AUTO_CONFIGURATION_SERVLET_DELETE
        {
            @Override
            HttpServlet getServlet()
            {
                return new AutoConfigurationServlet(i18nResolver, internalHostApplication, messageFactory,
                        templateRenderer, webResourceManager, adminUIAuthenticator, applicationLinkService,
                        trustedApplicationsManager, configurationManager, trustedAppsManager, trustConfigurator,
                        batchedJSONi18NBuilderFactory, loginUriProvider, documentationLinker, webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "DELETE";
            }
        },

        TRUSTED_CONSUMER_CONFIGURATION_SERVLET_GET
        {
            @Override
            HttpServlet getServlet()
            {
                return new ConsumerConfigurationServlet(i18nResolver, templateRenderer, adminUIAuthenticator,
                        webResourceManager, configurationManager, applicationLinkService, messageFactory,
                        trustedApplicationsManager, trustedAppsManager, internalHostApplication, trustConfigurator,
                        batchedJSONi18NBuilderFactory, loginUriProvider, documentationLinker, webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "GET";
            }
        },

        TRUSTED_CONSUMER_CONFIGURATION_SERVLET_POST
        {
            @Override
            HttpServlet getServlet()
            {
                return new ConsumerConfigurationServlet(i18nResolver, templateRenderer, adminUIAuthenticator,
                        webResourceManager, configurationManager, applicationLinkService, messageFactory,
                        trustedApplicationsManager, trustedAppsManager, internalHostApplication, trustConfigurator,
                        batchedJSONi18NBuilderFactory, loginUriProvider, documentationLinker, webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "POST";
            }
        },

        TRUSTED_PROVIDER_CONFIGURATION_SERVLET_GET
        {
            @Override
            HttpServlet getServlet()
            {
                return new ProviderConfigurationServlet(i18nResolver, templateRenderer, adminUIAuthenticator,
                        webResourceManager, applicationLinkService, messageFactory, trustedAppsManager,
                        configurationManager, trustedApplicationsManager, internalHostApplication, trustConfigurator,
                        batchedJSONi18NBuilderFactory, loginUriProvider, documentationLinker, webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "GET";
            }
        },

        TRUSTED_PROVIDER_CONFIGURATION_SERVLET_POST
        {
            @Override
            HttpServlet getServlet()
            {
                return new ProviderConfigurationServlet(i18nResolver, templateRenderer, adminUIAuthenticator,
                        webResourceManager, applicationLinkService, messageFactory, trustedAppsManager,
                        configurationManager, trustedApplicationsManager, internalHostApplication, trustConfigurator,
                        batchedJSONi18NBuilderFactory, loginUriProvider, documentationLinker, webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "POST";
            }
        },

        OAUTH_ADD_ATLASSIAN_SERVICE_PROVIDER_SERVLET_GET
        {
            @Override
            HttpServlet getServlet()
            {
                return new AddAtlassianServiceProviderServlet(i18nResolver, messageFactory, templateRenderer,
                        webResourceManager, applicationLinkService, adminUIAuthenticator, configurationManager,
                        consumerTokenStoreService, internalHostApplication, batchedJSONi18NBuilderFactory,
                        loginUriProvider, documentationLinker, webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "GET";
            }
        },

        OAUTH_ADD_ATLASSIAN_SERVICE_PROVIDER_SERVLET_POST
        {
            @Override
            HttpServlet getServlet()
            {
                return new AddAtlassianServiceProviderServlet(i18nResolver, messageFactory, templateRenderer,
                        webResourceManager, applicationLinkService, adminUIAuthenticator, configurationManager,
                        consumerTokenStoreService, internalHostApplication, batchedJSONi18NBuilderFactory,
                        loginUriProvider, documentationLinker, webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "POST";
            }
        },

        OAUTH_ADD_CONSUMER_RECIPROCAL_SERVLET_GET
        {
            @Override
            HttpServlet getServlet()
            {
                return new AddConsumerReciprocalServlet(i18nResolver, messageFactory, templateRenderer,
                        webResourceManager, applicationLinkService, adminUIAuthenticator, configurationManager,
                        consumerTokenStoreService, internalHostApplication, batchedJSONi18NBuilderFactory,
                        loginUriProvider, documentationLinker, webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "GET";
            }
        },

        BASIC_SERVLET_GET
        {
            @Override
            HttpServlet getServlet()
            {
                return new BasicServlet(adminUIAuthenticator, applicationLinkService, configurationManager,
                        i18nResolver, templateRenderer, webResourceManager, messageFactory,
                        batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider, internalHostApplication,
                        webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "GET";
            }
        },

        BASIC_SERVLET_PUT
        {
            @Override
            HttpServlet getServlet()
            {
                return new BasicServlet(adminUIAuthenticator, applicationLinkService, configurationManager,
                        i18nResolver, templateRenderer, webResourceManager, messageFactory,
                        batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider, internalHostApplication,
                        webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "PUT";
            }
        },

        BASIC_SERVLET_POST
        {
            @Override
            HttpServlet getServlet()
            {
                return new BasicServlet(adminUIAuthenticator, applicationLinkService, configurationManager,
                        i18nResolver, templateRenderer, webResourceManager, messageFactory,
                        batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider, internalHostApplication,
                        webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "POST";
            }
        },

        BASIC_SERVLET_DELETE
        {
            @Override
            HttpServlet getServlet()
            {
                return new BasicServlet(adminUIAuthenticator, applicationLinkService, configurationManager,
                        i18nResolver, templateRenderer, webResourceManager, messageFactory,
                        batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider, internalHostApplication,
                        webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
            }

            @Override
            String getMethod()
            {
                return "DELETE";
            }
        };

        abstract HttpServlet getServlet();
        abstract String getMethod();
    }

    private void whenUserIsLoggedInAsAdmin()
    {
        when(adminUIAuthenticator.canAccessAdminUI(request)).thenReturn(true);
    }
}
