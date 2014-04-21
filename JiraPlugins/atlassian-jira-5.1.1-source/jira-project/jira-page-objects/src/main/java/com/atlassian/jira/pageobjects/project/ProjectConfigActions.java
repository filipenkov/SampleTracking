package com.atlassian.jira.pageobjects.project;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Section of the project configuration summary page covering pluggable view project operations, e.g.
 * the edit/delete/browse project links among others
 *
 * @since v4.4
 */
public class ProjectConfigActions
{
    @Inject
    private PageBinder pageBinder;

    @ElementBy (id = "project-config-actions-list")
    private PageElement dropDown;

    @Inject
    private PageElementFinder elementFinder;

    public enum ProjectOperations
    {
        CHANGE_AVATAR("project-config-avatar-edit"),
        BROWSE("browse_project"),
        EDIT("edit_project"),
        DELETE("delete_project");

        private final By locator;

        ProjectOperations(final String id)
        {
            this.locator = By.id(id);
        }

        public By getLocator()
        {
            return locator;
        }
    }

    public <T> T click(final Class<T> action, final ProjectOperations operation)
    {
        getDropDownItem(operation.getLocator()).click();
        return pageBinder.bind(action);
    }

    public boolean hasItem(final ProjectOperations operation)
    {
        return getDropDownItem(operation.getLocator()).isPresent();
    }

    public String getUrl(final ProjectOperations operation)
    {
        return getDropDownItem(operation.getLocator()).getAttribute("href");
    }

    public <T> T clickById(final Class<T> destination, final String id)
    {
        getDropDownItem(By.id(id)).click();
        return pageBinder.bind(destination);
    }

    public String getUrlById(final String id)
    {
        return getDropDownItem(By.id(id)).getAttribute("href");
    }

    public boolean hasItemById(final String id)
    {
        return getDropDownItem(By.id(id)).isPresent();
    }

    private PageElement getDropDownItem(final By locator)
    {
        return elementFinder.find(locator);
    }

    public TimedQuery<Boolean> isOpen()
    {
        return dropDown.timed().isVisible();
    }
}
