package com.atlassian.jira.collector.plugin.components;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.project.Project;

import java.util.List;
import java.util.Map;

public interface CollectorService
{

    ServiceOutcome<Collector> getCollector(final String collectorId);

    ServiceOutcome<Map<Long, List<Collector>>> getCollectorsPerProject(final User remoteUser);
    
    ServiceOutcome<List<Collector>> getCollectors(final User remoteUser, final Project project);

    ServiceOutcome<Collector> validateCreateCollector(final User remoteUser, final String name, Long projectId, Long issueTypeId, String reporter, String description,
            final String templateId, boolean recordWebInfo, final boolean useCredentials, final Trigger trigger, final String customMessage,
            final List<String> customTemplateFields,
            final String customTemplateTitle, final String customTemplateLabels);

    ServiceOutcome<Collector> createCollector(final User loggedInUser, ServiceOutcome<Collector> outcome);

    ServiceOutcome<Boolean> enableCollector(final User remoteUser, final Project project, String collectorId);

    ServiceOutcome<Boolean> disableCollector(final User remoteUser, final Project project, String collectorId);

    ServiceOutcome<Collector> validateDeleteCollector(final User remoteUser, final Project project, String collectorId);

	ServiceOutcome<Boolean> validateAddCollectorPremission(final Project project, final User user);

    void deleteCollector(User loggedInUser, ServiceOutcome<Collector> validationResult);

    Long getArchivedProjectId(String collectorId);
}
