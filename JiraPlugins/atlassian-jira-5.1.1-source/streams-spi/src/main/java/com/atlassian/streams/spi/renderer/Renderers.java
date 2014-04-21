package com.atlassian.streams.spi.renderer;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.atlassian.streams.api.Html;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import static com.atlassian.streams.api.Html.html;

import static org.apache.commons.lang.StringUtils.isBlank;

public final class Renderers
{
    private static final int EXCERPT_LIMIT = 250;

    public static String render(final TemplateRenderer renderer, final String template, final Map<String, ?> context)
    {
        final StringWriter writer = new StringWriter();
        try
        {
            @SuppressWarnings("unchecked")
            final Map<String, Object> ctx = (Map<String, Object>) context;
            renderer.render(template, ctx, writer);
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Error rendering " + template + " template", e);
        }

        return unescapeLineBreaks(writer.toString());
    }

    // TODO: Undo when http://jira.atlassian.com/browse/CONF-12514 is resolved (STRM-1062)
    // but this is also used to get excerpts of JIRA content (comments and descriptions), so it may need to stick around
    // copied confluence: com.atlassian.confluence.core.ContentEntityObject
    public static String getExcerpt(String strippedContent)
    {
        return getExcerptUsingLimit(strippedContent, EXCERPT_LIMIT);
    }

    public static String getExcerptUsingLimit(String strippedContent, int limit)
    {
        if (strippedContent.length() > limit)
        {
            int index = strippedContent.lastIndexOf(" ", limit);
            return strippedContent.substring(0, index > 0 ? index : limit) + "...";
        }
        else
        {
            return strippedContent;
        }
    }

    public static Function<String, String> stripBasicMarkup()
    {
        return StripBasicMarkup.INSTANCE;
    }

    public static String stripBasicMarkup(String content)
    {
        return stripBasicMarkup().apply(content);
    }

    private enum StripBasicMarkup implements Function<String, String>
    {
        INSTANCE;

        // TODO: Undo when http://jira.atlassian.com/browse/CONF-12514 is resolved (STRM-1062)
        // copied from sources as marked below
        public String apply(String content)
        {
            if (isBlank(content))
            {
                return content;
            }

            // markup addition for Streams: Remove the media markup (but not any exclamation marks with whitespace in between or consecutive exclamation marks)
            content = content.replaceAll("!([^ \\t\\r\\n\\f\\\\!]+)!", " ");

            // STRM-704 before removing macros, filter out gadgets first so as to remove gadget bodies in the process
            content = content.replaceAll("(?<!\\\\)\\{gadget[\\S]*\\{gadget}", "");

            // markup addition for Streams (Crucible review descriptions)
            content = content.replaceAll("(?<!\\{)\\{cs:[^\\}]+\\}:?", ""); // STRM-1202 - changeset macro

            // copied from confluence: com.atlassian.confluence.search.summary.HitHighlighter.stripMarkup
            content = content.replace("&nbsp;", " ");         // CONF-5116

            // copied from atlassian-renderer (with modifications to "basic formatting", "macros", and line breaks): RendererUtil.stripBasicMarkup(content)
            content = content.replaceAll("h[0-9]\\.", " "); // headings
            content = content.replaceAll("\\[.*///.*\\]", ""); // system links
            //content = content.replaceAll("[\\[\\]\\*_\\^\\-\\~\\+]", ""); // basic formatting
            content = content.replaceAll("(^|\\W)(?<!\\\\)[\\[\\]\\*_\\^\\-\\~\\+]+(\\w)", "$1$2"); // basic formatting start markup
            content = content.replaceAll("(\\w)(?<!\\\\)[\\[\\]\\*_\\^\\-\\~\\+]+(\\W|$)", "$1$2"); // basic formatting end markup
            content = content.replaceAll("\\|", " "); // table breaks
            content = content.replaceAll("(?<!\\\\)\\{([^:\\}\\{]+)(?::([^\\}\\{]*))?\\}(?!\\})", " "); // macros
            content = content.replaceAll("\\n", "<br>"); //line breaks (see excess line break removal below)
            content = content.replaceAll("\\r", "<br>"); //line breaks
            content = content.replaceAll("bq\\.", " ");
            content = content.replaceAll("  ", " ");

            // continuation of copying from confluence: com.atlassian.confluence.search.summary.HitHighlighter.stripMarkup
            content = content.replace("\\", "");              //  \
            content = content.replace("#", "");               //  #
            content = content.replace("{{", "");              //  {{
            content = content.replace("}}", "");              //  }}
            content = content.replace("\u00A0", " ");         // non breaking unicode space
            content = content.replaceAll("[ \\t\\f]+", " ");  // Replace all whitespace with *one* space

            // STRM-1409 remove excess line breaks - have a max of two consecutive line breaks
            content = content.replaceAll("<br>( *(<br>))+", "<br><br>");

            return content;
        }
    }

    public static String replaceNbsp(String before)
    {
        // Replace &nbsp; to avoid overly long paragraph. And to avoid the entry looking weird. Need to replace
        // &amp;nbsp; too to handle when the string has already been HTML escaped.
        return before.replaceAll("&nbsp;", " ").replaceAll("&amp;nbsp;", " ");
    }

