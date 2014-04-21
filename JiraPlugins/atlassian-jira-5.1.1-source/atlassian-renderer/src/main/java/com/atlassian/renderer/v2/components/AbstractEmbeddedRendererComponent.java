package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.embedded.EmbeddedResource;
import com.atlassian.renderer.embedded.EmbeddedResourceParser;
import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 */
public abstract class AbstractEmbeddedRendererComponent extends AbstractRegexRendererComponent
{
    static final Pattern EMBEDDED_PATTERN = Pattern.compile(buildPhraseRegExp("\\!", "\\!"));

    public String render(String wiki, RenderContext context)
    {
        if (wiki.indexOf("!") == -1)
        {
            return wiki;
        }
        return regexRender(wiki, context, EMBEDDED_PATTERN);
    }

    public void appendSubstitution(StringBuffer buffer, RenderContext context, Matcher matcher)
    {
        // Make sure there's a minimum number of characters between the bangs, so stupid strings
        // like ?!?!?!? don't get matched (CONF-3227)
        // Make sure the first character in the buffer isn't a ), so we don't get
        // confused with emoticons (CONF-3369)
        String originalString = matcher.group(2);
        if (originalString.length() < 5 || originalString.charAt(0) == ')')
        {
 	        buffer.append("!").append(originalString).append("!");
 	        return;
 	    }

        // turn data into a resource, return token into stream so that it is ignored.
        EmbeddedResourceParser parser = new EmbeddedResourceParser(originalString);
        EmbeddedResource resource = findResource(context, parser, originalString);
        if (resource != null)
        {
            EmbeddedResourceRenderer renderer = context.getEmbeddedResourceRenderer();
            buffer.append(renderer.renderResource(resource, context));
        }
        else
        {
            // Change to group 0 and check.
            buffer.append("!").append(originalString).append("!");
        }
    }

    abstract protected EmbeddedResource findResource(final RenderContext context, final EmbeddedResourceParser parser,final String originalString);

    // Salvaged from the old Radeox code. This is the only place it's used. Might be some serious overkill...
    protected static String buildPhraseRegExp(String phrase_start_sign, String phrase_end_sign)
    {
        String valid_start = "(?<![\\p{L}\\p{Nd}\\\\])";
        String valid_end = "(?![\\p{L}\\p{Nd}])";
        String phrase_content = "[^\\s" + phrase_start_sign + "]((?!" + phrase_end_sign + ")[\\p{L}\\p{Nd}\\p{Z}\\p{S}\\p{M}\\p{P}]*?[^\\s" + phrase_end_sign + "])?";

        return ("(^|" + valid_start + ")" + phrase_start_sign + "(" + phrase_content + ")(?<!\\\\)" + phrase_end_sign + "(" + valid_end + "|$)");
    }
}
