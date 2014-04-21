package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Results on issue navigator. Can be used for either simple or advanced mode.
 *
 * @since v4.4
 */
public class IssueNavigatorResults
{

    protected PageElement totalCount;
    protected PageElement newSeachLink;
    protected PageElement issuetable;

    @Inject
    PageBinder binder;

    @Inject
    PageElementFinder finder;

    @Inject
    private AtlassianWebDriver webDriver;

    @WaitUntil
    public void loaded()
    {
        finder.find(By.className("results-wrap")).isPresent();
    }

    @Init
    public void getElements()
    {
        totalCount = finder.find(By.className("results-count-total"));
        newSeachLink = finder.find(By.id("new_filter"));
        issuetable = finder.find(By.id("issuetable"));
    }

    public int getTotalCount()
    {
        if (!totalCount.isPresent())
        {
            return 0;
        }
        else
        {
            return Integer.parseInt(totalCount.getText());
        }

    }

    public SelectedIssue getSelectedIssue()
    {
        return binder.bind(SelectedIssue.class);
    }

    public IssueNavigatorResults focus()
    {
        issuetable.click();
        return this;
    }

    public IssueNavigatorResults nextIssue()
    {
        issuetable.type("j");
        return this;
    }

    public SelectedIssue selectIssue(String issueId)
    {
        webDriver.executeScript("JIRA.IssueNavigator.Shortcuts.focusRow('" + issueId + "', 0, true)");
        return getSelectedIssue();
    }
}
