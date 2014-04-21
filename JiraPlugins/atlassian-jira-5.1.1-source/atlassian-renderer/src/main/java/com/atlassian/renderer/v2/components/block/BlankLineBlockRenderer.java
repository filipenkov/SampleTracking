package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.RenderContext;

import java.util.regex.Pattern;

/**
 * Block-level blank line handling. At the block level, newlines are separators between
 * blocks, so we don't render them as HTML. We do, however, return a blank line so that
 * our HTML output looks prettier.
 */
public class BlankLineBlockRenderer implements BlockRenderer
{
    private static final Pattern NON_WHITESPACE = Pattern.compile("\\S+");

    public String renderNextBlock(String thisLine, LineWalker nextLines, RenderContext context, SubRenderer subRenderer)
    {
        if (RenderUtils.isBlank(thisLine))
            return "";
        else
            return null;
    }
}
