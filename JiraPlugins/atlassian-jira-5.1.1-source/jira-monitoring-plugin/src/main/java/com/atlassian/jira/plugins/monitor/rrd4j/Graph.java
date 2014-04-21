package com.atlassian.jira.plugins.monitor.rrd4j;

import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

/**
 * Abstract base class for graphs.
 *
 * @since v5.0.3
 */
public interface Graph
{
    String getTitleI18nKey();
    void addDatasources(RrdDef rrdDef);
    void setDatasourceValues(Sample sample);
    void addGraphElements(RrdGraphDef rrdGraphDef, String rrdPath);
}
