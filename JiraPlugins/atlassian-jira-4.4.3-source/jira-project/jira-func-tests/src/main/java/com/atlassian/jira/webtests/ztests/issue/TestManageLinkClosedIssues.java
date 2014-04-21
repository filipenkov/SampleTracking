package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestManageLinkClosedIssues extends FuncTestCase
{
    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestManageLinksClosedIssues.xml");
    }

    public void testManageLinkClosedIssue() throws SAXException
    {
        navigation.issue().gotoIssue("HSP-4");
        assertEquals("resolution", tester.getDialog().getResponse().getLinkWith("HSP-5").getClassName());
    }

    public void testManageLinkNonClosedIssue() throws SAXException
    {
        navigation.issue().gotoIssue("HSP-5");
        assertNotSame("resolution", tester.getDialog().getResponse().getLinkWith("HSP-4").getClassName());
        text.assertTextPresent(locator.page(), "HSP-4");
    }
}
