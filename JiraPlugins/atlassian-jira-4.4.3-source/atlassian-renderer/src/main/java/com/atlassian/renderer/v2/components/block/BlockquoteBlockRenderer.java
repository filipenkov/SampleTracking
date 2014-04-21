package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockquoteBlockRenderer implements BlockRenderer
{
    private static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile("\\s*bq\\.\\s*(.*)");

    public String renderNextBlock(String thisLine, LineWalker nextLines, RenderContext context, SubRenderer subRenderer)
    {
        Matcher matcher = BLOCKQUOTE_PATTERN.matcher(thisLine);
        if (matcher.matches())
        {
            String body = matcher.group(1);
            String renderedBody = subRenderer.render(body, context, context.getRenderMode().and(RenderMode.INLINE));
            StringBuffer sb = new StringBuffer();
            if (context.isRenderingForWysiwyg())
            {
                sb.append("<blockquote markup='bq'><p>");
            }
            else
            {
                sb.append("<blockquote><p>");
            }
            return sb.append(renderedBody).append("</p></blockquote>").toString();
        }

        return null;
    }
}
