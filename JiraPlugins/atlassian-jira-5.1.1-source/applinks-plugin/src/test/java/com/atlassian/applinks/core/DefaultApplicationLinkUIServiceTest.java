package com.atlassian.applinks.core;

import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkUIService.MessageFormat;
import com.atlassian.applinks.core.DefaultApplicationLinkUIService;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultApplicationLinkUIServiceTest
{
    private static final String APPLINK_ID = UUID.randomUUID().toString();
    private static final String APP_NAME = "AppName";
    private static final String APP_NAME_XSS = "App \"><thingy";
    private static final String APP_NAME_XSS_ESCAPED = "App &quot;&gt;&lt;thingy";
    private static final String APP_URI = "http://app";
    private static final String AUTH_URI = "http://auth";
    
    @Mock I18nResolver i18nResolver;
    @Mock TemplateRenderer templateRenderer;
    @Mock ApplicationLink appLink;
    @Mock ApplicationLinkRequestFactory requestFactory;
    
    DefaultApplicationLinkUIService applicationLinkUIService;
    
    @Mock HttpServletRequest request;
    ByteArrayOutputStream baos;
    
    @Before
    public void createService()
    {
        applicationLinkUIService = new DefaultApplicationLinkUIService(i18nResolver, templateRenderer);
        
        when(appLink.getId()).thenReturn(new ApplicationId(APPLINK_ID));
        when(appLink.getName()).thenReturn(APP_NAME);
        when(appLink.getDisplayUrl()).thenReturn(URI.create(APP_URI));
        when(appLink.createAuthenticatedRequestFactory()).thenReturn(requestFactory);
        when(requestFactory.getAuthorisationURI()).thenReturn(URI.create(AUTH_URI));
        
        when(i18nResolver.getText(eq("applinks.util.auth.request"), anyString(), anyString(), anyString()))
                .thenReturn("MESSAGE");
        when(i18nResolver.getText(eq("applinks.util.auth.request.inline"), anyString()))
                .thenReturn("MESSAGE2");
    }
    
    @Test
    public void requestBannerCallsRenderer() throws Exception
    {
        applicationLinkUIService.authorisationRequest(appLink)
                .format(MessageFormat.BANNER)
                .getHtml();
        
        verify(i18nResolver)
            .getText("applinks.util.auth.request",
                     AUTH_URI,
                     APP_URI,
                     APP_NAME);
        
        ImmutableMap<String, Object> argsShouldBe = ImmutableMap.<String, Object>builder()
                .put("applinkId", APPLINK_ID)
                .put("appName", APP_NAME)
                .put("appUri", APP_URI)
                .put("authUri", AUTH_URI)
                .put("messageHtml", "MESSAGE")
                .put("contentHtml", "")
                .build();
        
        verify(templateRenderer)
            .render(eq("templates/fragments/auth_request_banner.vm"),
                    eq(argsShouldBe),
                    (Writer) Mockito.anyObject());
    }

    @Test
    public void requestBannerAppNameHtmlEscaping() throws Exception
    {
        when(appLink.getName()).thenReturn(APP_NAME_XSS);

        applicationLinkUIService.authorisationRequest(appLink)
                .format(MessageFormat.BANNER)
                .getHtml();
        
        verify(i18nResolver)
            .getText("applinks.util.auth.request",
                     AUTH_URI,
                     APP_URI,
                     APP_NAME_XSS_ESCAPED);
        
        ImmutableMap<String, Object> argsShouldBe = ImmutableMap.<String, Object>builder()
                .put("applinkId", APPLINK_ID)
                .put("appName", APP_NAME_XSS)
                .put("appUri", APP_URI)
                .put("authUri", AUTH_URI)
                .put("messageHtml", "MESSAGE")
                .put("contentHtml", "")
                .build();
        
        verify(templateRenderer)
            .render(eq("templates/fragments/auth_request_banner.vm"),
                    eq(argsShouldBe),
                    (Writer) Mockito.anyObject());
    }

    @Test
    public void requestInlineCallsRenderer() throws Exception
    {
        String contentHtml = "<b>CONTENT</b>";
        
        applicationLinkUIService.authorisationRequest(appLink)
                .format(MessageFormat.INLINE)
                .contentHtml(contentHtml)
                .getHtml();
        
        verify(i18nResolver).getText("applinks.util.auth.request.inline", AUTH_URI);
        
        ImmutableMap<String, Object> argsShouldBe = ImmutableMap.<String, Object>builder()
                .put("applinkId", APPLINK_ID)
                .put("appName", APP_NAME)
                .put("appUri", APP_URI)
                .put("authUri", AUTH_URI)
                .put("messageHtml", "MESSAGE2")
                .put("contentHtml", contentHtml)
                .build();
        
        verify(templateRenderer)
            .render(eq("templates/fragments/auth_request_inline.vm"),
                    eq(argsShouldBe),
                    (Writer) Mockito.anyObject());
    }
}
