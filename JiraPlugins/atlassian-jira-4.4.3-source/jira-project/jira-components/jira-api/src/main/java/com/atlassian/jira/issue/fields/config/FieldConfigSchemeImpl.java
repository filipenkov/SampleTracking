package com.atlassian.jira.issue.fields.config;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersister;
import com.atlassian.jira.util.MapUtils;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FieldConfigSchemeImpl implements FieldConfigScheme
{
    // ------------------------------------------------------------------------------------------------------- Constants
    private static final Logger log = Logger.getLogger(FieldConfigSchemeImpl.class);

    // ------------------------------------------------------------------------------------------------- Type Properties
    private final Long id;
    private final String name;
    private final String description;
    private final String fieldId;
    private final Map<String, FieldConfig> configs;
    private final FieldConfigContextPersister configContextPersister;

    private final LazyReference<List<JiraContextNode>> applicableContexts = new LazyReference<List<JiraContextNode>>()
    {
        @Override
        protected List<JiraContextNode> create()
        {
            if (configContextPersister == null)
            {
                // this should only happen when a new Issue Type Scheme is created through the UI and hasn't hit the db yet - therefore no context anyway
                return Collections.emptyList();
            }
            final List<JiraContextNode> applicableContexts = new ArrayList<JiraContextNode>(
                configContextPersister.getAllContextsForConfigScheme(FieldConfigSchemeImpl.this));
            Collections.sort(applicableContexts);
            return Collections.unmodifiableList(applicableContexts);
        }
    };

    private final LazyReference<MultiMap> configsByConfig = new LazyReference<MultiMap>()
    {
        @Override
        protected MultiMap create()
        {
            return MapUtils.invertMap(getConfigs());
        }
    };

    // ---------------------------------------------------------------------------------------------------- Constructors

    public FieldConfigSchemeImpl(final Long id, final String fieldId, final String name, final String description, final Map<String, FieldConfig> configs, final FieldConfigContextPersister configContextPersister)
    {
        this.id = id;
        this.fieldId = fieldId;
        this.name = StringUtils.abbreviate(name, FieldConfigPersister.ENTITY_LONG_TEXT_LENGTH);
        this.description = description;
        this.configs = (configs != null) ? CollectionUtil.copyAsImmutableMap(configs) : Collections.<String, FieldConfig> emptyMap();
        this.configContextPersister = configContextPersister;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods

    // -------------------------------------------------------------------------------------- Basic accessors & mutators

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public Map<String, FieldConfig> getConfigs()
    {
        return configs;
    }

    public List<JiraContextNode> getContexts()
    {
        return applicableContexts.get();
    }

    public Long getId()
    {
        return id;
    }

    String getFieldId()
    {
        return fieldId;
    }

    public ConfigurableField getField()
    {
        return ComponentAccessor.getFieldAccessor().getConfigurableField(fieldId);
    }

    // ----------------------------------------------------------------------------------------- COnvenience Context Methods

    public boolean isInContext(final IssueContext issueContext)
    {
        final List<JiraContextNode> contexts = getContexts();
        if (contexts != null)
        {
            for (final JiraContextNode contextNode : contexts)
            {
                if (contextNode.isInContext(issueContext))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public List<GenericValue> getAssociatedProjectCategories()
    {
        if (isEnabled())
        {
            final List<GenericValue> list = new LinkedList<GenericValue>();

            for (final JiraContextNode contextNode : getContexts())
            {
                if ((contextNode.getProjectCategory() != null) && (contextNode.getProject() == null))
                {
                    list.add(contextNode.getProjectCategory());
                }
            }
            return list.isEmpty() ? null : list;
        }
        else
        {
            return null;
        }
    }

    public List<GenericValue> getAssociatedProjects()
    {
        if (isEnabled())
        {
            final List<GenericValue> list = new LinkedList<GenericValue>();

            for (final JiraContextNode contextNode : getContexts())
            {
                if (contextNode.getProject() != null)
                {
                    list.add(contextNode.getProject());
                }
            }
            return list;
        }
        else
        {
            return Collections.emptyList();
        }
    }

    // TODO: This may be broken, this needs investigation
    public Set<GenericValue> getAssociatedIssueTypes()
    {
        Set<GenericValue> associatedIssueTypes = null;
        if (isEnabled())
        {
            //TODO: Should we really return null if configs is empty?
            if ((getConfigs() != null) && !getConfigs().isEmpty())
            {
                associatedIssueTypes = new HashSet<GenericValue>();
                final ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
                for (final String issueTypeId : getConfigs().keySet())
                {
                    associatedIssueTypes.add(constantsManager.getIssueType(issueTypeId));
                }
            }
        }
        return associatedIssueTypes;
    }

    public boolean isGlobal()
    {
        return isAllProjects() && isAllIssueTypes();
    }

    public boolean isAllProjects()
    {
        if (isEnabled())
        {
            for (final JiraContextNode contextNode : getContexts())
            {
                if ((contextNode.getProjectCategory() == null) && (contextNode.getProject() == null))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAllIssueTypes()
    {
        final Set<GenericValue> issueTypes = getAssociatedIssueTypes();
        return (issueTypes != null) && issueTypes.contains(null);
    }

    public boolean isEnabled()
    {
        return !getContexts().isEmpty();
    }

    public boolean isBasicMode()
    {
        final MultiMap configsByConfig = getConfigsByConfig();
        return (configsByConfig == null) || (configsByConfig.size() <= 1);
    }

    public MultiMap getConfigsByConfig()
    {
        return configsByConfig.get();
    }

    public FieldConfig getOneAndOnlyConfig()
    {
        final MultiMap configsByConfig = getConfigsByConfig();
        if ((configsByConfig != null) && (configsByConfig.size() == 1))
        {
            return (FieldConfig) configsByConfig.keySet().iterator().next();
        }
        else
        {
            if (configsByConfig != null)
            {
                log.warn("There is not exactly one config for this scheme (" + getId() + "). Configs are " + configsByConfig + ".");
            }
            return null;
        }
    }

    FieldConfigContextPersister getFieldConfigContextPersister()
    {
        return configContextPersister;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof FieldConfigScheme))
        {
            return false;
        }
        final FieldConfigScheme rhs = (FieldConfigScheme) o;
        return new EqualsBuilder().append(getId(), rhs.getId()).append(getName(), rhs.getName()).append(getDescription(), rhs.getDescription()).isEquals();
    }

    public int compareTo(final Object obj)
    {
        final FieldConfigScheme o = (FieldConfigScheme) obj;
        return new CompareToBuilder().append(getId(), o.getId()).append(getName(), o.getName()).append(getDescription(), o.getDescription()).toComparison();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(77, 147).append(getId()).append(getName()).append(getDescription()).toHashCode();
    }
}
