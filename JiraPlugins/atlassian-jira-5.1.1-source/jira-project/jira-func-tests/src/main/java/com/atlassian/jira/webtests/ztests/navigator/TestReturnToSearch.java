package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestReturnToSearch extends JIRAWebTest
{

    public TestReturnToSearch(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestReturnToSearch.xml");
    }

    public void testReturnToSearchNotPresentIfSearchRequestInvalid()
    {
        log("Testing 'Return to serach' box is not present on view issue if no results returned by previous search");
        clickLink("find_link");
        setFormElement("query", "TST-999]"); // notice closing ']' - makes it invalid
        submit("show");
        navigation.issue().viewIssue("TST-1");

        assertLinkNotPresent("return-to-search");
    }

    public void testReturnToSearchNotPresentIfNoSearchResults()
    {
        log("Testing 'Return to serach' box is not present on view issue if no results returned by previous search");
        clickLink("find_link");
        setFormElement("query", "TST-999");
        submit("show");
        navigation.issue().viewIssue("TST-1");
        // without issue count
        assertLinkNotPresent("return-to-search");
    }

    public void testReturnToSearchPresentIfSearchResultsExist()
    {
        log("Testing 'Return to serach' box is present on view issue if results returned by previous search exist");
        clickLink("find_link");
        selectOption("type", "Bug");
        submit("show");

        // box present
        // not position and count displayed - issue is not in results of the previous search
        navigation.issue().viewIssue("TST-3");
        // without issue count
        assertLinkNotPresent("return-to-search");

        // box present with issue position and count
        // issue is one of the results of the previous search
        navigation.issue().viewIssue("TST-2");
        // without issue count
        assertLinkPresent("return-to-search");
    }
//
//    public void testManageAttachmentsReturnToSearch()
//    {
//        log("Testing 'Return to Search' box in Manage Attachments");
//
//        enableAttachments();
//
//        attachFile("TST-2");
//
//        clickLink("find_link");
//        selectOption("type", "Any");
//        submit("show");
//        clickLinkWithText("TST-2");
//
//        //Assert return to search box present on view issue screen
//        assertLinkPresent("return-to-search-icon");
//        assertLinkPresent("return-to-search");
//        assertLinkPresent("previous-issue");
//        assertLinkPresent("next-issue");
//
//        //Assert return to search box present on manage attachments screen
//        clickLinkWithText("File Attachments");
//
//        // without issue count
//        assertLinkPresent("return-to-search-icon");
//        assertLinkPresent("return-to-search");
//        assertLinkPresent("previous-issue");
//        assertLinkPresent("next-issue");
//    }


    public void testReturnToSearchNavigation()
    {
        clickLink("find_link");
        selectOption("type", "Any");
        submit("show");

        clickLinkWithText("TST-2");

        assertLinkPresent("return-to-search");
        assertLinkPresent("previous-issue");
        assertLinkPresent("next-issue");

        clickLink("previous-issue");

        assertLinkPresent("return-to-search");
        assertLinkNotPresent("previous-issue");
        assertLinkPresent("next-issue");

        clickLink("next-issue");
        clickLink("next-issue");

        assertLinkPresent("return-to-search");
        assertLinkPresent("previous-issue");
        assertLinkNotPresent("next-issue");
    }

}
