package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;

/**
 * Functional tests for JRA-14552
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestIssueNavigatorColumnLinks extends FuncTestCase
{
    public void testColumnLinks()
    {
        administration.restoreData("TestIssueNavigatorColumnLinks.xml");
        navigation.issueNavigator().hideActionsColumn();
        _testColumnLinksInIssueNavigator();
        _testColumnLinksInUserIssueColumns();
    }

    public void _testColumnLinksInIssueNavigator()
    {
        log("Checking the linkage of column values in the Issue Navigator");
        navigation.issueNavigator().displayAllIssues();
        TableLocator tableLocator = new TableLocator(tester, "issuetable");
        WebTable table = tableLocator.getTable();

        for (int col=1; col < table.getColumnCount(); col++)
        {
            // only check 2 rows
            for (int row=4; row <= 5; row++)
            {
                TableCell cell = table.getTableCell(row, col);
                String cellString = "Cell (" + row + ", " + col + ")";
                if (shouldColumnHaveLink(col))
                {
                    WebLink[] links = cell.getLinks();
                    assertTrue(cellString + " should have a link: " + cell.asText().trim(), links.length > 0);
                    for (int i = 0; i < links.length; i++)
                    {
                        WebLink link = links[i];
                        String valueName = link.asText();
                        String url = link.getURLString();
                        log(cellString + " has a link with text '" + valueName + "' and URL '" + url + "'. Following link ...");

                        // visit the link (e.g. /browse/HSP) and ensure the value (e.g. "homosapien") is present on the page
                        tester.gotoPage(trimUrl(url));
                        tester.assertTextPresent(valueName);
                    }
                }
                else
                {
                    WebLink[] links = cell.getLinks();
                    assertTrue(cellString + " should not have any links: " + cell.asText().trim(), links.length == 0);
                    log(cellString + " has no links. Next cell ...");
                }
            }
        }
    }

    public void _testColumnLinksInUserIssueColumns()
    {
        log("Checking the linkage of column values in the User Issue Columns screen");
        navigation.issueNavigator().displayAllIssues();
        tester.clickLinkWithText("Configure");
        
        TableLocator tableLocator = new TableLocator(tester, "issuetable");
        WebTable table = tableLocator.getTable();

        for (int col=1; col < table.getColumnCount(); col++)
        {
            // only check 2 rows
            for (int row=4; row <= 5; row++)
            {
                TableCell cell = table.getTableCell(row, col);
                String cellString = "Cell (" + row + ", " + col + ")";

                // no columns should have links
                WebLink[] links = cell.getLinks();
                assertTrue(cellString + " should not have any links: " + cell.asText().trim(), links.length == 0);
                log(cellString + " has no links. Next cell ...");
            }
        }
    }

    private boolean shouldColumnHaveLink(int col)
    {
        // idx  Heading              Link?
        // 0    T                    dont care
        // 1    Key                  link
        // 2    Summary              link
        // 3    Assignee             link
        // 4    Reporter             link
        // 5    Pr                   no link
        // 6    Status               no link
        // 7    Res                  no link
        // 8    Affects Version/s    no link
        // 9    Fix Version/s        link
        // 10   Components           link
        // 11   Votes                no link
        // 12   Project              link
        // 13   Stakeholders         link
        // 14   Related Project      link
        // 15   Culprit              link
        if (col < 1 || col > 15)
        {
            throw new IllegalArgumentException("Specify column between 1 and 15");
        }
        return col <= 4 || col == 9 || col == 10 || col >= 12;
    }

    private String trimUrl(String url)
    {
        if (url.startsWith(getEnvironmentData().getContext()))
        {
            return url.substring(getEnvironmentData().getContext().length());
        }
        return url;
    }
}
