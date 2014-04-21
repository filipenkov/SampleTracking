package com.atlassian.jira.plugins.monitor.database;

import com.atlassian.instrumentation.Instrument;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.plugins.monitor.rrd4j.Graph;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

import java.awt.*;

import static com.atlassian.jira.instrumentation.Instrumentation.getInstrument;
import static com.atlassian.jira.instrumentation.InstrumentationName.DBCP_ACTIVE;
import static com.atlassian.jira.instrumentation.InstrumentationName.DBCP_IDLE;
import static com.atlassian.jira.plugins.monitor.MonitorConstants.DEFAULT_HEARTBEAT;
import static org.rrd4j.DsType.GAUGE;

/**
 * This graph plots the active and idle connections in the JIRA connection pool.
 *
 * @since v5.0.3
 */
public class ConnectionPoolGraph implements Graph
{
    @Override
    public String getTitleI18nKey()
    {
        return "admin.monitor.connection.pool";
    }

    @Override
    public void addDatasources(RrdDef rrdDef)
    {
        rrdDef.addDatasource(DBCP_ACTIVE.getInstrumentName(), GAUGE, DEFAULT_HEARTBEAT, 0, Double.NaN);
        rrdDef.addDatasource(DBCP_IDLE.getInstrumentName(), GAUGE, DEFAULT_HEARTBEAT, 0, Double.NaN);
    }

    @Override
    public void setDatasourceValues(Sample sample)
    {
        Instrument active = getInstrument(InstrumentationName.DBCP_ACTIVE.getInstrumentName());
        if (active != null)
        {
            sample.setValue(DBCP_ACTIVE.getInstrumentName(), active.getValue());
        }
        Instrument idle = getInstrument(InstrumentationName.DBCP_IDLE.getInstrumentName());
        if (idle != null)
        {
            sample.setValue(DBCP_IDLE.getInstrumentName(), idle.getValue());
        }
    }

    @Override
    public void addGraphElements(RrdGraphDef rrdGraphDef, String rrdPath)
    {
        Instrument maxActive = getInstrument(InstrumentationName.DBCP_MAX.getInstrumentName());
        if (maxActive != null)
        {
            rrdGraphDef.setMaxValue(maxActive.getValue());
        }

        rrdGraphDef.datasource("active.conns", rrdPath, DBCP_ACTIVE.getInstrumentName(), ConsolFun.AVERAGE);
        rrdGraphDef.datasource("idle.conns", rrdPath, DBCP_IDLE.getInstrumentName(), ConsolFun.AVERAGE);

        rrdGraphDef.area("active.conns", Color.BLUE, "NumActive" + RrdGraphDef.ALIGN_LEFTNONL_MARKER);
        rrdGraphDef.gprint("active.conns", ConsolFun.MIN, "min: %.0f");
        rrdGraphDef.gprint("active.conns", ConsolFun.MAX, "max: %.0f" + RrdGraphDef.ALIGN_RIGHT_MARKER);

        rrdGraphDef.stack("idle.conns", Color.GREEN, "NumIdle" + RrdGraphDef.ALIGN_LEFTNONL_MARKER);
        rrdGraphDef.gprint("idle.conns", ConsolFun.MIN, "min: %.0f");
        rrdGraphDef.gprint("idle.conns", ConsolFun.MAX, "max: %.0f" + RrdGraphDef.ALIGN_RIGHT_MARKER);
    }
}
