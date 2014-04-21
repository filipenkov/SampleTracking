/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.worklog.Worklog;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@PublicApi
public class IssueUpdateBean
{
    //required fields for an update
    private final GenericValue changedIssue;
    private final GenericValue originalIssue;
    private final Long eventTypeId;
    private final User user;
    private final boolean sendMail;

    //optional fields for an update
    private Worklog worklog;
    private boolean dispatchEvent = true;
    private Comment comment;

    private Map params = new HashMap(); //this cannot be collections.EMPTY_MAP as consumers of this object add items to it
    Collection<ChangeItemBean> changeItems;
    private boolean subtasksUpdated;

    public Worklog getWorklog()
    {
        return worklog;
    }

    public void setWorklog(Worklog worklog)
    {
        this.worklog = worklog;
    }

    public IssueUpdateBean(Issue changedIssue, Issue originalIssue, Long eventTypeId, User user)
    {
        this(changedIssue, originalIssue, eventTypeId, user, true, false);
    }

    public IssueUpdateBean(GenericValue changedIssue, GenericValue originalIssue, Long eventTypeId, User user)
    {
        this(changedIssue, originalIssue, eventTypeId, user, true, false);
    }

    public IssueUpdateBean(GenericValue changedIssue, GenericValue originalIssue, Long eventTypeId, User user, boolean sendMail, boolean subtasksUpdated)
    {
        this.changedIssue = changedIssue;
        this.originalIssue = originalIssue;
        this.eventTypeId = eventTypeId;
        this.user = user;
        this.sendMail = sendMail;
        this.subtasksUpdated = subtasksUpdated;
    }

    public IssueUpdateBean(Issue changedIssue, Issue originalIssue, Long eventTypeId, User user, boolean sendMail,
            boolean subtasksUpdated)
    {
        this(changedIssue.getGenericValue(), originalIssue.getGenericValue(), eventTypeId, user, sendMail, subtasksUpdated);
    }


    public boolean isDispatchEvent()
    {
        return dispatchEvent;
    }

    public void setDispatchEvent(boolean dispatchEvent)
    {
        this.dispatchEvent = dispatchEvent;
    }

    public Comment getComment()
    {
        return comment;
    }

    public void setComment(Comment comment)
    {
        this.comment = comment;
    }

    public Map getParams()
    {
        return params;
    }

    public void setParams(Map params)
    {
        this.params = params;
    }

    public Collection<ChangeItemBean> getChangeItems()
    {
        return changeItems;
    }

    public void setChangeItems(Collection<ChangeItemBean> changeItems)
    {
        this.changeItems = changeItems;
    }

    public GenericValue getChangedIssue()
    {
        return changedIssue;
    }

    public User getUser()
    {
        return user;
    }

    public GenericValue getOriginalIssue()
    {
        return originalIssue;
    }

    public Long getEventTypeId()
    {
        return eventTypeId;
    }

    public boolean isSendMail()
    {
        return sendMail;
    }

    public boolean isSubtasksUpdated()
    {
        return subtasksUpdated;
    }

    @SuppressWarnings ( { "RedundantIfStatement" })
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof IssueUpdateBean)) return false;

        final IssueUpdateBean issueUpdateBean = (IssueUpdateBean) o;

        if (dispatchEvent != issueUpdateBean.dispatchEvent) return false;
        if ((eventTypeId == null && issueUpdateBean.getEventTypeId() != null) || (eventTypeId != null && !eventTypeId.equals(issueUpdateBean.getEventTypeId()))) return false;
        if (changeItems != null ? !changeItems.equals(issueUpdateBean.changeItems) : issueUpdateBean.changeItems != null) return false;
        if (changedIssue != null ? !changedIssue.equals(issueUpdateBean.changedIssue) : issueUpdateBean.changedIssue != null) return false;
        if (comment != null ? !comment.equals(issueUpdateBean.comment) : issueUpdateBean.comment != null) return false;
        if (originalIssue != null ? !originalIssue.equals(issueUpdateBean.originalIssue) : issueUpdateBean.originalIssue != null) return false;
        if (params != null ? !params.equals(issueUpdateBean.params) : issueUpdateBean.params != null) return false;
        if (user != null ? !user.equals(issueUpdateBean.user) : issueUpdateBean.user != null) return false;
        if (eventTypeId != null ? !eventTypeId.equals(issueUpdateBean.eventTypeId) : issueUpdateBean.eventTypeId != null) return false;
        if (sendMail != issueUpdateBean.sendMail) return false;
        if (subtasksUpdated != issueUpdateBean.subtasksUpdated) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (changedIssue != null ? changedIssue.hashCode() : 0);
        result = 29 * result + (originalIssue != null ? originalIssue.hashCode() : 0);
        result = 29 * result + (eventTypeId != null ? eventTypeId.hashCode() : 0);
        result = 29 * result + (user != null ? user.hashCode() : 0);
        result = 29 * result + (dispatchEvent ? 1 : 0);
        result = 29 * result + (sendMail ? 1 : 0);
        result = 29 * result + (comment != null ? comment.hashCode() : 0);
        result = 29 * result + (params != null ? params.hashCode() : 0);
        result = 29 * result + (changeItems != null ? changeItems.hashCode() : 0);
        result = 29 * result + (subtasksUpdated  ? 1 : 0);
        return result;
    }
}
