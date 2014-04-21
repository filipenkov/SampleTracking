package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestBrowseProjectSummaryScreen extends FuncTestCase
{

    protected void setUpTest()
    {
        administration.restoreData("TestBrowseProjectSummaryTab.xml");
    }


    public void testProjectDescriptionFrag()
    {
        navigation.login(ADMIN_USERNAME);
        navigation.browseProject("NODESC");
        assertNodeExists("//div[@id='fragprojectdescription']");

        assertNodeHasText("//div[@id='fragprojectdescription']//h3", "Description");

        text.assertTextPresent(new IdLocator(tester, "pd-url"), "http://www.atlassian.com");
        text.assertTextPresent(new IdLocator(tester, "project_summary_admin"), ADMIN_FULLNAME);
        text.assertTextPresent(new IdLocator(tester, "pd-key"), "NODESC");
        assertEquals(0, (new IdLocator(tester, "pd-desc").getNodes().length));

        navigation.browseProject("NOURL");
        assertNodeExists("//div[@id='fragprojectdescription']");

        assertNodeHasText("//div[@id='fragprojectdescription']//h3", "Description");
        text.assertTextPresent(new IdLocator(tester, "project_summary_admin"), ADMIN_FULLNAME);
        text.assertTextPresent(new IdLocator(tester, "pd-key"), "NOURL");
        text.assertTextPresent(new IdLocator(tester, "pd-desc"), "This is a description");
    }

    public void testDueIssuesFrag()
    {
        navigation.login(ADMIN_USERNAME);
        navigation.browseProject("NODESC");

        XPathLocator locator = new XPathLocator(tester, "//div[@id='fragdueissues']");
        assertFalse(locator.exists());

        navigation.issue().createIssue("No Description", "Bug", "Summary 1");
        navigation.issue().createIssue("No Description", "Bug", "Summary 2");
        navigation.issue().createIssue("No Description", "Bug", "Summary 3");
        navigation.issue().createIssue("No Description", "Bug", "Summary 4");
        navigation.issue().createIssue("No Description", "Bug", "Summary 5");

        resolveIssue("NODESC-1");
        
        navigation.browseProject("NODESC");

        assertNodeExists("//div[@id='fragdueissues']");

        assertNodeHasText("//div[@id='fragdueissues']//h3", "Issues: Unresolved");
        assertNodeHasText("//div[@id='fragdueissues']//ul/li[1]", "NODESC-2");
        assertNodeHasText("//div[@id='fragdueissues']//ul/li[2]", "NODESC-3");
        assertNodeHasText("//div[@id='fragdueissues']//ul/li[3]", "NODESC-4");

        navigation.issue().gotoEditIssue("NODESC-5");
        tester.setFormElement("duedate", "9/Feb/09");
        tester.submit("Update");

        navigation.browseProject("NODESC");

        assertNodeExists("//div[@id='fragdueissues']");

        assertNodeHasText("//div[@id='fragdueissues']//h3", "Issues: Unresolved");
        assertNodeHasText("//div[@id='fragdueissues']//ul/li[1]", "NODESC-5");
        assertNodeHasText("//div[@id='fragdueissues']//ul/li[2]", "NODESC-2");
        assertNodeHasText("//div[@id='fragdueissues']//ul/li[3]", "NODESC-3");

        navigation.issue().gotoEditIssue("NODESC-4");
        tester.setFormElement("priority", "1");
        tester.submit("Update");

        navigation.browseProject("NODESC");

        assertNodeExists("//div[@id='fragdueissues']");

        assertNodeHasText("//div[@id='fragdueissues']//h3", "Issues: Unresolved");
        assertNodeHasText("//div[@id='fragdueissues']//ul/li[1]", "NODESC-5");
        assertNodeHasText("//div[@id='fragdueissues']//ul/li[2]", "NODESC-4");
        assertNodeHasText("//div[@id='fragdueissues']//ul/li[3]", "NODESC-2");

        resolveIssue("NODESC-2");
        resolveIssue("NODESC-3");
        resolveIssue("NODESC-4");
        resolveIssue("NODESC-5");

        navigation.browseProject("DUEDATEHIDDEN");
        navigation.browseProject("NODESC");

        assertNodeDoesNotExists("//div[@id='fragdueissues']");

        assertNodeDoesNotExists("//div[@id='fragdueissues']");

    }

    private void resolveIssue(String key)
    {
        navigation.issue().viewIssue(key);
        tester.clickLinkWithText("Resolve Issue");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");
    }

    public void testCreatedVsResolvedFrag()
    {
        navigation.login(ADMIN_USERNAME);
        navigation.browseProject("NODESC");
        assertNodeExists("//div[@id='fragcreatedvsresolved']");

        XPathLocator locator = new XPathLocator(tester, "//div[@id='fragcreatedvsresolved']/div");
        assertTrue(locator.exists());
        text.assertTextSequence(locator, "Issues:", "0", "created and", "0", "resolved");
        
        navigation.issue().createIssue("No Description", "Bug", "Summary 1");
        navigation.issue().createIssue("No Description", "Bug", "Summary 2");
        navigation.issue().createIssue("No Description", "Bug", "Summary 3");
        navigation.issue().createIssue("No Description", "Bug", "Summary 4");

        resolveIssue("NODESC-1");

        navigation.browseProject("NODESC");
        assertNodeExists("//div[@id='fragcreatedvsresolved']");

        locator = new XPathLocator(tester, "//div[@id='fragcreatedvsresolved']/div");
        assertTrue(locator.exists());
        text.assertTextSequence(locator, "Issues:", "4", "created and", "1", "resolved");
        
    }

    public void assertNodeHasText(final String xpath, final String textToTest)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, textToTest);
    }

    public void assertNodeDoesNotHaveText(final String xpath, final String textToTest)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        assertTrue(locator.exists());
        text.assertTextNotPresent(locator, textToTest);
    }

    public void assertNodeExists(final String xpath)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        assertTrue(locator.exists());
    }
    public void assertNodeDoesNotExists(final String xpath)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        assertFalse(locator.exists());
    }
}