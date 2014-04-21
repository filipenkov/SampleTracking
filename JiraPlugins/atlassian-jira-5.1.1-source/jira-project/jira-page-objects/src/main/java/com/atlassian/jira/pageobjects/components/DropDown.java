package com.atlassian.jira.pageobjects.components;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Simple dropdown that has a "trigger" element that opens a "target" element (generally some kind of menu).
 *
 * @since v5.0
 */
public class DropDown
{
    private final By triggerLocator;
    private final By dropdownLocator;

    @Inject private PageBinder pageBinder;
    @Inject private PageElementFinder elementFinder;

    private PageElement dropDown;
    private PageElement trigger;

    public DropDown(By triggerLocator, By dropdownLocator)
    {
        this.triggerLocator = triggerLocator;
        this.dropdownLocator = dropdownLocator;
    }

    @Init
    public void init()
    {
        trigger = elementFinder.find(triggerLocator);
        dropDown = elementFinder.find(dropdownLocator);
    }

    public DropDown open()
    {
        if (!isOpen())
        {
            trigger.click();
            waitForOpen();
        }
        return this;
    }

    public <T> T openAndClick(By locator, final Class<T> next, Object...args)
    {
        open();
        return click(locator, next, args);
    }

    public <T> T click(By locator, final Class<T> next, Object...args)
    {
        getDropDownItem(locator).click();
        return pageBinder.bind(next, args);
    }

    public boolean isExists()
    {
        return trigger.isPresent();
    }

    public boolean hasItemById(final String id)
    {
        return hasItemBy(By.id(id));
    }

    public boolean hasItemBy(final By locator)
    {
        if (!isExists())
        {
            return false;
        }

        boolean opened = false;

        if (!isOpen())
        {
            opened = true;
            open();
        }
        boolean present = getDropDownItem(locator).isPresent();

        if (opened)
        {
            close();
        }

        return present;
    }

    private PageElement getDropDownItem(final By locator)
    {
        return dropDown.find(locator);
    }

    public void waitForOpen()
    {
        Poller.waitUntilTrue(dropDown.timed().isVisible());
    }

    public boolean isOpen()
    {
        return dropDown.isVisible();
    }

    public DropDown close()
    {
        if (isOpen())
        {
            trigger.click();
        }
        return this;
    }
}
