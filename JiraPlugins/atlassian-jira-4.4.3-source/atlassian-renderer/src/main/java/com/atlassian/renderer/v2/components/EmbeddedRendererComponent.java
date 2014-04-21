package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.embedded.EmbeddedResource;
import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;
import com.atlassian.renderer.embedded.EmbeddedResourceResolver;
import com.atlassian.renderer.v2.RenderMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 */
public class EmbeddedRendererComponent extends AbstractRegexRendererComponent
{
    static final Pattern IMAGE_PATTERN = Pattern.compile(buildPhraseRegExp("\\!", "\\!"));

    public boolean shouldRender(RenderMode renderMode)
    {
        // ??? what is the appropriate value here?
        return renderMode.renderImages();
    }

    public String render(String wiki, RenderContext context)
    {
        if (wiki.indexOf("!") == -1)
        {
            return wiki;
        }
        return regexRender(wiki, context, IMAGE_PATTERN);
    }

    public void appendSubstitution(StringBuffer buffer, RenderContext context, Matcher matcher)
    {
        // Make sure there's a minimum number of characters between the bangs, so stupid strings
        // like ?!?!?!? don't get matched (CONF-3227)
        // Make sure the first character in the buffer isn't a ), so we don't get
        // confused with emoticons (CONF-3369)
        String matchStr = matcher.group(2);
        if (matchStr.length() < 5 || matchStr.charAt(0) == ')')
        {
 	        buffer.append("!").append(matchStr).append("!");
 	        return;
 	    }

        // turn data into a resource, return token into stream so that it is ignored.
        EmbeddedResource r = EmbeddedResourceResolver.create(matcher.group(2));
        EmbeddedResourceRenderer renderer = context.getEmbeddedResourceRenderer();
        buffer.append(context.getRenderedContentStore().addInline(renderer.renderResource(r, context)));
    }

    // Salvaged from the old Radeox code. This is the only place it's used. Might be some serious overkill...
    private static String buildPhraseRegExp(String phrase_start_sign, String phrase_end_sign)
    {
        String valid_start = "(?<![\\p{L}\\p{Nd}\\\\])";
        String valid_end = "(?![\\p{L}\\p{Nd}])";
        String phrase_content = "[^\\s" + phrase_start_sign + "]((?!" + phrase_end_sign + ")[\\p{L}\\p{Nd}\\p{Z}\\p{S}\\p{M}\\p{P}]*?[^\\s" + phrase_end_sign + "])?";

        return ("(^|" + valid_start + ")" + phrase_start_sign + "(" + phrase_content + ")(?<!\\\\)" + phrase_end_sign + "(" + valid_end + "|$)");
    }
}
