package com.atlassian.jira.config.properties;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.commons.lang.StringUtils;

/**
 * Bean mainly used to maintain a version number for all the menu colours.  This allows us
 * to cache this information until it is updated (i.e.:version number is incremented).
 */
public class LookAndFeelBean
{
    private final ApplicationProperties ap;

    /**
     * These are the "intended" colours for the JIRA Header.
     *
     * @since v4.0
     */
    public static final class DefaultColours
    {
        public static final String TOP_HIGHLIGHTCOLOUR = "#326ca6";
        public static final String TOP_TEXTHIGHLIGHTCOLOUR = "#f0f0f0";
        public static final String TOP_SEPARATOR_BGCOLOUR = "#003366";

        public static final String TOP_BGCOLOUR = "#003366";
        public static final String TOP_TEXTCOLOUR = "#ffffff";

        public static final String MENU_BGCOLOUR = "#326ca6";
        public static final String MENU_TEXTCOLOUR = "#ffffff";
        public static final String MENU_SEPARATOR = "#f0f0f0";

        public static final String TEXT_LINKCOLOR = "#326ca6";
        public static final String TEXT_ACTIVELINKCOLOR = "#326ca6";
        public static final String TEXT_HEADINGCOLOR = "#292929";


        private DefaultColours()
        {
        }
    }


    public static class DefaultFaviconDimensions
    {
        public static final String FAVICON_DIMENSION = "16";
        public static final String FAVICON_HIRES_DIMENSION = "32";

        private DefaultFaviconDimensions()
        {
        }
    }

    private LookAndFeelBean(final ApplicationProperties ap)
    {
        this.ap = ap;
    }

    public static LookAndFeelBean getInstance(final ApplicationProperties ap)
    {
        return new LookAndFeelBean(ap);
    }

    private void incrementVersion()
    {
        long version = getVersion();
        version++;
        ap.setString(APKeys.WEB_RESOURCE_FLUSH_COUNTER, Long.toString(version));
    }


    public String stripHash(String colour)
    {
        if (StringUtils.isNotBlank(colour) && colour.startsWith("#"))
        {
            return colour.substring(1, colour.length());
        }
        return colour;
    }

    private void setValue(final String key, final String value)
    {
        ap.setString(key, value);
        incrementVersion();
    }

    /**
     * Convenience method used by data import (see JRA-11680) to update version number to
     * the greater version number after the import (to make sure LF values wont be cached).
     *
     * @param oldVersion the previous version
     */
    public void updateVersion(long oldVersion)
    {
        long currentVersion = getVersion();

        if (oldVersion > currentVersion)
        {
            ap.setString(APKeys.WEB_RESOURCE_FLUSH_COUNTER, Long.toString(++oldVersion));
        }
        else
        {
            ap.setString(APKeys.WEB_RESOURCE_FLUSH_COUNTER, Long.toString(++currentVersion));
        }
    }

    public long getVersion()
    {
        final String editVersion = ap.getDefaultBackedString(APKeys.WEB_RESOURCE_FLUSH_COUNTER);
        return Long.parseLong(StringUtils.isNotEmpty(editVersion) ? editVersion : "1");
    }

    /*
     * ======== LOGO ==================
     */

    public String getLogoUrl()
    {
        return ap.getDefaultBackedString(APKeys.JIRA_LF_LOGO_URL);
    }

    public String getAbsoluteLogoUrl()
    {
        String jiraLogo = getLogoUrl();
        if (jiraLogo != null && !jiraLogo.startsWith("http://") && !jiraLogo.startsWith("https://"))
        {
            jiraLogo = ComponentAccessor.getComponent(WebResourceManager.class).getStaticResourcePrefix(UrlMode.AUTO) + jiraLogo;
        }

        return jiraLogo;
    }

    public void setLogoUrl(final String logoUrl)
    {
        setValue(APKeys.JIRA_LF_LOGO_URL, logoUrl);
    }

    public String getLogoWidth()
    {
        return ap.getDefaultBackedString(APKeys.JIRA_LF_LOGO_WIDTH);
    }

    public String getLogoPixelWidth()
    {
        return getLogoWidth() + "px";
    }

    public void setLogoWidth(final String logoWidth)
    {
        setValue(APKeys.JIRA_LF_LOGO_WIDTH, logoWidth);
    }

    public String getLogoHeight()
    {
        return ap.getDefaultBackedString(APKeys.JIRA_LF_LOGO_HEIGHT);
    }

    public String getLogoPixelHeight()
    {
        return getLogoHeight() + "px";
    }

    public void setLogoHeight(final String logoHeight)
    {
        setValue(APKeys.JIRA_LF_LOGO_HEIGHT, logoHeight);
    }

      /*
     * ======== FAVICON ==================
     */


    public String getFaviconUrl()
    {
        return ap.getDefaultBackedString(APKeys.JIRA_LF_FAVICON_URL);
    }

    public void setFaviconUrl(String faviconUrl)
    {
        setValue(APKeys.JIRA_LF_FAVICON_URL, faviconUrl);
    }

    public String getFaviconWidth()
    {
        return DefaultFaviconDimensions.FAVICON_DIMENSION;
    }

