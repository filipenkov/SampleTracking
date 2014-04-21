package com.atlassian.jira.collector.plugin.components;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;

import java.util.Map;
import java.util.Set;

public interface CollectorFieldValidator {

    Map<String,Set<String>> getRequiredInvalidFieldsForProject(final User loggedUser, final Project project);

	Set<String> getRequiredInvalidFieldsForIssueType(final User loggedUser, final Project project, final String issueTypeId);

	Set<String> getAllowedCustomFieldIds(final Project project, final String issueType);

	boolean isFieldAllowedInCustomCollector(final String fieldId);
}
