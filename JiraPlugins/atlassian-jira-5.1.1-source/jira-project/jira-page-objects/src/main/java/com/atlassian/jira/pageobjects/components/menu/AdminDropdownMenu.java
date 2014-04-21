package com.atlassian.jira.pageobjects.components.menu;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.webdriver.utils.by.ByJquery;

import javax.inject.Inject;

/**
 * Object for interacting with the Admin menu in the JIRA header.
 * TODO: extend for all available links.
 */
public class AdminDropdownMenu
{

    @Inject
    PageBinder pageBinder;

    @ElementBy(id = "plugins_lnk")
    private PageElement pluginsPageLink;

//    @ClickableLink(id = "plugins_lnk", nextPage = PluginsPage.class)
//    WebDriverLink<PluginsPage> pluginsLink;
//
//    @ClickableLink(id = "license_details_lnk", nextPage = LicenseDetailsPage.class)
//    WebDriverLink<LicenseDetailsPage> licenseDetailsLink;
//
//    @ClickableLink(id = "user_browser_lnk", nextPage = UserBrowserPage.class)
//    WebDriverLink<UserBrowserPage> userBrowserLink;
//
//    @ClickableLink(id = "view_projects_lnk", nextPage = ProjectsViewPage.class)
//    WebDriverLink<ProjectsViewPage> projectsViewPage;

    private AuiDropdownMenu adminMenu;

    @Init
    public void initialise()
    {
        adminMenu = pageBinder.bind(AuiDropdownMenu.class, ByJquery.$("#admin_link").parent("li"));
    }

    // TODO when pages are implemented

//    public Pl gotoPluginsPage()
//    {
//        return pluginsLink.activate();
//    }

//    public LicenseDetailsPage gotoLicenseDetailsPage()
//    {
//        return licenseDetailsLink.activate();
//    }
//
//    public UserBrowserPage gotoUserBrowserPage()
//    {
//        return userBrowserLink.activate();
//    }
//
//    public ProjectsViewPage gotoProjectsPage()
//    {
//        return projectsViewPage.activate();
//    }

    public AdminDropdownMenu open()
    {
        adminMenu.open();
        return this;
    }

    public boolean isOpen()
    {
        return adminMenu.isOpen();
    }

    public AdminDropdownMenu close()
    {
        adminMenu.close();
        return this;
    }
}
