package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertionsImpl;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.PROJECTS })
public class TestViewProject extends JIRAWebTest
{
    public TestViewProject(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
    }

    public void testViewProjectUrlEncoding()
    {
        restoreData("TestViewProject.xml");
        
        setUnsafeUrl("http://<b>hi</b>\">");

        // assert that the url gets HTML encoded and the anchor text
        assertUrlEncoded();

        tester.clickLink("view_projects");

        // assert that the url gets HTML encoded and the anchor text
        assertUrlEncoded();

        tester.gotoPage("secure/BrowseProjects.jspa");
        assertUrlEncoded();

        tester.gotoPage("browse/HSP");
        assertUrlEncoded();

        tester.gotoPage("browse/HSP#selectedTab=com.atlassian.jira.plugin.system.project%3Asummary-panel");
        assertUrlEncoded();
    }

    private void setUnsafeUrl(final String url)
    {
        gotoAdmin();
        // set the project url to be a dodgy one
        tester.gotoPage("/plugins/servlet/project-config/" + "HSP" + "/summary");

        tester.clickLink("edit_project");
        tester.setFormElement("name", "homosapien");
        tester.setFormElement("url", url);
        tester.submit("Update");
    }

    private void assertUrlEncoded()
    {
        assertTextPresent("http://&lt;b&gt;hi&lt;/b&gt;&quot;&gt;");
    }

}
