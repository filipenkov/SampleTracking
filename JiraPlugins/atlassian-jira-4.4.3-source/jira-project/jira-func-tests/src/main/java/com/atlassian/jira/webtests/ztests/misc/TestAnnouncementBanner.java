package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;

/**
 * Tests the functionality of the announcement banner
 * 
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestAnnouncementBanner extends JIRAWebTest
{
    private static final String ANNOUNCEMENT = "<p>JIRA 3.4 IS NOW INSTALLED " +
            "<a href=\"http://confluence.atlassian.com/display/JIRA/JIRA+3.4+and+3.4.1+Release%20Notes\"" +
            " target=\"_blank\">Review Release Notes Here</a></p>";

    public TestAnnouncementBanner(String name)
    {
        super(name);
    }

    public void testAnnouncementBanner()
    {
        try
        {
            setBannerText(ANNOUNCEMENT);
        }
        finally
        {
            clearBannerText();
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testAnnouncementBannerDoesNotShowInPrivateModeWithNoUser()
    {
        try
        {
            setBannerText(ANNOUNCEMENT);
            setBannerDisplayMode(false);
            logout();
            beginAt("/secure/Dashboard.jspa");
            assertTextNotPresent(ANNOUNCEMENT);
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
            clearBannerText();
        }
    }

    public void testAnnouncementBannerShownInPublicModeWithNoUser()
    {
        try
        {
            setBannerText(ANNOUNCEMENT);
            setBannerDisplayMode(true);
            logout();
            beginAt("/secure/Dashboard.jspa");
            assertTextPresent(ANNOUNCEMENT);
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
            clearBannerText();
        }
    }

    private void setJIRAModePublic(boolean publicMode)
    {
        gotoAdmin();
        clickLink("general_configuration");
        clickLinkWithText("Edit Configuration");
        selectOption("mode", (publicMode) ? "Public" : "Private");
        submit();
    }

    // Set the banner mode to be public or private
    protected void setBannerDisplayMode(boolean isPublic)
    {
        gotoAdmin();
        clickLink("edit_announcement");
        if (isPublic)
            checkCheckbox("bannerVisibility", "public");
        else
            checkCheckbox("bannerVisibility", "private");
        submit("Set Banner");
    }

    protected void clearBannerText()
    {
        // clear the banner text
        gotoBannerPage();
        setFormElement("announcement", "");
        submit();
    }

    protected void setBannerText(String bannerText)
    {
        HttpUnitOptions.setScriptingEnabled(true);
        gotoBannerPage();
        setFormElement("announcement", bannerText);
        submit();
        assertTextPresent(bannerText);
    }

    // NOTE: to use the preview link this method should be called with javascript turned on
    protected void gotoBannerPage()
    {
        gotoAdmin();
        clickLink("edit_announcement");
    }

    // This does not work because of the javascript used to set the preview value
//    public void testAnnouncementBannerPreview()
//    {
//        try
//        {
//            HttpUnitOptions.setScriptingEnabled(true);
//            gotoBannerPage();
//            setFormElement("announcement", ANNOUNCEMENT);
//            clickButton("previewButton");
//            assertTextPresent(ANNOUNCEMENT_DISPLAY);
//        }
//        finally
//        {
//            HttpUnitOptions.setScriptingEnabled(false);
//        }
//    }

}
