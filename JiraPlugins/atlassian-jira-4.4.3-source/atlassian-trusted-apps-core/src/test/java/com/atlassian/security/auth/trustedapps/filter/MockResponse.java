/**
 * 
 */
package com.atlassian.security.auth.trustedapps.filter;

import com.mockobjects.servlet.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

class MockResponse extends MockHttpServletResponse
{
    private final Map<String, String> headers = new HashMap<String, String>();

    @Override
    public void addHeader(final String arg1, final String arg2)
    {
        headers.put(arg1, arg2);
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }
}