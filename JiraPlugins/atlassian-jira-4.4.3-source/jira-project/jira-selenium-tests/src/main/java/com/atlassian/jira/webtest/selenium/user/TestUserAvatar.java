package com.atlassian.jira.webtest.selenium.user;

import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.jira.webtest.selenium.framework.components.IssueNavResults;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import com.atlassian.jira.webtest.selenium.framework.pages.IssueNavigator;
import com.atlassian.selenium.Browser;
import junit.framework.Test;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Quarantine
@WebTest({Category.SELENIUM_TEST })
public class TestUserAvatar extends JiraSeleniumTest
{
    public static final int USER_HOVER_WAIT = 15000;

    private static final String TMP_DIR_PATH = System.getProperty("java.io.tmpdir");
    private static final int UPLOAD_TIMEOUT = 60000;

    private static final String CUSTOM_AVATAR_FILE_FIELD = "imageFile";
    private static final String INVALID_IMAGE_FILE_NAME = "testfile.jpg";
    private static final int INVALID_IMAGE_FILE_SIZE = 2048;

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestUserAvatars.xml");
    }

     public static Test suite()
    {
        return suiteFor(TestUserAvatar.class);
    }

    public void testAll()
    {
        _testHoverDoesntDisableKeyboardShortcuts();
        _testEditUserChooseSystemAvatar();
        _testAllTab();
        _testTabNavigation();
        _testEditPrivileges();
        _testUserHoverAppears();
        _testUserHover();
        _testDeleteAvatar();
        _testUploadAvatar();

        // IE does not allow you to close a popup
        if (client.getBrowser() != Browser.IE) {
            //this test should come last
            _testRedirectIfLoggedOut();
        }
    }

    private void _testUserHoverAppears()
    {
        getNavigator().login(ADMIN_USERNAME);
        getNavigator().gotoPage("/browse/FOO-1?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel", true);

        assertHoverVisible("id=commentauthor_10000_verbose", "admin_user_hover");
        //going back to the page here because Selenium is a piece of shit and I can't use a sensible selector by class for
        //these hovers!!!!
        getNavigator().gotoPage("/browse/FOO-1?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel", true);
        assertHoverVisible("id=changehistoryauthor_10000", "admin_user_hover");
        getNavigator().gotoPage("/browse/FOO-1?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel", true);
        assertHoverVisible("id=worklogauthor_10000", "admin_user_hover");
        getNavigator().gotoPage("/browse/FOO-1?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel", true);
        assertHoverVisible("id=commentauthor_10011_verbose", "brad_user_hover");

        getNavigator().gotoBrowseProject("Foobar");
        assertHoverVisible("id=project_summary_admin", "admin_user_hover");
        client.click("issues-panel-panel");
        assertThat.visibleByTimeout("id=unresolved_assignee_brad", PAGE_LOAD_WAIT_TIME);
        assertHoverVisible("id=unresolved_assignee_brad", "brad_user_hover");

        getNavigator().findIssuesWithJql("assignee = brad");
        client.click("viewfilter", true);
        assertHoverVisible("id=searcher-profile-link", "brad_user_hover");
    }

    private void _testHoverDoesntDisableKeyboardShortcuts()
    {
        final IssueNavigator issueNavigator = new IssueNavigator(context());
        IssueNavResults searchResults = issueNavigator.goTo().findAll().results();
        searchResults.assertIssueSelectedWith("FOO-3");
        searchResults.down();
        searchResults.assertIssueSelectedWith("FOO-2");

        //now display a user hover
        Mouse.mouseover(client, "id=reporter_admin");
        visibleByTimeoutWithDelay("admin_user_hover", USER_HOVER_WAIT);
        searchResults.assertIssueSelectedWith("FOO-2");
        searchResults.down();
        searchResults.assertIssueSelectedWith("FOO-1");
    }

    private void assertHoverVisible(String trigger, String hoverId)
    {
        assertThat.elementNotVisible(hoverId);
        Mouse.mouseover(client, trigger);
        visibleByTimeoutWithDelay(hoverId, USER_HOVER_WAIT);
        client.mouseOut(trigger);
        assertThat.notVisibleByTimeout(hoverId, USER_HOVER_WAIT);
    }

    public void _testUserHover()
    {
        getNavigator().login(ADMIN_USERNAME);
        getNavigator().gotoIssue("FOO-1");
        assertThat.elementNotVisible("admin_user_hover");
        Mouse.mouseover(client, "id=commentauthor_10000_verbose");
        assertThat.visibleByTimeout("admin_user_hover", USER_HOVER_WAIT);
        client.mouseOut("id=commentauthor_10000_verbose");
        assertThat.notVisibleByTimeout("admin_user_hover", USER_HOVER_WAIT);

        Mouse.mouseover(client, "id=commentauthor_10000_verbose");
        assertThat.visibleByTimeout("admin_user_hover", USER_HOVER_WAIT);
        //assert all the links are present
        assertThat.elementVisible("uh_view_activity");
        assertThat.elementVisible("user-hover-more-trigger");
        assertThat.elementNotVisible("uh_view_profile");
        assertThat.elementNotVisible("uh_view_current_issues");
        assertThat.elementNotVisible("uh_view_administrate");
        //now click the dropdown
        client.click("id=user-hover-more-trigger", false);
        waitFor(DROP_DOWN_WAIT);
        //check the dialog's still there
        assertThat.elementVisible("admin_user_hover");
        assertThat.elementVisible("uh_view_activity");
        assertThat.elementVisible("user-hover-more-trigger");
        assertThat.elementVisible("uh_view_profile");
        assertThat.elementVisible("uh_view_current_issues");
        assertThat.elementVisible("uh_view_administrate");

        //now try a non-admin user and make sure the administrate link is not shown
        getNavigator().logout(getXsrfToken());
        getNavigator().login("fred");
        getNavigator().gotoIssue("FOO-1");
        client.mouseOver("id=commentauthor_10000_verbose");
        assertThat.visibleByTimeout("admin_user_hover", USER_HOVER_WAIT);
        assertThat.elementVisible("uh_view_activity");
        assertThat.elementVisible("user-hover-more-trigger");
        assertThat.elementNotVisible("uh_view_profile");
        assertThat.elementNotVisible("uh_view_current_issues");
        assertThat.elementNotVisible("uh_view_administrate");

        //now click the dropdown
        client.click("id=user-hover-more-trigger", false);
        waitFor(DROP_DOWN_WAIT);
        //check the dialog's still there
        assertThat.elementVisible("admin_user_hover");
        assertThat.elementVisible("uh_view_activity");
        assertThat.elementVisible("user-hover-more-trigger");
        assertThat.elementVisible("uh_view_profile");
        assertThat.elementVisible("uh_view_current_issues");
        assertThat.elementNotVisible("uh_view_administrate");

        getNavigator().logout(getXsrfToken());
        getNavigator().login(ADMIN_USERNAME);

        //finally disable all items except for two and make sure the dropdown isn't shown in the user hover!
        getAdministration().disablePluginModule("27246", "User Hover Current Issues link");
        getAdministration().disablePluginModule("27246", "User Hover Administer this user link");
        getNavigator().gotoIssue("FOO-1");
        client.mouseOver("id=commentauthor_10000_verbose");
        assertThat.visibleByTimeout("admin_user_hover", USER_HOVER_WAIT);
        assertThat.elementVisible("uh_view_activity");
        assertThat.elementVisible("uh_view_profile");
        assertThat.elementNotVisible("user-hover-more-trigger");
        assertThat.elementNotVisible("uh_view_current_issues");
        assertThat.elementNotVisible("uh_view_administrate");
    }

    public void testEditAvatarAffectsBrowseIssue()
    {
        getNavigator().gotoIssue("FOO-1");

        //this is an admin avatar
        assertAvatarBackgroundPresent("commentauthor_10000_verbose", "/secure/useravatar?size=small&ownerId=admin&avatarId=10110");
        assertAvatarBackgroundPresent("commentauthor_10011_verbose", "/secure/useravatar?size=small&ownerId=brad&avatarId=10070");

        //now change admin's avatar
        getNavigator().gotoUserProfile("admin");
        client.click("id=user_avatar_image");
        assertThat.visibleByTimeout("id=avatar-dialog", USER_HOVER_WAIT);
        client.click("10122");
        waitFor(2000);
        assertAvatarPresent("user_avatar_image", "/secure/useravatar?size=large&avatarId=10122");

        //and check the view issue page got updated correctly
        getNavigator().gotoIssue("FOO-1");
        assertAvatarBackgroundPresent("commentauthor_10000_verbose", "/secure/useravatar?size=small&avatarId=10122");
        assertAvatarBackgroundPresent("commentauthor_10011_verbose", "/secure/useravatar?size=small&ownerId=brad&avatarId=10070");

        //also check anonymous users show up with anonymous avatar!
        assertAvatarBackgroundPresentByJQuery("#comment-10030 span.user-avatar", "/secure/useravatar?size=small&avatarId=10143");
    }

    public void _testEditPrivileges()
    {
        //as admin I can edit all avatars!
        getNavigator().gotoUserProfile("admin");
        openDialog();
        getNavigator().gotoUserProfile("brad");
        openDialog();
        getNavigator().gotoUserProfile("fred");
        openDialog();

        //as brad I can only edit brad
        getNavigator().login("brad", "brad");
        getNavigator().gotoUserProfile("brad");
        openDialog();
        getNavigator().gotoUserProfile("fred");
        assertThat.elementNotPresent("id=user_avatar_link");
        getNavigator().gotoUserProfile("admin");
        assertThat.elementNotPresent("id=user_avatar_link");
    }

    public void _testEditUserChooseSystemAvatar()
    {
        getNavigator().gotoUserProfile();
        assertAvatarPresent("user_avatar_image", "/secure/useravatar?size=large&ownerId=admin&avatarId=10110");

        client.click("id=user_avatar_image");
        assertThat.visibleByTimeout("id=avatar-dialog", USER_HOVER_WAIT);
        client.click("10122");
        waitFor(2000);
        assertAvatarPresent("user_avatar_image", "/secure/useravatar?size=large&avatarId=10122");

        //reload the page and ensure we still have the new avatar!
        getNavigator().gotoUserProfile();
        assertAvatarPresent("user_avatar_image", "/secure/useravatar?size=large&ownerId=admin&avatarId=10122");
    }

    private void assertAvatarPresent(final String id, final String avatarUrl)
    {
        assertThat.elementPresentByTimeout("id=" + id);
        final String avatarSrc = client.getAttribute("id=" + id + "@src");
        assertTrue("Avatar element ID < " + id + "> source <" + avatarSrc + "> expected to end with <" + avatarUrl + "> but did not",
                avatarSrc.endsWith(avatarUrl));
    }

    private void assertAvatarBackgroundPresent(final String id, final String avatarUrl)
    {
        assertThat.elementPresent("id=" + id);
        final String avatarSrc = client.getAttribute("id=" + id + "@style");
        // selenium 2.0 returns 'style' attribute value in lower case: http://code.google.com/p/selenium/issues/detail?id=1089
        assertTrue("avatarSrc \"" + avatarSrc + "\" does not contain expected avatarUrl \"" + avatarUrl + "\"",
                avatarSrc.contains(avatarUrl.toLowerCase()));
    }

    private void assertAvatarBackgroundPresentByJQuery(final String jQuery, final String avatarUrl)
    {
        assertThat.elementPresent("jQuery=" + jQuery);
        final String avatarSrc = client.getAttribute("jQuery=" + jQuery + "@style");
        // selenium 2.0 returns 'style' attribute value in lower case: http://code.google.com/p/selenium/issues/detail?id=1089
        assertTrue("avatarSrc \"" + avatarSrc + "\" does not contain expected avatarUrl \"" + avatarUrl + "\"",
                avatarSrc.contains(avatarUrl.toLowerCase()));
    }

    public void _testRedirectIfLoggedOut()
    {
        getNavigator().gotoUserProfile();
        final String atlToken = getXsrfToken();
        Window.openAndSelect(client, client.getLocation(), "LogOutWindow");
        getNavigator().gotoPage("logout?" + XsrfCheck.ATL_TOKEN + "=" + atlToken, true);
        Window.close(client, "LogOutWindow");
        waitFor(1000);
        client.click("user_avatar_link", true);
        assertThat.elementNotPresentByTimeout("avatar-dialog", 5000);
        assertThat.textPresent("You must log in to access this page");
    }

    public void _testTabNavigation()
    {
        getNavigator().gotoUserProfile();
        openDialog();
        assertIsTab("All");
        client.click("//ul[@class='dialog-page-menu']/li[1]", false);
        assertIsTab("Custom");
        client.click("//ul[@class='dialog-page-menu']/li[2]", false);
        assertIsTab("Built-in");
        getTotalAvatarsForTab();
        client.click("//h2/button[@class='close']", false);
        assertThat.elementNotVisible("avatar-dialog");
        openDialog();
    }

    public void _testAllTab()
    {
        getNavigator().gotoUserProfile();
        openDialog();
        client.click("//ul[@class='dialog-page-menu']/li[2]/button", false);
        Integer customCount = getTotalAvatarsForTab();
        client.click("//ul[@class='dialog-page-menu']/li[3]/button", false);
        Integer builtinCount = getTotalAvatarsForTab();
        client.click("//ul[@class='dialog-page-menu']/li[1]/button", false);
        Integer allCount = getTotalAvatarsForTab();
        if ((builtinCount + customCount) != allCount) {
           throw new RuntimeException("Expected All tab to contain the sum of built-in avatars and custom avatars");
        }
    }

    public void _testDeleteAvatar()
    {
        // changed to jQuery to try and improve reliability
        // there may also be multiple delete icons so select first
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoUserProfile();
        openDialog();
        String avatarSrc = client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\".dialog-panel-body.avatar-all img\").attr('src')");
        client.click("jquery=.dialog-panel-body.avatar-all a.del:first", false);
        assertThat.elementPresentByTimeout("jquery=.dialog-panel-body.avatar-all .module.message", 50000);
        assertThat.textPresent("Confirm that you would like to delete this avatar.");
        client.click("jquery=.dialog-panel-body.avatar-all :button.cancel", false);
        assertThat.elementNotPresentByTimeout("jquery=.dialog-panel-body.avatar-all div.module.message", 50000);
        client.click("jquery=.dialog-panel-body.avatar-all a.del:first", false);
        assertThat.elementPresentByTimeout("jquery=.dialog-panel-body.avatar-all :submit:first", 50000);
        client.click("jquery=.dialog-panel-body.avatar-all :submit:first", false);
        assertThat.elementNotPresentByTimeout("jquery=.dialog-panel-body.avatar-all .module.message", 50000);
        assertThat.elementNotPresentByTimeout("jquery=.dialog-panel-body.avatar-all li.avatar.custom img[src='" + avatarSrc + "']", 50000);
        assertThat.elementNotPresentByTimeout("jquery=.dialog-panel-body.avatar-all div.dialog-panel-body.avatar-uploaded li.avatar custom img[src='" + avatarSrc + "']", 50000);

        // check that message dissapears when navigating away

        // todo: Add this back in

