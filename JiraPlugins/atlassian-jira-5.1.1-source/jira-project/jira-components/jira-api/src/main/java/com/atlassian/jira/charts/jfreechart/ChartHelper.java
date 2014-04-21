package com.atlassian.jira.charts.jfreechart;

import com.atlassian.core.util.RandomGenerator;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.servlet.ServletUtilities;

import java.io.IOException;

/**
 * A nice utility class to manage the generation of a charts.
 *
 * @since v4.0
 */
public class ChartHelper
{
    private final static Logger log = Logger.getLogger(ChartHelper.class);

    private JFreeChart chart;
    private ChartRenderingInfo renderingInfo = null;
    private String location = null;
    private String imageMap;
    private String imageMapName;

    public ChartHelper(JFreeChart chart)
    {
        this.chart = chart;
    }

    public JFreeChart getChart()
    {
        return chart;
    }

    public void generate(int width, int height) throws IOException
    {
        if (log.isDebugEnabled())
        {
            log.debug("ChartHelper.generate() Create a ChartRenderingInfo.");
        }
        renderingInfo = new ChartRenderingInfo();
        // NOTE: the chart will be deleted directly after it is streamed once.
        // Profile the call to JFreeChart
        final String logLine = "ChartHelper calling JFreeChart: ServletUtilities.saveChartAsPNG()";
        UtilTimerStack.push(logLine);
        try
        {
            log.info("ChartHelper.generate(): Use JFreeChart to create PNG file.");
            location = ServletUtilities.saveChartAsPNG(chart, width, height, renderingInfo, null);
            log.info("ChartHelper.generate(): PNG file created in '" + location + "'.");
        }
        finally
        {
            UtilTimerStack.pop(logLine);
        }
    }

    public ChartRenderingInfo getRenderingInfo()
    {
        return renderingInfo;
    }

    public String getLocation()
    {
        return location;
    }

    public String getImageMap()
    {
        if (imageMap == null)
        {
            imageMapName = "chart-" + RandomGenerator.randomString(5);
            imageMap = ChartUtilities.getImageMap(imageMapName, renderingInfo);
        }

        return imageMap;
    }

    public String getImageMapName()
    {
        return imageMapName;
    }
}
