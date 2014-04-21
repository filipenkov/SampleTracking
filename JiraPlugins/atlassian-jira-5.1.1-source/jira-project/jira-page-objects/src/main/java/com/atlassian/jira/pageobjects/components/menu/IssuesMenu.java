package com.atlassian.jira.pageobjects.components.menu;

import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.pageobjects.elements.PageElement;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v5.0
 */
public class IssuesMenu extends JiraAuiDropdownMenu
{
    public IssuesMenu()
    {
        super(By.id("find_link_drop"), By.id("find_link_drop_drop"));
    }

    public IssuesMenu open()
    {
        super.open();
        return this;
    }

    public CreateIssueDialog createIssue()
    {
        getDropdown().find(By.id("issues_new_issue_link_lnk")).click();
        return pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);
    }

    public List<String> getRecentIssues()
    {
        final List<PageElement> elements = getDropdown().findAll(By.cssSelector("#issues_history_main a"));
        List<String> issues = new ArrayList<String>();
        for (PageElement element : elements)
        {
            if (!element.getAttribute("id").equals("issue_lnk_more_lnk"))
            {
                issues.add(StringUtils.trim(element.getText()));
            }
        }
        return issues;
    }
}
