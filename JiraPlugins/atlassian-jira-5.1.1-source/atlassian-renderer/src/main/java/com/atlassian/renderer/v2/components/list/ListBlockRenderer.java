package com.atlassian.renderer.v2.components.list;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.components.block.BlockRenderer;
import com.atlassian.renderer.v2.components.block.LineWalker;
import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class ListBlockRenderer implements BlockRenderer
{
    private static final Pattern LIST_PATTERN = Pattern.compile("\\s*([\\*\\-#]+)\\s+(.*?)\\s*");
    private static final Pattern POSSIBLE_DASH_PATTERN = Pattern.compile("\\s*-{2,3}.*");
    public static final ListType BULLET_LIST = new ListType("*", "<ul>", "</ul>");
    public static final ListType DASHED_LIST = new ListType("-", "<ul class=\"alternate\" type=\"square\">", "</ul>");
    public static final ListType NUMBERED_LIST = new ListType("#", "<ol>", "</ol>");

    // TODO: Make this more dynamic
    public static final Map LIST_TYPES = new HashMap();

    static
    {
        LIST_TYPES.put("*", BULLET_LIST);
        LIST_TYPES.put("-", DASHED_LIST);
        LIST_TYPES.put("#", NUMBERED_LIST);
    }

    public String renderNextBlock(String thisLine, LineWalker nextLines, RenderContext context, SubRenderer subRenderer)
    {
        if (!context.getRenderMode().renderLists())
        {
            return null;
        }

        String wiki = null;
        Matcher matcher = LIST_PATTERN.matcher(thisLine);
        int length = 0;
        if (matcher.matches() && !POSSIBLE_DASH_PATTERN.matcher(thisLine).matches())
        {
            ListRenderable root = new ListRenderable();

            ArrayList lastText = new ArrayList();
            String lastBullets = matcher.group(1);
            lastText.add(matcher.group(2));
            length += matcher.group(2).length();

            while (nextLines.hasNext())
            {
                String nextLine = nextLines.next();
                matcher = LIST_PATTERN.matcher(nextLine);
                if (matcher.matches())
                {
                    root.addListItem(lastBullets, new ListItem(TextUtils.join("\n", lastText.iterator())));
                    lastText.clear();

                    lastBullets = matcher.group(1);
                    lastText.add(matcher.group(2));
                    length += matcher.group(2).length();
                }
                else if (RenderUtils.isBlank(nextLine))
                {
                    nextLines.pushBack(nextLine);
                    break;
                }
                else
                {
                    lastText.add(nextLine);
                }
            }

            root.addListItem(lastBullets, new ListItem(TextUtils.join("\n", lastText.iterator())));

            StringBuffer buffer = new StringBuffer((int) (length * 1.25));
            root.toHtml(buffer, 0, subRenderer, context);
            wiki = buffer.toString();
        }

        return wiki;
    }

}
