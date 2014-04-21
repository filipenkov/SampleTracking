package com.atlassian.jira.charts.jfreechart;

import com.atlassian.jira.util.PathTraversalException;
import org.jfree.chart.servlet.DisplayChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static com.atlassian.jira.util.PathUtils.ensurePathInSecureDir;

/**
 * Display chart servlet that delegates processing to {@link DisplayChart}, after a few additional checks have been
 * done on the <code>filename</code> request parameter:
 * <ol>
 *     <li>If there is an attempted path traversal, it is logged and the Check for path traversal using </li>
 *     <li>Check that the </li>
 * </ol>
 *
 * @since v4.4.5
 */
public class DisplayChartServlet implements Servlet
{
    /**
     * Logger for DisplayChartServlet.
     */
    public static final Logger log = LoggerFactory.getLogger(DisplayChartServlet.class);

    /**
     * The servlet that we delegat to.
     */
    private final Servlet displayChart = new DisplayChart();

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        displayChart.init(config);
    }

    @Override
    public void destroy()
    {
        displayChart.destroy();
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        String filename = request.getParameter("filename");
        String tmpDir = System.getProperty("java.io.tmpdir");
        try
        {
            // the path that the filename will resolve to in JChart
            File path = new File(tmpDir, filename);

            ensurePathInSecureDir(tmpDir, path.getAbsolutePath());
            if (path.exists())
            {
                displayChart.service(request, response);
                return;
            }

            log.warn("File not found, returning 404 (filename='{}').", filename);
        }
        catch (PathTraversalException e)
        {
            log.warn("Possible path traversal attempt, returning 404 (filename='{}').", filename);
        }
        catch (IOException e)
        {
            log.error("Error checking path, returning 404 (filename='{}').", filename);
        }

        // fallback: return 404
        ((HttpServletResponse) response).sendError(404);
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return displayChart.getServletConfig();
    }

    @Override
    public String getServletInfo()
    {
        return displayChart.getServletInfo();
    }
}
