package com.atlassian.jira.webtest.webdriver.tests.admin.generalconfiguration;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.model.JiraMode;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.gadgets.LoginGadget;
import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.jira.webtests.Groups;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * When the config property 'Contact Admin form' is OFF, links to contact JIRA Administrator should not
 * be rendered.
 *
 * @since v5.1
 */
@RestoreOnce ("xml/blankprojects.xml")
@WebTest({Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.SECURITY})
public class TestContactAdminLinks extends BaseJiraWebTest
{

    private static final String USER_FORBIDDEN = "forbidden";
    private static final User POOR_GUY = new User(USER_FORBIDDEN, USER_FORBIDDEN, "forbidden@atlassian.com", USER_FORBIDDEN);

    @Inject private WebDriver webDriver;

    @BeforeClass
    public static void createUserWithNoLoginRights()
    {
        backdoor.mailServers().addSmtpServer(1);
        backdoor.usersAndGroups().addUserEvenIfUserExists(USER_FORBIDDEN).removeUserFromGroup(USER_FORBIDDEN, Groups.USERS);
    }

    @After
    public void resetLoginCount()
    {
        backdoor.usersAndGroups().resetLoginCount(USER_FORBIDDEN);
    }

    @After
    public void backToMainWindow()
    {
        webDriver.switchTo().defaultContent();
    }

    @Test
    public void shouldNotRenderLinksOnPublicModeOffMessageOnLoginGadgetIfAdminFormIsOff()
    {
        backdoor.generalConfiguration().setJiraMode(JiraMode.PRIVATE).setContactAdminFormOff();
        LoginGadget loginGadget = jira.logout().gotoHomePage().gadgets().getLoginGadget();
        assertFalse(loginGadget.getPublicModeOffMessage().find(By.tagName("a")).isPresent());
    }

    @Test
    public void shouldNotRenderLinksOnLoginFailedMessageOnLoginGadgetIfAdminFormIsOff()
    {
        backdoor.generalConfiguration().setContactAdminFormOff();
        LoginGadget loginGadget = jira.logout().gotoHomePage().gadgets().getLoginGadget();
        loginGadget = loginGadget.login(POOR_GUY).gadgets().getLoginGadget();
        assertTrue(loginGadget.hasErrors());
        assertFalse(loginGadget.getError().find(By.tagName("a")).isPresent());
    }

    @Test
    public void shouldRenderLinkOnPublicModeOffMessageOnLoginGadgetIfAdminFormIsOn()
    {
        backdoor.generalConfiguration().setJiraMode(JiraMode.PRIVATE).setContactAdminFormOn();
        LoginGadget loginGadget = jira.logout().gotoHomePage().gadgets().getLoginGadget();
        assertTrue(loginGadget.getPublicModeOffMessage().find(By.tagName("a")).isPresent());
    }

    @Test
    public void shouldRenderLinkOnLoginFailedMessageOnLoginGadgetIfAdminFormIsOn()
    {
        backdoor.generalConfiguration().setContactAdminFormOn();
        LoginGadget loginGadget = jira.logout().gotoHomePage().gadgets().getLoginGadget();
        loginGadget = loginGadget.login(POOR_GUY).gadgets().getLoginGadget();
        assertTrue(loginGadget.hasErrors());
        assertTrue(loginGadget.getError().find(By.tagName("a")).isPresent());
    }


    @Test
    public void shouldNotRenderLinksOnPublicModeOffMessageOnLoginPageIfAdminFormIsOff()
    {
        backdoor.generalConfiguration().setJiraMode(JiraMode.PRIVATE).setContactAdminFormOff();
        JiraLoginPage loginPage = jira.logout().gotoLoginPage();
        assertFalse(loginPage.getSingUpHint().find(By.tagName("a")).isPresent());
    }

    @Test
    public void shouldNotRenderLinkOnLoginFailedMessageOnLoginPageIfAdminFormIsOff()
    {
        backdoor.generalConfiguration().setContactAdminFormOff();
        JiraLoginPage loginPage = jira.logout().gotoLoginPage().login(POOR_GUY, JiraLoginPage.class);
        assertTrue(loginPage.hasErrors());
        assertFalse(loginPage.getError().find(By.tagName("a")).isPresent());
    }

    @Test
    public void shouldRenderLinkOnPublicModeOffMessageOnLoginPageIfAdminFormIsOn()
    {
        backdoor.generalConfiguration().setJiraMode(JiraMode.PRIVATE).setContactAdminFormOn();
        JiraLoginPage loginPage = jira.logout().gotoLoginPage();
        assertTrue(loginPage.getSingUpHint().find(By.tagName("a")).isPresent());
    }

    @Test
    public void shouldRenderLinkOnLoginFailedMessageOnLoginPageIfAdminFormIsOn()
    {
        backdoor.generalConfiguration().setContactAdminFormOn();
        JiraLoginPage loginPage = jira.logout().gotoLoginPage().login(POOR_GUY, JiraLoginPage.class);
        assertTrue(loginPage.hasErrors());
        assertTrue(loginPage.getError().find(By.tagName("a")).isPresent());
    }


}
