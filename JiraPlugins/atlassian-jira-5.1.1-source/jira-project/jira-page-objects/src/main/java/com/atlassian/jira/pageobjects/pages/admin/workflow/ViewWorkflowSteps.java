package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Represents the ViewWorkflowSteps page in JIRA.
 *
 * @since v4.4
 */
public class ViewWorkflowSteps extends AbstractJiraPage implements WorkflowHeader
{
    @ElementBy(id = "steps_table")
    private PageElement stepsElement;

    private final String url;
    private final String workflowName;
    private WorkflowHeaderDelegate header;

    public ViewWorkflowSteps(String workflowName, boolean draft)
    {
        this.workflowName = workflowName;
        url = String.format("/secure/admin/workflows/ViewWorkflowSteps.jspa?workflowMode=%s&workflowName=%s",
                draft ? "draft" : "live", encodeParameter(workflowName));
    }

    public ViewWorkflowSteps(String workflowName)
    {
        this(workflowName, false);
    }

    public ViewWorkflowSteps()
    {
        url = null;
        workflowName = null;
    }

    @Override
    public boolean canCreateDraft() {return getWorkflowHeader().canCreateDraft();}

    @Override
    public boolean canDiscard() {return getWorkflowHeader().canDiscard();}

    @Override
    public boolean canEditNameOrDescription() {return getWorkflowHeader().canEditNameOrDescription();}

    @Override
    public boolean canPublish() {return getWorkflowHeader().canPublish();}

    @Override
    public <T extends WorkflowHeader> T createDraftInMode(WorkflowMode<T> mode) {return getWorkflowHeader().createDraftInMode(mode);}

    @Override
    public <T extends WorkflowHeader> T createDraft(WorkflowMode<T> mode) {return getWorkflowHeader().createDraft(mode); }

    @Override
    public <T extends WorkflowHeader> T setCurrentEditMode(WorkflowMode<T> mode)
    {
        return header.setCurrentEditMode(mode);
    }

    @Override
    public void setCurrentViewMode(WorkflowMode<?> mode) {header.setCurrentViewMode(mode);}

    @Override
    public DiscardDraftDialog openDiscardDialog() {return getWorkflowHeader().openDiscardDialog();}

    @Override
    public EditWorkflowNameAndDescriptionDialog editNameOrDescription() {return getWorkflowHeader().editNameOrDescription();}

    @Override
    public WorkflowMode<?> getCurrentMode() {return getWorkflowHeader().getCurrentMode();}

    @Override
    public List<String> getInfoMessages() {return getWorkflowHeader().getInfoMessages();}

    @Override
    public List<String> getSharedProjects() {return getWorkflowHeader().getSharedProjects();}

    @Override
    public List<String> getWarningMessages() {return getWorkflowHeader().getWarningMessages();}

    @Override
    public String getWorkflowDescription() {return getWorkflowHeader().getWorkflowDescription();}

    @Override
    public String getWorkflowName() {return getWorkflowHeader().getWorkflowName();}

    @Override
    public ViewWorkflowSteps gotoLiveWorkflow() {return getWorkflowHeader().gotoLiveWorkflow();}

    @Override
    public boolean hasLinkToLiveWorkflow() {return getWorkflowHeader().hasLinkToLiveWorkflow();}

    @Override
    public boolean isActive() {return getWorkflowHeader().isActive();}

    @Override
    public boolean isDraft() {return getWorkflowHeader().isDraft();}

    @Override
    public boolean isInactive() {return getWorkflowHeader().isInactive();}

    @Override
    public boolean isSystem() {return getWorkflowHeader().isSystem();}

    @Override
    public PublishDialog openPublishDialog() {return getWorkflowHeader().openPublishDialog();}

    @Override
    public ProjectSharedBy sharedBy() {return getWorkflowHeader().sharedBy();}

    private WorkflowHeaderDelegate getWorkflowHeader()
    {
        if (header == null)
        {
            header = pageBinder.bind(WorkflowHeaderDelegate.class);
        }
        return header;
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
        return Conditions.and(stepsElement.timed().isPresent(),
                getWorkflowHeader().isPresentCondition(workflowName));
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
