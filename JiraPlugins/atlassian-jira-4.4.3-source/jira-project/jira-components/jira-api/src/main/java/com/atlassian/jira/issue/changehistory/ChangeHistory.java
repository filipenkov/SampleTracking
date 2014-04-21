package com.atlassian.jira.issue.changehistory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.OSUserConverter;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

/**
 * Represents an issue change history.<br>
 * ChangeHistory is essentially a GenericValue wrapper with getters
 * @see com.atlassian.jira.issue.changehistory.ChangeHistoryManager#getChangeHistoriesForUser(com.atlassian.jira.issue.Issue, com.opensymphony.user.User)
 */
public class ChangeHistory
{
    private GenericValue changeHistory;
    private IssueManager issueManager;
    private List changeItems;
    private User user;

    public ChangeHistory(GenericValue changeHistoryGV, IssueManager issueManager)
    {
        changeHistory = changeHistoryGV;
        this.issueManager = issueManager;
    }

    public Long getId()
    {
        return changeHistory.getLong("id");
    }

    public String getUsername()
    {
        return changeHistory.getString("author");
    }

    public User getAuthorUser()
    {
        if (user == null)
        {
            user = UserUtils.getUser(getUsername());
        }
        return user;
    }

    public com.opensymphony.user.User getUser()
    {
        return OSUserConverter.convertToOSUser(getAuthorUser());
    }

    public String getFullName()
    {
        if (getAuthorUser() != null)
            return getAuthorUser().getDisplayName();
        return null;
    }

    public Timestamp getTimePerformed()
    {
        return changeHistory.getTimestamp("created");
    }

    public String getLevel()
    {
        return changeHistory.getString("level");
    }

    public String getComment()
    {
        return changeHistory.getString("body");
    }

    public List getChangeItems()
    {
        if (changeItems == null)
        {
            try
            {
                changeItems = changeHistory.getRelated("ChildChangeItem");
            }
            catch (GenericEntityException e)
            {
                return Collections.EMPTY_LIST;
            }
        }
        return changeItems;
    }

    public Issue getIssue()
    {
        return issueManager.getIssueObject(changeHistory.getLong("issue"));
    }

    public Long getIssueId()
    {
        return changeHistory.getLong("issue");
    }
}
