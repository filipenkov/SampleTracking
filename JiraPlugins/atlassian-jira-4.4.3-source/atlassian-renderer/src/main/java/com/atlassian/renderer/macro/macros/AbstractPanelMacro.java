package com.atlassian.renderer.macro.macros;

import com.atlassian.renderer.macro.BaseMacro;
import org.apache.commons.lang.StringUtils;
import org.radeox.macro.parameter.MacroParameter;

import java.io.IOException;
import java.io.Writer;

public abstract class AbstractPanelMacro extends BaseMacro
{
    private String[] myParamDescription = new String[]{"?1: title", "?2: borderStyle", "?3: borderColor", "?4: borderWidth", "?5: bgColor", "?6: titleBGColor"};

    public String[] getParamDescription()
    {
        return myParamDescription;
    }

    protected abstract String getPanelCSSClass();

    protected abstract String getPanelHeaderCSSClass();

    protected abstract String getPanelContentCSSClass();

    /**
     * What should the title of the panel be? Override in subclasses
     *
     * @param macroParameter the macroParameter context for this macro
     * @return the contents of the panel's title.
     */
    protected String getTitle(MacroParameter macroParameter)
    {
        return macroParameter.get("title");
    }

    /**
     * What should the body content of the panel be? Override in subclasses
     *
     * @param macroParameter the macroParameter context for this macro
     * @return the contents of the panel's body.
     */
    protected String getBodyContent(MacroParameter macroParameter)
    {
        return macroParameter.getContent();
    }

    public void execute(Writer writer, MacroParameter macroParameter) throws IllegalArgumentException, IOException
    {
        String title = getTitle(macroParameter);
        String content = getBodyContent(macroParameter);

        String borderStyle = macroParameter.get("borderStyle");
        String borderColor = macroParameter.get("borderColor");
        Integer borderWidth = null;

        String borderWidthString = macroParameter.get("borderWidth");
        if (borderWidthString != null)
        {
            // handle borderWidth if suffixed with "px"
            if (borderWidthString.indexOf("px") != -1)
                borderWidthString = borderWidthString.replaceAll("px", "");
            borderWidth = new Integer(borderWidthString);
        }

        String backgroundColor = macroParameter.get("bgColor");
        String titleBackgroundColor = macroParameter.get("titleBGColor");

        if (StringUtils.isEmpty(titleBackgroundColor) && StringUtils.isNotEmpty(backgroundColor))
            titleBackgroundColor = backgroundColor;

        writer.write("<div class=\"" + getPanelCSSClass() + "\"");
        if (StringUtils.isNotEmpty(borderStyle))
        {
            writer.write(" style=\"border-style: " + borderStyle + "; ");
            if (borderWidth != null && borderWidth.intValue() >= 1)
                writer.write("border-width: " + borderWidth + "px; ");
            if (StringUtils.isNotEmpty(borderColor))
                writer.write("border-color: " + borderColor + "; ");
            writer.write("\"");
        }
        writer.write(">");
        if (StringUtils.isNotEmpty(title))
            writeHeader(writer, title, borderStyle, borderColor, borderWidth, titleBackgroundColor);
        if (StringUtils.isNotEmpty(content))
            writeContent(writer, macroParameter, content, backgroundColor );
        writer.write("</div>");
    }

    protected void writeHeader(Writer writer, String title, String borderStyle, String borderColor, Integer borderWidth, String titleBackgroundColor) throws IOException
    {
        writer.write("<div class=\"" + getPanelHeaderCSSClass() + "\"");

        if (StringUtils.isNotEmpty(borderStyle) || StringUtils.isNotEmpty(titleBackgroundColor))
        {
            writer.write(" style=\"");
            if (StringUtils.isNotEmpty(borderStyle))
            {
                writer.write("border-bottom-style: " + borderStyle + "; ");
                if (borderWidth != null && borderWidth.intValue() >= 1)
                    writer.write("border-bottom-width: " + borderWidth + "; ");
                if (StringUtils.isNotEmpty(borderColor))
                    writer.write("border-bottom-color: " + borderColor + "; ");
            }
            if (StringUtils.isNotEmpty(titleBackgroundColor))
                writer.write("background-color: " + titleBackgroundColor + "; ");
            writer.write("\"");
        }
        writer.write("><b>");
        writer.write(title);
        writer.write("</b></div>");
    }

    protected void writeContent(Writer writer, MacroParameter macroParameter, String content, String backgroundColor) throws IOException
    {
        writer.write("<div class=\"" + getPanelContentCSSClass() + "\"");
        if (StringUtils.isNotEmpty(backgroundColor))
            writer.write(" style=\"background-color: " + backgroundColor + "; \"");
        writer.write(">\n");
        writer.write(content.trim());
        writer.write("\n</div>");
    }
}
