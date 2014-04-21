package com.atlassian.gadgets.renderer.internal.servlet;

import java.net.URI;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.GadgetSpecUrlChecker;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.velocity.one.six.VelocityTemplateRenderer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetSpecUrlRenderPermissionServletFilterTest
{
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain chain;
    @Mock GadgetSpecUrlChecker gadgetChecker;
    @Mock I18nResolver i18n;
    @Mock UserManager userManager;
    @Mock VelocityTemplateRenderer renderer;

    private static final String URL_PARAM = "url";

    private GadgetSpecUrlRenderPermissionServletFilter filter;

    @Before
    public void setup()
    {
        filter = new GadgetSpecUrlRenderPermissionServletFilter(gadgetChecker, i18n, userManager, renderer);
    }

    @Test
    public void missingUrlParameterIsForwardedWithoutComment() throws Exception
    {
        when(request.getParameter(URL_PARAM)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void emptyUrlParameterIsForwardedWithoutComment() throws Exception
    {
        when(request.getParameter(URL_PARAM)).thenReturn("");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void validUrlParameterForwardsToFilterChain() throws Exception
    {
        URI validUri = URI.create("http://example.com/a-valid-uri");
        when(request.getParameter(URL_PARAM)).thenReturn(validUri.toString());

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void invalidUrlParameterReturnsHttpBadRequest() throws Exception
    {
        String invalidUri = "::@!$!@232:!@$:";
        when(request.getParameter(URL_PARAM)).thenReturn(invalidUri);

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
        verifyZeroInteractions(chain);
    }

    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void validUrlNoLongerInDirectoryReturnsHttpGone() throws Exception
    {
        URI validUri = URI.create("http://example.com/a-valid-uri-that-is-now-gone");
        when(request.getParameter(URL_PARAM)).thenReturn(validUri.toString());
        doThrow(new GadgetSpecUriNotAllowedException("")).when(gadgetChecker).assertRenderable(validUri.toString());

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_GONE);
        verifyZeroInteractions(chain);
    }

}
