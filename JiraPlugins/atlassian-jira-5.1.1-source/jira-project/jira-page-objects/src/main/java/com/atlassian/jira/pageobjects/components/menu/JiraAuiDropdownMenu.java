package com.atlassian.jira.pageobjects.components.menu;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;
import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * The JIRA version of the AUI dropdown.
 * https://extranet.atlassian.com/x/VocUc
 *
 * @since v5.0
 */
public class JiraAuiDropdownMenu
{
    @Inject
    protected PageElementFinder finder;

    @Inject
    protected PageBinder pageBinder;

    private final By triggerLocator;
    private final By dropdownLocator;
    protected PageElement triggerElement;

    /**
     * @param triggerLocator The locator to the trigger element
     * @param dropdownLocator The locator to the dropdown element
     */
    public JiraAuiDropdownMenu(By triggerLocator, By dropdownLocator)
    {
        this.triggerLocator = triggerLocator;
        this.dropdownLocator = dropdownLocator;
    }

    @Init
    public void initialize()
    {
        triggerElement = finder.find(triggerLocator);
    }

    /**
     * Opens dropdown by clicking on trigger element
     */
    public JiraAuiDropdownMenu open()
    {
        if (!isOpen())
        {
            triggerElement.click();
            waitUntilOpen();
        }
        return this;
    }

    /**
     * Is the dialog open
     */
    public boolean isOpen()
    {
        return finder.find(dropdownLocator).isPresent() && finder.find(dropdownLocator).isVisible();
    }

    /**
     * Gets dropdown page element
     */
    protected PageElement getDropdown()
    {
        return finder.find(dropdownLocator);
    }

    /**
     * Closes dropdown by clicking trigger element again (toggling state)
     */
    public JiraAuiDropdownMenu close()
    {
        if (isOpen())
        {
            triggerElement.click();
            waitUntilClose();
        }
        return this;
    }

    public void waitUntilOpen()
    {
        waitUntilTrue(finder.find(dropdownLocator).timed().isVisible());
    }

    public void waitUntilClose()
    {
        waitUntilFalse(finder.find(dropdownLocator).timed().isVisible());
    }

}
