package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * @since v4.4
 */
public class IssueNavigatorResults
{

    protected PageElement totalCount;
    protected PageElement newSeachLink;

    @Inject
    PageElementFinder finder;

    @WaitUntil
    public void loaded()
    {
        finder.find(By.id("results-count-total")).isPresent();
    }

    @Init
    public void getElements()
    {
        totalCount = finder.find(By.id("results-count-total"));
        newSeachLink = finder.find(By.id("new_filter"));
    }

    public int getTotalCount()
    {
        return Integer.parseInt(totalCount.getText());
    }

}
