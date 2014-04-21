package com.atlassian.renderer.wysiwyg;

import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.UnpermittedLink;
import com.atlassian.renderer.links.UnresolvedLink;
import com.opensymphony.util.TextUtils;
import org.w3c.dom.Node;

public class WysiwygLinkHelper
{
    public static String getLinkInfoAttributes(Link link)
    {
        // Encode the HTML here to fix characters that may interfere with the link text, e.g. " (CONF-4544)
        return "linktype=\"raw\" linktext=\"" + TextUtils.htmlEncode(link.getOriginalLinkText()) + "\"" +
                (link instanceof UnpermittedLink || link instanceof UnresolvedLink ? " class=\"linkerror\"" : "");
    }

    public static String createLinkWikiText(Node node, String alias, String aliasUnformatted)
    {
        // strip off any text sperator -- it isn't needed at the beginning of a link alias
        if (alias.startsWith(DefaultWysiwygConverter.TEXT_SEPERATOR))
        {
            alias = alias.substring(DefaultWysiwygConverter.TEXT_SEPERATOR.length());
        }
        String linkType = node.getAttributes().getNamedItem("linktype").getNodeValue();
        // we don't use the linktext, because the user can only edit it with the link editor pop-up, but we may need
        // to get some hints from it, e.g. mailto: links, where aliasUnformatted doesn't contain the mailto: prefix.
        String linkText = node.getAttributes().getNamedItem("linktext").getNodeValue();
        if (linkText.startsWith("mailto:"))
        {
            aliasUnformatted = "mailto:" + aliasUnformatted;
        }
        if (linkText.startsWith("#") && !aliasUnformatted.startsWith("#"))
        {
            aliasUnformatted = "#" + aliasUnformatted;
        }
        if (linkText.startsWith("^") && !aliasUnformatted.startsWith("^"))
        {
            aliasUnformatted = "^" + aliasUnformatted;
        }
        if (linkText.startsWith("~") && !aliasUnformatted.startsWith("~") || isShortcutLink(linkText))
        {
            aliasUnformatted = linkText;
        }
        alias = alias.trim();
        if (linkType.equals("raw"))
        {
            /*
             * Err... We don't actually know why decoding isn't necessary here.
             * Let's assume it is done automatically by the DOM
             */
            String linkTextWithoutAlias = linkText;
            if (linkTextWithoutAlias.indexOf(alias) == 0)
                linkTextWithoutAlias = linkTextWithoutAlias.substring(alias.length());
            String[] components = linkTextWithoutAlias.split("\\|");
            StringBuffer linkBody = new StringBuffer("[");
            if (components.length == 1)
            {
                // [link]
                linkBody.append(aliasUnformatted);
            }
            else if (components.length == 2)
            {
                // [alias|link]
                // but don't use the alias if it is the same as the link with a prefix removed
                if (stringEqualsStringMinusPrefix(alias, components[1], new String[] {"mailto:", "#", "^"}))
                {
                    linkBody.append(components[1]);
                }
                else
                {
                    linkBody.append(alias).append("|").append(components[1]);
                }
            }
            else if (components.length == 3)
            {
                // [alias|link|tip]
                linkBody.append(alias).append("|").append(components[1]).append("|").append(components[2]);
            }
            return linkBody.append("]").toString();
        }
        return "";
    }

    public static boolean isShortcutLink(String linkText)
    {
        return linkText.indexOf('@') != -1;
    }

    private static boolean stringEqualsStringMinusPrefix(String s1, String s2, String[] prefixes)
    {
        for (int i = 0; i < prefixes.length; ++i)
        {
            if (s2.equals(prefixes[i] + s1))
            {
                return true;
            }
        }
        return false;
    }
}
