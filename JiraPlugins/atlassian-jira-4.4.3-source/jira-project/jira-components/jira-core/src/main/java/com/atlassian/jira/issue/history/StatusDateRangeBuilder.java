package com.atlassian.jira.issue.history;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Implementation  implementation of DateRangeBuilder - will work for status fields only.
 *
 * @since v4.4
 */
public class StatusDateRangeBuilder extends AbstractDateRangeBuilder
{
    private static final Logger log = Logger.getLogger(StatusDateRangeBuilder.class);
    private static final String EMPTY_VALUE = "-1";

    public StatusDateRangeBuilder()
    {
        this(IssueFieldConstants.STATUS);
    }

    public StatusDateRangeBuilder(String field)
    {
        super(field, EMPTY_VALUE);
    }

    @Override
    protected ChangeHistoryItem createInitialChangeItem(Issue issue)
    {
        final String statusName = issue.getStatusObject() == null ? null : issue.getStatusObject().getName();
        final String statusValue = issue.getStatusObject() == null ? EMPTY_VALUE : issue.getStatusObject().getId();
        return new ChangeHistoryItem.Builder().withId(-1L).inChangeGroup(-1L).forIssue(issue.getId(), issue.getKey()).
                                            inProject(issue.getProjectObject().getId()).field(getField()).
                                            on(issue.getCreated()).to(statusName, statusValue).
                                            byUser(issue.getReporter() == null ? DocumentConstants.ISSUE_NO_AUTHOR : issue.getReporter().getName()).
                                            build();

    }

}
