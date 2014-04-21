package com.atlassian.jira.plugins.monitor.database;

import com.atlassian.instrumentation.Instrument;
import com.atlassian.instrumentation.operations.OpCounter;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.plugins.monitor.rrd4j.Graph;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

import java.awt.*;

import static com.atlassian.jira.instrumentation.Instrumentation.getInstrument;
import static com.atlassian.jira.plugins.monitor.MonitorConstants.DEFAULT_HEARTBEAT;
import static com.atlassian.jira.plugins.monitor.MonitorConstants.DEFAULT_STEP;
import static org.rrd4j.DsType.DERIVE;

/**
 * This graph plots the average time that it takes for JIRA do to a database reads and writes.
 *
 * @since v5.0.3
 */
public class DatabaseReadWritesGraph implements Graph
{
    @Override
    public String getTitleI18nKey()
    {
        return "admin.monitor.database.read.writes";
    }

    @Override
    public void addDatasources(RrdDef rrdDef)
    {
        rrdDef.addDatasource("db.reads.inv", DERIVE, DEFAULT_HEARTBEAT, 0, Double.NaN);
        rrdDef.addDatasource("db.reads.time", DERIVE, DEFAULT_HEARTBEAT, 0, Double.NaN);
        rrdDef.addDatasource("db.writes.inv", DERIVE, DEFAULT_HEARTBEAT, 0, Double.NaN);
        rrdDef.addDatasource("db.writes.time", DERIVE, DEFAULT_HEARTBEAT, 0, Double.NaN);
    }

    @Override
    public void setDatasourceValues(Sample sample)
    {
        Instrument dbReads = getInstrument(InstrumentationName.DB_READS.getInstrumentName());
        if (dbReads instanceof OpCounter)
        {
            OpCounter counter = (OpCounter) dbReads;

            sample.setValue("db.reads.inv", counter.getInvocationCount());
            sample.setValue("db.reads.time", counter.getCpuTime());
        }

        Instrument dbWrites = getInstrument(InstrumentationName.DB_WRITES.getInstrumentName());
        if (dbWrites instanceof OpCounter)
        {
            OpCounter writesCounter = (OpCounter) dbWrites;
            sample.setValue("db.writes.inv", writesCounter.getInvocationCount());
            sample.setValue("db.writes.time", writesCounter.getCpuTime());
        }
    }

    @Override
    public void addGraphElements(RrdGraphDef rrdGraphDef, String rrdPath)
    {
        rrdGraphDef.setValueAxis(10, 1);

        rrdGraphDef.datasource("db.reads.rate", rrdPath, "db.reads.inv", ConsolFun.AVERAGE);
        rrdGraphDef.datasource("db.reads.rate.per.second", String.format("db.reads.rate,%d,/", DEFAULT_STEP));
        rrdGraphDef.datasource("db.writes.rate", rrdPath, "db.writes.inv", ConsolFun.AVERAGE);
        rrdGraphDef.datasource("db.writes.rate.per.second", String.format("db.writes.rate,%d,/", DEFAULT_STEP));

        rrdGraphDef.line("db.writes.rate.per.second", Color.BLUE, "Writes / sec" + RrdGraphDef.ALIGN_LEFTNONL_MARKER);
        rrdGraphDef.gprint("db.writes.rate.per.second", ConsolFun.AVERAGE, " avg:%2.2f");
        rrdGraphDef.gprint("db.writes.rate.per.second", ConsolFun.MAX, "max: %2.2f" + RrdGraphDef.ALIGN_RIGHT_MARKER);

        rrdGraphDef.line("db.reads.rate.per.second", Color.RED, "Reads / sec" + RrdGraphDef.ALIGN_LEFTNONL_MARKER);
        rrdGraphDef.gprint("db.reads.rate.per.second", ConsolFun.AVERAGE, " avg:%2.2f");
        rrdGraphDef.gprint("db.reads.rate.per.second", ConsolFun.MAX, "max: %2.2f" + RrdGraphDef.ALIGN_RIGHT_MARKER);
    }
}
