package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteHistoryEntry;
import com.atlassian.jira.issue.vote.VoteHistoryEntryImpl;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.component.multiuserpicker.UserBean;
import com.opensymphony.user.User;
import org.joda.time.DateMidnight;
import org.joda.time.Days;
import org.ofbiz.core.entity.GenericEntityException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class ViewVoters extends AbstractIssueSelectAction
{
    private final VoteManager voteManager;
    private final VoteService voteService;
    private final PermissionManager permissionManager;
    private final DateTimeFormatter dateTimeFormatter;

    private Collection voters;
    private Boolean votedAlready;

    public ViewVoters(VoteManager voteManager, VoteService voteService, PermissionManager permissionManager,  DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        this.voteManager = voteManager;
        this.voteService = voteService;
        this.permissionManager = permissionManager;
        this.dateTimeFormatter = dateTimeFormatterFactory.formatter().forLoggedInUser().withStyle(DateTimeStyle.ISO_8601_DATE);
    }

    public String doDefault() throws Exception
    {
        if (!isIssueValid())
        {
            return ISSUE_PERMISSION_ERROR;
        }

        if (!permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, getProject(), getRemoteUser()))
        {
            return "securitybreach";
        }

        return super.doDefault();
    }

    public Collection/*<UserBean>*/ getVoters()
    {
        if (voters == null)
        {
            final Collection/*<String>*/ usernames = voteManager.getVoterUsernames(getIssue());
            voters = UserBean.convertUsernamesToUserBeans(getLocale(), usernames);
        }
        return voters;
    }

    public boolean isVotingEnabled()
    {
        return voteManager.isVotingEnabled();
    }

    public SimpleVoteHistory getVoteHistory()
    {
        List<VoteHistoryEntry> voteHistory = voteManager.getVoteHistory(getIssueObject());
        return new SimpleVoteHistory(getIssueObject());
    }

    public String getCommaSeperatedDateParts(Date date)
    {
        String commaSeperatedDateParts = dateTimeFormatter.format(date);
        return commaSeperatedDateParts.replace("-", ",");
    }

    // Add vote for current user on this issue
    @RequiresXsrfCheck
    public String doAddVote() throws GenericEntityException
    {
        if (!isIssueValid())
        {
            return ISSUE_PERMISSION_ERROR;
        }

        if (!permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, getProject(), getRemoteUser()))
        {
            return "securitybreach";
        }

        // Cannot vote for an issue that is reported by the current user or an issue that has been resolved.
        if (isIssueReportedByMe() || (getIssue().getString("resolution") != null))
        {
            return "securitybreach";
        }

        voteManager.addVote(getRemoteUser(), getIssue());
        refreshIssueObject();

        return SUCCESS;
    }

    /**
     * Remove the current users vote for this issue
     * @return The name of the view to be rendered. {@link webwork.action.Action#ERROR} is returned if the issue could
     * not be found or if the user does not have permission to see the issue.
     * @throws GenericEntityException
     */
    @RequiresXsrfCheck
    public String doRemoveVote() throws GenericEntityException
    {
        if (!isIssueValid())
        {
            return ISSUE_PERMISSION_ERROR;
        }

        if (!(permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, getProject(), getRemoteUser())))
        {
            return "securitybreach";
        }

        // Cannot remove vote for an issue that is reported by the current user
        // or an issue that has been resolved.
        if (isIssueReportedByMe() || (getIssue().getString("resolution") != null))
        {
            return "securitybreach";
        }

        voteManager.removeVote(getRemoteUser(), getIssue());
        refreshIssueObject();

        return SUCCESS;
    }

    /**
     * Determine whether the current user has voted already or not
     *
     * @return true if current user has already voted, false otherwise
     */
    public boolean isVotedAlready()
    {
        if (votedAlready == null)
        {
            if (getRemoteUser() != null)
            {
                votedAlready = Boolean.valueOf(voteManager.hasVoted(getRemoteUser(), getIssue()));
            }
            else
            {
                votedAlready = Boolean.FALSE;
            }
        }
        return votedAlready.booleanValue();
    }

    public boolean isIssueReportedByMe()
    {
        final String reporter = getIssue().getString("reporter");
        final User user = getRemoteUser();
        return user != null && reporter != null && reporter.equals(user.getName());
    }
    
    public boolean isCanAddVote()
    {
        User user = getRemoteUser();
        return user != null && voteService.validateAddVote(user, user, getIssueObject()).isValid();
    }

    public boolean isCanRemoveVote()
    {
        User user = getRemoteUser();
        return user != null && voteService.validateRemoveVote(user, user, getIssueObject()).isValid();
    }

    public class SimpleVoteHistory
    {
        private final List<VoteHistoryEntry> voteHistory = new ArrayList<VoteHistoryEntry>();
        private final int numberOfDays;
        public SimpleVoteHistory(Issue issue)
        {
            voteHistory.add(new VoteHistoryEntryImpl(issue.getId(), issue.getCreated(), 0));
            voteHistory.addAll(voteManager.getVoteHistory(issue));
            voteHistory.add(new VoteHistoryEntryImpl(issue.getId(), new Timestamp(System.currentTimeMillis()), issue.getVotes()));

            numberOfDays = Days.daysBetween(new DateMidnight(issue.getCreated()), new DateMidnight()).getDays() + 1;
        }

        public List<VoteHistoryEntry> getVoteHistory()
        {
            return voteHistory;
        }

        public int getNumberOfDays()
        {
            return numberOfDays;
        }
    }

}
