package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Represents the ViewWorkflowSteps page in JIRA.
 *
 * @since v4.4
 */
public class ViewWorkflowSteps extends AbstractJiraPage
{
    @ElementBy(id = "workflow-name")
    private PageElement nameElement;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private String url;

    public ViewWorkflowSteps(String workflowName, boolean draft)
    {
        String mode = draft ? "draft" : "live";
        url = String.format("/secure/admin/workflows/ViewWorkflowSteps.jspa?workflowMode=%s&workflowName=%s",
                mode, encodeParameter(workflowName));
    }

    public ViewWorkflowSteps(String workflowName)
    {
        this(workflowName, false);
    }

    public String getWorkflowName()
    {
        return nameElement.getText();
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

    @Override
    public String getUrl()
    {
        if (url == null)
        {
            throw new IllegalStateException("Need to use other constructor");
        }
        return url;
    }

    @Override
    public TimedCondition isAt()
    {
        return nameElement.timed().isPresent();
    }

    private String encodeParameter(String value)
    {
        try
        {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
