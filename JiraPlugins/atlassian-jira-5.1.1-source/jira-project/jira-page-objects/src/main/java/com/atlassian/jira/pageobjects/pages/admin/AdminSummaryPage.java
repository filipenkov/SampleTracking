package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import static org.junit.Assert.assertTrue;

/**
 * The landing page for Administration.
 *
 * @since v4.4
 */
public class AdminSummaryPage extends AbstractJiraAdminPage
{
    @ElementBy (id = "admin-summary-panel")
    private PageElement container;

    @ElementBy (id = "admin-summary-section-admin_project_menu")
    private PageElement projectsSection;

    @ElementBy (id = "admin-summary-section-admin_plugins_menu")
    private PageElement pluginsSection;

    @ElementBy (id = "admin-summary-section-admin_users_menu")
    private PageElement usersSection;

    @ElementBy (id = "admin-summary-section-admin_issues_menu")
    private PageElement optionsSection;

    @ElementBy (id = "admin-summary-section-admin_system_menu")
    private PageElement systemSection;

    @ElementBy (id = "admin-summary-project-link-allprojects")
    private PageElement allProjectLink;

    @ElementBy (id = "admin-summary-project-link-addproject")
    private PageElement addNewProjectLink;

    @ElementBy (id = "admin-summary-project-link-projectcategories")
    private PageElement projectCategoriesLink;

    @ElementBy (className = "add-project-intro-trigger")
    private PageElement addProjectIntroTrigger;

    private final static String URI = "/secure/AdminSummary.jspa";

    public AdminSummaryPage()
    {
    }

    public String getUrl()
    {
        return URI;
    }

    @Override
    public String linkId()
    {
        return "view_projects";
    }

    public PageElement getProjectLinkFor(String projectKey)
    {
        if (projectsSection.isPresent())
        {
            return projectsSection.find(By.id("admin-summary-recent-projects-" + projectKey));
        }
        return null;
    }

    public String getUsersText()
    {
        if (projectsSection.isPresent())
        {
            PageElement usersLink = usersSection.find(By.id("as-count-users"));
            if (usersLink != null)
            {
                return usersLink.getText();
            }
        }
        return null;
    }

    public String getGroupsText()
    {
        if (projectsSection.isPresent())
        {
            PageElement groupsLink = usersSection.find(By.id("as-count-groups"));
            if (groupsLink != null)
            {
                return groupsLink.getText();
            }
        }
        return null;
    }

    public String getRolesText()
    {
        if (projectsSection.isPresent())
        {
            PageElement rolesLink = usersSection.find(By.id("as-count-roles"));
            if (rolesLink != null)
            {
                return rolesLink.getText();
            }
        }
        return null;
    }

    public PageElement getProjectsSection()
    {
        return projectsSection;
    }

    public PageElement getPluginsSection()
    {
        return pluginsSection;
    }

    public PageElement getUsersSection()
    {
        return usersSection;
    }

    public PageElement getOptionsSection()
    {
        return optionsSection;
    }

    public PageElement getSystemSection()
    {
        return systemSection;
    }

    public PageElement getProjectCategoriesLink()
    {
        return projectCategoriesLink;
    }

    public PageElement getAllProjectLink()
    {
        return allProjectLink;
    }

    public PageElement getAddNewProjectLink()
    {
        return addNewProjectLink;
    }

    public ProjectIntroVideoDialog openAddProjectVideo()
    {
        assertTrue("Add project Video link not present.", addProjectIntroTrigger.isPresent());
        addProjectIntroTrigger.click();
        return pageBinder.bind(ProjectIntroVideoDialog.class);
    }

    @Override
    public TimedCondition isAt()
    {
        return container.timed().isPresent();
    }

}
