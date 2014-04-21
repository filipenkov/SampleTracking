package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriod;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @since v4.0
 */
public class TestDatePeriodStatisticsMapper extends LegacyJiraMockTestCase
{
    public void testGetUrlSuffixWithClauses() throws Exception
    {
        DatePeriodStatisticsMapper mapper = new DatePeriodStatisticsMapper(null, DocumentConstants.ISSUE_RESOLUTION_DATE, RegularTimePeriod.DEFAULT_TIME_ZONE);

        TimePeriod value = new Month(3, 2008);
        final Date expectedStart = value.getStart();
        final Date expectedEnd = value.getEnd();

        final TerminalClauseImpl projectClause = new TerminalClauseImpl(IssueFieldConstants.PROJECT, Operator.EQUALS, 13L);
        final TerminalClauseImpl issueTypeClause = new TerminalClauseImpl(IssueFieldConstants.ISSUE_TYPE, Operator.EQUALS, "Bug");
        final AndClause totalExistingClauses = new AndClause(projectClause, issueTypeClause);
        TimeZoneManager timeZoneManager = new TimeZoneManager()
        {
            @Override
            public TimeZone getLoggedInUserTimeZone()
            {
                return TimeZone.getDefault();
            }
        };

        final JqlDateSupportImpl support = new JqlDateSupportImpl(timeZoneManager);
        final TerminalClauseImpl afterClause = new TerminalClauseImpl(IssueFieldConstants.RESOLUTION_DATE, Operator.GREATER_THAN_EQUALS, support.getDateString(expectedStart));
        final TerminalClauseImpl beforeClause = new TerminalClauseImpl(IssueFieldConstants.RESOLUTION_DATE, Operator.LESS_THAN_EQUALS, support.getDateString(expectedEnd));
        final AndClause totalExpectedClauses = new AndClause(afterClause, beforeClause);

        Query query = new QueryImpl(totalExistingClauses);
        SearchRequest sr = new MockJqlSearchRequest(10000L, query);

        final SearchRequest urlSuffix = mapper.getSearchUrlSuffix(value, sr);
        final List<Clause> modifiedClauses = urlSuffix.getQuery().getWhereClause().getClauses();

        assertEquals(2, modifiedClauses.size());
        assertTrue(modifiedClauses.contains(totalExistingClauses));
        assertTrue(modifiedClauses.contains(totalExpectedClauses));
    }
}
