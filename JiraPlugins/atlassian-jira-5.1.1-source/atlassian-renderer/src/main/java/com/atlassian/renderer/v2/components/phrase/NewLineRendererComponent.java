package com.atlassian.renderer.v2.components.phrase;

import com.atlassian.renderer.v2.components.AbstractRendererComponent;
import com.atlassian.renderer.v2.components.block.LineWalker;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderContext;

import java.util.regex.Pattern;

public class NewLineRendererComponent extends AbstractRendererComponent
{
    private static final Pattern LINEBREAK = Pattern.compile("\r?\n");

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderLinebreaks();
    }

    public String render(String wiki, RenderContext context)
    {
        if (wiki.indexOf("\n") < 0)
            return wiki;

        StringBuffer out = new StringBuffer(wiki.length());
        LineWalker walker = new LineWalker(wiki);
        while (walker.hasNext())
        {
            String line = walker.next();

            out.append(line);
            if (walker.hasNext())
            {
                String nextLine = walker.peek();
                if (nextLine.trim().startsWith("<br") || line.trim().endsWith("<br clear=\"all\" />"))
                    out.append("\n");
                else
                    out.append("<br/>\n");
            }
        }

        return out.toString();
    }
}
