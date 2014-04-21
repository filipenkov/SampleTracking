package com.atlassian.jira.charts.jfreechart;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Calendar;

public class TestChartHelper extends ListeningTestCase
{

    private JFreeChart getChart()
    {
        final Calendar now = Calendar.getInstance();
        final DefaultCategoryDataset sourceSet = new DefaultCategoryDataset();
        final boolean legend = false;
        final boolean tooltips = false;
        final boolean urls = false;

        final StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {

                now.add(Calendar.DAY_OF_YEAR, 1);

                stringBuffer.setLength(0);

                sourceSet.addValue(
                        new Integer(stringBuffer.append(i).append(j).toString()),
                        String.valueOf(i),
                        now.getTime());
            }
        }

        return ChartFactory.createBarChart("fooTitle", "fooYLabel", "fooXLabel", sourceSet,
                PlotOrientation.VERTICAL, legend, tooltips, urls);
    }

    @Test
    public void testGetRenderingInfo() throws Exception
    {
        final ChartHelper chartHelper = new ChartHelper(getChart());

        assertNull(chartHelper.getRenderingInfo());
        chartHelper.generate(512, 384);
        assertNotNull(chartHelper.getRenderingInfo());
    }

    @Test
    public void testGetLocation() throws Exception
    {
        final ChartHelper chartHelper = new ChartHelper(getChart());

        assertNull(chartHelper.getLocation());
        chartHelper.generate(512, 384);
        assertNotNull(chartHelper.getLocation());
    }

    @Test
    public void testGetImageMap() throws Exception
    {
        final ChartHelper chartHelper = new ChartHelper(getChart());

        try
        {
            assertNull(chartHelper.getImageMap());
            fail("should have thrown NPE.");
        }
        catch (NullPointerException e)
        {
            //yay
        }
        chartHelper.generate(512, 384);
        assertNotNull(chartHelper.getImageMap());
        assertNotNull(chartHelper.getImageMapName());
    }
}
