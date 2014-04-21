package com.atlassian.jira.issue.history;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.log4j.Logger;

/**
 * Implementation of DateRangeBuilder - will work for assignee fields only.
 *
 * @since v4.4
 */
public class AssigneeDateRangeBuilder extends AbstractDateRangeBuilder
{
    private static final Logger log = Logger.getLogger(AssigneeDateRangeBuilder.class);
    private static final String EMPTY_VALUE =  DocumentConstants.ISSUE_UNASSIGNED;

    public AssigneeDateRangeBuilder()
    {
           this(IssueFieldConstants.ASSIGNEE, EMPTY_VALUE);
    }

    public AssigneeDateRangeBuilder(String field, String emptyValue)
    {
        super(field, emptyValue);
    }

    @Override
    protected ChangeHistoryItem createInitialChangeItem(Issue issue)
    {
        final String assigneeName =   issue.getAssignee() == null ? null : issue.getAssignee().getDisplayName();
        final String assigneeValue =  issue.getAssignee() == null ? EMPTY_VALUE :  issue.getAssignee().getName();
        return new ChangeHistoryItem.Builder().withId(-1L).inChangeGroup(-1L).forIssue(issue.getId(), issue.getKey()).
                                        inProject(issue.getProjectObject().getId()).field(getField()).
                                        on(issue.getCreated()).to(assigneeName, assigneeValue).
                                        byUser(issue.getReporter() == null ? DocumentConstants.ISSUE_NO_AUTHOR : issue.getReporter().getName()).
                                        build();

    }

}
