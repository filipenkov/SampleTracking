package com.atlassian.gadgets.publisher.internal.impl;

import java.io.IOException;
import java.io.InputStream;

import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.plugin.Plugin;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.IOUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PluginGadgetSpecBuilder
{
    static final String PLUGIN_KEY = "com.atlassian.test";
    static final String MODULE_KEY = "test-gadget";
    static final String GADGET_LOCATION = "path/to/gadget.xml";

    static PluginGadgetSpec newPluginGadgetSpec(String location)
    {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn(PLUGIN_KEY);
        when(plugin.getResourceAsStream(GADGET_LOCATION)).thenAnswer(new Answer<InputStream>()
        {
            public InputStream answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return getHelloWorldGadget();
            }
        });

        return new PluginGadgetSpec(plugin, MODULE_KEY, location, ImmutableMap.<String, String>of());
    }

    static byte[] getHelloWorldGadgetBytes() throws IOException
    {
        InputStream in = getHelloWorldGadget();
        try
        {
            return IOUtils.toByteArray(in);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    private static InputStream getHelloWorldGadget()
    {
        return PluginGadgetSpecBuilder.class.getResourceAsStream("hello.xml");
    }
}
