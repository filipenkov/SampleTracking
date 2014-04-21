package com.atlassian.streams.api;

import java.io.Serializable;

import com.atlassian.streams.api.common.Option;

import com.google.common.base.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.streams.api.common.Functions.trimToNone;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A wrapper around {@code String}s that are allowed to contain HTML markup and may
 * be directly inserted into the feed output without HTML escaping.  Any content that
 * is in a normal String rather than an {@link Html} instance is assumed not to
 * contain markup, and will be escaped before displaying.
 * <p>
 * This class also filters its content to remove any non-printable control characters
 * other than tab and newline.
 */
@SuppressWarnings("serial")
public final class Html implements Serializable
{
    private static final Logger log = LoggerFactory.getLogger(Html.class);

    private final String value;

    public Html(String value)
    {
        this.value = stripControlChars(checkNotNull(value));
    }

    public static Function<String, Html> html()
    {
        return HtmlF.INSTANCE;
    }

    public static Html html(String s)
    {
        return html().apply(s);
    }

    private enum HtmlF implements Function<String, Html>
    {
        INSTANCE;

        public Html apply(String s)
        {
            return new Html(s);
        }
    }

    public static Function<Html, String> htmlToString()
    {
        return HtmlToString.INSTANCE;
    }

    private enum HtmlToString implements Function<Html, String>
    {
        INSTANCE;

        public String apply(Html h)
        {
            return h.toString();
        }
    }

    public static Function<Html, Option<Html>> trimHtmlToNone()
    {
        return TrimHtmlToNone.INSTANCE;
    }

    private enum TrimHtmlToNone implements Function<Html, Option<Html>>
    {
        INSTANCE;

        public Option<Html> apply(Html h)
        {
            return trimToNone().apply(h.toString()).map(html());
        }
    }

    @Override
    public String toString()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        Html other = (Html) obj;
        return value.equals(other.value);
    }

    private String stripControlChars(final String s)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray())
        {
            if (illegal(c))
            {
                log.debug("Invalid character encountered: codePoint = {}", String.valueOf(c).codePointAt(0));
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean illegal(final char c)
    {
        return between(c, '\u0000', '\u0008')
            || between(c, '\u000B', '\u001F')
            || between(c, '\uFFFE', '\uFFFF');
    }

    private boolean between(char c, char minInclusive, char maxInclusive)
    {
        return c >= minInclusive && c <= maxInclusive;
    }
}