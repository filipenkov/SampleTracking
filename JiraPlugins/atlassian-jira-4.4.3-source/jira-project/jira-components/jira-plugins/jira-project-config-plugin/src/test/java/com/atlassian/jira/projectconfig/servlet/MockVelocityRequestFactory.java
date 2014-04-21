package com.atlassian.jira.projectconfig.servlet;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Simple No-op implementation for tests.
 *
 * @since v4.4
 */
public class MockVelocityRequestFactory implements VelocityContextFactory
{
    @Override
    public Map<String, Object> createVelocityContext(Map<String, Object> initContext)
    {
        return initContext;
    }

    @Override
    public Map<String, Object> createDefaultVelocityContext()
    {
        return Collections.emptyMap();
    }
}
