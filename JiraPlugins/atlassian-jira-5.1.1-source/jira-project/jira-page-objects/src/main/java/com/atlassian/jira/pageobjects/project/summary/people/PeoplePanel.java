package com.atlassian.jira.pageobjects.project.summary.people;

import com.atlassian.jira.pageobjects.project.EditProjectLeadAndDefaultAssigneeDialog;
import com.atlassian.jira.pageobjects.project.summary.AbstractSummaryPanel;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Represents the people panel in the project configuration page.
 *
 * @since v4.4
 */
public class PeoplePanel extends AbstractSummaryPanel
{
    @Inject
    private PageBinder pageBinder;

    @ElementBy(id = "project-config-summary-people-list")
    private PageElement peopleList;

    @ElementBy(id = "project-config-summary-people-project-lead")
    private PageElement projectLead;

    @ElementBy(id = "project-config-summary-people-default-assignee")
    private PageElement defaultAssignee;

    @ElementBy(id = "project-config-summary-people-project-lead-avatar")
    private PageElement projectLeadAvatar;

    public String getProjectLead()
    {
        return projectLead.getText();
    }

    public String getDefaultAssignee()
    {
        return defaultAssignee.getText();
    }

    public boolean isProjectLeadNonExistentIndicated()
    {
        return projectLead.find(By.className("errLabel")).isPresent();
    }

    public boolean isProjectLeadNotAssignableIndicated()
    {
        return peopleList.find(By.className("project-config-invalid")).isPresent();
    }

    public boolean isDefaultAssigneeUserHoverEnabled()
    {
        final PageElement a = projectLead.find(By.tagName("a"));
        return a.isPresent() && a.hasClass("user-hover");
    }

    public boolean isProjectLeadAvatarPresent()
    {
        return projectLeadAvatar.isPresent();
    }

    public String getProjectLeadAvatarSrc()
    {
        return projectLeadAvatar.getAttribute("src");
    }
}
