package com.atlassian.upm;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginManagerServletTest
{
    @Mock TemplateRenderer renderer;
    @Mock PermissionEnforcer permissionEnforcer;
    @Mock LoginUriProvider loginUriProvider;
    @Mock WebSudoManager webSudoManager;
    @Mock PluginAccessorAndController accessor;

    HttpServlet servlet;

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock HttpSession session;

    @Before
    public void createPluginManagerServlet()
    {
        servlet = new PluginManagerServlet(
            new PluginManagerHandler(renderer, permissionEnforcer, loginUriProvider, webSudoManager, accessor)
        );
    }

    @Before
    public void prepareRequest()
    {
        when(request.getMethod()).thenReturn("GET");
    }

    @Before
    public void prepareResponse() throws Exception
    {
        when(response.getWriter()).thenReturn(new PrintWriter(new ByteArrayOutputStream()));
    }

    @Test
    public void verifyContentTypeIsSetToTextHtmlWhenLoggedInAsAdmin() throws Exception
    {
        whenUserIsLoggedInAsAdmin();

        when(request.getSession()).thenReturn(session);
        servlet.service(request, response);

        verify(webSudoManager).willExecuteWebSudoRequest(request);
        verify(session).removeAttribute(PluginManagerHandler.JIRA_SERAPH_SECURITY_ORIGINAL_URL);
        verify(session).removeAttribute(PluginManagerHandler.CONF_SERAPH_SECURITY_ORIGINAL_URL);
        verify(response).setContentType("text/html;charset=utf-8");
    }

    @Test
    public void verifyRenderCalledWhenLoggedInAsAdmin() throws Exception
    {
        whenUserIsLoggedInAsAdmin();

        when(request.getSession()).thenReturn(session);
        servlet.service(request, response);

        verify(webSudoManager).willExecuteWebSudoRequest(request);
        verify(session).removeAttribute(PluginManagerHandler.JIRA_SERAPH_SECURITY_ORIGINAL_URL);
        verify(session).removeAttribute(PluginManagerHandler.CONF_SERAPH_SECURITY_ORIGINAL_URL);
        verify(renderer).render(eq("plugin-manager.vm"), isA(Map.class), isA(Writer.class));
    }

    @Test
    public void verifyUserRedirectedToLoginWhenNotLoggedIn() throws Exception
    {
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://upm"));
        when(request.getQueryString()).thenReturn("param=value");
        when(request.getSession()).thenReturn(session);
        final String uriString = "http://upm?param=value";
        when(loginUriProvider.getLoginUri(URI.create(uriString))).thenReturn(URI.create("http://login"));

        servlet.service(request, response);

        verify(webSudoManager).willExecuteWebSudoRequest(request);
        verify(session).setAttribute(PluginManagerHandler.JIRA_SERAPH_SECURITY_ORIGINAL_URL, uriString);
        verify(session).setAttribute(PluginManagerHandler.CONF_SERAPH_SECURITY_ORIGINAL_URL, uriString);
        verify(response).sendRedirect("http://login");
    }

    @Test
    public void verifyUserRedirectedToLoginWhenNotLoggedInAsAdmin() throws Exception
    {
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://upm"));
        when(request.getQueryString()).thenReturn("param=value");
        when(request.getSession()).thenReturn(session);
        when(loginUriProvider.getLoginUri(URI.create("http://upm?param=value"))).thenReturn(URI.create("http://login"));

        servlet.service(request, response);

        verify(webSudoManager).willExecuteWebSudoRequest(request);
        verify(response).sendRedirect("http://login");
    }

    @Test
    public void verifyWebSudoGivenControlWhenRequestRequiresSudoAuth() throws Exception
    {
        doThrow(new WebSudoSessionException("blah")).when(webSudoManager).willExecuteWebSudoRequest(request);
        servlet.service(request, response);

        verify(webSudoManager).enforceWebSudoProtection(request, response);
    }

    @Test
    public void verifyUserRedirectedtoTunneledFragmentTag() throws Exception
    {
        whenUserIsLoggedInAsAdmin();
        when(request.getParameter("fragment")).thenReturn("manage/com.atlassian.plugin.key");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://upm"));
        when(request.getQueryString()).thenReturn("fragment=manage/com.atlassian.plugin.key");
        when(request.getSession()).thenReturn(session);

        servlet.service(request, response);

        verify(response).sendRedirect("http://upm#manage/com.atlassian.plugin.key");
    }

    private void whenUserIsLoggedInAsAdmin()
    {
        when(permissionEnforcer.isAdmin()).thenReturn(true);
    }

}
