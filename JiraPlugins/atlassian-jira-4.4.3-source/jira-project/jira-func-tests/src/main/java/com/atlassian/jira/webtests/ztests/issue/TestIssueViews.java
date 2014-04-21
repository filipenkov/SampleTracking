/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueViews extends JIRAWebTest
{
    public TestIssueViews(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueViews.xml");
    }

    public void testViewLinksChangeForModifiedFilter()
    {
        gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=10000");

        // Check that the links contain the id of the saved filter.
        assertTextPresent("/sr/jira.issueviews:searchrequest-xml/10000/SearchRequest-10000.xml");
        assertTextPresent("/sr/jira.issueviews:searchrequest-rss/10000/SearchRequest-10000.xml");
        assertTextPresent("/sr/jira.issueviews:searchrequest-comments-rss/10000/SearchRequest-10000.xml");
        assertTextPresent("/sr/jira.issueviews:searchrequest-printable/10000/SearchRequest-10000.html");
        assertTextPresent("/sr/jira.issueviews:searchrequest-word/10000/SearchRequest-10000.doc");
        assertTextPresent("/sr/jira.issueviews:searchrequest-fullcontent/10000/SearchRequest-10000.html");
        assertTextPresent("/sr/jira.issueviews:searchrequest-excel-current-fields/10000/SearchRequest-10000.xls");
        // Check that the temporary filter links are not present
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.xml?");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-word/temp/SearchRequest.xml?");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-fullcontent/temp/SearchRequest.xml?");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-excel-current-fields/temp/SearchRequest.xml?");

        // Now lets modify the filter.
        gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=10000&sorter/field=summary&sorter/order=DESC");

        // All the issueview links should change to temporary links
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-xml/10000/SearchRequest-10000.xml");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-rss/10000/SearchRequest-10000.xml");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-comments-rss/10000/SearchRequest-10000.xml");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-printable/10000/SearchRequest-10000.html");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-word/10000/SearchRequest-10000.doc");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-fullcontent/10000/SearchRequest-10000.html");
        assertTextNotPresent("/sr/jira.issueviews:searchrequest-excel-current-fields/10000/SearchRequest-10000.xls");

        // Check that the temp filter links are present
        assertTextPresent("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?");
        assertTextPresent("/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?");
        assertTextPresent("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?");
        assertTextPresent("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.html?");
        assertTextPresent("/sr/jira.issueviews:searchrequest-word/temp/SearchRequest.doc?");
        assertTextPresent("/sr/jira.issueviews:searchrequest-fullcontent/temp/SearchRequest.html?");
        assertTextPresent("/sr/jira.issueviews:searchrequest-excel-current-fields/temp/SearchRequest.xls?");
    }

    public void testPermissionErrorWithGzipEnabled()
    {
        //JRADEV-3406
        getAdministration().generalConfiguration().turnOnGZipCompression();

        getNavigation().issue().viewIssue("HSP-1");
        getNavigation().logout();

        getNavigation().gotoPage("/si/jira.issueviews:issue-html/HSP-1/HSP-1.html");
        assertTextPresent("You must log in to access this page.");
    }

    public void testEnableDisableIssueViewsPlugin()
    {
        displayAllIssues();
        //Test that all links are present
        assertLinkPresent("printable");
        assertLinkPresent("fullContent");
        assertLinkPresent("xml");
        assertLinkPresent("rssIssues");
        assertLinkPresent("rssComments");
        assertLinkPresent("word");
        assertLinkPresent("allExcelFields");
        assertLinkPresent("currentExcelFields");

        // Go through each of the different views and disable them one by one. Check that the
        // appropriate link no longer appears in the issue navigator if disabled
        togglePluginModule(false, "printable");
        displayAllIssues();
        assertLinkNotPresent("printable");
        assertLinkPresent("fullContent");
        assertLinkPresent("xml");
        assertLinkPresent("rssIssues");
        assertLinkPresent("rssComments");
        assertLinkPresent("word");
        assertLinkPresent("allExcelFields");
        assertLinkPresent("currentExcelFields");
        togglePluginModule(true, "printable");

        togglePluginModule(false, "fullcontent");
        displayAllIssues();
        assertLinkPresent("printable");
        assertLinkNotPresent("fullContent");
        assertLinkPresent("xml");
        assertLinkPresent("rssIssues");
        assertLinkPresent("rssComments");
        assertLinkPresent("word");
        assertLinkPresent("allExcelFields");
        assertLinkPresent("currentExcelFields");
        togglePluginModule(true, "fullcontent");

        togglePluginModule(false, "xml");
        displayAllIssues();
        assertLinkPresent("printable");
        assertLinkPresent("fullContent");
        assertLinkNotPresent("xml");
        assertLinkPresent("rssIssues");
        assertLinkPresent("rssComments");
        assertLinkPresent("word");
        assertLinkPresent("allExcelFields");
        assertLinkPresent("currentExcelFields");
        togglePluginModule(true, "xml");

        togglePluginModule(false, "rss");
        displayAllIssues();
        assertLinkPresent("printable");
        assertLinkPresent("fullContent");
        assertLinkPresent("xml");
        assertLinkNotPresent("rssIssues");
        assertLinkPresent("rssComments");
        assertLinkPresent("word");
        assertLinkPresent("allExcelFields");
        assertLinkPresent("currentExcelFields");
        togglePluginModule(true, "rss");

        togglePluginModule(false, "comments-rss");
        displayAllIssues();
        assertLinkPresent("printable");
        assertLinkPresent("fullContent");
        assertLinkPresent("xml");
        assertLinkPresent("rssIssues");
        assertLinkNotPresent("rssComments");
        assertLinkPresent("word");
        assertLinkPresent("allExcelFields");
        assertLinkPresent("currentExcelFields");
        togglePluginModule(true, "comments-rss");

        togglePluginModule(false, "word");
        displayAllIssues();
        assertLinkPresent("printable");
        assertLinkPresent("fullContent");
        assertLinkPresent("xml");
        assertLinkPresent("rssIssues");
        assertLinkPresent("rssComments");
        assertLinkNotPresent("word");
        assertLinkPresent("allExcelFields");
        assertLinkPresent("currentExcelFields");
        togglePluginModule(true, "word");

        togglePluginModule(false, "excel-all-fields");
        displayAllIssues();
        assertLinkPresent("printable");
        assertLinkPresent("fullContent");
        assertLinkPresent("xml");
        assertLinkPresent("rssIssues");
        assertLinkPresent("rssComments");
        assertLinkPresent("word");
        assertLinkNotPresent("allExcelFields");
        assertLinkPresent("currentExcelFields");
        togglePluginModule(true, "excel-all-fields");

        togglePluginModule(false, "excel-current-fields");
        displayAllIssues();
        assertLinkPresent("printable");
        assertLinkPresent("fullContent");
        assertLinkPresent("xml");
        assertLinkPresent("rssIssues");
        assertLinkPresent("rssComments");
        assertLinkPresent("word");
        assertLinkPresent("allExcelFields");
        assertLinkNotPresent("currentExcelFields");
        togglePluginModule(true, "excel-current-fields");


        // Now test special cases such as RSS and Excel, where if both modules are disabled,
        //nothing should be displayed (e.g.: RSS (Issues, Comments) should not be shown if
        // Issues and Comments are disabled)
        togglePluginModule(false, "rss");
        togglePluginModule(false, "comments-rss");
        displayAllIssues();
        assertLinkPresent("printable");
        assertLinkPresent("fullContent");
        assertLinkPresent("xml");
        assertTextNotPresent("| RSS");
        assertLinkNotPresent("rssIssues");
        assertLinkNotPresent("rssComments");
        assertLinkPresent("word");
        assertLinkPresent("allExcelFields");
        assertLinkPresent("currentExcelFields");
        togglePluginModule(true, "rss");
        togglePluginModule(true, "comments-rss");

        togglePluginModule(false, "excel-all-fields");
        togglePluginModule(false, "excel-current-fields");
        displayAllIssues();
        assertLinkPresent("printable");
        assertLinkPresent("fullContent");
        assertLinkPresent("xml");
        assertLinkPresent("rssIssues");
        assertLinkPresent("rssComments");
        assertLinkPresent("word");
        assertTextNotPresent("| Excel");
        assertLinkNotPresent("allExcelFields");
        assertLinkNotPresent("currentExcelFields");
        togglePluginModule(true, "excel-all-fields");
        togglePluginModule(true, "excel-current-fields");
    }

    private void togglePluginModule(boolean enable, String module)
    {
        if (enable)
        {
            administration.plugins().enablePluginModule("jira.issueviews","jira.issueviews:searchrequest-"+module);
        }
        else
        {
            administration.plugins().disablePluginModule("jira.issueviews","jira.issueviews:searchrequest-"+module);
        }
    }
}
