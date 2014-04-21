package com.atlassian.jira.pageobjects.project;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import static org.junit.Assert.assertTrue;

/**
 * View Projects Page. Still minimal and needs to be filled in as time goes by =)
 *
 * @since v4.4
 */
public class ViewProjectsPage extends AbstractJiraPage
{
    private static final String URI = "/secure/project/ViewProjects.jspa";

    @ElementBy(id = "project-list")
    private PageElement table;

    @ElementBy(id = "add_project")
    private PageElement addProject;

    @ElementBy(id = "noprojects")
    private PageElement noProjects;

    public boolean isRowPresent(final String projectName)
    {
        final PageElement row = table.find(getProjectRowByNameLocator(projectName));
        return row.isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI;
    }

    private static By getProjectRowByNameLocator(final String projectName)
    {
        return By.linkText(projectName);
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.or(table.timed().isPresent(), noProjects.timed().isPresent());
    }

    public boolean hasProjects()
    {
        return table.isPresent();
    }

    public boolean canCreateProject()
    {
        return addProject.isPresent();
    }

    public AddProjectDialog openCreateProjectDialog()
    {
        assertTrue("Add project link not present.", addProject.isPresent());
        addProject.click();
        return pageBinder.bind(AddProjectDialog.class);
    }
}
