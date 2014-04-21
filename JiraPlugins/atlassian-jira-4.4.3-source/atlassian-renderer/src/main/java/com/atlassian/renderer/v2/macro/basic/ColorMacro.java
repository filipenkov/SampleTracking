package com.atlassian.renderer.v2.macro.basic;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.macro.basic.validator.*;
import com.opensymphony.util.TextUtils;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * A simple macro to colour HTML
 */
public class ColorMacro extends BaseMacro
{
    public boolean isInline()
    {
        return true;
    }

    public boolean hasBody()
    {
        return true;
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.INLINE.or(RenderMode.allow(RenderMode.F_MACROS));
    }

    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        String color = StringUtils.trimToEmpty((String) parameters.get("0"));
        new ColorStyleValidator().assertValid(color);
        return "<font color=\"" + color + "\">" + body + "</font>";
    }

    public boolean suppressSurroundingTagDuringWysiwygRendering()
    {
        return true;
    }

    public boolean suppressMacroRenderingDuringWysiwyg()
    {
        return false;
    }
}
