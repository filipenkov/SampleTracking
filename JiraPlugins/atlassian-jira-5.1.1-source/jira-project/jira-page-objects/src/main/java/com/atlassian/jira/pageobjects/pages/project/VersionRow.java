package com.atlassian.jira.pageobjects.pages.project;

import com.atlassian.jira.pageobjects.pages.project.browseversion.BrowseVersionPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

public class VersionRow
{

    private final String projectKey;
    private final PageElement rowElement;
    private final PageElement nameCell;
    private final PageElement link;

    @Inject
    private PageBinder pageBinder;

    public VersionRow(String projectKey, PageElement rowElem)
    {
        this.projectKey = checkNotNull(projectKey);
        this.rowElement = checkNotNull(rowElem);
        this.nameCell = rowElem.find(By.className("version-name-cell"));
        this.link = nameCell.find(By.tagName("a"));
    }

    public String getId()
    {
        final String htmlId = link.getAttribute("id");
        return htmlId.replace("version_", "");
    }

    public String getName()
    {
        return rowElement.getAttribute("data-version-name");
    }

    public BrowseVersionPage goToBrowseVersion()
    {
        final String id = getId();
        link.click();
        return pageBinder.bind(BrowseVersionPage.class, projectKey, id);
    }
}
