package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestNavigatorPages extends JIRAWebTest
{
    private static final String VOTED_ISSUES_TITLE = "Voted Issues";
    private static final String WATCHED_ISSUES_TITLE = "Watched Issues";
    private static final String TEST_FILTER_ID = "10000";

    public TestNavigatorPages(String name)
    {
        super(name);
    }

    private Collection issues = new ArrayList();
    private static final int numberOfIssues = 30;
    private static final int maximumIssuesPerPage = 10;
    private static final int halfmaximumIssuesPerPage = 5;

    private static final String NEXT = "Next";
    private static final String PREVIOUS = "Previous";
    private static final String TEST_FILTER = "All issues";

    protected void setMaximumIssuesPerPage(int maxIssues)
    {
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("edit_prefs_lnk");
        tester.setFormElement("userIssuesPerPage", Integer.toString(maxIssues));
        tester.submit();
        text.assertTextPresent(new IdLocator(tester, "up-p-pagesize"), "10");
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("TestNavigatorPages.xml");
        getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
    }

    @Override
    public void tearDown()
    {
        getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        super.tearDown();
    }

    public void testNavigatorPages()
    {
        issues.addAll(createIssuesInBulk(numberOfIssues, PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test issue navigator paging",
                "Minor", null, null, null, BOB_FULLNAME, "test environment 1", "test description 1 for test issue paging", null, null));
        navigatorPagesIssueNavigator();
    }

    public void navigatorPagesIssueNavigator()
    {
        log("Checking paging for issue navigator");

        setMaximumIssuesPerPage(maximumIssuesPerPage);
        navigation.issueNavigator().displayAllIssues();
        sortIssues("issuekey", "ASC");
        checkIssuesInPages(issues, 1, maximumIssuesPerPage);
        tester.clickLink("page_" + 2);
        checkIssuesInPages(issues, 2, maximumIssuesPerPage);
        tester.clickLinkWithText(NEXT);
        checkIssuesInPages(issues, 3, maximumIssuesPerPage);
        tester.clickLinkWithText(PREVIOUS);
        checkIssuesInPages(issues, 2, maximumIssuesPerPage);

        saveFilter(TEST_FILTER, "");
    }

    public void navigatorPagesYourVotesTable()
    {
        log("Checking paging for 'Your Votes'");
        try
        {
            navigation.logout();
            navigation.login(BOB_USERNAME, BOB_PASSWORD);
            setMaximumIssuesPerPage(halfmaximumIssuesPerPage);
            Collection votedIssues = new ArrayList();
            votedIssues.addAll(voteForIssues(issues));
            navigation.userProfile().gotoCurrentUserProfile();
            clickLinkWithText("Your Votes");

            checkIssuesInPages(VOTED_ISSUES_TITLE, votedIssues, 1, halfmaximumIssuesPerPage);

            tester.clickLink("page_" + 2);
            checkIssuesInPages(VOTED_ISSUES_TITLE, votedIssues, 2, halfmaximumIssuesPerPage);

            tester.clickLinkWithText(NEXT);
            checkIssuesInPages(VOTED_ISSUES_TITLE, votedIssues, 3, halfmaximumIssuesPerPage);

            tester.clickLinkWithText(PREVIOUS);
            checkIssuesInPages(VOTED_ISSUES_TITLE, votedIssues, 2, halfmaximumIssuesPerPage);
        }
        finally
        {
            navigation.logout();
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void navigatorPagesYourWatchesTable()
    {
        log("Checking paging for 'Your Watches'");

        setMaximumIssuesPerPage(halfmaximumIssuesPerPage);
        Collection watchedIssues = new ArrayList();
        watchedIssues.addAll(watchIssues(issues));
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLinkWithText("Your Watches");

        checkIssuesInPages(WATCHED_ISSUES_TITLE, watchedIssues, 1, halfmaximumIssuesPerPage);

        tester.clickLink("page_" + 2);
        checkIssuesInPages(WATCHED_ISSUES_TITLE, watchedIssues, 2, halfmaximumIssuesPerPage);

        tester.clickLinkWithText(NEXT);
        checkIssuesInPages(WATCHED_ISSUES_TITLE, watchedIssues, 3, halfmaximumIssuesPerPage);

        tester.clickLinkWithText(PREVIOUS);
        checkIssuesInPages(WATCHED_ISSUES_TITLE, watchedIssues, 2, halfmaximumIssuesPerPage);
    }

    protected void checkIssuesInPages(Collection issuesInFilter, int currentPage, int maxIssues) {
        checkIssuesInPages(null, issuesInFilter, currentPage, maxIssues);
    }

    protected void checkIssuesInPages(String navTitle, Collection issuesInFilter, int currentPage, int maxIssues)
    {
        int numberOfIssuesInFilter = issuesInFilter.size();
        int startIssues = (currentPage - 1) * maxIssues;
        int endIssues = Math.min(numberOfIssuesInFilter, startIssues + maxIssues);

        if (navTitle == null)
        {
            assertTextPresent("Issue Navigator");
            if (maxIssues < numberOfIssuesInFilter)
            {
                assertIssueNavigatorDisplaying(String.valueOf(startIssues + 1), String.valueOf(endIssues), String.valueOf(numberOfIssuesInFilter));
            }
        }
        else
        {
            assertTextPresent(navTitle);
        }

        int i = 0;
        for (Iterator iterator = issuesInFilter.iterator(); iterator.hasNext();)
        {
            String issue = (String) iterator.next();
            if (i >= startIssues && i < endIssues)
            {
                assertTextPresent(issue);
            }
            i++;
        }
    }

    protected Collection watchIssues(Collection allIssues)
    {
        Collection issuesToWatch = new ArrayList();
        int i = 0;
        for (Iterator iterator = allIssues.iterator(); iterator.hasNext();)
        {
            String issue = (String) iterator.next();

            if ((i % 2) == 0)
            {
                startWatchingAnIssue(issue);
                issuesToWatch.add(issue);
            }

            i++;
        }
        return issuesToWatch;
    }

    protected Collection voteForIssues(Collection allIssues)
    {
        Collection issuesToVote = new ArrayList();
        int i = 0;
        for (Iterator iterator = allIssues.iterator(); iterator.hasNext();)
        {
            String issue = (String) iterator.next();

            if ((i % 2) == 0)
            {
                voteForIssue(issue);
                issuesToVote.add(issue);
            }

            i++;
        }
        return issuesToVote;
    }
}
