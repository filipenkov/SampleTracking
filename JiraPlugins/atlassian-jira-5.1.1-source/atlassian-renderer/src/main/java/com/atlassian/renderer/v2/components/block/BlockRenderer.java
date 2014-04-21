package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.RenderContext;

public interface BlockRenderer
{
    /**
     * Render the next block in a document.
     *
     * <p>Returning anything from this method will indicate this renderer could handle the given
     * line, and no further processing should be performed on it. Return null to let the other
     * block renderers have a go on the same line.
     *
     * @param thisLine the line that will form the start of the block
     * @param nextLines a LineWalker positioned at the line in the document after thisLine, in case
     *        the renderer needs to consume multiple lines. If the walker is used, implementors should
     *        ensure that at the end of the renderNextBlock call, it is positioned at the start of the
     *        first line that does not form part of the block.
     * @param context The current rendercontext
     * @param subRenderer A subrenderer that can be used to render the contents of the block
     * @return the block rendered as HTML, or null if this renderer can not handle the line
     */
    String renderNextBlock(String thisLine, LineWalker nextLines, RenderContext context, SubRenderer subRenderer);
}
