package com.atlassian.security.auth.trustedapps.filter;

import com.atlassian.security.auth.trustedapps.TransportErrorMessage;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;
import com.atlassian.security.auth.trustedapps.filter.Authenticator.Result;

import com.mockobjects.servlet.MockFilterChain;
import com.mockobjects.servlet.MockFilterConfig;
import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;
import com.mockobjects.servlet.MockHttpSession;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

public class TestTrustedApplicationsFilter extends TestCase
{
    public void testAuthenticatorResultCtor() throws Exception
    {
        try
        {
            new Authenticator.Result(null, new TransportErrorMessage.UserUnknown("dodgy"), new Principal()
            {
                public String getName()
                {
                    return "dodgy";
                }
            });
            fail("null Status should have thrown IllegalArg");
        }
        catch (final IllegalArgumentException expected)
        {
            // yay
        }
    }

    @SuppressWarnings("deprecation")
    public void testFilterLifecycle() throws Exception
    {
        final TrustedApplicationsFilter filter = new MockTrustedApplicationsFilter();
        final MockFilterConfig config = new MockFilterConfig();

        assertNull(filter.getFilterConfig());

        filter.init(config);
        assertSame(config, filter.getFilterConfig());

        filter.destroy();
        assertNull(filter.getFilterConfig());

        filter.setFilterConfig(config);
        assertSame(config, filter.getFilterConfig());

        filter.setFilterConfig(null); // should not clear if null passed
        assertSame(config, filter.getFilterConfig());
    }

    public void testGetPathInfoNullContext() throws Exception
    {
        final TrustedApplicationsFilter filter = new MockTrustedApplicationsFilter();
        final MockRequest request = new MockRequest("/some/path")
        {
            @Override
            public String getRequestURI()
            {
                return getPathInfo();
            }

            @Override
            public String getContextPath()
            {
                return null;
            }
        };
        assertEquals("/some/path", filter.getPathInfo(request));
    }

    public void testGetPathInfoWithContext() throws Exception
    {
        final TrustedApplicationsFilter filter = new MockTrustedApplicationsFilter();
        final MockRequest request = new MockRequest("/some/context/some/path")
        {
            @Override
            public String getRequestURI()
            {
                return getPathInfo();
            }

            @Override
            public String getContextPath()
            {
                return "/some/context";
            }
        };
        assertEquals("/some/path", filter.getPathInfo(request));
    }

    public void testCertificateServerCalledForTrustURL() throws Exception
    {
        final String certificate = "Certificate server called!";
        final String contextPath = "/some/context";

        final MockTrustedApplicationsFilter filter = new MockTrustedApplicationsFilter();
        filter.getMockCertificateServer().setCertificate(certificate);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setupGetContextPath(contextPath);
        request.setupGetRequestURI(contextPath + TrustedApplicationUtils.Constant.CERTIFICATE_URL_PATH);

        final FilterChain chain = new MockFilterChain();
        final StringWriter writer = new StringWriter();
        final ServletOutputStream stream = new ServletOutputStream()
        {
            @Override
            public void write(final int b) throws IOException
            {
                writer.write(b);
            }
        };

        final MockResponse response = new MockResponse()
        {
            @Override
            public ServletOutputStream getOutputStream() throws IOException
            {
                return stream;
            }
        };

        filter.doFilter(request, response, chain);
        assertEquals(certificate, writer.toString());
    }

    public void testAuthenticateCalledForNonCertURL() throws Exception
    {
        final MockTrustedApplicationsFilter filter = new MockTrustedApplicationsFilter();
        filter.getMockAuthenticator().setResult(new Result(Result.Status.SUCCESS));

        final boolean[] invalidateCalled = new boolean[] { false };
        final MockHttpSession session = new MockHttpSession()
        {
            @Override
            public void invalidate()
            {
                invalidateCalled[0] = true;
            }
        };

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setupGetContextPath("/some/context");
        mockRequest.setupGetRequestURI("/some/context/some/url");
        mockRequest.setSession(session);
        mockRequest.setupGetAttribute(null);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        final FilterChain chain = new MockFilterChain()
        {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException
            {
                assertSame(mockRequest, request);
                assertSame(mockResponse, response);
            }
        };

        filter.doFilter(mockRequest, mockResponse, chain);
        assertTrue(invalidateCalled[0]);
    }

    public void testSessionInvalidateCalledForNonCertURLWhenExceptionThrown() throws Exception
    {
        final MockTrustedApplicationsFilter filter = new MockTrustedApplicationsFilter();
        filter.getMockAuthenticator().setResult(new Result(Result.Status.SUCCESS));

        final boolean[] invalidateCalled = new boolean[] { false };
        final MockHttpSession session = new MockHttpSession()
        {
            @Override
            public void invalidate()
            {
                invalidateCalled[0] = true;
            }
        };

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setupGetContextPath("/some/context");
        mockRequest.setupGetRequestURI("/some/context/some/url");
        mockRequest.setSession(session);
        mockRequest.setupGetAttribute(null);

        final MockResponse mockResponse = new MockResponse();
        final FilterChain chain = new MockFilterChain()
        {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException
            {
                assertSame(mockRequest, request);
                assertSame(mockResponse, response);
                throw new RuntimeException("poo");
            }
        };
        try
        {
            filter.doFilter(mockRequest, mockResponse, chain);
            fail("filter should not swallow exceptions");
        }
        catch (final RuntimeException expected)
        {
            // yay
        }
        assertTrue(invalidateCalled[0]);
    }

