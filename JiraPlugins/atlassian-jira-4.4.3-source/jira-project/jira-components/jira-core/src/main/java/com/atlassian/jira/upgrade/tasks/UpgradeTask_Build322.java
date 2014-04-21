package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This upgrade task will migrate instances to the new JRA-14349 color scheme.
 * <p/>
 * It does this ONLY IF the customers current colors ARE exactly the same
 * as was shipped in JIRA 3.12.
 * <p/>
 * If there are any differences, then the colors are left alone.
 * <p/>
 * Also the default LOGO is changed the new PNG logo if it still
 * has the same name and the same height and width.
 */
public class UpgradeTask_Build322 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build322.class);

    final ApplicationProperties applicationProperties;
    static final String OLD_IMAGES_JIRA_LOGO_SMALL_GIF = "/images/jira_logo_small.gif";
    static final String OLD_IMAGES_JIRA_LOGO_WIDTH = "111";
    static final String OLD_IMAGES_JIRA_LOGO_HEIGHT = "30";
    static final String NEW_IMAGES_JIRA_LOGO_SMALL_PNG = "/images/jira_logo_small.png";

    //
    /// These are package level so the test can use them
    static final Map<String, String> OLD_COLORS = new HashMap<String, String>();
    static final Map<String, String> NEW_COLORS = new HashMap<String, String>();

    static
    {
        //
        // old colors
        //
        OLD_COLORS.put(APKeys.JIRA_LF_TOP_BGCOLOUR, "#003366");
        OLD_COLORS.put(APKeys.JIRA_LF_TOP_TEXTCOLOUR, "#ffffff");

        OLD_COLORS.put(APKeys.JIRA_LF_MENU_BGCOLOUR, "#3c78b5");
        OLD_COLORS.put(APKeys.JIRA_LF_MENU_TEXTCOLOUR, "#ffffff");

        //
        // new colors
        //
        NEW_COLORS.put(APKeys.JIRA_LF_TOP_BGCOLOUR, "#114070");
        NEW_COLORS.put(APKeys.JIRA_LF_TOP_TEXTCOLOUR, "#f0f0f0");

        NEW_COLORS.put(APKeys.JIRA_LF_MENU_BGCOLOUR, "#3c78b5");
        NEW_COLORS.put(APKeys.JIRA_LF_MENU_TEXTCOLOUR, "#ffffff");
    }

    public UpgradeTask_Build322(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public String getBuildNumber()
    {
        return "322";
    }

    public String getShortDescription()
    {
        return "Migrating the old JIRA UI colors to the new JIRA 3.13 colors";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        boolean hasChanged = false;
        for (Map.Entry<String, String> entry : OLD_COLORS.entrySet())
        {
            final String oldKey = entry.getKey();
            final String oldColor = entry.getValue();
            final String currentColor = applicationProperties.getDefaultBackedString(oldKey);
            if (!sameColor(oldColor, currentColor))
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
            for (Map.Entry<String, String> entry : NEW_COLORS.entrySet())
            {
                final String newKey = entry.getKey();
                final String newColor = entry.getValue();
                applicationProperties.setString(newKey, newColor);
            }
        }
        else
        {
            // but since they have changed the old colors we want to "migrate" the menu separator color
            // to be there top background color so it less "car accident" like. UNLESS they already
            // have the target top color (fluke or a empty instance) in which case we dont want to change it
            final String topBackground = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_TOP_BGCOLOUR);
            final String newTopBackground = String.valueOf(NEW_COLORS.get(APKeys.JIRA_LF_TOP_BGCOLOUR));
            if (!sameColor(newTopBackground, topBackground))
            {
                applicationProperties.setString(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, topBackground);
            }
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
        if (OLD_IMAGES_JIRA_LOGO_SMALL_GIF.equals(currentLogoName) && OLD_IMAGES_JIRA_LOGO_WIDTH.equals(currentLogoWidth) && OLD_IMAGES_JIRA_LOGO_HEIGHT.equals(currentLogoHeight))
        {
            log.info("Upgrading JIRA Logo to new PNG format");
            applicationProperties.setString(APKeys.JIRA_LF_LOGO_URL, NEW_IMAGES_JIRA_LOGO_SMALL_PNG);
        }
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