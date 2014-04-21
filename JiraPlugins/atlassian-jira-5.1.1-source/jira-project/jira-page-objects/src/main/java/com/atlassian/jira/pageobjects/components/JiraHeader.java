package com.atlassian.jira.pageobjects.components;

import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.QueryableDropdownSelect;
import com.atlassian.jira.pageobjects.components.menu.AdminDropdownMenu;
import com.atlassian.jira.pageobjects.components.menu.DashboardMenu;
import com.atlassian.jira.pageobjects.components.menu.IssuesMenu;
import com.atlassian.jira.pageobjects.components.menu.JiraUserMenu;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.component.Header;
import com.atlassian.pageobjects.component.WebSudoBanner;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

/**
 * Representation of the JIRA header
 *
 * @since v4.4
 */
public class JiraHeader implements Header
{
    private static final String ADMIN_QUICK_NAV_QUERYABLE_CONTAINER = "admin-quick-nav-queryable-container";
    private static final String HEADER_ADMINISTRATION_SUGGESTIONS = "header-administration-suggestions";

    @Inject
    protected PageElementFinder elementFinder;

    @Inject
    PageBinder pageBinder;

    @Inject
    AtlassianWebDriver driver;

    @FindBy(id="header")
    private WebElement headerElement;

    @FindBy (id = "header-details-user-fullname")
    private WebElement headerUserFullName;
    
    private String userName;

    @Init
    public void init()
    {
        By byId = By.id("header-details-user-fullname");

        userName = driver.elementIsVisible(byId) ? driver.findElement(byId).getText() : null;
    }

     /**
     * Gets admin quick search. If it isn't present will return null
     *
     * @return admin quick search
     */
    public AutoComplete getAdminQuickSearch()
    {
        return pageBinder.bind(QueryableDropdownSelect.class, By.id(ADMIN_QUICK_NAV_QUERYABLE_CONTAINER),
                By.id(HEADER_ADMINISTRATION_SUGGESTIONS));
    }

    public DashboardMenu getDashboardMenu()
    {
        return pageBinder.bind(DashboardMenu.class);
    }
    public AdminDropdownMenu getAdminMenu()
    {
        return pageBinder.bind(AdminDropdownMenu.class);
    }

    public IssuesMenu getIssuesMenu()
    {
        return pageBinder.bind(IssuesMenu.class);
    }

    public JiraUserMenu getUserMenu()
    {
        return pageBinder.bind(JiraUserMenu.class);
    }

    public CreateIssueDialog createIssue()
    {
        elementFinder.find(By.id("create_link")).click();
        return pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);
    }

    public boolean hasCreateLink()
    {
        return elementFinder.find(By.id("create_link")).isPresent();
    }

    public boolean isLoggedIn()
    {
        return userName != null;
    }

    @Override
    public <M extends Page> M logout(Class<M> nextPage)
    {
        return getUserMenu().logout(nextPage);
    }

    @Override
    public WebSudoBanner getWebSudoBanner()
    {
        return pageBinder.bind(WebSudoBanner.class);
    }

    public boolean isAdmin()
    {
        return isLoggedIn() && driver.elementExistsAt(By.id("admin_link"), headerElement);
    }
    
    public String getCurrentUserFullName()
    {
        return headerUserFullName.getText();
    }

}
