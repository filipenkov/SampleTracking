package com.atlassian.jira.collector.plugin.components;

import java.util.List;

public interface CollectorStore
{

    List<Collector> getCollectors(final Long projectId);

    Collector addCollector(Collector input);

    Collector getCollector(String collectorId);

    boolean enableCollector(String collectorId);

    boolean disableCollector(String collectorId);

    void deleteCollector(final Long projectId, String collectorId);

    Long getArchivedProjectId(final String collectorId);
}
