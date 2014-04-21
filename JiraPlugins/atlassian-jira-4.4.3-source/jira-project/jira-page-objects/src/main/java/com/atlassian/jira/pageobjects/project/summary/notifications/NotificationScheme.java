package com.atlassian.jira.pageobjects.project.summary.notifications;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

public class NotificationScheme
{
    @ElementBy (className = "formtitle")
    private PageElement pageTitle;

    public String getTitle()
    {
        return pageTitle.getText();
    }
}