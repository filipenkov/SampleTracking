package com.atlassian.jira.pageobjects.components.menu;

import com.atlassian.jira.pageobjects.pages.LogoutPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Object for interacting with the User Menu in the JIRA Header.
 */
public class JiraUserMenu
{
    @Inject
    PageBinder pageBinder;

    @ElementBy(id = "log_out")
    private PageElement logoutLink;


    private AuiDropdownMenu userMenu;

    @Init
    public void initialise()
    {
        userMenu = pageBinder.bind(AuiDropdownMenu.class, By.id("header-details-user"));
    }

    public LogoutPage logout()
    {
        logoutLink.click();
        return pageBinder.bind(LogoutPage.class);
    }

    public <P extends Page> P logout(Class<P> targetPage)
    {
        logoutLink.click();
        return pageBinder.bind(targetPage);
    }

    public JiraUserMenu open()
    {
        userMenu.open();
        return this;
    }

    public boolean isOpen()
    {
        return userMenu.isOpen();
    }

    public JiraUserMenu close()
    {
        userMenu.close();
        return this;
    }
}
