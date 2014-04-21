package com.atlassian.gadgets.publisher.internal;

import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.plugins.PluginGadgetSpecEventListener;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.concurrent.ExecutorService;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginGadgetFinderTest
{
    private static final String LOCATION = "monkey.xml";

    @Mock PluginGadgetSpecEventListener eventListener;
    @Mock PluginAccessor pluginAccessor;
    @Mock PluginEventManager pluginEventManager;
    @Mock ThreadLocalDelegateExecutorFactory executorFactory;

    PluginGadgetFinder finder;
    PluginGadgetSpec pluginGadgetSpec;
    GadgetModuleDescriptor moduleDescriptor;

    @Before
    public void setUp()
    {
        when(executorFactory.createExecutorService(any(ExecutorService.class))).thenAnswer(new Answer<ExecutorService>() {
            public ExecutorService answer(final InvocationOnMock invocation) throws Throwable
            {
                return (ExecutorService) invocation.getArguments()[0];
            }
        });

        finder = new PluginGadgetFinder(pluginAccessor, pluginEventManager, executorFactory, singleton(eventListener));

        Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn("plugin.key");

        Element descriptorElement = mock(Element.class);
        when(descriptorElement.attributeValue("key")).thenReturn("monkey");
        when(descriptorElement.attributeValue("location")).thenReturn(LOCATION);

        moduleDescriptor = new GadgetModuleDescriptor();
        moduleDescriptor.init(plugin, descriptorElement);

        pluginGadgetSpec = moduleDescriptor.getModule();
    }

    @Test
    public void assertThatPluginsAreScannedAndGadgetsFoundAtStartup() throws Exception
    {
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(GadgetModuleDescriptor.class))
            .thenReturn(singletonList(moduleDescriptor));

        PluginGadgetFinder newFinder =
            new PluginGadgetFinder(pluginAccessor, pluginEventManager, executorFactory, singleton(eventListener));
        finder.destroy();
        verify(eventListener, timeout(1000)).pluginGadgetSpecEnabled(pluginGadgetSpec);
    }

    @After
    public void tearDown() throws Exception
    {
       finder.destroy();
    }
}
