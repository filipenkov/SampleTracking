package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.NamedDefault;
import com.atlassian.jira.projectconfig.beans.SimpleFieldConfigScheme;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides context for the field configs summary panel, in particular the field config scheme
 * and field configs (fieldLayouts), etc.
 *
 * @since v4.4
 */
public class FieldsSummaryPanelContextProvider implements CacheableContextProvider
{
    static final String CONTEXT_FIELD_CONFIG_SCHEME_KEY = "fieldConfigScheme";
    static final String CONTEXT_FIELD_CONFIGS_KEY = "fieldConfigs";

    private final FieldLayoutManager fieldLayoutManager;
    private final ContextProviderUtils contextProviderUtils;
    private final TabUrlFactory tabUrlFactory;
    private final ComparatorFactory comparatorFactory;

    public FieldsSummaryPanelContextProvider(final FieldLayoutManager fieldLayoutManager,
            final ContextProviderUtils contextProviderUtils, final TabUrlFactory tabUrlFactory,
            final ComparatorFactory comparatorFactory)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.contextProviderUtils = contextProviderUtils;
        this.tabUrlFactory = tabUrlFactory;
        this.comparatorFactory = comparatorFactory;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Project project = (Project) context.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);

        final I18nHelper i18nHelper = (I18nHelper) context.get(ContextProviderUtils.CONTEXT_I18N_KEY);

        final List<FieldLayoutScheme> fieldLayoutSchemes = fieldLayoutManager.getFieldLayoutSchemes();

        final FieldConfigurationScheme fieldConfigurationScheme =
                fieldLayoutManager.getFieldConfigurationScheme(project);

        final SimpleFieldConfigScheme fieldConfigScheme;
        final Collection<SimpleFieldConfig> fieldConfigs;

        if(fieldLayoutSchemes == null || fieldLayoutSchemes.size() == 0 || fieldConfigurationScheme == null)
        {
            fieldConfigScheme = getDefaultFieldConfigScheme(project, i18nHelper);
            fieldConfigs = Collections.singletonList(SimpleFieldConfig.getSystemDefaultSimpleFieldConfig(getSystemDefaultEditConfigUrl(), true));
        }
        else
        {
            fieldConfigScheme = new SimpleFieldConfigScheme(fieldConfigurationScheme,
                    null, getEditSchemeUrl());
            fieldConfigs = Lists.newArrayList(getSimpleFieldConfigs(project, fieldConfigurationScheme));
        }

        return MapBuilder.<String, Object>newBuilder()
                .addAll(context)
                .add(CONTEXT_FIELD_CONFIG_SCHEME_KEY, fieldConfigScheme)
                .add(CONTEXT_FIELD_CONFIGS_KEY, fieldConfigs)
                .toMap();
    }

    private Set<SimpleFieldConfig> getSimpleFieldConfigs(final Project project, final FieldConfigurationScheme fieldConfigurationScheme)
    {
        final Set<SimpleFieldConfig> fieldConfigs = Sets.newTreeSet(comparatorFactory.createNamedDefaultComparator());

        Long defaultFieldLayoutId = fieldConfigurationScheme.getFieldLayoutId(null);
        if(defaultFieldLayoutId == null)
        {
            defaultFieldLayoutId = fieldLayoutManager.getFieldLayout().getId();
        }

        for(final IssueType issueType : project.getIssueTypes())
        {
            final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(project, issueType.getId());

            if(fieldLayout == null || fieldLayout.getId() == null)
            {
                boolean isDefaultFieldConfig = (defaultFieldLayoutId == null);
                fieldConfigs.add(SimpleFieldConfig.getSystemDefaultSimpleFieldConfig(getSystemDefaultEditConfigUrl(), isDefaultFieldConfig));
            }
            else
            {
                boolean isDefaultFieldConfig = fieldLayout.getId().equals(defaultFieldLayoutId);
                fieldConfigs.add(new SimpleFieldConfig(fieldLayout, getFieldConfigUrl(fieldLayout.getId()), isDefaultFieldConfig));
            }
        }

        return fieldConfigs;
    }

    String getEditSchemeUrl()
    {
        return tabUrlFactory.forFields();
    }


    String getFieldConfigUrl(final Long id)
    {
        return createUrlBuilder("/secure/admin/ConfigureFieldLayout!default.jspa")
                .addParameter("id", id).asUrlString();
    }

    String getSystemDefaultEditConfigUrl()
    {
        return createUrlBuilder("/secure/admin/ViewIssueFields.jspa").asUrlString();
    }

    private UrlBuilder createUrlBuilder(final String operation)
    {
        return contextProviderUtils.createUrlBuilder(operation);
    }

    private SimpleFieldConfigScheme getDefaultFieldConfigScheme(final Project project, final I18nHelper i18nHelper)
    {
        return new SimpleFieldConfigScheme(null, i18nHelper.getText("admin.projects.system.default.field.config"), "",
                null, getEditSchemeUrl());
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimpleFieldConfig implements NamedDefault
    {
        private final Long id;
        private final String name;
        private final String description;
        private final String url;
        private final boolean defaultFieldConfig;

        public static SimpleFieldConfig getSystemDefaultSimpleFieldConfig(final String url, final boolean defaultFieldconfig)
        {
            return new SimpleFieldConfig(null, EditableDefaultFieldLayout.NAME, EditableDefaultFieldLayout.DESCRIPTION, url, defaultFieldconfig);
        }

        public SimpleFieldConfig(final FieldLayout fieldLayout, final String url, boolean defaultFieldConfig)
        {
            this.id = fieldLayout.getId();
            this.name = fieldLayout.getName();
            this.description = fieldLayout.getDescription();
            this.url = url;
            this.defaultFieldConfig = defaultFieldConfig;
        }

        SimpleFieldConfig(final Long id, final String name, final String description, final String url, boolean defaultFieldConfig)
        {
            this.id = id;
            this.name = name;
            this.description = description;
            this.url = url;
            this.defaultFieldConfig = defaultFieldConfig;
        }

        public Long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        @Override
        public boolean isDefault()
        {
            return isDefaultFieldConfig();
        }

        public String getUrl()
        {
            return url;
        }

        public boolean isDefaultFieldConfig()
        {
            return defaultFieldConfig;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleFieldConfig that = (SimpleFieldConfig) o;

            if (defaultFieldConfig != that.defaultFieldConfig) { return false; }
            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
            if (description != null ? !description.equals(that.description) : that.description != null) { return false; }
            if (url != null ? !url.equals(that.url) : that.url != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + (defaultFieldConfig ? 1 : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

}
