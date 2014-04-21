package com.atlassian.renderer.v2.macro.basic;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.macro.basic.validator.BorderStyleValidator;
import com.atlassian.renderer.v2.macro.basic.validator.SizeParameterValidator;
import com.atlassian.renderer.v2.macro.basic.validator.ColorStyleValidator;
import com.atlassian.renderer.v2.macro.basic.validator.ValidatedMacroParameters;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract class for panel macros. Your implementation will need to provide css classes for panel container, panel
 * header and panel body. These CSS classes will define the default look and feel. Users can override these default
 * styles through styles specified in macro parameters.
 */
public abstract class AbstractPanelMacro extends BaseMacro
{
    private SubRenderer subRenderer;

    protected abstract String getPanelCSSClass();

    protected abstract String getPanelHeaderCSSClass();

    protected abstract String getPanelContentCSSClass();

    public void setSubRenderer(SubRenderer subRenderer)
    {
        this.subRenderer = subRenderer;
    }

    public boolean hasBody()
    {
        return true;
    }

    protected String getBodyContent(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        return body;
    }

    protected String getTitle(Map parameters, String body, RenderContext renderContext)
    {
        return (String) parameters.get("title");
    }

    public boolean isInline()
    {
        return false;
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.ALL;
    }

    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        StringBuffer buffer = new StringBuffer(body.length() + 100);

        String title = subRenderer.render(getTitle(parameters, body, renderContext), renderContext,
            renderContext.getRenderMode().and(RenderMode.INLINE));
        body = getBodyContent(parameters, body, renderContext);

        ValidatedMacroParameters validatedParameters = new ValidatedMacroParameters(parameters);
        validatedParameters.setValidator("borderStyle", new BorderStyleValidator());
        validatedParameters.setValidator("borderColor", new ColorStyleValidator());
        validatedParameters.setValidator("bgColor", new ColorStyleValidator());
        validatedParameters.setValidator("titleBGColor", new ColorStyleValidator());
        validatedParameters.setValidator("borderWidth", new SizeParameterValidator());

        String borderStyle = validatedParameters.getValue("borderStyle");
        String borderColor = validatedParameters.getValue("borderColor");
        String backgroundColor = validatedParameters.getValue("bgColor");
        String titleBackgroundColor = validatedParameters.getValue("titleBGColor");
        String borderWidthString = validatedParameters.getValue("borderWidth");

        int borderWidth = 1;
        if (borderWidthString != null)
        {
            CssSizeValue cssBorderWidth = new CssSizeValue(borderWidthString);
            borderWidth = cssBorderWidth.value();
        }

        Map explicitStyles = prepareExplicitStyles(borderWidth, borderStyle, borderColor, backgroundColor);

        if (StringUtils.isBlank(titleBackgroundColor) && StringUtils.isNotBlank(backgroundColor))
            titleBackgroundColor = backgroundColor;

        buffer.append("<div class=\"").append(getPanelCSSClass()).append("\"");

        if (explicitStyles.size() > 0)
            handleExplicitStyles(buffer, explicitStyles);

        buffer.append(">");

        if (StringUtils.isNotBlank((title)))
            writeHeader(renderContext, buffer, title, borderStyle, borderColor, borderWidth, titleBackgroundColor);
        if (StringUtils.isNotBlank(body))
            writeContent(buffer, parameters, body, backgroundColor);

        buffer.append("</div>");

        return buffer.toString();
    }

    private void handleExplicitStyles(StringBuffer buffer, Map explicitStyles)
    {
        buffer.append(" style=\"");

        for (Iterator iterator = explicitStyles.keySet().iterator(); iterator.hasNext();)
        {
            String styleAttribute = (String) iterator.next();
            String styleValue = (String) explicitStyles.get(styleAttribute);
            buffer.append(styleAttribute).append(": ").append(styleValue).append(";");
        }

        buffer.append("\"");
    }

    private Map prepareExplicitStyles(int borderWidth, String borderStyle, String borderColor, String backgroundColor)
    {
        Map explicitStyles = new TreeMap();

        explicitStyles.put("border-width", borderWidth + "px");
        if (borderWidth > 0)
        {
            if (StringUtils.isNotBlank(borderStyle))
                explicitStyles.put("border-style", borderStyle);
            if (StringUtils.isNotBlank(borderColor))
                explicitStyles.put("border-color", borderColor);
        }
        else
        {
            // hack to make borderless panels look right (the bottom margins of block level elements like <p> and <ul>
            // do NOT pad out the bottom of the panel body unless there is a border
            if (borderWidth == 0)
                explicitStyles.put("border-bottom", "1px solid white");
        }

        if (StringUtils.isNotBlank(backgroundColor))
            explicitStyles.put("background-color", backgroundColor);
        return explicitStyles;
    }

    protected void writeHeader(RenderContext renderContext, StringBuffer buffer, String title, String borderStyle,
        String borderColor, int borderWidth, String titleBackgroundColor)
    {
        buffer.append("<div class=\"").append(getPanelHeaderCSSClass()).append("\"").append(
            renderContext.isRenderingForWysiwyg() ? " wysiwyg=\"ignore\" " : "");

        buffer.append(" style=\"");

        buffer.append("border-bottom-width: ").append(borderWidth).append("px;");
        if (borderWidth > 0)
        {
            if (StringUtils.isNotBlank(borderStyle))
                buffer.append("border-bottom-style: ").append(borderStyle).append(";");
            if (StringUtils.isNotBlank(borderColor))
                buffer.append("border-bottom-color: ").append(borderColor).append(";");
        }
        if (StringUtils.isNotBlank(titleBackgroundColor))
            buffer.append("background-color: ").append(titleBackgroundColor).append(";");

        buffer.append("\"");
        buffer.append("><b>");
        buffer.append(title);
        buffer.append("</b></div>");
    }

    protected void writeContent(StringBuffer buffer, Map parameters, String content, String backgroundColor)
    {
        buffer.append("<div class=\"").append(getPanelContentCSSClass()).append("\"");
        if (StringUtils.isNotBlank(backgroundColor))
            buffer.append(" style=\"background-color: ").append(backgroundColor).append(";\"");
        buffer.append(">\n");
        buffer.append(content.trim());
        buffer.append("\n</div>");
    }

    protected SubRenderer getSubRenderer()
    {
        return subRenderer;
    }
}
