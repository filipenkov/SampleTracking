package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.IOException;

@WebTest ({Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestBrowseProjectCreateIssue extends FuncTestCase
{
    private static final String NEW_COMPONENT_1 = "New Component 1";
    private static final String NEW_VERSION_1 = "New Version 1";
    private static final String VERSIONS = "Versions";
    private static final String CONTENT_HEADER_H1 = "#content > header h1";
    private static final String CREATE_ISSUE_SHORTCUTS = "#create-issue";
    private static final String CREATE_ISSUE_SHORTCUT_ITEM = "#create-issue ul li";

    protected void setUpTest()
    {
        administration.restoreData("testBrowseProjectCreateIssue.xml");
    }

    public void testRespectPermission() throws IOException
    {
        navigation.logout();
        navigation.browseProject("HSP");

        Locator locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertFalse(locator.exists());

        tester.clickLinkWithText(VERSIONS);
        tester.clickLinkWithText(NEW_VERSION_1);

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, NEW_VERSION_1);

        locator = new CssLocator(tester, "#content > header .breadcrumbs");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertFalse(locator.exists());

        navigation.browseProject("HSP");
        tester.clickLinkWithText("Components");
        tester.clickLinkWithText(NEW_COMPONENT_1);

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, NEW_COMPONENT_1);

        locator = new CssLocator(tester, "#content > header .breadcrumbs");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertFalse(locator.exists());

        // shouldn't see create here either
        navigation.browseProject("THREE");

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "3 ISSUE TYPES");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertFalse(locator.exists());

        // should see create here
        navigation.browseProject("TWO");

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "Two Issue Types");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");


        // test for logged in user
        navigation.login("user2");

        navigation.browseProject("HSP");

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "Task", "New Feature", "Other", "Bug", "Improvement");


        tester.clickLinkWithText(VERSIONS);
        tester.clickLinkWithText(NEW_VERSION_1);

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, NEW_VERSION_1);

        locator = new CssLocator(tester, "#content > header .breadcrumbs");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");


        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "Task", "New Feature", "Other", "Bug", "Improvement");


        navigation.browseProject("HSP");
        tester.clickLinkWithText("Components");
        tester.clickLinkWithText(NEW_COMPONENT_1);

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, NEW_COMPONENT_1);

        locator = new CssLocator(tester, "#content > header .breadcrumbs");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");


        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "Task", "New Feature", "Other", "Bug", "Improvement");


        // should see create here
        navigation.browseProject("THREE");

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "3 ISSUE TYPES");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "Bug", "Improvement", "New Feature");


        // should see create here
        navigation.browseProject("TWO");

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "Two Issue Types");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "Bug", "Task");


        // Goto project can't see
        navigation.browseProject("HIDDEN");

        text.assertTextPresent(tester.getDialog().getResponse().getText(), "It seems that you have tried to perform an operation which you are not permitted to perform.");

        // develeopr permission
        navigation.login(FRED_USERNAME);

        navigation.browseProject("HSP");

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "New Feature", "Task", "Other", "Bug", "Improvement");


        tester.clickLinkWithText(VERSIONS);
        tester.clickLinkWithText(NEW_VERSION_1);

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, NEW_VERSION_1);

        locator = new CssLocator(tester, "#content > header .breadcrumbs");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "New Feature", "Task", "Other", "Bug", "Improvement");


        navigation.browseProject("HSP");
        tester.clickLinkWithText("Components");
        tester.clickLinkWithText(NEW_COMPONENT_1);

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, NEW_COMPONENT_1);

        locator = new CssLocator(tester, "#content > header .breadcrumbs");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "New Feature", "Task", "Other", "Bug", "Improvement");

        // should see create here
        navigation.browseProject("THREE");

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "3 ISSUE TYPES");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "Bug", "Improvement", "New Feature");


        // should see create here
        navigation.browseProject("TWO");

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "Two Issue Types");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "Bug", "Task");


        // Goto project can't see
        navigation.browseProject("HIDDEN");

        locator = new CssLocator(tester, CONTENT_HEADER_H1);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "No can see");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertFalse(locator.exists());

    }


    public void testCustomCreateButtons()
    {
        navigation.logout();
        navigation.browseProject("HSP");

        CssLocator locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertFalse(locator.exists());


        navigation.login(FRED_USERNAME);
        navigation.browseProject("HSP");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "New Feature", "Task", "Other", "Bug", "Improvement");


        navigation.login("user2");
        navigation.browseProject("HSP");
        
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "Task", "New Feature", "Other", "Bug", "Improvement");


        navigation.login(ADMIN_USERNAME);
        navigation.browseProject("HSP");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "New Feature", "Bug", "Other", "Task", "Improvement");


        navigation.login("user2");
        navigation.issue().createIssue("homosapien", "Improvement", "My first improvement");
        navigation.browseProject("HSP");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "Improvement", "Task", "Other", "Bug", "New Feature");


        navigation.login(FRED_USERNAME);
        navigation.browseProject("HSP");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "Improvement", "New Feature", "Other", "Bug", "Task");

    }

    public void testDifferentNumberOfTypes()
    {
        navigation.login(ADMIN_USERNAME);
        navigation.browseProject("HSP");

        CssLocator locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Other");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        // Make sure they are all in the correct order
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        text.assertTextSequence(locator, "New Feature", "Bug", "Other", "Task", "Improvement");


        navigation.browseProject("ONE");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        locator = new CssLocator(tester, "#create-issue ul li + li");
        assertFalse(locator.exists());


        navigation.browseProject("TWO");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Task");
        locator = new CssLocator(tester, "#create-issue ul li + li + li");
        assertFalse(locator.exists());


        navigation.browseProject("THREE");

        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUTS);
        assertTrue(locator.exists());
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Bug");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "Improvement");
        locator = new CssLocator(tester, CREATE_ISSUE_SHORTCUT_ITEM);
        text.assertTextPresent(locator, "New Feature");
        text.assertTextNotPresent(locator, "Other");
        locator = new CssLocator(tester, "#create-issue ul li + li + li + li");
        assertFalse(locator.exists());
        locator = new CssLocator(tester, "#create-issue ul li + li + li ul");
        assertFalse(locator.exists());

    }
}
