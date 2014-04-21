package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.junit.Ignore;

@Ignore ("Needs to be reincarnated as a Selenium test. See JRADEV-3654.")
@WebTest ({ Category.FUNC_TEST, Category.COMPONENTS_AND_VERSIONS, Category.BROWSING })
public class TestBrowseVersionsAndComponentsPaging extends JIRAWebTest
{
    public TestBrowseVersionsAndComponentsPaging(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestBrowseVersionsAndComponentsPaging.xml");
    }

    public void testBrowseComponentOpenIssuesOverLimit()
    {
        gotoComponentBrowse(PROJECT_HOMOSAP_KEY, "New Component 1");
        clickLinkWithText("Open Issues");
        assertTextSequence(new String[]{
                "Viewing 50 of", "58", " unresolved issue(s).",
                "Viewing 50 of", "58", "Issues."});
        //check the first element is the issue w/ highest priority.
        assertTableCellHasText("openissues_table", 0, 2, "HSP-20");

        //let's check the links to the IssueNavigator
        clickLink("unresolved_link");
        assertTextSequence(new String[]{"Displaying issues ", "1", "to", "50", "of", "58", "matching issues."});
        //check the first element is still the issue w/ highest priority.
        assertTableCellHasText("issuetable", 1, 1, "HSP-20");

        gotoComponentBrowse(PROJECT_HOMOSAP_KEY, "New Component 1");
        clickLinkWithText("Open Issues");
        clickLink("pager_footer_link");
        assertTextSequence(new String[]{"Displaying issues ", "1", "to", "50", "of", "58", "matching issues."});
        //check the first element is still the issue w/ highest priority.
        assertTableCellHasText("issuetable", 1, 1, "HSP-20");
    }

    public void testBrowseComponentOpenIssuesUnderLimit()
    {
        gotoComponentBrowse(PROJECT_HOMOSAP_KEY, "New Component 2");
        clickLinkWithText("Open Issues");
        assertTextNotPresent("Viewing ");
        //check the first element is the issue w/ highest priority.
        assertTableCellHasText("openissues_table", 0, 2, "HSP-61");

        //let's check the links to the IssueNavigator
        clickLink("unresolved_link");
        assertTextSequence(new String[]{"Displaying issues ", "1", "to", "1", "of", "1", "matching issues."});
        //check the first element is still the issue w/ highest priority.
        assertTableCellHasText("issuetable", 1, 1, "HSP-61");

        assertLinkNotPresent("pager_footer_link");
    }

    public void testBrowseVersionSummaryOverLimit()
    {
        gotoVersionBrowse(PROJECT_HOMOSAP_KEY, "New Version 1");
        clickLinkWithText("Summary");
        assertTextSequence(new String[] {"Progress:", "2", "of", "60", "issues have been resolved"});
        assertTextSequence(new String[]{"Viewing 50 of", "60", "Issues."});
        //check the first element is the issue w/ highest priority.
        assertTableCellHasText("summary_table", 0, 2, "HSP-20");
        clickLink("pager_footer_link");
        assertTextSequence(new String[]{"Displaying issues ", "1", "to", "50", "of", "60", "matching issues."});
        //check the first element is still the issue w/ highest priority.
        assertTableCellHasText("issuetable", 1, 1, "HSP-20");

        gotoVersionBrowse(PROJECT_HOMOSAP_KEY, "New Version 1");
        clickLinkWithText("Summary");
        clickLink("unresolved_link");
        assertTextSequence(new String[] {"Progress:", "2", "of", "60", "issues have been resolved"});
        clickLink("pager_footer_link");
        assertTextSequence(new String[]{"Displaying issues ", "1", "to", "50", "of", "58", "matching issues."});
        //check the first element is still the issue w/ highest priority.
        assertTableCellHasText("issuetable", 1, 1, "HSP-20");

    }

    public void testBrowseVersionSummaryUnderLimit()
    {
        gotoVersionBrowse(PROJECT_HOMOSAP_KEY, "New Version 4");
        clickLinkWithText("Summary");
        assertTextNotPresent("Viewing ");
        //check the first element is the issue w/ highest priority.
        assertTableCellHasText("summary_table", 0, 2, "HSP-61");

        //let's check the links to the IssueNavigator
        clickLink("unresolved_link");
        //check the first element is still the issue w/ highest priority.
        assertTableCellHasText("summary_table", 0, 2, "HSP-61");

        assertLinkNotPresent("pager_footer_link");
    }

}