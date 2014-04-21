package com.atlassian.jira.pageobjects.components.menu;

import org.openqa.selenium.By;

/**
 * Cog dropdown
 *
 * @since 5.0
 */
public class IssueActionsMenu extends JiraAuiDropdownMenu
{

    /**
     * @param triggerLocator selector for item that when clicked will invoke dropdown
     * @param dropdownLocator selector for the dropdown itself
     */
    public IssueActionsMenu(By triggerLocator, By dropdownLocator)
    {
        super(triggerLocator, dropdownLocator);
    }

    /**
     * Opens dropdown
     */
    @Override
    public IssueActionsMenu open()
    {
        super.open();
        return this;
    }

    /**
     * Clicks specified item/action in the menu
     *
     * @param menuItem - Item/Action to be clicked in the menu
     */
    public IssueActionsMenu clickItem(final IssueActions menuItem)
    {
        getDropdown().find(menuItem.getSelector()).click();
        return this;
    }

}
