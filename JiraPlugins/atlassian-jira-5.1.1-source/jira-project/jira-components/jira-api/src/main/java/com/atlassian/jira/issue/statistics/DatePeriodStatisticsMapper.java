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
public class DatePeriodStatisticsMapper implements StatisticsMapper<TimePeriod>
{
    private final Class timePeriodClass;
    private final TimeZone periodTimeZone;
    private final String documentConstant;

    /**
     * Creates a new DatePeriodStatisticsMapper using the given time zone.
     */
    public DatePeriodStatisticsMapper(Class timePeriodClass, String documentConstant, TimeZone periodTimeZone)
    {
        this.documentConstant = documentConstant;
        this.timePeriodClass = timePeriodClass;
        this.periodTimeZone = periodTimeZone;
    }

    @Override
    public SearchRequest getSearchUrlSuffix(TimePeriod value, SearchRequest searchRequest)
     {
        TimePeriod timePeriod = value;
        Date startDate = timePeriod.getStart();
        Date endDate = new Date(timePeriod.getEnd().getTime());

        //copy the old searchrequest's query
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
        builder.where().defaultAnd().addDateRangeCondition(documentConstant, startDate, endDate);

        return new SearchRequest(builder.buildQuery(), searchRequest.getOwnerUserName(), null, null);
    }

    @Override
    public String getDocumentConstant()
    {
        return documentConstant;
    }

    public TimePeriod getValueFromLuceneField(String documentValue)
    {
        Date date = LuceneUtils.stringToDate(documentValue);
        if (date == null)
        {
            return null;
        }
        return RegularTimePeriod.createInstance(timePeriodClass, date, periodTimeZone);
    }

    public Comparator<TimePeriod> getComparator()
    {
        return new Comparator<TimePeriod>()
        {
            public int compare(TimePeriod timePeriod1, TimePeriod timePeriod2)
            {
                if (timePeriod1 == null)
                {
                    return -1;
                }

                return timePeriod1.compareTo(timePeriod2);
            }
        };
    }


    @Override
    public boolean isValidValue(TimePeriod value)
    {
        return value != null;
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

}