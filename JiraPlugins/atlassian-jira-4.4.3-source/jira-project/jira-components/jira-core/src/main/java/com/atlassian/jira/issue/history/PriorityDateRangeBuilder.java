package com.atlassian.jira.issue.history;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.log4j.Logger;

/**
 * Implementation  implementation of DateRangeBuilder - will work for status fields only.
 *
 * @since v4.4
 */
public class PriorityDateRangeBuilder extends AbstractDateRangeBuilder
{
    private static final Logger log = Logger.getLogger(PriorityDateRangeBuilder.class);
    private static final String EMPTY_VALUE = "-1";

    public PriorityDateRangeBuilder()
    {
        this(IssueFieldConstants.PRIORITY, EMPTY_VALUE);
    }

    public PriorityDateRangeBuilder(String field, final String emptyValue)
    {
        super(field, emptyValue);
    }

    @Override
    protected ChangeHistoryItem createInitialChangeItem(Issue issue)
    {
        final String priorityName =   issue.getPriorityObject() == null ? null : issue.getPriorityObject().getName();
        final String priorityValue =   issue.getPriorityObject() == null ? EMPTY_VALUE : issue.getPriorityObject().getId();
        return new ChangeHistoryItem.Builder().withId(-1L).inChangeGroup(-1L).forIssue(issue.getId(), issue.getKey()).
                                    inProject(issue.getProjectObject().getId()).field(getField()).
                                    on(issue.getCreated()).to(priorityName, priorityValue).
                                    byUser(issue.getReporter() == null ? DocumentConstants.ISSUE_NO_AUTHOR : issue.getReporter().getName()).
                                    build();

    }

}