    public static Function<String, String> unescapeLineBreaks()
    {
        return UnescapeLineBreaks.INSTANCE;
    }

    public static String unescapeLineBreaks(String s)
    {
        return unescapeLineBreaks().apply(s);
    }

    private enum UnescapeLineBreaks implements Function<String, String>
    {
        INSTANCE;

        public String apply(String s)
        {
            //STRM-1329 we want to include newlines in the rendered content
            return s.replaceAll("&lt;br&gt;", "<br>");
        }
    }

    public static Html truncate(int len, Html html)
    {
        Stack<HtmlOpenTag> stack = new Stack<HtmlOpenTag>();
        StringBuilder sb = new StringBuilder();
        int currentLength = 0;
        HtmlTokenizer tokens = new HtmlTokenizer(html.toString());
        HtmlToken token = tokens.next();
        while (token != EndOfInput && currentLength < len)
        {
            if (token instanceof SelfClosingTag)
            {
                sb.append(token);
            }
            else if (token instanceof HtmlOpenTag)
            {
                stack.push((HtmlOpenTag) token);
                sb.append(token);
            }
            else if (token instanceof HtmlCloseTag)
            {
                HtmlCloseTag closeTag = (HtmlCloseTag) token;
                if (stack.isEmpty())
                {
                    sb.append(token);
                }
                else
                {
                    while (!stack.peek().getName().equals(closeTag.getName()) &&
                            stack.peek().missingCloseTagAllowed())
                    {
                        stack.pop();
                    }
                    if (stack.peek().getName().equals(closeTag.getName()))
                    {
                        stack.pop();
                        sb.append(token);
                    }
                    else
                    {
                        // STRM-1884:  If we're given unbalanced HTML tags in the input, we can't really
                        // fix it, but we can at least try to return output that isn't any worse.  Pop
                        // back to the nearest matching open tag, closing any tags we see along the way.
                        // If there's no matching open tag at all, then just discard the closing tag and
                        // don't pop anything.
                        boolean found = false;
                        for (HtmlOpenTag openTag: stack)
                        {
                            if (openTag.getName().equals(closeTag.getName()))
                            {
                                found = true;
                                break;
                            }
                        }
                        if (found)
                        {
                            while (!stack.peek().getName().equals(closeTag.getName()))
                            {
                                sb.append(new HtmlCloseTag(stack.pop().getName()));
                            }
                            sb.append(token);
                        }
                    }
                }
            }
            else
            {
                if (token instanceof HtmlChars)
                {
                    String s = token.toString();
                    if (s.length() + currentLength > len)
                    {
                        int spaceIndex = s.indexOf(' ');
                        if (currentLength == 0 && (spaceIndex == -1 || spaceIndex > len))
                        {
                            sb.append(s.substring(0, len));
                        }
                        else
                        {
                            int lastSpaceIndex = s.lastIndexOf(" ", len - currentLength);
                            sb.append(s.substring(0, lastSpaceIndex > 0 ? lastSpaceIndex : s.length()));
                        }
                        currentLength = len;
                    }
                    else
                    {
                        currentLength += token.toString().length();
                        sb.append(s);
                    }
                }
                else
                {
                    sb.append(token);
                    currentLength++;
                }
            }
            token = tokens.next();
        }
        if (currentLength < len)
        {
            return html;
        }
        else
        {
            while (!stack.isEmpty())
            {
                if (stack.peek().missingCloseTagAllowed())
                {
                    stack.pop();
                }
                else if(stack.peek().getName().equals("script"))
                {
                    //Take out the partial script section, it can cause serious errors for other parts of the page.
                    int i = sb.lastIndexOf("<script>");
                    sb.delete(i, sb.length());
                    stack.pop();
                }
                else
                {
                    sb.append(new HtmlCloseTag(stack.pop().getName()));
                }
            }
            return new Html(sb.toString());
        }
    }

    public static Function<Html, Html> truncate(int len)
    {
        return new Truncate(len);
    }

    private static class Truncate implements Function<Html, Html>
    {
        private final int len;

        public Truncate(int len)
        {
            this.len = len;
        }

        public Html apply(Html h)
        {
            return truncate(len, h);
        }
    }

    /**
     * Replaces all occurrences of a substring <i>not</i> including any occurrences that
     * are within HTML tags.  The replacement text can include HTML tags.
     */
    public static Html replaceText(String searchFor, String replaceWith, Html html)
    {
        // Shortcut in case the search text isn't there at all
        if (!html.toString().contains(searchFor))
        {
            return html;
        }
        
        StringBuilder sb = new StringBuilder();
        HtmlTokenizer tokens = new HtmlTokenizer(html.toString());
        for (HtmlToken token = tokens.next(); token != EndOfInput; token = tokens.next())
        {
            if (token instanceof HtmlChars)
            {
                sb.append(token.toString().replace(searchFor, replaceWith));
            }
            else
            {
                sb.append(token);
            }
        }
        return html(sb.toString());
    }
    
