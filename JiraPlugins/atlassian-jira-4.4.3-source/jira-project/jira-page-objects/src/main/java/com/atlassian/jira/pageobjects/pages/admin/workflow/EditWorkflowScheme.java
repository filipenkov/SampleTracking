package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * @since v4.4
 */
public class EditWorkflowScheme extends AbstractJiraPage
{
    @ElementBy (id = "workflow-scheme-id")
    private PageElement schemeIdElement;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private final long schemeId;

    public EditWorkflowScheme(long schemeId)
    {
        this.schemeId = schemeId;
    }

    public long getWorflowSchemeId()
    {
        return Long.parseLong(schemeIdElement.getValue());
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

    @Override
    public String getUrl()
    {
        return String.format("/secure/admin/EditWorkflowSchemeEntities!default.jspa?schemeId=%d", schemeId);
    }

    @Override
    public TimedCondition isAt()
    {
        return schemeIdElement.timed().isPresent();
    }
}
