package com.atlassian.jira.collector.plugin.components;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import com.google.common.collect.ImmutableList;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectorActivityHelperImpl implements CollectorActivityHelper
{
    private static final Logger log = Logger.getLogger(CollectorActivityHelperImpl.class);

    private final SearchService searchService;

    public CollectorActivityHelperImpl(final SearchService searchService)
    {
        this.searchService = searchService;
    }

	public List<Issue> getCollectorIssues(final User loggedInUser, final Collector collector, final int limit) {
		try
		{
			final PagerFilter pagerFilter = (limit <= 0) ? PagerFilter.getUnlimitedFilter() : new PagerFilter(limit);
			final SearchResults result = searchService.search(loggedInUser, getQuery(collector), pagerFilter);
			return ImmutableList.copyOf(result.getIssues());
		}
		catch (SearchException e)
		{
			log.error("Recent issues search exception occurred", e);
			return Collections.emptyList();
		}
	}

	public int getAllCollectorIssuesCount(final User loggedInUser, final Collector collector) {
			return getCollectorIssues(loggedInUser,collector,0).size();
	}

    @Override
    public String getJql(final User loggedInUser, final Collector collector)
    {
        return searchService.getJqlString(getQuery(collector));
    }

    @Override
    public String getIssueNavigatorUrl(final User loggedInUser, final Collector collector)
    {
        return "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(loggedInUser, getQuery(collector)) + "&mode=hide";
    }

    @Override
    public List<Integer> getIssuesCreatedPerDay(final User loggedInUser, final Collector collector, int daysPast)
    {
        int realDaysPast = Math.abs(daysPast);
        final Query query = JqlQueryBuilder.newClauseBuilder(getQuery(collector)).and().createdAfter("-" + realDaysPast + "d").buildQuery();

        try
        {
            final SearchResults result = searchService.search(loggedInUser, query, PagerFilter.getUnlimitedFilter());
            final List<Issue> issues = result.getIssues();

            final int[] createdPerDay = getNormalizedSums(realDaysPast, issues);

            final List<Integer> ret = new ArrayList<Integer>(createdPerDay.length);
            for (int sum : createdPerDay)
            {
                ret.add(sum);
            }
            return ret;
        }
        catch (SearchException e)
        {
            log.error("Error running search '" + query + "'", e);
        }

        return Collections.emptyList();
    }

    int[] getNormalizedSums(final int realDaysPast, final List<Issue> issues)
    {
        final int[] createdPerDay = new int[realDaysPast];
        final LocalDate dateTime = new LocalDate();
        LocalDate start = dateTime.dayOfYear().addToCopy(-realDaysPast);
        final int startDay = start.dayOfYear().get() + 1;

        for (Issue issue : issues)
        {
            final LocalDate created = new LocalDate(issue.getCreated());
            final int dayOfYear = created.dayOfYear().get();
            int arrayIndex = dayOfYear - startDay;
            //this means going into the past went across the year barrier.
            if(arrayIndex < 0)
            {
                arrayIndex = start.dayOfYear().getMaximumValue() + arrayIndex;
            }
            if (arrayIndex >= 0 && arrayIndex < realDaysPast)
            {
                createdPerDay[arrayIndex]++;
            }
        }
        return createdPerDay;
    }

    private Query getQuery(final Collector collector)
    {
        return JqlQueryBuilder.newBuilder().where().project(collector.getProjectId()).and().
                labels(COLLECTOR_LABEL_PREFIX + collector.getId()).
                endWhere().orderBy().createdDate(SortOrder.DESC).buildQuery();
    }
}
