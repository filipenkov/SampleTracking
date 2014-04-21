package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

public abstract class AbstractRendererComponentTest extends TestCase
{
    protected RendererComponent component;
    protected Mock mockSubRenderer;
    protected RenderContext renderContext;

    private TokenRendererComponent detokenizer;

    protected void setUp() throws Exception
    {
        super.setUp();

        detokenizer = new TokenRendererComponent(null);
        mockSubRenderer = new Mock(SubRenderer.class);
        renderContext = new RenderContext();
        renderContext.pushRenderMode(getTestCaseRenderMode());
    }

    protected String render(String wiki)
    {
        String tokenized = component.render(wiki, renderContext);
        return detokenizer.render(tokenized, renderContext);
    }

    protected RenderMode getTestCaseRenderMode()
    {
        return RenderMode.allow(getRequiredRenderModeFlags());
    }

    protected abstract long getRequiredRenderModeFlags();

    public void testShouldRender()
    {
        assertTrue(component.shouldRender(RenderMode.allow(getRequiredRenderModeFlags())));
        assertFalse(component.shouldRender(RenderMode.suppress(getRequiredRenderModeFlags())));
    }

    protected void testBasicRender(String wikiText, String expected)
    {
        assertEquals(expected, detokenizer.render(component.render(wikiText, renderContext), renderContext));
    }
}
