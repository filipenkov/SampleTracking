package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.macro.basic.BasicAnchorMacro;
import org.apache.log4j.Category;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeadingBlockRenderer implements BlockRenderer
{
    private static final Category log = Category.getInstance(HeadingBlockRenderer.class);
    private static final Pattern HEADER_PATTERN = Pattern.compile("\\s*h([1-6])\\.\\s*(.*)");
    private static final String REPLACE = "<h{0}><a name=\"{1}\"></a>{2}</h{0}>";

    public String renderNextBlock(String thisLine, LineWalker nextLines, RenderContext context, SubRenderer subRenderer)
    {
        Matcher matcher = HEADER_PATTERN.matcher(thisLine);
        if (matcher.matches())
        {
            String headingLevel = matcher.group(1);
            String body = matcher.group(2);
            return renderHeading(headingLevel, body, context, subRenderer);
        }
        return null;
    }

    /**
     * Render a heading using the default HTML format
     * @return rendered heading
     */
    protected String renderHeading(String headingLevel, String body, RenderContext context, SubRenderer subRenderer)
    {
        return renderHeading(headingLevel, body, context, subRenderer, REPLACE);
    }

    /**
     * Render a heading using a provided message format.
     * <p/>
     * There are three arguments provided to the format:
     * <ol>
     * <li>Heading level
     * <li>Anchor name/id value
     * <li>Heading content
     * </ol>
     * @param headingLevel heading level
     * @param body heading body
     * @param context context heading is being rendered in
     * @param subRenderer subrenderer for further rendering of content
     * @param renderFormat {@link MessageFormat} string to use for rendering.
     *
     * @return rendered heading
     */
    protected String renderHeading(String headingLevel, String body, RenderContext context, SubRenderer subRenderer, String renderFormat)
    {
        String anchor = getAnchor(context, body);
        String renderedBody = subRenderer.render(body, context, context.getRenderMode().and(RenderMode.INLINE));
        if (renderedBody.equals("") && context.isRenderingForWysiwyg())
        {
            renderedBody = "&#8201;";
        }
        return MessageFormat.format(renderFormat, new String[]{headingLevel, anchor, renderedBody});
    }

    /**
     * Default implementation that gets a simple anchor text.
     */
    protected String getAnchor(RenderContext context, String body)
    {
        return BasicAnchorMacro.getAnchor(context, body);
    }
}
