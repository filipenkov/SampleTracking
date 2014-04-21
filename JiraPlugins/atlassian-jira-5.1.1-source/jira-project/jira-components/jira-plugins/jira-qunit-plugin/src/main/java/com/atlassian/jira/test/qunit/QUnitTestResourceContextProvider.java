package com.atlassian.jira.test.qunit;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import java.util.Map;

/**
 * Velocity context provider for the QUnit items web-panel.
 */
public class QUnitTestResourceContextProvider implements ContextProvider
{
    @Override
    public void init(final Map<String, String> params)
            throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        context.put("qunitResourceProvider", new QUnitTestResourceProvider());
        return context;
    }
}
