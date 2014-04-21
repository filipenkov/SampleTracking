package com.atlassian.renderer;

import com.atlassian.renderer.util.RendererProperties;
import com.atlassian.renderer.links.UrlLink;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An icon manager that looks up a property to find the location of the emoticon icons and that has no
 * link decoration icons.
 */
public class DefaultIconManager implements IconManager
{
    private Map iconsMap;

    private String[] emoticons;
    private Map emoticonsMap;

    /**
     * Retrieve a link decoration for a particular icon name. If no icon matches, an icon that draws no
     * image, will be returned.
     *
     * @param iconName the name of the icon to retrieve
     * @return the appropriate icon, or an icon that draws no image if nothing is found matching the icon name
     */
    public Icon getLinkDecoration(String iconName)
    {
        if (getIconsMap().containsKey(iconName))
            return (Icon) getIconsMap().get(iconName);

        return Icon.NULL_ICON;
    }

    /**
     * Retrieve an emoticon for a particular icon name. If no icon matches, an icon that draws no
     * image, will be returned.
     *
     * @param symbol the symbol representing the emoticon to retrieve
     * @return the appropriate emoticon, or an icon that draws no image if nothing is found matching the supplied
     *         symbol
     */
    public Icon getEmoticon(String symbol)
    {
        if (getEmoticonsMap().containsKey(symbol))
            return (Icon) getEmoticonsMap().get(symbol);

        return Icon.NULL_ICON;
    }

    /**
     * Retrieve all the available emoticon symbols
     *
     * @return all the available emoticon symbols
     */
    public String[] getEmoticonSymbols()
    {
        if (emoticons == null)
        {
            String[] tmpEmoticons = new String[getEmoticonsMap().size()];

            int i = 0;
            for (Iterator it = getEmoticonsMap().keySet().iterator(); it.hasNext(); i++)
            {
                String key = (String) it.next();
                tmpEmoticons[i] = key;
            }

            emoticons = tmpEmoticons;
        }

        return emoticons;
    }

    protected Map getIconsMap()
    {
        if (iconsMap == null)
        {
            iconsMap = new HashMap();
            iconsMap.put(UrlLink.MAILTO_ICON, Icon.makeRenderIcon(RendererProperties.ICONS_PATH + "mail_small.gif", Icon.ICON_RIGHT, 12, 13));
            iconsMap.put(UrlLink.EXTERNAL_ICON, Icon.makeRenderIcon(RendererProperties.ICONS_PATH + "linkext7.gif", Icon.ICON_RIGHT, 7, 7));
        }

        return iconsMap;
    }

    protected synchronized Map getEmoticonsMap()
    {
        if (emoticonsMap == null)
        {
            emoticonsMap = new HashMap();
            emoticonsMap.put(":-)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "smile.gif", 20, 20));
            emoticonsMap.put(":)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "smile.gif", 20, 20));
            emoticonsMap.put(":P", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "tongue.gif", 20, 20));
            emoticonsMap.put(":p", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "tongue.gif", 20, 20));
            emoticonsMap.put(";-)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "wink.gif", 20, 20));
            emoticonsMap.put(";)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "wink.gif", 20, 20));
            emoticonsMap.put(":D", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "biggrin.gif", 20, 20));
            emoticonsMap.put(":-(", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "sad.gif", 20, 20));
            emoticonsMap.put(":(", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "sad.gif", 20, 20));
            emoticonsMap.put("(y)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "thumbs_up.gif", 19, 19));
            emoticonsMap.put("(n)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "thumbs_down.gif", 19, 19));
            emoticonsMap.put("(i)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "information.gif", 16, 16));
            emoticonsMap.put("(/)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "check.gif", 16, 16));
            emoticonsMap.put("(x)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "error.gif", 16, 16));
            emoticonsMap.put("(+)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "add.gif", 16, 16));
            emoticonsMap.put("(-)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "forbidden.gif", 16, 16));
            emoticonsMap.put("(!)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "warning.gif", 16, 16));
            emoticonsMap.put("(?)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "help_16.gif", 16, 16));
            emoticonsMap.put("(on)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "lightbulb_on.gif", 16, 16));
            emoticonsMap.put("(off)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "lightbulb.gif", 16, 16));
            emoticonsMap.put("(*)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "star_yellow.gif", 16, 16));
            emoticonsMap.put("(*b)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "star_blue.gif", 16, 16));
            emoticonsMap.put("(*y)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "star_yellow.gif", 16, 16));
            emoticonsMap.put("(*g)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "star_green.gif", 16, 16));
            emoticonsMap.put("(*r)", Icon.makeEmoticon(RendererProperties.EMOTICONS_PATH + "star_red.gif", 16, 16));
        }

        return emoticonsMap;
    }
}
