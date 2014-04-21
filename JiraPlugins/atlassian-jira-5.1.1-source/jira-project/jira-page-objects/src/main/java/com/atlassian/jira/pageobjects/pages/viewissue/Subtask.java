package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

/**
 * Representation of a single subtask on the view issue page
 *
 * @since v5.0
 */
public class Subtask
{
    private final PageElement issue;

    public Subtask(final PageElement issue)
    {
        this.issue = issue;
    }

    public String getSummary()
    {
        return issue.find(By.className("stsummary")).getText();
    }
}
