/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 28, 2004
 * Time: 1:46:23 PM
 */
package com.atlassian.renderer.v2;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.components.RendererComponent;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

// Test that the renderer itself works - this is just a unit test of how the renderer
// interacts with its components, not a test of end-to-end rendering
public class TestV2Renderer extends TestCase
{
    private V2Renderer wikiRenderer;
    private Mock mockComponent1;
    private Mock mockComponent2;
    private RenderContext context;

    protected void setUp() throws Exception
    {
        mockComponent1 = new Mock(RendererComponent.class);
        mockComponent2 = new Mock(RendererComponent.class);

        wikiRenderer = new V2Renderer();
        List components = new ArrayList();
        components.add(mockComponent1.proxy());
        components.add(mockComponent2.proxy());
        wikiRenderer.setComponents(components);

        context = new RenderContext();
        context.setImagePath("http://localhost:8080/images");
        context.setSiteRoot("http://localhost:8080");
        context.setLinkRenderer(new V2LinkRenderer());
    }

    public void testBlankRender()
    {
        assertRendered(null, "");
        assertRendered("", "");
    }

    public void testBasicRender()
    {
        addSuccessfulRender(mockComponent1, "bob and mary", "first render");
        addSuccessfulRender(mockComponent2, "first render", "second render");
        assertRendered("bob and mary", "second render");
        verifyMocks();
    }

    public void testShouldntRender()
    {
        addShouldntRender(mockComponent1);
        addShouldntRender(mockComponent2);
        assertRendered("bob and mary", "bob and mary");
        verifyMocks();

        addShouldntRender(mockComponent1);
        addSuccessfulRender(mockComponent2, "bob and mary", "rendered");
        assertRendered("bob and mary", "rendered");
        verifyMocks();

        addShouldntRender(mockComponent2);
        addSuccessfulRender(mockComponent1, "bob and mary", "rendered");
        assertRendered("bob and mary", "rendered");
        verifyMocks();
    }

    public void testNoRenderMode()
    {
        context.pushRenderMode(RenderMode.NO_RENDER);
        assertRendered("bob and jane", "bob and jane");
    }

    private void addSuccessfulRender(Mock mockComponent, String input, String output)
    {
        mockComponent.expectAndReturn("shouldRender", context.getRenderMode(), true);
        mockComponent.expectAndReturn("render", C.args(C.eq(input), C.eq(context)), output);
    }

    private void addShouldntRender(Mock mockComponent)
    {
        mockComponent.expectAndReturn("shouldRender", context.getRenderMode(), false);
    }

    private void verifyMocks()
    {
        mockComponent1.verify();
        mockComponent2.verify();
    }

    private void assertRendered(String raw, String result)
    {
        assertEquals(result, wikiRenderer.render(raw, context));
    }
}