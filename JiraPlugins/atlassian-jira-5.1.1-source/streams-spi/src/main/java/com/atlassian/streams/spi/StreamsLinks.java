package com.atlassian.streams.spi;

import java.net.URI;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Deprecated
public final class StreamsLinks
{
    private StreamsLinks()
    {
        throw new RuntimeException("StreamsLinks cannot be instantiated");
    }

    @Deprecated
    public static String nullSafeLink(final URI uri, final String label)
    {
        String escapedLabel = escapeHtml(label);
        return uri != null && isNotBlank(uri.toASCIIString()) ?
                "<a href=\"" + uri.toASCIIString() + "\">" + escapedLabel + "</a>" :
                    escapedLabel;
    }

    @Deprecated
    public static String nullSafeLink(final URI uri, final String label, String styleClass)
    {
        String escapedLabel = escapeHtml(label);
        return uri != null && isNotBlank(uri.toASCIIString()) ?
                "<a href=\"" + uri.toASCIIString() + "\" class=\"" + styleClass + "\">" + escapedLabel + "</a>" :
                    "<span class=\"" + styleClass + "\">" + escapedLabel + "</span>";
    }
}
