package com.atlassian.renderer.links;

import java.util.List;
import java.util.Collections;

public abstract class BaseLink extends Link
{
    private boolean canSetTitle = true;
    private GenericLinkParser originalParser;

    protected BaseLink(GenericLinkParser parser)
    {
        super(parser.getOriginalLinkText());
        this.originalParser = parser;

        if (parser.getLinkBody() == null)
            linkBody = parser.getNotLinkBody();
        else
            linkBody = parser.getLinkBody();

        if (parser.getLinkTitle() != null)
        {
            title = parser.getLinkTitle();
            // can not override title from below using setter
            canSetTitle = false;
        }
    }

    protected void setTitle(String title)
    {
        if (canSetTitle)
            this.title = title;
    }

    //use this method if you are doing i18n with arguments

    /**
     * Set I18n key and argument list to use as a title.  This will <b>not</b>
     * cause {@link #getTitle} to return the translated version, but will allow
     * {@link #getTitleKey} and {@link #getTitleArgs} to be used.
     *
     * @param titleKey i18n key for the title of the link.
     * @param titleArgs arguments to use in an internationalised message.
     */
    protected void setI18nTitle(String titleKey, List titleArgs)
    {
        if (canSetTitle)
        {
            this.titleKey = titleKey;
            this.titleArgs = titleArgs;
        }
    }

    public GenericLinkParser getOriginalParser()
    {
        return originalParser;
    }

    public boolean equals(Object o)
    {
        if (o == this)
            return true;

        if (!o.getClass().equals(this.getClass()))
            return false;

        BaseLink link = (BaseLink) o;
        if (link.getUrl() == null || getUrl() == null)
            return link.getUrl() == getUrl();

        return link.getUrl().equals(getUrl());
    }

    public int hashCode()
    {
        int result;
        result = getClass().hashCode();
        result = 29 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        return result;
    }
}
