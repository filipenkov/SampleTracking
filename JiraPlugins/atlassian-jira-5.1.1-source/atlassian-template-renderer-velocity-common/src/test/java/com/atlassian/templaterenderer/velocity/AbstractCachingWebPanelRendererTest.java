package com.atlassian.templaterenderer.velocity;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v1.3.3
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractCachingWebPanelRendererTest
{
    @Mock
    private PluginEventManager manager;

    @Mock
    private TemplateRenderer renderer;

    @Mock
    private Plugin pluginOne;

    @Mock
    private Plugin pluginTwo;

    @Before
    public void setup()
    {
        when(pluginOne.getKey()).thenReturn("plugin-one");
        when(pluginTwo.getKey()).thenReturn("plugin-two");
    }

    @Test
    public void testRenderersAreCached() throws IOException
    {
        final TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);
        final AtomicInteger createCalled = new AtomicInteger(0);

        AbstractCachingWebPanelRenderer renderer = new AbstractCachingWebPanelRenderer(manager)
        {
            @Override
            protected TemplateRenderer createRenderer(final Plugin plugin)
            {
                createCalled.incrementAndGet();
                return mockTemplateRenderer;
            }
        };

        assertEquals("velocity", renderer.getResourceType());

        renderer.render("testResource", pluginOne, Collections.<String, Object>emptyMap(), null);
        renderer.render("testResource", pluginOne, Collections.<String, Object>emptyMap(), null);
        renderer.renderFragment("testFragment", pluginOne, Collections.<String, Object>emptyMap());
        renderer.renderFragment("testFragment", pluginOne, Collections.<String, Object>emptyMap());

        verify(mockTemplateRenderer, times(2)).render("testResource", Collections.<String, Object>emptyMap(), null);
        verify(mockTemplateRenderer, times(2)).renderFragment("testFragment", Collections.<String, Object>emptyMap());

        assertEquals(1, createCalled.get());

        renderer.render("testResource", pluginTwo, Collections.<String, Object>emptyMap(), null);
        renderer.render("testResource", pluginTwo, Collections.<String, Object>emptyMap(), null);
        renderer.renderFragment("testFragment", pluginTwo, Collections.<String, Object>emptyMap());
        renderer.renderFragment("testFragment", pluginTwo, Collections.<String, Object>emptyMap());

        verify(mockTemplateRenderer, times(4)).render("testResource", Collections.<String, Object>emptyMap(), null);
        verify(mockTemplateRenderer, times(4)).renderFragment("testFragment", Collections.<String, Object>emptyMap());

        assertEquals(2, createCalled.get());
    }

    @Test
    public void testRenderersAreCachedAndReset() throws IOException
    {
        final TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);
        final AtomicInteger createCalled = new AtomicInteger(0);

        AbstractCachingWebPanelRenderer renderer = new AbstractCachingWebPanelRenderer(manager)
        {
            @Override
            protected TemplateRenderer createRenderer(final Plugin plugin)
            {
                createCalled.incrementAndGet();
                return mockTemplateRenderer;
            }
        };

        assertEquals("velocity", renderer.getResourceType());

        renderer.render("testResource", pluginOne, Collections.<String, Object>emptyMap(), null);
        renderer.render("testResource", pluginOne, Collections.<String, Object>emptyMap(), null);
        verify(mockTemplateRenderer, times(2)).render("testResource", Collections.<String, Object>emptyMap(), null);
        assertEquals(1, createCalled.get());

        //This event should not affect pluginOne.
        renderer.pluginUnloaded(new PluginDisabledEvent(pluginTwo));
        renderer.render("testResource", pluginOne, Collections.<String, Object>emptyMap(), null);
        verify(mockTemplateRenderer, times(3)).render("testResource", Collections.<String, Object>emptyMap(), null);
        assertEquals(1, createCalled.get());

        //The plugin's renderer should have been reset.
        renderer.pluginUnloaded(new PluginDisabledEvent(pluginOne));
        renderer.render("testResource", pluginOne, Collections.<String, Object>emptyMap(), null);
        verify(mockTemplateRenderer, times(4)).render("testResource", Collections.<String, Object>emptyMap(), null);
        assertEquals(2, createCalled.get());

        //The plugin's renderer should be cached again.
        renderer.render("testResource", pluginOne, Collections.<String, Object>emptyMap(), null);
        verify(mockTemplateRenderer, times(5)).render("testResource", Collections.<String, Object>emptyMap(), null);
        assertEquals(2, createCalled.get());
    }
}
