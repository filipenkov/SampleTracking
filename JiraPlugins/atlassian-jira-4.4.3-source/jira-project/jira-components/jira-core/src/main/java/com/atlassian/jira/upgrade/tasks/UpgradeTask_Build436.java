package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UpgradeTask_Build436 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build436.class);

    final ApplicationProperties applicationProperties;
    static final String OLD_IMAGES_JIRA_LOGO_SMALL_PNG = "/images/jira_logo_small.png";
    static final String OLD_IMAGES_JIRA_LOGO_WIDTH = "111";
    static final String OLD_IMAGES_JIRA_LOGO_HEIGHT = "30";
    static final String NEW_IMAGES_JIRA_LOGO_SMALL_PNG = "/images/jira111x30.png";

    //
    /// These are package level so the test can use them
    static final Map<String, String> OLD_COLORS = new HashMap<String, String>();
    static final Map<String, String> NEW_COLORS = new HashMap<String, String>();


    //Keys to delete
    public static final String JIRA_LF_MENU_HIGHLIGHTCOLOUR = "jira.lf.menu.highlightcolour";
    public static final String JIRA_LF_MENU_TEXTHIGHLIGHTCOLOUR = "jira.lf.menu.texthighlightcolour";
    public static final String JIRA_LF_TOP_MENUINDICATORIMAGE = "jira.lf.top.menuindicatorimage";
    public static final String JIRA_LF_MENU_DROPDOWN_BORDERCOLOR = "jira.lf.menu.dropdown.bordercolour";

    static
    {
        //
        // old colors
        //
        OLD_COLORS.put(APKeys.JIRA_LF_TOP_BGCOLOUR, "#114070");
        OLD_COLORS.put(APKeys.JIRA_LF_TOP_TEXTCOLOUR, "#f0f0f0");
        OLD_COLORS.put(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, "#325c82");
        OLD_COLORS.put(APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR, "#ffffff");
        OLD_COLORS.put(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, "#003366");

        OLD_COLORS.put(APKeys.JIRA_LF_MENU_BGCOLOUR, "#3c78b5");
        OLD_COLORS.put(APKeys.JIRA_LF_MENU_TEXTCOLOUR, "#ffffff");

        //
        // new colors
        // Hard coding these as it will break if we update L&F
        //
        NEW_COLORS.put(APKeys.JIRA_LF_TOP_BGCOLOUR, "#114070");
        NEW_COLORS.put(APKeys.JIRA_LF_TOP_TEXTCOLOUR, "#ffffff");
        NEW_COLORS.put(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, "#325c82");
        NEW_COLORS.put(APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR, "#f0f0f0");
        NEW_COLORS.put(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, "#114070");

        NEW_COLORS.put(APKeys.JIRA_LF_MENU_BGCOLOUR, "#3c78b5");
        NEW_COLORS.put(APKeys.JIRA_LF_MENU_TEXTCOLOUR, "#ffffff");
        NEW_COLORS.put(APKeys.JIRA_LF_MENU_SEPARATOR, "#f0f0f0");
    }

    public UpgradeTask_Build436(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public String getBuildNumber()
    {
        return "436";
    }

    public String getShortDescription()
    {
        return "Migrating the old JIRA UI colors to the new JIRA 4.0 colors";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        boolean hasChanged = false;
        for (final Iterator iterator = OLD_COLORS.entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final String oldKey = (String) entry.getKey();
            final String oldColor = (String) entry.getValue();
            final String currentColor = applicationProperties.getString(oldKey);
            if (currentColor != null && !sameColor(oldColor, currentColor))
            {
                log.info("Custom color detected for " + oldKey + " : " + currentColor);
                hasChanged = true;
                break;
            }
        }
        //
        // so has it changed from the old defaults?
        if (!hasChanged)
        {
            log.info("Upgrading to new JIRA colors as no customisations have been detected");
            for (final Iterator iterator = NEW_COLORS.entrySet().iterator(); iterator.hasNext();)
            {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final String newKey = (String) entry.getKey();
                // Return to default.
                applicationProperties.setString(newKey, null);
            }
        }
        else
        {
            log.info("Customisations to the Look And Feel have been detected.  Not migrating to new scheme.");            
        }
        //
        // this is needed to prevent any CSS caching problems
        final LookAndFeelBean lAndF = LookAndFeelBean.getInstance(applicationProperties);
        long version = lAndF.getVersion();
        lAndF.updateVersion(version++);

        //
        // now what about the logo.  If its the same name, height and width then update it to the new PNG version
        final String currentLogoName = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_URL);
        final String currentLogoWidth = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_WIDTH);
        final String currentLogoHeight = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_HEIGHT);
        if (OLD_IMAGES_JIRA_LOGO_SMALL_PNG.equals(currentLogoName) && OLD_IMAGES_JIRA_LOGO_WIDTH.equals(currentLogoWidth) && OLD_IMAGES_JIRA_LOGO_HEIGHT.equals(currentLogoHeight))
        {
            log.info("Upgrading JIRA Logo to new PNG");
            applicationProperties.setString(APKeys.JIRA_LF_LOGO_URL, NEW_IMAGES_JIRA_LOGO_SMALL_PNG);
        }

        applicationProperties.setString(JIRA_LF_MENU_HIGHLIGHTCOLOUR, null);
        applicationProperties.setString(JIRA_LF_MENU_DROPDOWN_BORDERCOLOR, null);
        applicationProperties.setString(JIRA_LF_TOP_MENUINDICATORIMAGE, null);
        applicationProperties.setString(JIRA_LF_MENU_TEXTHIGHLIGHTCOLOUR, null);

    }

    /**
     * Compares colors for the same value.  It turns out that colors might be missing the # at the front
     * so we do it a  bit mroe smartly
     *
     * @param color1 color to compare
     * @param color2 coor to compare
     * @return true if they are the same
     */
    static boolean sameColor(String color1, String color2)
    {
        color1 = (color1 == null ? "" : color1.trim());
        color2 = (color2 == null ? "" : color2.trim());

        if ((color1.length() > 1) && color1.startsWith("#"))
        {
            color1 = color1.substring(1);
        }
        if ((color2.length() > 1) && color2.startsWith("#"))
        {
            color2 = color2.substring(1);
        }
        return color1.equalsIgnoreCase(color2);
    }
}