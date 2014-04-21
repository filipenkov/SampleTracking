package com.atlassian.jira.action.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import org.ofbiz.core.entity.GenericValue;

/**
 * @deprecated as of 4.1
 *
 * Use {@link com.atlassian.jira.bc.issue.IssueService#validateUpdate(com.atlassian.crowd.embedded.api.User, Long, com.atlassian.jira.issue.IssueInputParameters)}
 * and {@link com.atlassian.jira.bc.issue.IssueService#update(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult)}
 */
@Deprecated
public class IssueUpdate extends AbstractIssueAction
{
    private GenericValue originalIssue;
    private MutableIssue issueObject;
    private Long eventTypeId;
    private Boolean sendMail;

    private final IssueManager issueManager;

    public IssueUpdate(IssueManager issueManager)
    {
        this.issueManager = issueManager;
    }

    public String doExecute() throws Exception
    {
        // We always default to the ISSUE_UPDATED event type
        EventDispatchOption currEventTypeOption = (eventTypeId == null) ? EventDispatchOption.ISSUE_UPDATED : EventDispatchOption.Factory.get(eventTypeId);
        boolean sendMailBool = (sendMail == null) || sendMail.booleanValue();
        issueManager.updateIssue(getLoggedInUser(), getIssueObject(), currEventTypeOption, sendMailBool);
        return getResult();
    }

    ///CLOVER:OFF
    public MutableIssue getIssueObject()
    {
        return issueObject;
    }

    public void setIssueObject(MutableIssue issueObject)
    {
        this.issueObject = issueObject;
    }

    public void setSendMail(Boolean sendMail)
    {
        this.sendMail = sendMail;
    }

    public Long getEventTypeId()
    {
        return eventTypeId;
    }

    public void setEventTypeId(Long eventTypeId)
    {
        this.eventTypeId = eventTypeId;
    }

    /**
     * Here we override the AbstractGVIssueAction.setIssue() method and store a clone of the original issue.
     * <p/>
     * This means we can automatically generate changelogs - nice!
     */
    public void setIssue(GenericValue issue)
    {
        if (originalIssue == null)
        {
            originalIssue = ComponentAccessor.getIssueManager().getIssue(issue.getLong("id"));
        }

        super.setIssue(issue);
    }

    protected GenericValue getOriginalIssue()
    {
        return originalIssue;
    }

}
