package com.atlassian.renderer.v2.components.phrase;

import com.atlassian.renderer.v2.components.AbstractRendererComponent;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.Replacer;
import com.atlassian.renderer.RenderContext;

import java.util.regex.Pattern;

public class DashRendererComponent extends AbstractRendererComponent
{
    public static final Replacer EN_DASH = new Replacer(Pattern.compile("(^|\\s)--(\\s|$)"), "$1&#8211;$2", new String[]{"--"});
    public static final Replacer EM_DASH = new Replacer(Pattern.compile("(^|\\s)---(\\s|$)"), "$1&#8212;$2", new String[]{"---"});

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderPhrases();
    }

    public String render(String wiki, RenderContext context)
    {        
        wiki = EM_DASH.replaceAll(wiki);
        wiki = EN_DASH.replaceAll(wiki);
        return wiki;
    }
}