//        client.click("jquery=.dialog-panel-body.avatar-all a.del:first", false);
//        assertThat.elementPresentByTimeout("jquery=.dialog-panel-body.avatar-all div.module.message", 50000);
//        client.click("jquery=.dialog-page-menu li button:last", false);
//        assertThat.elementNotPresentByTimeout("jquery=.dialog-panel-body.avatar-all .module.message", 50000);
    }

    private void _testUploadAvatar() {

        getNavigator().gotoUserProfile();
        openDialog();

        final File tempfile = getTempfile(INVALID_IMAGE_FILE_NAME, INVALID_IMAGE_FILE_SIZE);
        client.type(CUSTOM_AVATAR_FILE_FIELD, tempfile.getAbsolutePath());
        assertThat.elementPresentByTimeout("jquery=div.error p:contains(The image you uploaded could not be read correctly. Please check that it is not corrupted.)", UPLOAD_TIMEOUT);
    }

    private File getTempfile(final String fileName, final int size)
    {
        File file = new File(fileName);
        //if the file doesn't already exist, create a bogus file!
        if (!file.exists())
        {
            //create a temp file first!
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++)
            {
                data[i] = 'x';
            }
            try
            {
                file = new File(TMP_DIR_PATH + File.separator + fileName);
                FileUtils.writeByteArrayToFile(file, data);
                file.deleteOnExit();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

        }
        return file;
    }

    private void openDialog()
    {
        client.click("user_avatar_link", false);
        assertThat.elementPresentByTimeout("avatar-dialog", DROP_DOWN_WAIT);
        assertThat.elementVisible("avatar-dialog");
    }

    private Integer getTotalAvatarsForTab()
    {
        return Integer.parseInt(client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\"#avatar-dialog .dialog-panel-body:visible li:has(img)\").length"));
    }

    private Boolean assertIsTab(String tabName)
    {
        return tabName.equals(client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\".dialog-page-menu li.selected button\").text()"));
    }
}
