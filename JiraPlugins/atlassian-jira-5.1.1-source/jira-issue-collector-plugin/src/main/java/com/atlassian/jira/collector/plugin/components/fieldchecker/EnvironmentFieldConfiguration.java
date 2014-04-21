package com.atlassian.jira.collector.plugin.components.fieldchecker;

import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.issue.IssueFieldConstants;

public class EnvironmentFieldConfiguration implements FieldConfiguration {
    @Override
    public String getFieldName() {
        return IssueFieldConstants.ENVIRONMENT;
    }

    @Override
    public boolean isUsed(Collector collector) {
        return collector.isRecordWebInfo();
    }
}
