package com.atlassian.jira.webtest.selenium.admin.avatarpicker;

import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

/**
 * Tests the panel system in the avatar picker.
 *
 * @since v4.0
 */

@SkipInBrowser(browsers={ Browser.IE})
@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestAvatarPicker extends JiraSeleniumTest
{
    public static Test suite()
    {
        return suiteFor(TestAvatarPicker.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("avatar.xml");
        getNavigator().gotoPage("/plugins/servlet/project-config/HSP", true);
    }

    public void testRedirectIfLoggedOut()
    {

        client.click("edit_project");
        assertThat.elementPresentByTimeout("css=#project-config-project-edit-dialog.aui-dialog-content-ready");

        final String xsrfToken = getXsrfToken();
        // This may fail in IE if the Popup blocker is still on.
        Window.openAndSelect(client, client.getLocation(), "LogOutWindow");

        getNavigator().gotoPage("logout?" + XsrfCheck.ATL_TOKEN + "=" + xsrfToken, true);
        Window.close(client, "LogOutWindow");
        client.click("project_avatar_image", false);
        assertThat.elementNotPresentByTimeout("avatar-dialog", 5000);
        client.waitForPageToLoad(5000);
        assertThat.textPresent("You must log in to access this page");
    }

    public void testTabNavigation()
    {
        openDialog();
        assertIsTab("All");
        client.click("//ul[@class='dialog-page-menu']/li[1]", false);
        assertIsTab("Custom");
        client.click("//ul[@class='dialog-page-menu']/li[2]", false);
        assertIsTab("Built-in");
        getTotalAvatarsForTab();
    }

    public void testAllTab()
    {
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

    public void testDeleteAvatar()
    {
        openDialog();
        assertThat.elementPresent("jquery=img[src*='avatarId=10140']");
        client.click("jquery=.avatar-all .avatar:last .del");
        assertThat.elementPresentByTimeout("jquery=.avatar-all .message", 3000);
        assertThat.elementHasText("jquery=.avatar-all .message", "Confirm that you would like to delete this avatar");
        client.click("jquery=.avatar-all .message :submit");
        assertThat.elementNotPresentByTimeout("jquery=#avatar-dialog img[src*='avatarId=10140']", 50000);
    }

    public void testSelectAvatar() throws InterruptedException
    {
        openDialog();
        client.click("//div[@class='dialog-panel-body avatar-all']//li");
        assertThat.elementNotPresentByTimeout("jquery=#avatar-dialog.aui-dialog-open");
        Thread.sleep(200);
        client.click("project-edit-submit", true);
        assertThat.attributeContainsValue("id=project-config-header-avatar", "src", "/jira/secure/projectavatar?pid=10000&avatarId=10110");
    }

    private void openDialog()
    {
        client.click("edit_project");
        assertThat.elementPresentByTimeout("css=#project-config-project-edit-dialog.aui-dialog-content-ready");
        client.click("project_avatar_image", false);
        assertThat.elementPresentByTimeout("avatar-dialog", 5000);
        assertThat.elementVisible("avatar-dialog");
        assertThat.elementPresentByTimeout("avatar-dialog", 5000);
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
