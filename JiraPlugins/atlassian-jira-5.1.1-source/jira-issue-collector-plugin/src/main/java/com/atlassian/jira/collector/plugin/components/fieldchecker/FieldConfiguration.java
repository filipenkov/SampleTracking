package com.atlassian.jira.collector.plugin.components.fieldchecker;


import com.atlassian.jira.collector.plugin.components.Collector;

public interface FieldConfiguration {
    String getFieldName();
    boolean isUsed(Collector collector);
}
