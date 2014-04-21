package com.atlassian.jira.issue.changehistory;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.user.UserUtils;
import com.google.common.base.Function;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.transform;

/**
 * Represents an issue change history.<br>
 * ChangeHistory is essentially a GenericValue wrapper with getters
 * @see com.atlassian.jira.issue.changehistory.ChangeHistoryManager#getChangeHistoriesForUser(com.atlassian.jira.issue.Issue, User)
 */
@PublicApi
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

    /**
     * Returns the author of this Change
     * @return the author of this Change
     * @deprecated Use {@link #getAuthor()} instead. Since v5.0.
     */
    public String getUsername()
    {
        return changeHistory.getString("author");
    }

    /**
     * Returns the author of this Change
     * @return the author of this Change
     */
    public String getAuthor()
    {
        return changeHistory.getString("author");
    }

    /**
     * Returns the author of this Change
     * @return the author of this Change
     */
    public User getAuthorUser()
    {
        if (user == null)
        {
            user = UserUtils.getUserEvenWhenUnknown(getAuthor());
        }
        return user;
    }

    /**
     * Returns the display name of the author of this Change
     * @return the display name of the author of this Change
     */
    public String getAuthorDisplayName()
    {
        if (getAuthorUser() != null)
            return getAuthorUser().getDisplayName();
        return null;
    }

    /**
     * Returns the display name of the author of this Change
     * @return the display name of the author of this Change
     * @deprecated Use {@link #getAuthorDisplayName()} instead. Since v5.0.
     */
    public String getFullName()
    {
        return getAuthorDisplayName();
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

    public List<ChangeItemBean> getChangeItemBeans()
    {
        List<?> items = getChangeItems();
        return transform(items, new Function<Object, ChangeItemBean>() {
            @Override
            public ChangeItemBean apply(@Nullable Object from)
            {
                GenericValue changeItemGV = (GenericValue) from;
                return new ChangeItemBean(changeItemGV.getString("fieldtype"),
                    changeItemGV.getString("field"), changeItemGV.getString("oldvalue"),
                    changeItemGV.getString("oldstring"), changeItemGV.getString("newvalue"),
                    changeItemGV.getString("newstring"), getTimePerformed());
            }
        });
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
        return issueManager.getIssueObject(getIssueId());
    }

    public Long getIssueId()
    {
        return changeHistory.getLong("issue");
    }
}
