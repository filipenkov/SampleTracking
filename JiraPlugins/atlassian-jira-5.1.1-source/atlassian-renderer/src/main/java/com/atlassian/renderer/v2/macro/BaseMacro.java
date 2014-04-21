package com.atlassian.renderer.v2.macro;

public abstract class BaseMacro implements Macro
{
    public boolean suppressSurroundingTagDuringWysiwygRendering()
    {
        return false;
    }

    public boolean suppressMacroRenderingDuringWysiwyg()
    {
        return true;
    }
}
