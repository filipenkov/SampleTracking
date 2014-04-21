package com.atlassian.renderer.v2;

import junit.framework.TestCase;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.ConstraintMatcher;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RendererConfiguration;
import com.atlassian.renderer.links.LinkRenderer;

public class TestV2RendererFacade extends TestCase
{
    private V2RendererFacade rendererFacade;
    private Mock mockRenderer;
    private Mock mockRendererConfiguration;
    private RenderContext context;
    private LinkRenderer linkRenderer;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockRenderer = new Mock(Renderer.class);

        mockRendererConfiguration = new Mock(RendererConfiguration.class);

        rendererFacade = new V2RendererFacade();
        linkRenderer = new V2LinkRenderer();

        rendererFacade.setRendererConfiguration((RendererConfiguration) mockRendererConfiguration.proxy());
        rendererFacade.setDefaultLinkRenderer(linkRenderer);
        rendererFacade.setRenderer((Renderer) mockRenderer.proxy());

        context = new RenderContext();
        context.setImagePath("http://localhost:8080/images");
        context.setSiteRoot("http://localhost:8080");
        context.setLinkRenderer(new V2LinkRenderer());
    }

    public void testPathsNotSet()
    {
        context.setImagePath(null);
        context.setSiteRoot(null);
        mockRenderer.expectAndReturn("render", new ConstraintMatcher() {
            public boolean matches(Object[] objects)
            {
                RenderContext context = (RenderContext) objects[1];
                assertEquals("http://test.example.com:8081", context.getSiteRoot());
                assertEquals("http://test.example.com:8081/images", context.getImagePath());
                return true;
            }

            public Object[] getConstraints()
            {
                return new Object[0];
            }
        }, "first render");
        mockRendererConfiguration.matchAndReturn("getWebAppContextPath", "http://test.example.com:8081");
        mockRendererConfiguration.matchAndReturn("getCharacterEncoding", "UTF-8");
        assertRendered("bob and mary", "first render");
    }

    public void testLinkRendererNotSet()
    {
        context.setLinkRenderer(null);
        mockRenderer.expectAndReturn("render", new ConstraintMatcher() {
            public boolean matches(Object[] objects)
            {
                RenderContext context = (RenderContext) objects[1];
                assertEquals(V2LinkRenderer.class, context.getLinkRenderer().getClass());
                return true;
            }

            public Object[] getConstraints()
            {
                return new Object[0];
            }
        }, "first render");
        mockRendererConfiguration.matchAndReturn("getWebAppContextPath", "http://test.example.com:8081");
        mockRendererConfiguration.matchAndReturn("getCharacterEncoding", "UTF-8");
        assertRendered("bob and mary", "first render");
    }

    private void assertRendered(String raw, String result)
    {
        assertEquals(result, rendererFacade.convertWikiToXHtml(context, raw));
    }
}
