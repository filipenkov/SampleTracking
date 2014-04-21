package com.atlassian.jira.webtest.selenium.popupuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.thoughtworks.selenium.SeleniumException;
import junit.framework.Test;

import java.util.Arrays;

/**
 *
 */
@WebTest({Category.SELENIUM_TEST })
public class TestPopupGroupPicker extends JiraSeleniumTest
{
    public static Test suite()
      {
          return suiteFor(TestPopupGroupPicker.class);
      }

    public void testPickerWithSimpleUsername()
    {
        restoreData("TestPopupGroupPicker.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD).gotoFindIssuesSimple();
        client.select("assigneeSelect", "label=Specify Group");
        client.click("//img[@name='assigneeGroupImage']");
        client.waitForPopUp("GroupPicker", PAGE_LOAD_WAIT);
        client.selectWindow("GroupPicker");
        try {
            client.click("//tr[3]/td[1]/font");
        } catch (SeleniumException se)
        {
            // Possible error but Firefox 1.5.0.12 on OSX always throws this exception here
            // We catch the error and test whether the correct user was selected so
            // ignoring this exception shouldn't matter.
        }
        assertFalse("selecting an icon should close the userpicker window",
                    Arrays.asList(client.getAllWindowNames()).contains("UserPicker"));
        client.selectWindow("null");
        assertEquals("jira-administrators", client.getValue("assignee"));
    }

    public void testPickerWithSingleQuotesInUsername()
    {
        restoreData("TestPopupGroupPicker.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD).gotoFindIssuesSimple();

        client.select("assigneeSelect", "label=Specify User");
        client.click("//img[@name='assigneeGroupImage']");
        client.waitForPopUp("GroupPicker", PAGE_LOAD_WAIT);
        client.selectWindow("GroupPicker");
        try {
            client.click("//tr[2]/td[1]/font");
        } catch (SeleniumException se)
        {
            // Possible error but Firefox 1.5.0.12 on OSX always throws this exception here
            // We catch the error and test whether the correct user was selected so
            // ignoring this exception shouldn't matter.
        }
        assertFalse("selecting an icon should close the userpicker window",
                    Arrays.asList(client.getAllWindowNames()).contains("UserPicker"));
        client.selectWindow("null");
        assertEquals("'); alert('boo!", client.getValue("assignee"));
    }
        
    public void testMultiPickerWithGroupnameFromFirstScreen()
    {
        restoreData("TestMultiGroupPicker.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);
        
        client.click("issue-create-submit", true);

        client.click("//img[@name='assigneeImage']");
        client.waitForPopUp("GroupPicker", PAGE_LOAD_WAIT);
        client.selectWindow("GroupPicker");
        client.click("//input[@id='group_1']");
        try
        {
            client.click("//input[@value='Select'][1]");
        }
        catch (SeleniumException ignored) { }
        
        assertFalse("selecting an icon should close the GroupPicker window",
                    Arrays.asList(client.getAllWindowNames()).contains("GroupPicker"));
        client.selectWindow("null");
        assertEquals("g1", client.getValue("customfield_10090"));
    }

    public void testMultiPickerWithGroupnameFromBothScreens()
    {
        restoreData("TestMultiGroupPicker.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);

        client.click("issue-create-submit", true);

        client.click("//img[@name='assigneeImage']");
        client.waitForPopUp("GroupPicker", PAGE_LOAD_WAIT);
        client.selectWindow("GroupPicker");

        client.select("//select[@name='max']", "10");

        client.click("//input[@value='Filter']");
        client.waitForPageToLoad(PAGE_LOAD_WAIT);
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");

        client.click("//input[@id='group_1']");
        moveToPage(10);

        client.click("//input[@id='group_0']");

        try
        {
            client.click("//input[@value='Select'][1]");
        }
        catch (SeleniumException ignored) { }

        assertFalse("selecting an icon should close the GroupPicker window",
                    Arrays.asList(client.getAllWindowNames()).contains("GroupPicker"));
        client.selectWindow("null");
        assertEquals("g1, jira-administrators", client.getValue("customfield_10090"));
    }

    public void testMultiPickerWithGroupnameUnselect()
    {
        restoreData("TestMultiGroupPicker.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);

        client.click("issue-create-submit", true);

        client.click("//img[@name='assigneeImage']");
        client.waitForPopUp("GroupPicker", PAGE_LOAD_WAIT);
        client.selectWindow("GroupPicker");

        client.select("//select[@name='max']", "10");
        client.click("//input[@value='Filter']");

        client.waitForPageToLoad(PAGE_LOAD_WAIT);
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");

        // select 1 on screen 1
        client.click("//input[@id='group_1']");

        moveToPage(10);

        // select admin on screen 2
        client.click("//input[@id='group_0']");

        moveToPage(0);

        // make sure 1 is still selected, then unselect and select 2
        assertTrue(client.isChecked("//input[@id='group_1']"));
        client.click("//input[@id='group_1']");
        client.click("//input[@id='group_2']");

        moveToPage(10);

        // make sure admin is still selected, then unselect and select bob
        assertTrue(client.isChecked("//input[@id='group_0']"));
        client.click("//input[@id='group_0']");
        client.click("//input[@id='group_1']");

        moveToPage(0);

        // make sure 1 is still unselected, and 2 is still selected
        assertFalse(client.isChecked("//input[@id='group_1']"));
        assertTrue(client.isChecked("//input[@id='group_2']"));

        try
        {
            client.click("//input[@value='Select'][1]");
        }
        catch (SeleniumException ignored) { }

        assertFalse("selecting an icon should close the GroupPicker window",
                    Arrays.asList(client.getAllWindowNames()).contains("GroupPicker"));
        client.selectWindow("null");
        assertEquals("g2, jira-developers", client.getValue("customfield_10090"));
    }

    private void moveToPage(int start)
    {
        client.click("//a[@href='javascript:moveToPage("+start+")'][1]");
        client.waitForPageToLoad(PAGE_LOAD_WAIT);
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
    }
}
