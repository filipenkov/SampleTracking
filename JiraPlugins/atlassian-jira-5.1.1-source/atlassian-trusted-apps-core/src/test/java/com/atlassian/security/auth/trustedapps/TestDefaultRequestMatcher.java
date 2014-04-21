package com.atlassian.security.auth.trustedapps;

import com.mockobjects.servlet.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TestDefaultRequestMatcher extends TestCase
{
    public void testAllGood() throws Exception
    {
        final RequestValidator matcher = new DefaultRequestValidator(new IPMatcher()
        {
            public boolean match(final String ipAddress)
            {
                assertEquals("123.12.0.89", ipAddress);
                return true;
            }
        }, new URLMatcher()
        {
            public boolean match(final String urlPath)
            {
                assertEquals("/some/request/url", urlPath);
                return true;
            }
        });
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setupGetRemoteAddr("123.12.0.89");
        request.setupGetRequestURI("/some/request/url");
        request.setupGetContextPath("");
        request.setupAddHeader("X-Forwarded-For", null);
        matcher.validate(request);
    }

    public void testBadIpAddress() throws Exception
    {
        final RequestValidator matcher = new DefaultRequestValidator(new MockIPMatcher(false)
        {
            @Override
            public boolean match(final String ipAddress)
            {
                assertEquals("123.45.67.89", ipAddress);
                return super.match(ipAddress);
            }
        }, new MockURLMatcher(true));

        try
        {
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setupGetRemoteAddr("123.45.67.89");
            matcher.validate(request);
            fail("should have thrown invalid ip");
        }
        catch (final InvalidRemoteAddressException e)
        {
            // expected
        }
    }

    public void testIpAddressInXForwardedForChecked() throws Exception
    {
        final List<String> checkIps = new ArrayList<String>();
        final RequestValidator matcher = new DefaultRequestValidator(new IPMatcher()
        {
            public boolean match(final String ipAddress)
            {
                checkIps.add(ipAddress);
                return true;
            }
        }, new MockURLMatcher(true));

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setupGetRemoteAddr("123.45.67.89");
        request.setupAddHeader("X-Forwarded-For", "192.68.0.123");
        request.setupGetRequestURI("/some/request/url");
        request.setupGetContextPath("");
        matcher.validate(request);

        assertEquals(2, checkIps.size());
        assertTrue(checkIps.contains("123.45.67.89"));
        assertTrue(checkIps.contains("192.68.0.123"));
    }

    public void testMultipleIpAddressesInXForwardedForChecked() throws Exception
    {
        final List<String> checkIps = new ArrayList<String>();
        final RequestValidator matcher = new DefaultRequestValidator(new IPMatcher()
        {
            public boolean match(final String ipAddress)
            {
                checkIps.add(ipAddress);
                return true;
            }
        }, new MockURLMatcher(true));

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setupGetRemoteAddr("123.45.67.89");
        request.setupAddHeader("X-Forwarded-For", "192.68.0.123, 192.1.2.3, 192.4.5.6");
        request.setupGetRequestURI("/some/request/url");
        request.setupGetContextPath("");
        matcher.validate(request);

        assertEquals(4, checkIps.size());
        assertTrue(checkIps.contains("123.45.67.89"));
        assertTrue(checkIps.contains("192.68.0.123"));
        assertTrue(checkIps.contains("192.1.2.3"));
        assertTrue(checkIps.contains("192.4.5.6"));
    }

    public void testBadIpAddressInXForwardedFor() throws Exception
    {
        final RequestValidator matcher = new DefaultRequestValidator(new IPMatcher()
        {
            public boolean match(final String ipAddress)
            {
                return "123.45.67.89".equals(ipAddress);
            }
        }, new MockURLMatcher(true));

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setupGetRemoteAddr("123.45.67.89");
        request.setupGetRequestURI("/some/request/url");
        request.setupGetContextPath("");
        request.setupAddHeader("X-Forwarded-For", "192.68.0.123");
        try
        {
            matcher.validate(request);
            fail("Should have thrown illegal xforwarded ex");
        }
        catch (final InvalidXForwardedForAddressException e)
        {
            // expected
        }
    }

    public void testBadUrl() throws Exception
    {
        final RequestValidator matcher = new DefaultRequestValidator(new MockIPMatcher(true), new MockURLMatcher(false));
        try
        {
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setupGetRemoteAddr("123.12.0.89");
            request.setupGetRequestURI("/some/request/url");
            request.setupGetContextPath("");
            request.setupAddHeader("X-Forwarded-For", null);
            matcher.validate(request);
            fail("should have thrown invalid url");
        }
        catch (final InvalidRequestUrlException e)
        {
            // expected
        }
    }

    public void testContextPathRemoval() throws Exception
    {
        final RequestValidator matcher = new DefaultRequestValidator(new MockIPMatcher(true), new URLMatcher()
        {
            public boolean match(final String urlPath)
            {
                assertEquals("/some/request/url", urlPath);
                return true;
            }
        });

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setupGetRemoteAddr("123.12.0.89");
        request.setupGetRequestURI("/context/some/request/url");
        request.setupGetContextPath("/context");
        request.setupAddHeader("X-Forwarded-For", null);
        matcher.validate(request);
    }

    public void testNullIpMatcher() throws Exception
    {
        try
        {
            new DefaultRequestValidator(null, new MockURLMatcher(true));
            fail("should have thrown NPE");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testNullUrlMatcher() throws Exception
    {
        try
        {
            new DefaultRequestValidator(new MockIPMatcher(true), null);
            fail("should have thrown NPE");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    static class MockIPMatcher implements IPMatcher
    {
        final boolean result;

        public MockIPMatcher(final boolean result)
        {
            this.result = result;
        }

        public boolean match(final String ipAddress)
        {
            return result;
        }
    }

    static class MockURLMatcher implements URLMatcher
    {
        final boolean result;

        public MockURLMatcher(final boolean result)
        {
            this.result = result;
        }

        public boolean match(final String urlPath)
        {
            return result;
        }
    }

}