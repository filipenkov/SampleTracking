package com.atlassian.renderer.v2.components.phrase;

import com.atlassian.renderer.IconManager;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.util.RegExpUtil;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.components.AbstractRegexRendererComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmoticonRendererComponent extends AbstractRegexRendererComponent
{
    private IconManager iconManager;
    private Pattern[] emoticonPatterns;
    private String[] emoticonSymbols;

    public EmoticonRendererComponent(IconManager iconManager)
    {
        this.iconManager = iconManager;
    }

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderPhrases();
    }

    public String render(String wiki, RenderContext context)
    {
        Pattern[] patterns = getEmoticonPatterns();
        for (int i = 0; i < patterns.length; i++)
        {
            if (wiki.indexOf(emoticonSymbols[i]) != -1)
            {
                Pattern pattern = patterns[i];
                wiki = regexRender(wiki, context, pattern);
            }
        }

        return wiki;
    }

    private Pattern[] getEmoticonPatterns()
    {
        if (emoticonPatterns == null)
        {
            String[] emoticons = iconManager.getEmoticonSymbols();
            emoticonPatterns = new Pattern[emoticons.length];
            emoticonSymbols = new String[emoticons.length];

            for (int i = 0; i < emoticons.length; i++)
            {
                String symbol = emoticons[i];
                String patternString = "(" + RegExpUtil.convertToRegularExpression(symbol) + ")($|(?![a-zA-Z]))";

                if (symbol.startsWith(";"))
                    patternString = "(^|(?<!\\&#?[a-zA-Z0-9]{1,10}))" + patternString;

                emoticonPatterns[i] = Pattern.compile(patternString);
                emoticonSymbols[i] = symbol;
            }
        }

        return emoticonPatterns;
    }

    public void appendSubstitution(StringBuffer buffer, RenderContext context, Matcher matcher)
    {
        String symbol = matcher.group(1);

        if (matcher.groupCount() == 3)
            symbol = matcher.group(2);

        buffer.append(iconManager.getEmoticon(symbol).toHtml(context.getImagePath()));
    }
}
