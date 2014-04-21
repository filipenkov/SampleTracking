package com.atlassian.renderer.macro.macros;

import com.atlassian.renderer.macro.BaseMacro;
import com.opensymphony.util.TextUtils;
import org.radeox.macro.parameter.MacroParameter;

import java.io.IOException;
import java.io.Writer;

/**
 * A simple macro to colour HTML
 */
public class ColorMacro extends BaseMacro
{
    private String[] myParamDescription = new String[]{"1: name"};

    public String getName()
    {
        return "color";
    }

    public String[] getParamDescription()
    {
        return myParamDescription;
    }

    public void execute(Writer writer, MacroParameter macroParameter) throws IllegalArgumentException, IOException
    {
        String color = TextUtils.noNull(macroParameter.get(0)).trim();

        writer.write("<font color=\"" + color + "\">" + macroParameter.getContent() + "</font>");
    }
}