    public static Function<Html, Html> replaceText(String searchFor, String replaceWith)
    {
        return new ReplaceText(searchFor, replaceWith);
    }
    
    public static class ReplaceText implements Function<Html, Html>
    {
        private final String searchFor;
        private final String replaceWith;
        
        public ReplaceText(String searchFor, String replaceWith)
        {
            this.searchFor = searchFor;
            this.replaceWith = replaceWith;
        }
        
        @Override
        public Html apply(Html from)
        {
            return replaceText(searchFor, replaceWith, from);
        }
    }

    /**
     * Same as {@link Renderers#replaceText(String, String)}, but replaces the search text with
     * a hyperlink to the given URI (and the same text).
     */
    public static Function<Html, Html> replaceTextWithHyperlink(String searchFor, URI linkUri)
    {
        return new ReplaceText(searchFor, "<a href=\"" + linkUri + "\">" + searchFor + "</a>");
    }
    
    private interface HtmlToken {}

    private static final HtmlToken EndOfInput = new HtmlToken() {};

    private static final class HtmlChars implements HtmlToken
    {
        private final String str;

        HtmlChars(String str)
        {
            this.str = str;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    private static final class HtmlEntity implements HtmlToken
    {
        private final String entity;

        HtmlEntity(String entity)
        {
            this.entity = entity;
        }

        @Override
        public String toString()
        {
            return String.format("&%s;", entity);
        }
    }

    private static class HtmlOpenTag implements HtmlToken
    {
        private static final Set<String> allowsMissingClosedTag =
            ImmutableSet.of("br", "img", "input", "tr", "td", "th", "colgroup", "col");

        protected final String name;
        protected final String attributes;

        HtmlOpenTag(String name, String attributes)
        {
            this.name = name;
            this.attributes = attributes;
        }

        String getName()
        {
            return name;
        }

        public boolean missingCloseTagAllowed()
        {
            return allowsMissingClosedTag.contains(name);
        }

        @Override
        public String toString()
        {
            return String.format("<%s%s>", name, attributes);
        }
    }

    private static final class SelfClosingTag extends HtmlOpenTag
    {
        SelfClosingTag(String name, String attributes)
        {
            super(name, attributes);
        }

        @Override
        public String toString()
        {
            return String.format("<%s%s/>", name, attributes);
        }
    }

    private static final class HtmlCloseTag implements HtmlToken
    {
        private final String name;

        HtmlCloseTag(String name)
        {
            this.name = name;
        }

        String getName()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return String.format("</%s>", name);
        }
    }

    private static final class HtmlTokenizer
    {
        private final String html;
        private int index = 0;

        public HtmlTokenizer(String html)
        {
            this.html = html;
        }

        public HtmlToken next()
        {
            if (index >= html.length())
            {
                return EndOfInput;
            }
            char c = html.charAt(index);
            index++;
            if (c == '&')
            {
                return newEntity();
            }
            else if (c != '<')
            {
                return newHtmlChars(c);
            }
            else if (html.charAt(index) == '/')
            {
                index++;
                return newCloseTag();
            }
            else
            {
                return newOpenTag();
            }
        }

        private HtmlToken newOpenTag()
        {
            char c = html.charAt(index);

            StringBuilder tagName = new StringBuilder();
            while (c != '>' && c != '/' && !Character.isWhitespace(c) && c != 0)
            {
                tagName.append(c);
                c = nextChar();
            }

            StringBuilder attributes = new StringBuilder();
            while (c != '>' && c != 0)
            {
                attributes.append(c);
                c = nextChar();
            }

            index++;
            if (attributes.length() > 0 && attributes.charAt(attributes.length() - 1) == '/')
            {
                return new SelfClosingTag(tagName.toString(), attributes.substring(0, attributes.length() - 1));
            }
            else
            {
                return new HtmlOpenTag(tagName.toString(), attributes.toString());
            }
        }

        private HtmlToken newCloseTag()
        {
            char c = html.charAt(index);
            StringBuilder tag = new StringBuilder();
            while (c != '>' && c != 0)
            {
                tag.append(c);
                c = nextChar();
            }
            index++;
            return new HtmlCloseTag(tag.toString());
        }

        private HtmlToken newHtmlChars(char c)
        {
            StringBuilder sb = new StringBuilder().append(c);
            while (index < html.length() && html.charAt(index) != '&' && html.charAt(index) != '<')
            {
                sb.append(html.charAt(index));
                index++;
            }
            return new HtmlChars(sb.toString());
        }

        private HtmlToken newEntity()
        {
            char c = html.charAt(index);
            StringBuilder entity = new StringBuilder();
            while (c != ';' && c != 0)
            {
                entity.append(c);
                c = nextChar();
            }
            index++;
            return new HtmlEntity(entity.toString());
        }

        private char nextChar()
        {
            if(index < html.length() - 1)
            {
                index++;
                return html.charAt(index);
            }
            else
            {
                //this helps break the loops if the malformed html is at the end of a snippet like <div></div><what now
                return 0;
            }
        }
    }
}
