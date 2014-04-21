package com.atlassian.jira.issue.history;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.log4j.Logger;

/**
 * Implementation of DateRangeBuilder - will work for reporter fields only.
 *
 * @since v4.4
 */
public class ReporterDateRangeBuilder extends AbstractDateRangeBuilder
{
    private static final Logger log = Logger.getLogger(ReporterDateRangeBuilder.class);
    private static final String EMPTY_VALUE = DocumentConstants.ISSUE_NO_AUTHOR;

    public ReporterDateRangeBuilder()
    {
        this(IssueFieldConstants.REPORTER, EMPTY_VALUE);
    }

    public ReporterDateRangeBuilder(String field, String emptyValue)
    {
        super(field, emptyValue);
    }

    @Override
    protected ChangeHistoryItem createInitialChangeItem(Issue issue)
    {

        final String reporterName = issue.getReporter() == null ? "" : issue.getReporter().getDisplayName();
        final String reporterValue = issue.getReporter() == null ? EMPTY_VALUE : issue.getReporter().getName();
        return new ChangeHistoryItem.Builder().withId(-1L).inChangeGroup(-1L).forIssue(issue.getId(), issue.getKey()).
                                            inProject(issue.getProjectObject().getId()).field(getField()).
                                            on(issue.getCreated()).to(reporterName, reporterValue).
                                            byUser(issue.getReporter() == null ? DocumentConstants.ISSUE_NO_AUTHOR : issue.getReporter().getName()).
                                            build();
    }

}
