package com.atlassian.jira.webtest.selenium.popupuserpicker;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.harness.util.DirtyFilterHandler;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import com.thoughtworks.selenium.SeleniumException;
import junit.framework.Test;

import static java.util.Arrays.asList;

/**
 *
 */
@SkipInBrowser (browsers={ Browser.IE}) // Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestPopupUserPicker extends JiraSeleniumTest
{
    /**
     * The name of the user picker window.
     */
    private static final String USER_PICKER = "UserPicker";

    public static Test suite()
    {
        return suiteFor(TestPopupUserPicker.class);
    }

    public void testPopupRowTitleXSFR()
    {
        restoreData("TestUserPopupXSRF.xml");

        getNavigator().issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        client.select("assigneeSelect", "label=Specify User");
        client.click("//img[@name='assigneeImage']");
        client.waitForPopUp(USER_PICKER, PAGE_LOAD_WAIT);
        client.selectWindow(USER_PICKER);

        assertThat.attributeContainsValue("Id=username_row_0", "title", "Click to select XSS\"><td><iframe src=\"http://www.google.com\"></iframe></td>");

        client.click("Id=username_row_0");
        client.selectWindow("null");
    }

    public void testPickerWithSimpleUsername()
    {
        restoreData("TestPopupUserPicker.xml");

        getNavigator().issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        client.select("assigneeSelect", "label=Specify User");
        client.click("//img[@name='assigneeImage']");
        client.waitForPopUp(USER_PICKER, PAGE_LOAD_WAIT);
        client.selectWindow(USER_PICKER);
        client.click("Id=username_row_0");
        assertFalse("selecting an icon should close the userpicker window",
                    asList(client.getAllWindowNames()).contains(USER_PICKER));
        client.selectWindow("null");
        assertEquals("admin", client.getValue("assignee"));
    }

    public void testMultiUserPickerWithSquareBracketInUsername()
    {
        restoreData("TestPopupMultiUserPicker.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().editIssue("HSP-1");

        client.click("jquery=img[name='multiuserImage']");
        client.waitForPopUp(USER_PICKER, PAGE_LOAD_WAIT);
        client.selectWindow(USER_PICKER);
        client.click("jquery=input[id='user_1']");
        client.click("jquery=input[type='submit'][value='Select']:first");
        assertFalse("submitting form should close the userpicker window", asList(client.getAllWindowNames()).contains(USER_PICKER));
        client.selectWindow("null");
        assertEquals("admin", client.getValue("customfield_10000"));
    }

    public void testPickerWithSingleQuotesInUsername()
    {
        restoreData("TestPopupUserPicker.xml");

        getNavigator().issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        client.select("assigneeSelect", "label=Specify User");
        client.click("//img[@name='assigneeImage']");
        client.waitForPopUp(USER_PICKER, PAGE_LOAD_WAIT);
        client.selectWindow(USER_PICKER);
        client.click("Id=username_row_2");

        assertFalse("selecting an icon should close the userpicker window",
                    asList(client.getAllWindowNames()).contains(USER_PICKER));
        client.selectWindow("null");
        assertEquals("user'with\"quotes", client.getValue("assignee"));
    }

    public void testMultiPickerWithUsernameFromFirstScreen()
    {
        restoreData("TestMultiUserPicker.xml");

        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);
        
        client.click("Next", true);

        client.click("//img[@name='multiuserImage']");
        client.waitForPopUp(USER_PICKER, PAGE_LOAD_WAIT);
        client.selectWindow(USER_PICKER);
        client.click("//input[@id='user_1']");
        try
        {
            client.click("//input[@value='Select'][1]");
        }
        catch (SeleniumException ignored) { }
        
        assertFalse("selecting an icon should close the userpicker window",
                    asList(client.getAllWindowNames()).contains(USER_PICKER));
        client.selectWindow("null");
        assertEquals("1", client.getValue("customfield_10010"));
    }

    public void testMultiPickerWithUsernameFromBothScreens()
    {
        restoreData("TestMultiUserPicker.xml");

        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);

        client.click("Next", true);

        client.click("//img[@name='multiuserImage']");
        client.waitForPopUp(USER_PICKER, PAGE_LOAD_WAIT);
        client.selectWindow(USER_PICKER);

        client.select("//select[@name='max']", "10");

        client.click("//input[@value='Filter']");
        client.waitForPageToLoad(PAGE_LOAD_WAIT);
        new DirtyFilterHandler(context()).resetDirtyFilter();

        client.click("//input[@id='user_1']");
        moveToPage(10);

        client.click("//input[@id='user_0']");

        try
        {
            client.click("//input[@value='Select'][1]");
        }
        catch (SeleniumException ignored) { }

        assertFalse("selecting an icon should close the userpicker window",
                    asList(client.getAllWindowNames()).contains(USER_PICKER));
        client.selectWindow("null");
        assertEquals("1, admin", client.getValue("customfield_10010"));
    }

    public void testMultiPickerWithUsernameUnselect()
    {
        restoreData("TestMultiUserPicker.xml");

        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);

        client.click("Next", true);

        client.click("//img[@name='multiuserImage']");
        client.waitForPopUp(USER_PICKER, PAGE_LOAD_WAIT);
        client.selectWindow(USER_PICKER);

        client.select("//select[@name='max']", "10");
        client.click("//input[@value='Filter']");

        client.waitForPageToLoad(PAGE_LOAD_WAIT);
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");

        // select 1 on screen 1
        client.click("//input[@id='user_1']");

        moveToPage(10);

        // select admin on screen 2
        client.click("//input[@id='user_0']");

        moveToPage(0);

        // make sure 1 is still selected, then unselect and select 2
        assertTrue(client.isChecked("//input[@id='user_1']"));
        client.click("//input[@id='user_1']");
        client.click("//input[@id='user_2']");

        moveToPage(10);

        // make sure admin is still selected, then unselect and select fred
        assertTrue(client.isChecked("//input[@id='user_0']"));
        client.click("//input[@id='user_0']");
        client.click("//input[@id='user_1']");

        moveToPage(0);

        // make sure 1 is still unselected, and 2 is still selected
        assertFalse(client.isChecked("//input[@id='user_1']"));
        assertTrue(client.isChecked("//input[@id='user_2']"));

        try
        {
            client.click("//input[@value='Select'][1]");
        }
        catch (SeleniumException ignored) { }

        assertFalse("selecting an icon should close the userpicker window",
                    asList(client.getAllWindowNames()).contains(USER_PICKER));
        client.selectWindow("null");
        assertEquals("2, fred", client.getValue("customfield_10010"));
    }


    public void testMultiUserPickerPopup()
    {
        restoreData("TestMultiUserPicker.xml");
        getNavigator().gotoAdmin();
        client.click("project_role_browser", true);
        client.click("manage_Developers", true);
        client.click("edit_Developers_atlassian-group-role-actor", true);

        client.click("//img[@name='assigneeImage']");
        client.waitForPopUp(USER_PICKER, PAGE_LOAD_WAIT);
        client.selectWindow(USER_PICKER);
        client.click("//input[@id='group_0']");
        client.click("//input[@id='group_1']");

        try
        {
            client.click("//input[@value='Select'][1]");
        }
        catch (SeleniumException ignored) { }

        client.selectWindow("null");
            assertEquals("jira-administrators, jira-developers", client.getValue("groupNames"));

    }

    private void moveToPage(int start)
    {
        client.click("//a[@href='javascript:moveToPage("+start+")'][1]");
        client.waitForPageToLoad(PAGE_LOAD_WAIT);
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
    }
}
