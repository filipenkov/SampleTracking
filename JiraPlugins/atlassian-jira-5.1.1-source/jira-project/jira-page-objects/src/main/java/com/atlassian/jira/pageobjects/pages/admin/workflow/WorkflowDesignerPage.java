package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Represents the workflow designer page for a particular workflow!
 *
 * @since v4.4
 */
public class WorkflowDesignerPage extends AbstractJiraPage implements WorkflowHeader
{
    private static final String URI_TEMPLATE = "/secure/admin/WorkflowDesigner.jspa?wfName=%s&workflowMode=%s";

    private final String uri;
    private final String workflowName;
    private WorkflowHeaderDelegate header;

    @ElementBy(id = "jwd", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement jwdElement;


    public WorkflowDesignerPage(final String workflowName, final boolean isDraft)
    {
        this.workflowName = workflowName;
        this.uri = String.format(URI_TEMPLATE, encodeUrl(workflowName), isDraft ? "draft" : "live");
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(jwdElement.timed().isPresent(),
                getWorkflowHeader().isPresentCondition(workflowName));
    }

    @Override
    public String getUrl()
    {
        return uri;
    }

    @Override
    public boolean canCreateDraft() {return header.canCreateDraft();}

    @Override
    public boolean canDiscard() {return header.canDiscard();}

    @Override
    public boolean canEditNameOrDescription() {return header.canEditNameOrDescription();}

    @Override
    public boolean canPublish() {return header.canPublish();}

    @Override
    public <T extends WorkflowHeader> T createDraftInMode(WorkflowMode<T> mode) {return header.createDraftInMode(mode);}

    @Override
    public <T extends WorkflowHeader> T createDraft(WorkflowMode<T> mode)
    {
        return header.createDraft(mode);
    }

    @Override
    public DiscardDraftDialog openDiscardDialog() {return header.openDiscardDialog();}

    @Override
    public EditWorkflowNameAndDescriptionDialog editNameOrDescription() {return header.editNameOrDescription();}

    @Override
    public WorkflowMode<?> getCurrentMode() {return header.getCurrentMode();}

    @Override
    public List<String> getInfoMessages() {return header.getInfoMessages();}

    @Override
    public List<String> getSharedProjects() {return header.getSharedProjects();}

    @Override
    public List<String> getWarningMessages() {return header.getWarningMessages();}

    @Override
    public String getWorkflowDescription() {return header.getWorkflowDescription();}

    @Override
    public String getWorkflowName() {return header.getWorkflowName();}

    @Override
    public ViewWorkflowSteps gotoLiveWorkflow() {return header.gotoLiveWorkflow();}

    @Override
    public boolean hasLinkToLiveWorkflow() {return header.hasLinkToLiveWorkflow();}

    @Override
    public boolean isActive() {return header.isActive();}

    @Override
    public boolean isDraft() {return header.isDraft();}

    @Override
    public boolean isInactive() {return header.isInactive();}

    @Override
    public boolean isSystem() {return header.isSystem();}

    @Override
    public PublishDialog openPublishDialog() {return header.openPublishDialog();}

    @Override
    public <T extends WorkflowHeader> T setCurrentEditMode(WorkflowMode<T> mode)
    {
        return header.setCurrentEditMode(mode);
    }

    @Override
    public void setCurrentViewMode(WorkflowMode<?> mode) {header.setCurrentViewMode(mode);}

    @Override
    public ProjectSharedBy sharedBy() {return header.sharedBy();}

    private WorkflowHeaderDelegate getWorkflowHeader()
    {
        if (header == null)
        {
            header = pageBinder.bind(WorkflowHeaderDelegate.class);
        }
        return header;
    }

    private static String encodeUrl(String param)
    {
        try
        {
            return URLEncoder.encode(param, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
