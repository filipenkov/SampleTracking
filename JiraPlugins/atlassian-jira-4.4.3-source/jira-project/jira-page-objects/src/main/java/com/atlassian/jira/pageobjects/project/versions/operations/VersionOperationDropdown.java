package com.atlassian.jira.pageobjects.project.versions.operations;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.inject.Inject;

/**
 * @since v4.4
 */
public class VersionOperationDropdown
{
    private PageElement rootMenuElement;
    private String menuId;
    private final String id;

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder binder;

    public VersionOperationDropdown(final String id)
    {
        this.id = id;
        this.menuId = "version-" + id + "-operations-trigger_drop";
    }

    @Init
    public void getElements()
    {
        rootMenuElement = elementFinder.find(By.id(menuId));
    }

    /**
     * Checks if the drop down has the specified operation
     *
     * @param operation the link text used to check for operation existence, e.g. "Edit Details"
     * @return true, if a link with the specified link text exists
     */
    public boolean hasOperation(final String operation)
    {
        PageElement menuItem = rootMenuElement.find(By.linkText(operation));
        return menuItem.isPresent() && menuItem.isVisible();
    }

    public DeleteOperation clickDelete()
    {
        rootMenuElement.find(By.className("project-config-operations-delete")).click();
        return binder.bind(DeleteOperation.class, id);
    }

    /**
     * Clicks the specified operation in the drop down.
     *
     * @param operation the link text to click
     */
    public void click(final String operation)
    {
        rootMenuElement.find(By.linkText(operation)).click();
    }

    /**
     * Closes the drop down by pressing esc.
     */
    public void close()
    {
        rootMenuElement.type(Keys.ESCAPE.toString());
    }

}
