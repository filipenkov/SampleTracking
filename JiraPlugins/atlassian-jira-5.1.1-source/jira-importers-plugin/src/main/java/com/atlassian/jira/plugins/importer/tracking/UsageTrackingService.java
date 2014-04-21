package com.atlassian.jira.plugins.importer.tracking;

public interface UsageTrackingService {
    void includeTrackingWhenActive();

    boolean isActive();

    boolean isTrackingStatusDefined();

    void activate();

    void deactivate();
}
