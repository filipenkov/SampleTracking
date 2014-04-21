package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

@WebTest ({Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestBrowseProjectCreateIssue extends FuncTestCase
{
    private static final String NEW_COMPONENT_1 = "New Component 1";
    private static final String NEW_VERSION_1 = "New Version 1";
    private static final String VERSIONS = "Versions";

    protected void setUpTest()
    {
        administration.restoreData("testBrowseProjectCreateIssue.xml");
    }

    public void testRespectPermission() throws IOException
    {
        navigation.logout();
        navigation.browseProject("HSP");

        XPathLocator locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertFalse(locator.exists());

        tester.clickLinkWithText(VERSIONS);
        tester.clickLinkWithText(NEW_VERSION_1);

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");
        text.assertTextPresent(locator, NEW_VERSION_1);

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        assertTrue(StringUtils.isBlank(locator.getText()));

        navigation.browseProject("HSP");
        tester.clickLinkWithText("Components");
        tester.clickLinkWithText(NEW_COMPONENT_1);

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");
        text.assertTextPresent(locator, NEW_COMPONENT_1);

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertFalse(locator.exists());

        // shouldn't see create here either
        navigation.browseProject("THREE");

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "3 ISSUE TYPES");

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertFalse(locator.exists());

        // should see create here
        navigation.browseProject("TWO");

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Bug");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Task");


        // test for logged in user
        navigation.login("user2");

        navigation.browseProject("HSP");

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Task");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "New Feature");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Bug", "Improvement");


        tester.clickLinkWithText(VERSIONS);
        tester.clickLinkWithText(NEW_VERSION_1);

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");
        text.assertTextPresent(locator, NEW_VERSION_1);

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Task");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "New Feature");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Bug", "Improvement");


        navigation.browseProject("HSP");
        tester.clickLinkWithText("Components");
        tester.clickLinkWithText(NEW_COMPONENT_1);

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");
        text.assertTextPresent(locator, NEW_COMPONENT_1);

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Task");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "New Feature");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Bug", "Improvement");


        // should see create here
        navigation.browseProject("THREE");

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "3 ISSUE TYPES");

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Bug");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Improvement");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextPresent(locator, "New Feature");


        // should see create here
        navigation.browseProject("TWO");

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        text.assertTextPresent(locator, "Two Issue Types");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Bug");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Task");


        // Goto project can't see
        navigation.browseProject("HIDDEN");

        text.assertTextPresent(tester.getDialog().getResponse().getText(), "It seems that you have tried to perform an operation which you are not permitted to perform.");

        // develeopr permission
        navigation.login(FRED_USERNAME);

        navigation.browseProject("HSP");

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "New Feature");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Task");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Bug", "Improvement");


        tester.clickLinkWithText(VERSIONS);
        tester.clickLinkWithText(NEW_VERSION_1);

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");
        text.assertTextPresent(locator, NEW_VERSION_1);

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "New Feature");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Task");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Bug", "Improvement");


        navigation.browseProject("HSP");
        tester.clickLinkWithText("Components");
        tester.clickLinkWithText(NEW_COMPONENT_1);

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "homosapien");
        text.assertTextPresent(locator, NEW_COMPONENT_1);

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "New Feature");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Task");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Bug", "Improvement");


        // should see create here
        navigation.browseProject("THREE");

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        assertTrue(locator.exists());
        text.assertTextPresent(locator, "3 ISSUE TYPES");

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Bug");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Improvement");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextPresent(locator, "New Feature");


        // should see create here
        navigation.browseProject("TWO");

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        text.assertTextPresent(locator, "Two Issue Types");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Bug");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Task");


        // Goto project can't see
        navigation.browseProject("HIDDEN");

        locator = new XPathLocator(tester, "//div[@id='content-top']");
        text.assertTextPresent(locator, "No can see");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertFalse(locator.exists());

    }


    public void testCustomCreateButtons()
    {
        navigation.logout();
        navigation.browseProject("HSP");

        XPathLocator locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertFalse(locator.exists());


        navigation.login(FRED_USERNAME);
        navigation.browseProject("HSP");
        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "New Feature");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Task");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Bug", "Improvement");


        navigation.login("user2");
        navigation.browseProject("HSP");
        
        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Task");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "New Feature");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Bug", "Improvement");

        navigation.login(ADMIN_USERNAME);
        navigation.browseProject("HSP");

        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "New Feature");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Bug");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Task", "Improvement");


        navigation.login("user2");
        navigation.issue().createIssue("homosapien", "Improvement", "My first improvement");

        navigation.browseProject("HSP");
        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Improvement");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Task");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Bug", "New Feature");

        navigation.login(FRED_USERNAME);
        navigation.browseProject("HSP");
        locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Improvement");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "New Feature");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Bug", "Task");


    }

    public void testDifferentNumberOfTypes()
    {
        navigation.login(ADMIN_USERNAME);
        navigation.browseProject("HSP");

        XPathLocator locator = new XPathLocator(tester, "//div[@id='create-issue']");
        assertTrue(locator.exists());

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "New Feature");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Bug");

        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextSequence(locator, "Other", "Task", "Improvement");

        navigation.browseProject("ONE");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Improvement");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        assertFalse(locator.exists());
        
        navigation.browseProject("TWO");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Bug");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Task");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        assertFalse(locator.exists());

        navigation.browseProject("THREE");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[1]");
        text.assertTextPresent(locator, "Bug");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[2]");
        text.assertTextPresent(locator, "Improvement");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[3]");
        text.assertTextPresent(locator, "New Feature");
        text.assertTextNotPresent(locator, "Other");
        locator = new XPathLocator(tester, "//div[@id='create-issue']/ul/li[4]");
        assertFalse(locator.exists());

    }
}