    public void testSessionInvalidateNotCalledForNonCertURLWhenResultFailed() throws Exception
    {
        final TrustedApplicationsFilter filter = new MockTrustedApplicationsFilter();
        final boolean[] invalidateCalled = new boolean[] { false };
        final MockHttpSession session = new MockHttpSession()
        {
            @Override
            public void invalidate()
            {
                invalidateCalled[0] = true;
            }
        };

        final MockRequest mockRequest = new MockRequest("/some/context/some/url")
        {
            @Override
            public String getRequestURI()
            {
                return getPathInfo();
            }

            @Override
            public String getContextPath()
            {
                return "/some/context";
            }

            @Override
            public HttpSession getSession()
            {
                return session;
            }
        };

        mockRequest.setupGetAttribute(null);

        final MockResponse mockResponse = new MockResponse();
        final FilterChain chain = new MockFilterChain()
        {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException
            {
                assertSame(mockRequest, request);
                assertSame(mockResponse, response);
                throw new RuntimeException("poo");
            }
        };
        try
        {
            filter.doFilter(mockRequest, mockResponse, chain);
            fail("filter should not swallow exceptions");
        }
        catch (final RuntimeException expected)
        {
            // yay
        }
        assertFalse(invalidateCalled[0]);
    }

    public void testSessionInvalidateNotCalledForNonCertURLWhenResultError() throws Exception
    {
        final TrustedApplicationsFilter filter = new MockTrustedApplicationsFilter();
        final boolean[] invalidateCalled = new boolean[] { false };
        final MockHttpSession session = new MockHttpSession()
        {
            @Override
            public void invalidate()
            {
                invalidateCalled[0] = true;
            }
        };

        final MockRequest mockRequest = new MockRequest("/some/context/some/url")
        {
            @Override
            public String getRequestURI()
            {
                return getPathInfo();
            }

            @Override
            public String getContextPath()
            {
                return "/some/context";
            }

            @Override
            public HttpSession getSession()
            {
                return session;
            }
        };

        mockRequest.setupGetAttribute(null);

        final MockResponse mockResponse = new MockResponse();
        final FilterChain chain = new MockFilterChain()
        {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException
            {
                assertSame(mockRequest, request);
                assertSame(mockResponse, response);
                throw new RuntimeException("poo");
            }
        };
        try
        {
            filter.doFilter(mockRequest, mockResponse, chain);
            fail("filter should not swallow exceptions");
        }
        catch (final RuntimeException expected)
        {
            // yay
        }
        assertFalse(invalidateCalled[0]);
    }

    public void testSessionInvalidateNotCalledForNonCertURLWhenResultNoAttempt() throws Exception
    {
        final TrustedApplicationsFilter filter = new MockTrustedApplicationsFilter();
        final boolean[] invalidateCalled = new boolean[] { false };
        final MockHttpSession session = new MockHttpSession()
        {
            @Override
            public void invalidate()
            {
                invalidateCalled[0] = true;
            }
        };

        final MockRequest mockRequest = new MockRequest("/some/context/some/url")
        {
            @Override
            public String getRequestURI()
            {
                return getPathInfo();
            }

            @Override
            public String getContextPath()
            {
                return "/some/context";
            }

            @Override
            public HttpSession getSession()
            {
                return session;
            }
        };

        mockRequest.setupGetAttribute(null);

        final MockResponse mockResponse = new MockResponse();
        final FilterChain chain = new MockFilterChain()
        {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException
            {
                assertSame(mockRequest, request);
                assertSame(mockResponse, response);
                throw new RuntimeException("poo");
            }
        };
        try
        {
            filter.doFilter(mockRequest, mockResponse, chain);
            fail("filter should not swallow exceptions");
        }
        catch (final RuntimeException expected)
        {
            // yay
        }
        assertFalse(invalidateCalled[0]);
    }

    public void testAythenticateNotCalledWhenAlreadyLoggedIn() throws Exception
    {
        final TrustedApplicationsFilter filter = new MockTrustedApplicationsFilter();

        final MockRequest mockRequest = new MockRequest("/some/context/some/url")
        {
            @Override
            public String getRequestURI()
            {
                return getPathInfo();
            }

            @Override
            public String getContextPath()
            {
                return "/some/context";
            }
        };

        mockRequest.setupGetAttribute("LOGGED_IN");

        final MockResponse mockResponse = new MockResponse();
        final FilterChain chain = new MockFilterChain()
        {
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException
            {
                assertSame(mockRequest, request);
                assertSame(mockResponse, response);
                throw new RuntimeException("poo");
            }
        };
        try
        {
            filter.doFilter(mockRequest, mockResponse, chain);
            fail("filter should not swallow exceptions");
        }
        catch (final RuntimeException expected)
        {
            // yay
        }
    }

    public void testResultStatusSuccess() throws Exception
    {
        assertEquals("success", Result.Status.SUCCESS.toString());
    }

    public void testResultStatusFailed() throws Exception
    {
        assertEquals("failed", Result.Status.FAILED.toString());
    }

    public void testResultStatusError() throws Exception
    {
        assertEquals("error", Result.Status.ERROR.toString());
    }

    public void testResultStatusNoAttempt() throws Exception
    {
        assertEquals("no attempt", Result.Status.NO_ATTEMPT.toString());
    }
}