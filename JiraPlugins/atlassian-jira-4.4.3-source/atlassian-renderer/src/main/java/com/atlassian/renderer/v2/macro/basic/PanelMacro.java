package com.atlassian.renderer.v2.macro.basic;

import com.atlassian.renderer.v2.V2SubRenderer;

public class PanelMacro extends AbstractPanelMacro
{
    public PanelMacro() {}
    
    public PanelMacro(V2SubRenderer subRenderer)
    {
        setSubRenderer(subRenderer);
    }

    protected String getPanelCSSClass()
    {
        return "panel";
    }

    protected String getPanelContentCSSClass()
    {
        return "panelContent";
    }

    public boolean suppressMacroRenderingDuringWysiwyg()
    {
        return false;
    }

    protected String getPanelHeaderCSSClass()
    {
        return "panelHeader";
    }
}
