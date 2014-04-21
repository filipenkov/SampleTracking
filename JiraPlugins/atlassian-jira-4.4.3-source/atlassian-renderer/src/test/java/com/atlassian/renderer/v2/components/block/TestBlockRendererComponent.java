package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.v2.components.AbstractRendererComponentTest;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.C;

import java.util.ArrayList;
import java.util.List;

public class TestBlockRendererComponent extends AbstractRendererComponentTest
{
    protected Mock blockOne;
    protected Mock blockTwo;
    protected Mock blockThree;

    protected void setUp() throws Exception
    {
        super.setUp();
        component = makeComponent();
    }

    private BlockRendererComponent makeComponent()
    {
        blockOne = new Mock(BlockRenderer.class);
        blockTwo = new Mock(BlockRenderer.class);
        blockThree = new Mock(BlockRenderer.class);

        List blockComponents = new ArrayList();
        blockComponents.add(blockOne.proxy());
        blockComponents.add(blockTwo.proxy());
        blockComponents.add(blockThree.proxy());

        BlockRendererComponent blockRenderer = new BlockRendererComponent((SubRenderer) mockSubRenderer.proxy(), blockComponents);
        return blockRenderer;
    }

    protected RenderMode getTestCaseRenderMode() {
        return RenderMode.allow(RenderMode.F_PARAGRAPHS | RenderMode.F_FIRST_PARA);
    }

    protected long getRequiredRenderModeFlags()
    {
        return RenderMode.F_PARAGRAPHS;
    }

    public void testSingleLine()
    {
        String text = "Line one";

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockTwo.expectAndReturn("renderNextBlock", C.ANY_ARGS, "Fish");

        testBasicRender(text, "Fish");

        blockOne.verify();
        blockTwo.verify();
        blockThree.verify();
    }

    public void testMultiLine()
    {
        String text = "Line one\nLine two\nLine three";

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockTwo.expectAndReturn("renderNextBlock", C.ANY_ARGS, "Fish");

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockTwo.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockThree.expectAndReturn("renderNextBlock", C.ANY_ARGS, "Goat");

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, "Sheep");

        testBasicRender(text, "Fish\nGoat\nSheep");

        blockOne.verify();
        blockTwo.verify();
        blockThree.verify();
    }

    public void testDefault()
    {
        String text = "Line one\nLine two\nLine three\nLine four";

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockTwo.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockThree.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockTwo.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockThree.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, "Fish");

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockTwo.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockThree.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);

        mockSubRenderer.expectAndReturn("render", C.args(C.eq("Line one\nLine two"), C.eq(renderContext), C.eq(getSubRenderMode())), "para1");
        mockSubRenderer.expectAndReturn("render", C.args(C.eq("Line four"), C.eq(renderContext), C.eq(getSubRenderMode())), "para2");

        testBasicRender(text, "<p>para1</p>\nFish\n<p>para2</p>");

        blockOne.verify();
        blockTwo.verify();
        blockThree.verify();
        mockSubRenderer.verify();
    }

    private RenderMode getSubRenderMode() {
        return renderContext.getRenderMode().and(RenderMode.INLINE);
    }

    public void testNoFirstParagraph()
    {
        renderContext.pushRenderMode(RenderMode.allow(RenderMode.F_PARAGRAPHS));

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockTwo.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockThree.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, "Fish");

        blockOne.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockTwo.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockThree.expectAndReturn("renderNextBlock", C.ANY_ARGS, null);


        mockSubRenderer.expectAndReturn("render", C.args(C.eq("Para one"), C.eq(renderContext), C.eq(getSubRenderMode())), "para1");
        mockSubRenderer.expectAndReturn("render", C.args(C.eq("Para three"), C.eq(renderContext), C.eq(getSubRenderMode())), "para3");


        testBasicRender("Para one\nPara two\nPara three", "para1\nFish\n<p>para3</p>");

        mockSubRenderer.verify();
    }

    public void testNoParagraphIfOnlyReplacedText()
    {
        String token1 = renderContext.addRenderedContent("One");
        String token2 = renderContext.addRenderedContent("Two");
        String token3 = renderContext.addRenderedContent("Three");

        setBlocksMatchNothing();

        testBasicRender(token1 + token2 + token3, "OneTwoThree");
        testBasicRender(token1 + " " + token2 + " " + token3, "One Two Three");
        testBasicRender(token1 + "\n" + token2 + "\n" + token3, "One\nTwo\nThree");
    }

    private void setBlocksMatchNothing() {
        blockOne.matchAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockTwo.matchAndReturn("renderNextBlock", C.ANY_ARGS, null);
        blockThree.matchAndReturn("renderNextBlock", C.ANY_ARGS, null);
    }
}
