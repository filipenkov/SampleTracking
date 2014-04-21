package com.atlassian.jira.pageobjects.project;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Delete project page
 *
 * @since v4.4
 */
public class DeleteProjectPage extends AbstractJiraPage
{
    private static final String URI = "/secure/admin/DeleteProject!default.jspa?pid=%d";
    private Long projectId;

    @ElementBy(id = "delete_submit")
    private PageElement deleteProject;

    public DeleteProjectPage()
    {
    }

    public DeleteProjectPage(final Long projectId)
    {
        this.projectId = checkNotNull(projectId);
    }

    @Override
    public TimedCondition isAt()
    {
        return deleteProject.timed().isPresent();
    }

    public void submitConfirm()
    {
        deleteProject.click();
    }

    @Override
    public String getUrl()
    {
        return String.format(URI, projectId);
    }
}
