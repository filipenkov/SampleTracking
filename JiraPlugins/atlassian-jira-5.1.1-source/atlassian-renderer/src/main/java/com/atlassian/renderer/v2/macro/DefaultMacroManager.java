package com.atlassian.renderer.v2.macro;

import com.atlassian.renderer.v2.macro.basic.*;
import com.atlassian.renderer.v2.macro.code.*;
import com.atlassian.renderer.v2.macro.code.formatter.*;
import com.atlassian.renderer.v2.V2SubRenderer;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple hard-coded implementation of a MacroManager that will server up the basic
 * and html macros included in the renderer component.
 */
public class DefaultMacroManager implements MacroManager
{
    private HashMap macros;

    public DefaultMacroManager(V2SubRenderer subRenderer)
    {
        macros = new HashMap();
        macros.put("anchor", new BasicAnchorMacro());
        macros.put("code", new CodeMacro(subRenderer, getCodeFormatters()));
        macros.put("quote", new QuoteMacro());
        macros.put("noformat", new NoformatMacro(subRenderer));
        macros.put("panel", new PanelMacro(subRenderer));
        macros.put("color", new ColorMacro());
        macros.put("loremipsum", new LoremIpsumMacro());
        macros.put("html", new InlineHtmlMacro());
    }

    public void registerMacro(String name, Macro macro)
    {
        macros.put(name, macro);
    }

    private List getCodeFormatters()
    {
        ArrayList codeFormatters = new ArrayList();
        codeFormatters.add(new SqlFormatter());
        codeFormatters.add(new JavaFormatter());
        codeFormatters.add(new JavaScriptFormatter());
        codeFormatters.add(new ActionScriptFormatter());
        codeFormatters.add(new XmlFormatter());
        codeFormatters.add(new NoneFormatter());
        return codeFormatters;
    }

    public Macro getEnabledMacro(String name)
    {
        return (Macro)macros.get(name);
    }

    public void unregisterMacro(String name)
    {
        macros.remove(name);
    }
}
