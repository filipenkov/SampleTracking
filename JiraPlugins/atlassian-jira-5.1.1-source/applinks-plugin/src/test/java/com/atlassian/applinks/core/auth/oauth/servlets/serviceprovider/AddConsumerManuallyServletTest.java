package com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.auth.oauth.ServiceProviderStoreService;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
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
public class AddConsumerManuallyServletTest
{
    @DataPoints public static String[] methods = new String[]{"GET", "POST"};

    I18nResolver i18nResolver;
    MessageFactory messageFactory;
    TemplateRenderer templateRenderer;
    WebResourceManager webResourceManager;
    ApplicationLinkService applicationLinkService;
    AdminUIAuthenticator adminUIAuthenticator;
    InternalHostApplication internalHostApplication;
    BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory;
    DocumentationLinker documentationLinker;
    LoginUriProvider loginUriProvider;
    WebSudoManager webSudoManager;
    RequestFactory requestFactory;
    ServiceProviderStoreService serviceProviderStoreService;
    XsrfTokenAccessor xsrfTokenAccessor;
    XsrfTokenValidator xsrfTokenValidator;

    HttpServletRequest request;
    HttpServletResponse response;
    HttpSession session;
    PrintWriter writer;

    HttpServlet servlet;

    @Before
    public void setUp()
    {
        i18nResolver = mock(I18nResolver.class);
        messageFactory = mock(MessageFactory.class);
        templateRenderer = mock(TemplateRenderer.class);
        webResourceManager = mock(WebResourceManager.class);
        applicationLinkService = mock(ApplicationLinkService.class);
        adminUIAuthenticator = mock(AdminUIAuthenticator.class);
        internalHostApplication = mock(InternalHostApplication.class);
        batchedJSONi18NBuilderFactory = mock(BatchedJSONi18NBuilderFactory.class);
        documentationLinker = mock(DocumentationLinker.class);
        loginUriProvider = mock(LoginUriProvider.class);
        webSudoManager = mock(WebSudoManager.class);
        requestFactory = mock(RequestFactory.class);
        serviceProviderStoreService = mock(ServiceProviderStoreService.class);
        xsrfTokenAccessor = mock(XsrfTokenAccessor.class);
        xsrfTokenValidator = mock(XsrfTokenValidator.class);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        writer = mock(PrintWriter.class);

        when(request.getPathInfo()).thenReturn("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
        when(request.getServletPath()).thenReturn("servlet-path");
        when(xsrfTokenValidator.validateFormEncodedToken(request)).thenReturn(true);

        servlet = new AddConsumerManuallyServlet(i18nResolver, messageFactory, templateRenderer, webResourceManager,
                applicationLinkService, adminUIAuthenticator, requestFactory, serviceProviderStoreService,
                internalHostApplication, batchedJSONi18NBuilderFactory, loginUriProvider, documentationLinker,
                webSudoManager, xsrfTokenAccessor, xsrfTokenValidator);
    }

    @Theory
    public void verifyThatAdminAccessIsCheckedForAdminOnlyServlet(String method) throws Exception
    {
        when(request.getMethod()).thenReturn(method);
        whenUserIsLoggedInAsAdmin();
        servlet.service(request, response);

        verify(adminUIAuthenticator).canAccessAdminUI(request);
    }

    @Theory
    public void verifyThatWebSudoRequestIsExecutedForAdminOnlyServlet(String method) throws Exception
    {
        when(request.getMethod()).thenReturn(method);
        whenUserIsLoggedInAsAdmin();

        servlet.service(request, response);

        verify(webSudoManager).willExecuteWebSudoRequest(request);
    }

    @Theory
    public void verifyWebSudoGivenControlWhenRequestRequiresSudoAuth(String method) throws Exception
    {
        when(request.getMethod()).thenReturn(method);
        whenUserIsLoggedInAsAdmin();

        doThrow(new WebSudoSessionException("blah")).when(webSudoManager).willExecuteWebSudoRequest(request);
        servlet.service(request, response);

        verify(webSudoManager).enforceWebSudoProtection(request, response);
    }

    private void whenUserIsLoggedInAsAdmin()
    {
        when(adminUIAuthenticator.canAccessAdminUI(request)).thenReturn(true);
    }
}