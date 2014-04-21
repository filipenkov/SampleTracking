package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;

import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class TestUpgradeTask_Build322 extends LegacyJiraMockTestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }


    final static Map OLD_COLORS = UpgradeTask_Build322.OLD_COLORS;
    final static Map NEW_COLORS = UpgradeTask_Build322.NEW_COLORS;

    public void testColorCompare() throws Exception
    {
        assertTrue(UpgradeTask_Build322.sameColor(null, null));
        assertTrue(UpgradeTask_Build322.sameColor("", ""));
        assertTrue(UpgradeTask_Build322.sameColor(" ", "\t"));
        assertTrue(UpgradeTask_Build322.sameColor("#ff00cc", "#ff00cc"));
        assertTrue(UpgradeTask_Build322.sameColor("#ff00cc", "ff00cc"));
        assertTrue(UpgradeTask_Build322.sameColor("#ff00cc", "ff00cc"));
        assertTrue(UpgradeTask_Build322.sameColor("ff00cc", "ff00cc"));
        assertTrue(UpgradeTask_Build322.sameColor("ff00cc", "#ff00cc"));
        assertTrue(UpgradeTask_Build322.sameColor("ff00cc ", "#ff00cc"));
        assertTrue(UpgradeTask_Build322.sameColor("ff00cc ", " #ff00cc"));

        assertFalse(UpgradeTask_Build322.sameColor("#fff", "#ffffff"));
        assertFalse(UpgradeTask_Build322.sameColor("ff#", "#ff"));
        assertFalse(UpgradeTask_Build322.sameColor(null, "#ff"));
        assertFalse(UpgradeTask_Build322.sameColor("", "#ff"));
        assertFalse(UpgradeTask_Build322.sameColor("", "#f f"));
        assertFalse(UpgradeTask_Build322.sameColor("", "#f f "));
        assertFalse(UpgradeTask_Build322.sameColor("#fc fc", "#fcfc"));
    }

    public void testHasCustomColors() throws Exception
    {
        ApplicationProperties ap = new MockApplicationProperties();
        for (Iterator iterator = OLD_COLORS.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            final String colorKey = (String) entry.getKey();
            ap.setString(colorKey, "#123456");
        }

        UpgradeTask_Build322 upgradeTask = new UpgradeTask_Build322(ap);
        upgradeTask.doUpgrade(false);

        assertFalse("The custom colors have not been detected", hasSameColors(ap, NEW_COLORS));

        //
        // now make sure the menu separator is the same color as the top background
        assertEquals(ap.getDefaultBackedString(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR), ap.getDefaultBackedString(APKeys.JIRA_LF_TOP_BGCOLOUR));
    }

    public void testHasCustomColorsButStangelyHasTheNewTopBackgroundColor() throws Exception
    {
        ApplicationProperties ap = new MockApplicationProperties();
        for (Iterator iterator = OLD_COLORS.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            final String colorKey = (String) entry.getKey();
            ap.setString(colorKey, "#123456");
        }
        ap.setString(APKeys.JIRA_LF_TOP_BGCOLOUR, String.valueOf(NEW_COLORS.get(APKeys.JIRA_LF_TOP_BGCOLOUR)));

        UpgradeTask_Build322 upgradeTask = new UpgradeTask_Build322(ap);
        upgradeTask.doUpgrade(false);

        assertFalse("The custom colors have not been detected", hasSameColors(ap, NEW_COLORS));

        //
        // now make sure the menu separator is NOT changed as they have the target top background color
        assertNull(ap.getDefaultBackedString(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR));
    }

    public void testHasOldColors() throws Exception
    {
        ApplicationProperties ap = new MockApplicationProperties();
        for (Iterator iterator = OLD_COLORS.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            final String colorKey = (String) entry.getKey();
            final String colorValue = (String) entry.getValue();
            ap.setString(colorKey, colorValue);
        }

        UpgradeTask_Build322 upgradeTask = new UpgradeTask_Build322(ap);
        upgradeTask.doUpgrade(false);

        assertTrue("The old default values have not been upgraded", hasSameColors(ap, NEW_COLORS));
        //
        // now make sure the menu separator has not changed at all
        assertNull(ap.getDefaultBackedString(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR));
    }

    public void testHasCustomLogo() throws Exception
    {
        for (int i = 0; i < 3; i++)
        {
            String expectedLogoURL = UpgradeTask_Build322.OLD_IMAGES_JIRA_LOGO_SMALL_GIF;
            String expectedLogoHeight = UpgradeTask_Build322.OLD_IMAGES_JIRA_LOGO_HEIGHT;
            String expectedLogoWidth = UpgradeTask_Build322.OLD_IMAGES_JIRA_LOGO_WIDTH;

            if (i == 0)
            {
                expectedLogoURL = "/a/new/image/here.gif";
            }
            else if (i == 1)
            {
                expectedLogoHeight = "456";
            }
            else
            {
                expectedLogoWidth = "87823";
            }

            ApplicationProperties ap = new MockApplicationProperties();
            ap.setString(APKeys.JIRA_LF_LOGO_URL, expectedLogoURL);
            ap.setString(APKeys.JIRA_LF_LOGO_HEIGHT, expectedLogoHeight);
            ap.setString(APKeys.JIRA_LF_LOGO_WIDTH, expectedLogoWidth);

            UpgradeTask_Build322 upgradeTask = new UpgradeTask_Build322(ap);
            upgradeTask.doUpgrade(false);
            assertEquals(ap.getDefaultBackedString(APKeys.JIRA_LF_LOGO_URL), expectedLogoURL);
            assertEquals(ap.getDefaultBackedString(APKeys.JIRA_LF_LOGO_HEIGHT), expectedLogoHeight);
            assertEquals(ap.getDefaultBackedString(APKeys.JIRA_LF_LOGO_WIDTH), expectedLogoWidth);
        }
    }

    /**
     * Uses the actual Application Properties
     *
     * @throws Exception
     */
    public void testHasDefaultLogo() throws Exception
    {
        ApplicationProperties ap = ComponentAccessor.getApplicationProperties();

        UpgradeTask_Build322 upgradeTask = new UpgradeTask_Build322(ap);
        upgradeTask.doUpgrade(false);
        assertEquals(ap.getDefaultBackedString(APKeys.JIRA_LF_LOGO_URL), UpgradeTask_Build322.NEW_IMAGES_JIRA_LOGO_SMALL_PNG);
        assertEquals(ap.getDefaultBackedString(APKeys.JIRA_LF_LOGO_HEIGHT), UpgradeTask_Build322.OLD_IMAGES_JIRA_LOGO_HEIGHT);
        assertEquals(ap.getDefaultBackedString(APKeys.JIRA_LF_LOGO_WIDTH), UpgradeTask_Build322.OLD_IMAGES_JIRA_LOGO_WIDTH);
    }

    private boolean hasSameColors(ApplicationProperties ap, Map mapOfColors)
    {
        boolean allSame = true;
        for (Iterator iterator = mapOfColors.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            final String colorKey = (String) entry.getKey();
            final String colorValue = (String) entry.getValue();
            if (!colorValue.equalsIgnoreCase(ap.getDefaultBackedString(colorKey)))
            {
                allSame = false;
                break;
            }
        }
        return allSame;
    }
}