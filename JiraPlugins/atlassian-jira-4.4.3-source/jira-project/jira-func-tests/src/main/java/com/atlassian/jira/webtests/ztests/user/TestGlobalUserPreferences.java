package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.util.TimeBomb;

/**
 * Test case to verify that the user counts are correct when updating global user preferences.
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestGlobalUserPreferences extends JIRAWebTest
{
    // TODO: fix the test and remove the Time Bomb
    private static final TimeBomb TIME_BOMB = new TimeBomb("15/5/2010");

    public TestGlobalUserPreferences(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        // TODO: fix the test and remove the Time Bomb
        if (TIME_BOMB.runTest())
        {
            restoreData("TestGlobalUserPreferences.xml");
        }
    }

    //Update everyone to html
    public void testUpdateEmailMIMETypeToHTML()
    {
        // TODO: fix the test and remove the Time Bomb
        if (TIME_BOMB.ignoreTest())
        {
            log("Ignoring TestGlobalUserPreferences temporarily.");
            return;
        }

        gotoAdmin();
        //update the users to HTML
        clickLink("user_defaults");
        assertTextPresent("html");
        clickLinkWithText("Apply");

        assertTextPresent("receive 'text' email to receive 'html' email instead");
        assertTextPresent("A total of 3 users");
        submit("Update");

        //check that the users were updated.
        clickLinkWithText("Apply");
        assertTextPresent("A total of 0 users");
    }

    public void testUpdateEmailMIMETypeToText()
    {
        // TODO: fix the test and remove the Time Bomb
        if (TIME_BOMB.ignoreTest())
        {
            log("Ignoring TestGlobalUserPreferences temporarily.");
            return;
        }

        gotoAdmin();
        //update the users to HTML
        clickLink("user_defaults");
        clickLinkWithText("Edit default values");
        selectOption("preference", "text");
        submit("Update");

        assertTextPresent("text");
        clickLinkWithText("Apply");

        assertTextPresent("receive 'html' email to receive 'text' email instead");
        assertTextPresent("A total of 1 users");
        submit("Update");

        //check that the users were updated.
        clickLinkWithText("Apply");
        assertTextPresent("A total of 0 users");
    }
}
