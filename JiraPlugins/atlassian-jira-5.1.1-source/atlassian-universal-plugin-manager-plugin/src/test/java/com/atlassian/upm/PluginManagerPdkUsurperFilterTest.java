package com.atlassian.upm;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.PluginManagerPdkUsurperFilter.FileExtractorError;
import com.atlassian.upm.PluginManagerPdkUsurperFilter.FileExtractorError.Type;
import com.atlassian.upm.PluginManagerPdkUsurperFilter.PluginFile;
import com.atlassian.upm.api.util.Either;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import com.google.common.base.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginManagerPdkUsurperFilterTest
{
    private PluginManagerPdkUsurperFilter filter;
    private StringWriter stringWriter;

    @Mock PluginInstaller pluginInstaller;
    @Mock I18nResolver i18nResolver;
    @Mock PermissionEnforcer permissionEnforcer;
    @Mock PluginAccessorAndController pluginAccessorAndController;
    @Mock Function<HttpServletRequest, Either<FileExtractorError, PluginFile>> pluginFileExtractor;

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;

    @Before
    public void createPluginManagerPdkUsurperFilter() throws Exception
    {
        filter = spy(new PluginManagerPdkUsurperFilter(pluginInstaller, i18nResolver, permissionEnforcer, pluginAccessorAndController, pluginFileExtractor));
    }

    @Before
    public void prepareResponse() throws Exception
    {
        stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    }

    @Test
    public void verifyDoNotServiceNonPostRequests() throws Exception
    {
        when(request.getMethod()).thenReturn("get");

        filter.doFilter(request, response, null);

        verify(response).setContentType("text/plain");
        verify(response).sendError(eq(SC_METHOD_NOT_ALLOWED), argThat(containsString("Requires post")));
    }

    @Test
    public void verifyDoNotServiceNonMultipartRequests() throws Exception
    {
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("text/html");

        filter.doFilter(request, response, null);

        verify(response).setContentType("text/plain");
        verify(response).sendError(eq(SC_BAD_REQUEST), argThat(containsString("Missing plugin file")));
    }

    @Test
    public void verifyDoNotServiceUnauthorizedRequests() throws Exception
    {
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/html");
        when(permissionEnforcer.hasPermission(MANAGE_PLUGIN_INSTALL)).thenReturn(false);

        filter.doFilter(request, response, null);

        verify(response).setContentType("text/plain");
        verify(response).sendError(eq(SC_UNAUTHORIZED), argThat(containsString("Must have permission to access this resource.")));
    }

    @Test
    public void verifyDoNotAllowPluginInstallInSafeMode() throws Exception
    {
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/html");
        when(permissionEnforcer.hasPermission(MANAGE_PLUGIN_INSTALL)).thenReturn(true);
        when(pluginAccessorAndController.isSafeMode()).thenReturn(true);
        when(i18nResolver.getText("upm.pluginInstall.error.safe.mode")).thenReturn("Bad Stuff");

        filter.doFilter(request, response, null);

        verify(response).setContentType("text/plain");
        verify(response).sendError(eq(SC_CONFLICT), argThat(containsString("Bad Stuff")));
    }

    @Test
    public void verifyBadRequestErrorWhenThereIsNoFileToInstall() throws Exception
    {
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/html");
        when(permissionEnforcer.hasPermission(MANAGE_PLUGIN_INSTALL)).thenReturn(true);
        when(pluginAccessorAndController.isSafeMode()).thenReturn(false);
        when(pluginFileExtractor.apply(request)).thenReturn(Either.<FileExtractorError, PluginFile>left(new FileExtractorError(Type.FILE_NOT_FOUND)));

        filter.doFilter(request, response, null);

        verify(response).setContentType("text/plain");
        verify(response).sendError(eq(SC_BAD_REQUEST), argThat(containsString("Missing plugin file")));
    }

    @Test
    public void verifySuccessfulInstallOfPlugin() throws Exception
    {
        final PluginFile plugin = new PluginFile(new File("blah"), "blah");
        when(pluginFileExtractor.apply(request)).thenReturn(Either.<FileExtractorError, PluginFile>right(plugin));
        doNothing().when(filter).installPlugin(response, plugin);

        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/html");
        when(permissionEnforcer.hasPermission(MANAGE_PLUGIN_INSTALL)).thenReturn(true);
        when(pluginAccessorAndController.isSafeMode()).thenReturn(false);

        filter.doFilter(request, response, null);

        verify(filter).installPlugin(response, plugin);
    }
}
