package com.atlassian.renderer.links;

import com.atlassian.renderer.util.RendererProperties;
import org.apache.log4j.Category;

import java.text.ParseException;
import java.util.Arrays;

public class UrlLink extends BaseLink
{
    private static Category log = Category.getInstance(UrlLink.class);

    public static final String EXTERNAL_ICON = "external";
    public static final String MAILTO_ICON = "mailto";

    public UrlLink(String url, String linkBody) throws ParseException
    {
        this(new GenericLinkParser(url));
        this.linkBody = linkBody;
    }

    public UrlLink(GenericLinkParser parser)
    {
        super(parser);
        iconName = EXTERNAL_ICON;
        url = parser.getNotLinkBody();
        setI18nTitle(RendererProperties.URL_LINK_TITLE, null);

        if (url.startsWith("///"))
        {
            url = url.substring(2);
            relativeUrl = true;
            setI18nTitle(RendererProperties.SITE_RELATIVE_LINK_TITLE, null);
        }
        else if (url.startsWith("//"))
        {
            url = url.substring(1);
            setI18nTitle(RendererProperties.RELATIVE_LINK_TITLE, null);
        }

        if (url.startsWith("\\\\"))
            url = "file:" + url.replaceAll("\\\\", "/");

        if (isMailLink())
        {
            // remove the "mailto:" from mail link bodies so we just get the
            // email address
            if (parser.getLinkBody() == null)
                linkBody = linkBody.substring(7);
            setI18nTitle(RendererProperties.SEND_MAIL_TO, Arrays.asList(new String[] {linkBody}));
            iconName = MAILTO_ICON;
        }
    }

    public boolean isMailLink()
    {
        return url.startsWith("mailto:");
    }

}
