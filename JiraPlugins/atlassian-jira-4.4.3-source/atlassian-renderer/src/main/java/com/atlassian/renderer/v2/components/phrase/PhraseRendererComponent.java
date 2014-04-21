package com.atlassian.renderer.v2.components.phrase;

import com.atlassian.renderer.v2.components.RendererComponent;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.Replacer;
import com.atlassian.renderer.RenderContext;

import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

public class PhraseRendererComponent implements RendererComponent
{
    private static Map heresOneWePreparedEarlier = new HashMap();

    private Replacer replacer;

    static
    {
        heresOneWePreparedEarlier.put("citation", new PhraseRendererComponent("\\?\\?", "cite"));
        heresOneWePreparedEarlier.put("strong", new PhraseRendererComponent("\\*", "b"));
        heresOneWePreparedEarlier.put("superscript", new PhraseRendererComponent("\\^", "sup"));
        heresOneWePreparedEarlier.put("subscript", new PhraseRendererComponent("~", "sub"));
        heresOneWePreparedEarlier.put("emphasis", new PhraseRendererComponent("_", "em"));
        heresOneWePreparedEarlier.put("deleted", new PhraseRendererComponent("-", "del"));
        heresOneWePreparedEarlier.put("inserted", new PhraseRendererComponent("\\+", "ins"));
        heresOneWePreparedEarlier.put("monospaced", new PhraseRendererComponent("\\{\\{", "\\}\\}", "tt"));
    }

    public static PhraseRendererComponent getDefaultRenderer(String name)
    {
        return (PhraseRendererComponent) heresOneWePreparedEarlier.get(name);
    }

    public PhraseRendererComponent(String delimiter, String tagName)
    {
        this(delimiter, delimiter, "<"+ tagName + ">", "</" + tagName + ">");
    }

    public PhraseRendererComponent(String startDelimiter, String endDelimiter, String tagName)
    {
        this(startDelimiter, endDelimiter, "<"+ tagName + ">", "</" + tagName + ">");
    }

    public PhraseRendererComponent(String startDelimiter, String endDelimiter, String startTag, String endTag)
    {
        this.replacer = new Replacer(
                makePattern(startDelimiter, endDelimiter),
                startTag + "$2" + endTag,
                new String[] {startDelimiter.replaceAll("\\\\",""),endDelimiter.replaceAll("\\\\","")}
        );
    }

    public String render(String wiki, RenderContext context)
    {
        String html = replacer.replaceAll(wiki);
        if (context.isRenderingForWysiwyg())
        {
            html = html.replaceAll("<ins>", "<u>").replaceAll("</ins>","</u>");
        }
        return html;
    }

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderPhrases();
    }

    public static final String VALID_START = "(?<![\\p{L}\\p{Nd}\\\\])";
    public static final String VALID_END = "(?![\\p{L}\\p{Nd}])";

    private Pattern makePattern(String startDelimiter, String endDelimiter)
    {
        String startDelimiter2 = "\\{" + startDelimiter + "\\}";
        String endDelimiter2 = "\\{" + endDelimiter + "\\}";

        String phrase_content = "[^\\s" + startDelimiter + "]((?!" + endDelimiter + ")[\\p{L}\\p{Nd}\\p{Z}\\p{S}\\p{M}\\p{P}]*?[^\\s" + endDelimiter + "])??";

        return Pattern.compile("(?:(?:(^|" + VALID_START + ")" + startDelimiter + ")|" + startDelimiter2 + ")(" + phrase_content +
                ")(?<!\\\\)(?:(?:" + endDelimiter + "(" + VALID_END + "|$))|" + endDelimiter2 + ")");
    }
}
