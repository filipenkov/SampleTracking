package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.net.URL;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestURLRewriteFilter extends FuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestURLRewriteFilter.xml");
    }

    public void testIssueTabPanelRedirects()
    {
        URL url;

        // All Tab Panel
        tester.gotoPage("browse/TST-1?page=all");
        url = tester.getDialog().getResponse().getURL();
        assertEquals("page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel", url.getQuery());

        // Comments tab panel
        tester.gotoPage("browse/TST-1?page=comments");
        url = tester.getDialog().getResponse().getURL();
        assertEquals("page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel", url.getQuery());

        // History Tab Panel
        tester.gotoPage("browse/TST-1?page=history");
        url = tester.getDialog().getResponse().getURL();
        assertEquals("page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel", url.getQuery());

        // Worklog Tab Panel
        tester.gotoPage("browse/TST-1?page=worklog");
        url = tester.getDialog().getResponse().getURL();
        assertEquals("page=com.atlassian.jira.plugin.system.issuetabpanels:worklog-tabpanel", url.getQuery());

        // Version Control tab panel
        tester.gotoPage("browse/TST-1?page=vcs");
        url = tester.getDialog().getResponse().getURL();
        assertEquals("page=com.atlassian.jira.plugin.system.issuetabpanels:cvs-tabpanel", url.getQuery());
    }
}
