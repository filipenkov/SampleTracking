package com.atlassian.renderer.v2;

import junit.framework.TestCase;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.C;
import com.mockobjects.constraint.Constraint;
import com.atlassian.renderer.WikiStyleRenderer;
import com.atlassian.renderer.RenderContext;

public class TestV2SubRenderer extends TestCase
{
    private Mock mockRenderer;
    private RenderContext renderContext;
    private V2SubRenderer subRenderer;

    protected void setUp() throws Exception
    {
        super.setUp();
        mockRenderer = new Mock(Renderer.class);
        renderContext = new RenderContext();
        renderContext.pushRenderMode(RenderMode.INLINE);
        subRenderer = new V2SubRenderer();
        subRenderer.setRenderer((Renderer) mockRenderer.proxy());
    }

    public void testSubRender()
    {
        final RenderMode newRenderMode = RenderMode.NO_ESCAPE;

        mockRenderer.expectAndReturn("render", C.args(C.eq("Fish"), new Constraint() {
            public boolean eval(Object o)
            {
                return (o instanceof RenderContext) && ((RenderContext)o).getRenderMode().equals(newRenderMode);
            }
        }), "Blah" );

        assertEquals("Blah", subRenderer.render("Fish", renderContext, newRenderMode));
        assertEquals("Render mode restored", RenderMode.INLINE, renderContext.getRenderMode());
    }

    public void testSubRenderNullRenderMode()
    {
        mockRenderer.expectAndReturn("render", C.args(C.eq("Fish"), new Constraint() {
            public boolean eval(Object o)
            {
                return (o instanceof RenderContext) && ((RenderContext)o).getRenderMode().equals(RenderMode.INLINE);
            }
        }), "Blah" );

        assertEquals("Blah", subRenderer.render("Fish", renderContext));
        assertEquals("Render mode restored", RenderMode.INLINE, renderContext.getRenderMode());
    }

}
