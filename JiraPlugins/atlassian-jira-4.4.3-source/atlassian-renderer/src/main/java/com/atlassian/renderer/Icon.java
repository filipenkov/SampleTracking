/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 28, 2004
 * Time: 8:05:18 PM
 */
package com.atlassian.renderer;

import java.text.MessageFormat;

/**
 * Encapsulates an icon the renderer has to draw, for example a link decoration or emoticon.
 */
public class Icon
{
    /** Template for converting an image to an HTML img tag */
    // TODO: make this private
    public static final String IMAGE_TEMPLATE = "<img class=\"{5}\" src=\"{0}/{1}\" height=\"{2}\" width=\"{3}\" align=\"absmiddle\" alt=\"{4}\" border=\"0\"/>";

    /** The icon is drawn to the left of the content it is decorating (i.e. [icon]link) */
    public static final int ICON_LEFT = -1;
    /** The icon is drawn to the right of the content it is decorating (i.e. link[icon]) */
    public static final int ICON_RIGHT = 1;

    /**
     * The NULL_ICON is a convenient icon that has no size and draws nothing. Useful at any family function.
     */
    public static final Icon NULL_ICON = new Icon(null, 0, 0, 0, "");

    // CSS classes for different icon types
    private static final String LINK_DECORATION_CLASS = "rendericon";
    private static final String EMOTICON_CLASS = "emoticon";

    // Instance variables
    public final String path;
    public final int position;
    public final int width;
    public final int height;
    public final String cssClass;

    /**
     * Factory method for a new rendericon
     *
     * @param path the path (relative to the renderer's image root) to the icon's image file
     * @param position one of ICON_LEFT or ICON_RIGHT
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @return the appropriate Icon object
     */
    public static Icon makeRenderIcon(String path, int position, int width, int height)
    {
        return new Icon(path, position, width, height, LINK_DECORATION_CLASS);
    }

    /**
     * Factory method for a new emoticon
     *
     * @param path the path (relative to the renderer's image root) to the icon's image file
     * @param height the height of the image in pixels
     * @param width the width of the image in pixels
     * @return the appropriate Icon object
     */
    public static Icon makeEmoticon(String path, int height, int width)
    {
        return new Icon(path, 0, width, height, EMOTICON_CLASS);
    }

    /**
     * Private. Use a factory method
     */
    private Icon(String path, int position, int width, int height, String cssClass)
    {
        this.path = path;
        this.position = position;
        this.width = width;
        this.height = height;
        this.cssClass = cssClass;
    }

    /**
     * Return the icon as HTML, relative to the given image root. Some icons may be
     * more than just a image tag - for example, rendericons are rendered as superscript.
     *
     * @param imageRoot the URL path to the root of the image directory
     * @return the HTML markup for displaying this icon as an image
     */
    public String toHtml(String imageRoot)
    {
        String imgTag = MessageFormat.format(IMAGE_TEMPLATE, new String[] {
            imageRoot,
            path,
            Integer.toString(width),
            Integer.toString(height),
            "",
            cssClass});

        if (width == 0 || height == 0)
            return "";
        else if (LINK_DECORATION_CLASS.equals(cssClass))
            return "<sup>" + imgTag + "</sup>";
        else
            return imgTag;
    }

    public String getPath()
    {
        return path;
    }

    /**
     * @see Object#toString
     */
    public String toString()
    {
        return cssClass + ": " + path;
    }
}
