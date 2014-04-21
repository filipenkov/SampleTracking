package com.atlassian.jira.issue.fields.config;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.issuetype.IssueType;

import org.apache.commons.collections.MultiMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A field config scheme is a set of {@link FieldConfig} objects that has been associated to a particular set of {@link IssueType}s
 * and then associated to a series of contexts ({@link JiraContextNode}). Methods return {@link List} will generally return nulls
 * unless specified.
 *
 */
public interface FieldConfigScheme
{
    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    String getName();

    String getDescription();

    /**
     * Returns a Map whose key is a {@link String} of issue types and the value is the {@link FieldConfig}
     * for that issue type
     * @return Map of {@link FieldConfig} keyed by name. Null if nothing associated
     */
    Map<String, FieldConfig> getConfigs();

    Long getId();

    /**
     * Returns a list of {@link JiraContextNode} objects this scheme is relevent to
     * @return list of {@link JiraContextNode}. an empty list if no contexts
     */
    List<JiraContextNode> getContexts();

    boolean isInContext(IssueContext issueContext);

    List<GenericValue> getAssociatedProjectCategories();

    /**
     * The associated projects, or an empty list if none associated.
     * @return a not null list.
     */
    List<GenericValue> getAssociatedProjects();

    Set<GenericValue> getAssociatedIssueTypes();

    boolean isGlobal();

    boolean isAllProjects();

    boolean isAllIssueTypes();

    boolean isEnabled();

    boolean isBasicMode();

    /**
     * Returns a Map whose key is a {@link FieldConfig} and values are associated a {@link Collection} Issue Type {@link GenericValue}
     * @return MultiMap of configs.
     */
    MultiMap getConfigsByConfig();

    /**
     * Returns the one and only config for this scheme iff there's only one config associated
     * @return The associated {@link FieldConfig}. Null if no configs, or more than one config
     */
    FieldConfig getOneAndOnlyConfig();

    ConfigurableField getField();

    public class Builder
    {
        private Long id;
        private String name;
        private String description;
        private String fieldId;
        private Map<String, FieldConfig> configs;
        private FieldConfigContextPersister configContextPersister;

        public Builder()
        {}

        public Builder(final FieldConfigScheme scheme)
        {
            if (scheme != null)
            {
                id = scheme.getId();
                name = scheme.getName();
                description = scheme.getDescription();
                configs = scheme.getConfigs();
                if (scheme instanceof FieldConfigSchemeImpl)
                {
                    final FieldConfigSchemeImpl fieldConfigSchemeImpl = (FieldConfigSchemeImpl) scheme;
                    configContextPersister = fieldConfigSchemeImpl.getFieldConfigContextPersister();
                    fieldId = fieldConfigSchemeImpl.getFieldId();
                }
                else
                {
                    final ConfigurableField field = scheme.getField();
                    fieldId = (field != null) ? field.getId() : null;
                }
            }
        }

        public Builder setId(final Long id)
        {
            this.id = id;
            return this;
        }

        public Builder setFieldId(final String fieldId)
        {
            this.fieldId = fieldId;
            return this;
        }

        public Builder setName(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder setDescription(final String description)
        {
            this.description = description;
            return this;
        }

        public Builder setConfigs(final Map<String, FieldConfig> configs)
        {
            this.configs = configs;
            return this;
        }

        public Builder setFieldConfigContextPersister(final FieldConfigContextPersister configContextPersister)
        {
            this.configContextPersister = configContextPersister;
            return this;
        }

        public FieldConfigScheme toFieldConfigScheme()
        {
            return new FieldConfigSchemeImpl(id, fieldId, name, description, configs, configContextPersister);
        }
    }
}
