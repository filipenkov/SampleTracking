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
public class ResolutionDateRangeBuilder extends AbstractDateRangeBuilder
{
    private static final Logger log = Logger.getLogger(ResolutionDateRangeBuilder.class);
    private static final String EMPTY_VALUE = "-1";


    public ResolutionDateRangeBuilder()
    {
        this(IssueFieldConstants.RESOLUTION, EMPTY_VALUE);
    }

    public ResolutionDateRangeBuilder(String field, String emptyValue)
    {
        super(field, emptyValue);
    }

    @Override
    protected ChangeHistoryItem createInitialChangeItem(Issue issue)
    {
        final String resolutionName =   issue.getResolutionObject() == null ? null : issue.getResolutionObject().getName();
        final String resolutionValue =   issue.getResolutionObject() == null ? EMPTY_VALUE : issue.getResolutionObject().getId();
        return (new ChangeHistoryItem(-1L, -1L, issue.getProjectObject().getId(), issue.getId(), issue.getKey(),
                getField(), issue.getCreated(), null, resolutionName, null, resolutionValue,
                issue.getReporter() == null ? DocumentConstants.ISSUE_NO_AUTHOR : issue.getReporter().getName()));
    }

}
