package com.atlassian.jira.plugins.monitor.rrd4j;

import java.util.List;

/**
 * The graph registry.
 *
 * @since v5.0.3
 */
public class GraphRegistry
{
    private final List<Graph> graphs;

    public GraphRegistry(List<Graph> graphs)
    {
        this.graphs = graphs;
    }

    public List<Graph> getGraphs()
    {
        return graphs;
    }
}