    public String getFaviconHeight()
    {
         return DefaultFaviconDimensions.FAVICON_DIMENSION;
    }

     public String getFaviconHiResUrl()
    {
        return ap.getDefaultBackedString(APKeys.JIRA_LF_FAVICON_HIRES_URL);
    }

    public void setFaviconHiResUrl(String faviconUrl)
    {
        setValue(APKeys.JIRA_LF_FAVICON_HIRES_URL, faviconUrl);
    }

    public String getFaviconHiResWidth()
    {
        return DefaultFaviconDimensions.FAVICON_HIRES_DIMENSION;
    }


    public String getFaviconHiResHeight()
    {
         return DefaultFaviconDimensions.FAVICON_HIRES_DIMENSION;
    }

   /*
     * ======== TOP ==================
     */
    public String getTopBackgroundColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TOP_BGCOLOUR, DefaultColours.TOP_BGCOLOUR);
    }

    public void setTopBackgroundColour(final String topBackgroundColour)
    {
        setValue(APKeys.JIRA_LF_TOP_BGCOLOUR, topBackgroundColour);
    }

    public String getTopTxtColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TOP_TEXTCOLOUR, DefaultColours.TOP_TEXTCOLOUR);
    }

    public void setTopTxtColour(final String topTxtColour)
    {
        setValue(APKeys.JIRA_LF_TOP_TEXTCOLOUR, topTxtColour);
    }

    public String getTopHighlightColor()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, DefaultColours.TOP_HIGHLIGHTCOLOUR);
    }

    public void setTopHighlightColor(final String newValue)
    {
        setValue(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, newValue);
    }

    public String getTopTextHighlightColor()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR, DefaultColours.TOP_TEXTHIGHLIGHTCOLOUR);
    }

    public void setTopTextHighlightColor(final String newValue)
    {
        setValue(APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR, newValue);
    }

    public String getTopSeparatorBackgroundColor()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, DefaultColours.TOP_SEPARATOR_BGCOLOUR);
    }

    public void setTopSeparatorBackgroundColor(final String newValue)
    {
        setValue(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, newValue);
    }

    /*
     * ======== MENU NAVIGATION ==================
     */
    public String getMenuTxtColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_MENU_TEXTCOLOUR, DefaultColours.MENU_TEXTCOLOUR);
    }

    public void setMenuTxtColour(final String menuTxtColour)
    {
        setValue(APKeys.JIRA_LF_MENU_TEXTCOLOUR, menuTxtColour);
    }

    public String getMenuBackgroundColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_MENU_BGCOLOUR, DefaultColours.MENU_BGCOLOUR);
    }

    public void setMenuBackgroundColour(final String menuBackgroundColour)
    {
        setValue(APKeys.JIRA_LF_MENU_BGCOLOUR, menuBackgroundColour);
    }

    public String getMenuSeparatorColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_MENU_SEPARATOR, DefaultColours.MENU_SEPARATOR);
    }

    public void setMenuSeparatorColour(final String menuSeparatorColour)
    {
        setValue(APKeys.JIRA_LF_MENU_SEPARATOR, menuSeparatorColour);
    }

    /*
     * ======== JIRA TEXT AND LINKS ==================
     */

    public String getTextHeadingColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, DefaultColours.TEXT_HEADINGCOLOR);
    }

    public void setTextHeadingColour(final String textHeadingColour)
    {
        setValue(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, textHeadingColour);
    }

    public String getTextLinkColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TEXT_LINKCOLOUR, DefaultColours.TEXT_LINKCOLOR);
    }

    public void setTextLinkColour(final String textLinkColour)
    {
        setValue(APKeys.JIRA_LF_TEXT_LINKCOLOUR, textLinkColour);
    }

    public String getTextActiveLinkColour()
    {
        return getDefaultBackedString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, DefaultColours.TEXT_ACTIVELINKCOLOR);
    }

    public void setTextActiveLinkColour(final String textActiveLinkColour)
    {
        setValue(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, textActiveLinkColour);
    }

    /*
     * ======== GADGET CHROME COLORS ==================
     */

    public String getGadgetChromeColor(final String id)
    {
        return ap.getDefaultBackedString(APKeys.JIRA_LF_GADGET_COLOR_PREFIX + id);
    }

    public void setGadgetChromeColor(final String id, final String gadgetChromeColor)
    {
        setValue(APKeys.JIRA_LF_GADGET_COLOR_PREFIX + id, gadgetChromeColor);
    }

    /*
     * ======== MISC ==================
     */

    public String getApplicationID()
    {
        return ap.getDefaultBackedString(APKeys.JIRA_LF_APPLICATION_ID);
    }

    /**
     * Performs a lookup on the application properties for the specified key. If the key returns a null value,
     * returns the default value specified. This would happen if no value exists in the database AND no value exists
     * in the jira-application.properties file.
     *
     * @param key          the Application Properties key to look up
     * @param defaultValue the value to return if the key yields null
     * @return the value of the key in the Application Properties, or the default value specified
     */
    public String getDefaultBackedString(final String key, final String defaultValue)
    {
        final String value = ap.getDefaultBackedString(key);
        return value == null ? defaultValue : value;
    }
}
