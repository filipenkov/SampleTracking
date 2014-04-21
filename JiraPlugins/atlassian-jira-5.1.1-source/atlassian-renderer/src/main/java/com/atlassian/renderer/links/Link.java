package com.atlassian.renderer.links;

import java.util.List;

/**
 * A link that can be rendered in a page. Ideally, links should be immutable.
 */
public abstract class Link
{
    private final String originalLinkText;
    protected String url;
    protected String title;
    protected String linkBody;
    protected boolean relativeUrl;
    protected String iconName;
    protected String titleKey;
    protected List titleArgs;

    /**
     * Construct a link from some text (as passed into LinkResolver#createLink)
     *
     * @param originalLinkText the original text of the link
     */
    public Link(String originalLinkText)
    {
        this.originalLinkText = originalLinkText;
    }

    /**
     * Get the original text used to build this link
     */
    public String getOriginalLinkText()
    {
        return originalLinkText;
    }

    /**
     * Get the destination URL for this link
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Get the link title text
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Get the contents that are being marked up by the link
     */
    public String getLinkBody()
    {
        return linkBody;
    }

    /**
     * Get the link body as it would be displayed if the link wasn't permitted to be viewed
     */
    public String getUnpermittedLinkBody()
    {
        return linkBody;
    }

    /**
     * Is the URL relativeUrl to the application's context path?
     *
     * @return true if the URL is relativeUrl to the context path.
     */
    public boolean isRelativeUrl()
    {
        return relativeUrl;
    }

    /**
     * This is a method that will allow a link to set any additional attributes that are related to
     * the specific link type. i.e. - if a link wants to set a css style, then this would be the
     * place.
     * @return a string containing the param that will be included within the anchor tag.
     */
    public String getLinkAttributes()
    {
        return "";
    }

    /**
     * A name that the renderer can use to look up an appropriate icon.
     *
     * @return the name of an icon to look up for this link, null if there
     *         is no possibility of an appropriate icon
     */
    public String getIconName()
    {
        return iconName;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o.getClass().equals(this.getClass()))) return false;

        final Link link = (Link) o;

        if (originalLinkText != null ? !originalLinkText.equals(link.originalLinkText) : link.originalLinkText != null) return false;

        return true;
    }

    public int hashCode()
    {
        return (originalLinkText != null ? originalLinkText.hashCode() : 0);
    }

    /**
     * Retrieve the i18n key for the title if defined.
     * @return a key or null if undefined, in which case check "getTitle"
     */
    public String getTitleKey()
    {
        return titleKey;
    }

    /**
     * Retrieves the arguments to use in the internationalised message identified by
     * {@link #getTitleKey()}
     *
     * @return a list of arguments; null or an empty list if there are none.
     */
    public List getTitleArgs()
    {
        return titleArgs;
    }
}