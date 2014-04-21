package com.atlassian.templaterenderer.velocity;

import com.atlassian.plugin.web.renderer.WebPanelRenderer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;

/**
 * @since 1.3.4
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingWebPanelRendererTrackerTest
{
    private CachingWebPanelRendererTracker underTest;

    @Mock
    private AbstractCachingWebPanelRenderer renderer0;
    @Mock
    private AbstractCachingWebPanelRenderer renderer1;
    @Mock
    private WebPanelRenderer nonCachingRenderer;

    @Before
    public void setUp()
    {
        underTest = new CachingWebPanelRendererTracker();
    }

    private Object processInitialization(Object o)
    {
        return underTest.postProcessAfterInitialization(
                underTest.postProcessBeforeInitialization(o, "blah"), "blah");
    }

    @Test
    public void testDoesNotTrackNonAbstractCachingWebPanelRenderer()
    {
        Object o = new Object();
        assertSame(o, processInitialization(o));
        assertEquals(0, underTest.numberOfTracked());

        assertSame(nonCachingRenderer, processInitialization(nonCachingRenderer));
        assertEquals(0, underTest.numberOfTracked());
    }

    @Test
    public void testDoesTrackAbstractCachingWebPanelRenderer()
    {
        assertSame(renderer0, processInitialization(renderer0));
        assertEquals(1, underTest.numberOfTracked());

        assertSame(renderer1, processInitialization(renderer1));
        assertEquals(2, underTest.numberOfTracked());
    }

    @Test
    public void testOnDestroy() throws Exception
    {
        processInitialization(renderer0);
        processInitialization(renderer1);

        underTest.destroy();

        verify(renderer0).destroy();
        verify(renderer1).destroy();
    }
}
