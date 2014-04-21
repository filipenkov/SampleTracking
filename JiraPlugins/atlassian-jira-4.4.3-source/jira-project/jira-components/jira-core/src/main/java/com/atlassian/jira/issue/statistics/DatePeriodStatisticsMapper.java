package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.LuceneUtils;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriod;

import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * A StatsMapper that takes the document constant name (lucene) and a JFreeChart TimePeriod class, and rounds the dates
 * to the appropriate time period
 *
 * @see org.jfree.data.time.TimePeriod
 */
public class DatePeriodStatisticsMapper extends AbstractDateStatisticsMapper
{
    private final Class timePeriodClass;
    private final TimeZone periodTimeZone;

    /**
     * Creates a new DatePeriodStatisticsMapper using the default time zone. Note that this will produce incorrect
     * results for users that have configured a custom time zone.
     *
     * @deprecated Use {@link #DatePeriodStatisticsMapper(Class, String, java.util.TimeZone)} instead. Since v4.4.
     */
    @Deprecated
    public DatePeriodStatisticsMapper(Class timePeriodClass, String documentConstant)
    {
        this(timePeriodClass, documentConstant, TimeZone.getDefault());
    }

    /**
     * Creates a new DatePeriodStatisticsMapper using the given time zone.
     */
    public DatePeriodStatisticsMapper(Class timePeriodClass, String documentConstant, TimeZone periodTimeZone)
    {
        super(documentConstant);
        this.timePeriodClass = timePeriodClass;
        this.periodTimeZone = periodTimeZone;
    }

    public SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest)
    {
        TimePeriod timePeriod = (TimePeriod) value;
        Date startDate = timePeriod.getStart();
        Date endDate = new Date(timePeriod.getEnd().getTime());

        //copy the old searchrequest's query
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
        builder.where().defaultAnd().addDateRangeCondition(documentConstant, startDate, endDate);

        return new SearchRequest(builder.buildQuery(), searchRequest.getOwnerUserName(), null, null);
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        Date date = LuceneUtils.stringToDate(documentValue);
        return RegularTimePeriod.createInstance(timePeriodClass, date, periodTimeZone);
    }

    public Comparator getComparator()
    {
        return new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                if (o1 == null)
                {
                    return -1;
                }

                return ((TimePeriod) o1).compareTo(o2);
            }
        };
    }

    @Override
    public boolean isValidValue(final Object value)
    {
        return value != null;
    }
}
