package com.atlassian.renderer.v2.macro.code;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.V2SubRenderer;
import com.atlassian.renderer.v2.components.HtmlEscaper;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.macro.basic.AbstractPanelMacro;
import com.opensymphony.util.TextUtils;

import java.util.*;

public class CodeMacro extends AbstractPanelMacro
{
    private static final String DEFAULT_LANG = "java";
    private SourceCodeFormatterRepository codeFormatterRepository;

    public CodeMacro(V2SubRenderer subRenderer, List formatters)
    {
        setCodeFormatterRepository(new SimpleSourceCodeFormatterRepository(formatters));
        setSubRenderer(subRenderer);
    }

    public void setCodeFormatterRepository(SourceCodeFormatterRepository codeFormatterRepository)
    {
        this.codeFormatterRepository = codeFormatterRepository;
    }

    public CodeMacro()
    {

    }

    public boolean suppressMacroRenderingDuringWysiwyg()
    {
        return false;
    }

    protected String getPanelCSSClass()
    {
        return "code panel";
    }

    protected String getPanelHeaderCSSClass()
    {
        return "codeHeader panelHeader";
    }

    protected String getPanelContentCSSClass()
    {
        return "codeContent panelContent";
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.allow(RenderMode.F_HTMLESCAPE);
    }

    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        String language = getLanguage(parameters).toLowerCase();
        language = HtmlEscaper.escapeAll(language, true);

        SourceCodeFormatter formatter = getFormatter(language);

        String preamble = "";
        if (formatter == null)
        {
            preamble = RenderUtils.blockError("Unable to find source-code formatter for language: " + language + ".", "Available languages are: " + TextUtils.join(", ", codeFormatterRepository.getAvailableLanguages()));
            formatter = getFormatter(DEFAULT_LANG);
        }

        return super.execute(parameters, preamble + "<pre class=\"code-" + language + "\">" + formatter.format(body.trim(), language) + "</pre>", renderContext);
    }

    private SourceCodeFormatter getFormatter(String language)
    {
        return codeFormatterRepository.getSourceCodeFormatter(language);
    }

    private String getLanguage(Map parameters)
    {
        String lang = (String) parameters.get("lang");

        if (!TextUtils.stringSet(lang))
        {
            lang = (String) parameters.get("0");
        }

        if (!TextUtils.stringSet(lang))
        {
            lang = DEFAULT_LANG;
        }

        return lang;
    }
}

