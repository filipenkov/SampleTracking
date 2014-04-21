package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.components.menu.IssueActions;
import com.atlassian.jira.pageobjects.components.menu.JiraAuiDropdownMenu;
import org.openqa.selenium.By;

/**
 * The more actions menu on the view issue page
 *
 * @since v5.0
 */
public class MoreActionsMenu extends JiraAuiDropdownMenu
{

    public MoreActionsMenu()
    {
        super(By.id("opsbar-operations_more"), By.id("opsbar-operations_more_drop"));
    }

    public MoreActionsMenu clickItem(final IssueActions menuItem)
    {
        getDropdown().find(menuItem.getSelector()).click();
        return this;
    }

    @Override
    public MoreActionsMenu open()
    {
        super.open();
        return this;
    }

}
