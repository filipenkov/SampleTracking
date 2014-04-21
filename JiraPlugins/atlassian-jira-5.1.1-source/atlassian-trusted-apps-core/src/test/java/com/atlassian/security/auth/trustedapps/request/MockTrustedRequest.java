package com.atlassian.security.auth.trustedapps.request;

import java.util.HashMap;
import java.util.Map;

/**
 * A mock {@link TrustedRequest} for easy testing
 */
public class MockTrustedRequest implements TrustedRequest
{
    private final Map<String, String> parameters = new HashMap<String, String>();

    public void addRequestParameter(final String name, final String value)
    {
        parameters.put(name, value);
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }
}
