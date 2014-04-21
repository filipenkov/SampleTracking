package com.atlassian.jira.charts.jfreechart.util;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.atlassian.jira.charts.ChartFactory.PeriodName.daily;
import static com.atlassian.jira.charts.ChartFactory.PeriodName.hourly;
import static com.atlassian.jira.charts.ChartFactory.PeriodName.monthly;
import static com.atlassian.jira.charts.ChartFactory.PeriodName.quarterly;
import static com.atlassian.jira.charts.ChartFactory.PeriodName.weekly;
import static com.atlassian.jira.charts.ChartFactory.PeriodName.yearly;
import com.atlassian.jira.local.ListeningTestCase;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

/**
 * @since 4.0
 */
public class TestChartUtil extends ListeningTestCase
{

    @Test
    public void testGetTimePeriodClass()
    {
        assertEquals(Day.class, ChartUtil.getTimePeriodClass(daily));
        assertEquals(Hour.class, ChartUtil.getTimePeriodClass(hourly));
        assertEquals(Week.class, ChartUtil.getTimePeriodClass(weekly));
        assertEquals(Month.class, ChartUtil.getTimePeriodClass(monthly));
        assertEquals(Quarter.class, ChartUtil.getTimePeriodClass(quarterly));
        assertEquals(Year.class, ChartUtil.getTimePeriodClass(yearly));        
    }
}
