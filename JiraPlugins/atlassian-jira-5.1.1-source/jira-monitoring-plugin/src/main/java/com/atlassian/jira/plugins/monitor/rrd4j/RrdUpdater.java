package com.atlassian.jira.plugins.monitor.rrd4j;

import com.atlassian.jira.plugins.monitor.MonitorService;
import com.atlassian.jira.startup.JiraHomePathLocator;
import com.atlassian.jira.startup.MultiTenantJiraHomeLocator;
import com.google.common.collect.Maps;
import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.plugins.monitor.MonitorConstants.DEFAULT_STEP;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.rrd4j.ConsolFun.AVERAGE;

/**
 * This class creates the RRD definitions for the monitoring plugin.
 *
 * @since v5.0.3
 */
public class RrdUpdater implements MonitorService
{
    private static final Logger log = LoggerFactory.getLogger(RrdUpdater.class);
    private final JiraHomePathLocator jiraHomePathLocator = new MultiTenantJiraHomeLocator();
    private final GraphRegistry graphRegistry;
    private final RrdBackendFactory rrdBackendFactory;

    private Map<Graph, RrdDb> rrdDbs = Maps.newHashMap();

    public RrdUpdater(GraphRegistry graphRegistry, RrdBackendFactory rrdBackendFactory)
    {
        this.graphRegistry = graphRegistry;
        this.rrdBackendFactory = rrdBackendFactory;
    }

    @Override
    public void start() throws Exception
    {
        createOrOpenDatabaseFiles();
    }

    @Override
    public void stop() throws Exception
    {
        closeDatabaseFiles();
    }

    public File getRrdDbPathFor(Graph graph)
    {
        File rrDbDir = new File(jiraHomePathLocator.getJiraHome() + File.separator + "monitor");
        if (!rrDbDir.exists() && !rrDbDir.mkdirs())
        {
            log.error("Error creating database dir: {}", rrDbDir.getAbsolutePath());
        }

        return new File(rrDbDir, graph.getClass().getSimpleName() + ".rrd4j");
    }

    public void addSample() throws IOException
    {
        for (Graph graph : graphRegistry.getGraphs())
        {
            RrdDb rrdDb = rrdDbs.get(graph);
            if (rrdDb != null)
            {
                Sample sample = rrdDb.createSample();
                sample.setTime(System.currentTimeMillis() / 1000);
                graph.setDatasourceValues(sample);

                try
                {
                    sample.update();
                }
                catch (IllegalArgumentException t)
                {
                    log.debug("Error adding sample", t);
                }
            }
        }
    }

    private void createOrOpenDatabaseFiles() throws IOException
    {
        // create 1 file per Graph
        for (Graph graph : graphRegistry.getGraphs())
        {
            File graphDbFile = getRrdDbPathFor(graph);
            if (graphDbFile.exists())
            {
                log.info("Reusing existing RrdDb: {}", graphDbFile);
                rrdDbs.put(graph, new RrdDb(graphDbFile.getAbsolutePath(), rrdBackendFactory));
            }
            else
            {
                RrdDef rrdDef = createRrdDef(graphDbFile);
                graph.addDatasources(rrdDef);

                log.info("Creating new RrdDb: {}", graphDbFile);
                rrdDbs.put(graph, new RrdDb(rrdDef, rrdBackendFactory));
            }
        }
    }

    private void closeDatabaseFiles() throws IOException
    {
        for (RrdDb rrdDb : rrdDbs.values())
        {
            try
            {
                rrdDb.close();
            }
            catch (IOException e)
            {
                log.error("Error closing RrdDb: " + rrdDb, e);
            }
        }
    }

    private RrdDef createRrdDef(File rrdbFile)
    {
        int step = DEFAULT_STEP;
        RrdDef rrdDef = new RrdDef(rrdbFile.getAbsolutePath(), step);

        // store average for every 5s over the last 24hrs
        rrdDef.addArchive(AVERAGE, 0.5, 1, toSeconds(HOURS, 24 / step));

        // store average, min, and max for every hour in the last 7 days
//        rrdDef.addArchive(MIN, 0.5, toSeconds(HOURS, 1) / step, toSeconds(DAYS, 7) / step);
//        rrdDef.addArchive(MAX, 0.5, toSeconds(HOURS, 1) / step, toSeconds(DAYS, 7) / step);
        rrdDef.addArchive(AVERAGE, 0.5, 120, 168);

        return rrdDef;
    }

    private int toSeconds(TimeUnit unit, int duration)
    {
        return (int) SECONDS.convert(duration, unit);
    }
}
