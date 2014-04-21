package com.atlassian.streams.jira.util;

import java.awt.Dimension;
import java.net.URI;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.streams.api.StreamsEntry.Html;
import com.atlassian.streams.api.common.Option;

import com.opensymphony.util.TextUtils;

import org.apache.commons.lang.StringUtils;

public class RenderingUtilities
{
    private static final String[] RESOURCES =
        {
            "com.atlassian.streams.streams-jira-plugin:streamsWebResources"
        };
    private static final int MAX_THUMBNAIL_HEIGHT = 100;

    private RenderingUtilities() {/*static methods only*/}

    /**
     * Calculates the new dimensions for an image based on its height. The current maximum is set to 100 but may change
     * in the future.
     *
     * @param width the width of the image, must be at least 1
     * @param height the height of the image, must be at least 1
     * @return the dimensions which should be applied
     * @throws IllegalArgumentException if width or height is less than 1
     */
    public static Dimension scaleToThumbnailSize(int width, int height)
    {
        if (height < 1)
            throw new IllegalArgumentException("Height cannot be less than 1.");
        if (width < 1)
            throw new IllegalArgumentException("Width cannot be less than 1.");

        double scaleFactor = height > MAX_THUMBNAIL_HEIGHT ? ((double) MAX_THUMBNAIL_HEIGHT) / (double) height : 1;
        return new Dimension((int) (width * scaleFactor), (int) (height * scaleFactor));
    }

    /**
     * A null-safe version of {@link com.opensymphony.util.TextUtils#htmlEncode(String)}
     *
     * @param s a string to encode, or <code>null</code>
     * @return an HTML-encoded version of <code>s</code>, or the empty string if <code>s</code> is null
     */
    public static String htmlEncode(String s)
    {
        if (s == null) return null;
        return TextUtils.htmlEncode(s);
    }

    public static String span(String classAttribute, String html)
    {
        if(classAttribute == null)
        {
            // we don't want the text "null" to appear as the attribute.
            throw new IllegalArgumentException("Class attribute is null");
        }
        if(html == null)
        {
            // we don't want the text "null" to appear as the content (unless it is deliberate).
            throw new IllegalArgumentException("HTML is null");
        }

        return "<span class='" + classAttribute + "'>" + html + "</span>";
    }

    /**
     * Create an <code>a</code> tag from a URI and a label. If the URI is null or blank, then an <code>a</code> tag is
     * not created and the original label is returned. In all cases the original label is HTML encoded.
     *
     * @param uri may be null or blank
     * @param label may not be null
     * @return an <code>a</code> with <code>uri</code> as the URI and <code>label</code> as the label or just the label
     * if the <code>uri</code> is blank.
     * @throws IllegalArgumentException if the label is null
     */
    public static String link(String uri, String label)
    {
        if(label == null)
        {
            // we don't want the text "null" to appear as a label.
            throw new IllegalArgumentException("Label is null");
        }

        return StringUtils.isNotBlank(uri) ? "<a href=\"" + uri + "\">" + htmlEncode(label) + "</a>" : htmlEncode(label);
    }

    /**
     * Create an <code>a</code> tag from a URI and a label. If the URI is {@code Option.none()}, then an <code>a</code>
     * tag is not created and the original label is returned. In all cases the original label is HTML encoded.
     *
     * @param uri uri to link to
     * @param label may not be null
     * @return an <code>a</code> with <code>uri</code> as the URI and <code>label</code> as the label or just the label
     * if the <code>uri</code> is blank.
     * @throws IllegalArgumentException if the label is null
     */
    public static Html link(Option<URI> uri, Html label)
    {
        if(label == null)
        {
            // we don't want the text "null" to appear as a label.
            throw new IllegalArgumentException("Label is null");
        }

        return uri.isDefined() ? new Html("<a href=\"" + uri.get().toASCIIString() + "\">" + label + "</a>") : label;
    }

    /**
     * Require all the activity stream resources.
     *
     * @param webResourceManager the manager to add the requirements to
     */
    public static void includeActivityStreamResources(WebResourceManager webResourceManager)
    {
        for (String resource : RESOURCES)
        {
            webResourceManager.requireResource(resource);
        }
    }
}
