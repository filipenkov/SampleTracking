package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.SubRenderer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HorizontalRuleBlockRenderer implements BlockRenderer
{
    private static final Pattern RULE_PATTERN = Pattern.compile("\\s*-{4,5}\\s*");

    public String renderNextBlock(String thisLine, LineWalker nextLines, RenderContext context, SubRenderer subRenderer)
    {
        Matcher matcher = RULE_PATTERN.matcher(thisLine);
        if (matcher.matches())
        {
            return "<hr />";
        }

        return null;
    }
}
