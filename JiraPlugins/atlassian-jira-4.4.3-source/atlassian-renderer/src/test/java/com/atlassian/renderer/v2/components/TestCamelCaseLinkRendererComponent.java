/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 14, 2004
 * Time: 1:42:22 PM
 */
package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RendererConfiguration;
import com.atlassian.renderer.links.LinkRenderer;
import com.atlassian.renderer.links.LinkResolver;
import com.atlassian.renderer.v2.MockLink;
import com.atlassian.renderer.v2.RenderMode;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;

public class TestCamelCaseLinkRendererComponent extends AbstractRendererComponentTest
{
    private Mock mockLinkResolver;
    private Mock mockLinkRenderer;
    private Mock mockRendererConfiguration;

    protected void setUp() throws Exception
    {
        super.setUp();
        mockLinkResolver = new Mock(LinkResolver.class);
        mockLinkRenderer = new Mock(LinkRenderer.class);

        mockRendererConfiguration = new Mock(RendererConfiguration.class);

        renderContext = new RenderContext();
        renderContext.setLinkRenderer((LinkRenderer) mockLinkRenderer.proxy());
        component = new CamelCaseLinkRendererComponent((LinkResolver) mockLinkResolver.proxy(), (RendererConfiguration)mockRendererConfiguration.proxy());
        mockRendererConfiguration.matchAndReturn("isAllowCamelCase", true);
    }

    protected long getRequiredRenderModeFlags()
    {
        return RenderMode.F_LINKS;
    }

    public void testCamelCaseOff()
    {
        mockRendererConfiguration.expectAndReturn("isAllowCamelCase", true);
        assertTrue(component.shouldRender(getTestCaseRenderMode()));
        mockRendererConfiguration.expectAndReturn("isAllowCamelCase", false);
        assertFalse(component.shouldRender(getTestCaseRenderMode()));
    }

    public void testSimpleLinks()
    {
        assertLinkWorked("LuLu", "1", "LuLu", "1");
        assertLinkWorked("CamelCase", "1", "CamelCase", "1");
        assertLinkWorked("CamelCaseWithMoreWords", "1", "CamelCaseWithMoreWords", "1");
        assertLinkWorked("CamelCAseWithConsecutiveCaps", "1", "CamelCAseWithConsecutiveCaps", "1");
        assertLinkWorked("CamelCa1seContainingDigit", "1", "CamelCa1seContainingDigit", "1");

        assertLinkWorked("The QuickBrown Fox", "The 1 Fox", "QuickBrown", "1");
    }

    public void testNotCamelCase()
    {
        assertLinkWorked("nothing", "nothing", null, null);
        assertLinkWorked("startsWithNonCap", "startsWithNonCap", null, null);
        assertLinkWorked("Multiple Capitalised Words", "Multiple Capitalised Words", null, null);
        assertLinkWorked("ALLCAPS", "ALLCAPS", null, null);
    }

    // CONF-3923
    public void testEmbeddedResourceWithCamelCase()
    {
        assertLinkWorked("!MicroHofft.jpg!", "!MicroHofft.jpg!", null, null);
        assertLinkWorked("!MicroHofft-AUS.jpg!", "!MicroHofft-AUS.jpg!", null, null);
    }

    // CONF-3447
    public void testAttachmentLinkWithCamelCase()
    {
        assertLinkWorked("[^FooBar.jpg]", "[^FooBar.jpg]", null, null);
        assertLinkWorked("[^FooBar-AUS.jpg]", "[^FooBar-AUS.jpg]", null, null);
    }

    private void assertLinkWorked(String wiki, String result, String originalLinkText, String resultLinkText)
    {
        assertLinkWorked(wiki, result, originalLinkText, resultLinkText, null, null);
    }

    private void assertLinkWorked(String wiki, String result, String originalLinkText, String resultLinkText, String originalLinkText2, String resultLinkText2)
    {
        if (originalLinkText != null)
        {
            final MockLink mockLink = new MockLink(originalLinkText);
            mockLinkResolver.expectAndReturn("createLink", C.args(C.eq(renderContext), C.eq(originalLinkText)), mockLink);
            if (resultLinkText != null)
                mockLinkRenderer.expectAndReturn("renderLink", C.args(C.eq(mockLink), C.eq(renderContext)), resultLinkText);
        }

        if (originalLinkText2 != null)
        {
            final MockLink mockLink = new MockLink(originalLinkText2);
            mockLinkResolver.expectAndReturn("createLink", C.args(C.eq(renderContext), C.eq(originalLinkText2)), mockLink);
            if (resultLinkText2 != null)
                mockLinkRenderer.expectAndReturn("renderLink", C.args(C.eq(mockLink), C.eq(renderContext)), resultLinkText2);
        }

        assertEquals(result, render(wiki));
    }
}