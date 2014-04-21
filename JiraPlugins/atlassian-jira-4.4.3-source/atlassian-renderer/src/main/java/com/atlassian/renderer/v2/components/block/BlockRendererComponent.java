package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderedContentStore;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.components.RendererComponent;

import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BlockRendererComponent implements RendererComponent
{
    private static final Pattern TOKEN_ONLY_PATTERN = Pattern.compile("(\\s*" + RenderedContentStore.BLOCK_TOKEN.getString() + ")+\\s*",
        Pattern.MULTILINE);
    private static final Pattern SINGLE_LINE_PARA = Pattern.compile("\\s*[\\p{Alnum}&&[^PLhb]][^\n]*");

    private BlockRenderer[] blockRenderers;
    private final SubRenderer subRenderer;

    public BlockRendererComponent(final SubRenderer subRenderer, final List blockRenderers)
    {
        this.subRenderer = subRenderer;
        this.blockRenderers = (BlockRenderer[]) blockRenderers.toArray(new BlockRenderer[blockRenderers.size()]);
    }

    public void setBlockRenderers(final List blockRenderers)
    {
        this.blockRenderers = (BlockRenderer[]) blockRenderers.toArray(new BlockRenderer[blockRenderers.size()]);
    }

    public boolean shouldRender(final RenderMode renderMode)
    {
        return renderMode.renderParagraphs();
    }

    public String render(final String wiki, final RenderContext context)
    {
        // Shortcut the really common case where we're rendering one line of something that definitely isn't
        // block-level markup. (Hopefully this helps performance a tad)
        if (SINGLE_LINE_PARA.matcher(wiki).matches() && !TOKEN_ONLY_PATTERN.matcher(wiki).matches())
        {
            return context.addRenderedContent(renderParagraph(true, context, wiki));
        }

        final LineWalker walker = new LineWalker(wiki);
        final List renderedLines = new ArrayList();
        final List paragraph = new ArrayList();
        boolean firstPara = true;

        while (walker.hasNext())
        {
            final String nextLine = walker.next();
            String rendered = null;

            if (TOKEN_ONLY_PATTERN.matcher(nextLine).matches())
            {
                rendered = nextLine;
            }
            else if (!SINGLE_LINE_PARA.matcher(wiki).matches())
            {
                for (int i = 0; i < blockRenderers.length; i++)
                {
                    final BlockRenderer blockRenderer = blockRenderers[i];
                    rendered = blockRenderer.renderNextBlock(nextLine, walker, context, subRenderer);
                    if (rendered != null)
                    {
                        break;
                    }
                }
            }

            if (rendered == null)
            {
                paragraph.add(nextLine);
            }
            else
            {
                flushParagraph(renderedLines, paragraph, context, firstPara);
                renderedLines.add(rendered);
                firstPara = false;
            }
        }

        flushParagraph(renderedLines, paragraph, context, firstPara);
        return context.addRenderedContent(TextUtils.join("\n", renderedLines));
    }

    private void flushParagraph(final List renderedLines, final List remainderedLines, final RenderContext context, final boolean firstParagraph)
    {
        if (remainderedLines.isEmpty())
        {
            return;
        }

        final String paragraph = TextUtils.join("\n", remainderedLines);
        renderedLines.add(renderParagraph(firstParagraph, context, paragraph));
        remainderedLines.clear();
    }

    private String renderParagraph(final boolean firstParagraph, final RenderContext context, final String paragraph)
    {
        if (firstParagraph && !context.getRenderMode().renderFirstParagraph())
        {
            return subRenderer.render(paragraph, context, context.getRenderMode().and(RenderMode.INLINE));
        }
        else
        {
            return "<p>" + subRenderer.render(paragraph, context, context.getRenderMode().and(RenderMode.INLINE)) + "</p>";
        }
    }
}
