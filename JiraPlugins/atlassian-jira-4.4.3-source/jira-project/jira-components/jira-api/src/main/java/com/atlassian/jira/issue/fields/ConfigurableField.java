package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;

import java.util.List;

/**
 * ConfigurableField are fields which have {@link FieldConfigItemType} that can be stored for a given
 * {@link JiraContextNode}
 */
public interface ConfigurableField extends OrderableField
{
    /**
     * Returns a List of {@link FieldConfigItemType} objects. This opens up possibilties for configurable custom fields
     *
     * @return List of {@link FieldConfigItemType} @NotNull
     */
    List getConfigurationItemTypes();

    List getAssociatedProjects();

    FieldConfig getRelevantConfig(IssueContext issueContext);
}
