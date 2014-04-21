package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.ProjectFieldLayoutSchemeHelper;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.renderer.DefaultHackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.NamedDefault;
import com.atlassian.jira.projectconfig.beans.SimpleFieldConfigScheme;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.beans.SimpleIssueTypeImpl;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Context Provider for the fields panel
 *
 * @since v4.4
 */
public class ProjectFieldsContextProvider implements CacheableContextProvider
{
    static final String CONTEXT_FIELDS_SCHEME = "fieldsScheme";
    static final String CONTEXT_FIELD_CONFIGS = "fieldConfigs";

    private final ContextProviderUtils contextProviderUtils;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final HackyFieldRendererRegistry hackyFieldRendererRegistry;
    private final FieldScreenManager fieldScreenManager;
    private final I18nHelper i18nHelper;
    private final ProjectFieldLayoutSchemeHelper projectFieldLayoutSchemeHelper;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final ComparatorFactory comparatorFactory;

    public ProjectFieldsContextProvider(final ContextProviderUtils contextProviderUtils,
            final FieldLayoutManager fieldLayoutManager,
            final RendererManager rendererManager,
            final JiraAuthenticationContext jiraAuthenticationContext,
            final FieldScreenManager fieldScreenManager,
            final ProjectFieldLayoutSchemeHelper projectFieldLayoutSchemeHelper,
            final IssueTypeSchemeManager issueTypeSchemeManager,
            final ComparatorFactory comparatorFactory)
    {
        this.contextProviderUtils = contextProviderUtils;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.fieldScreenManager = fieldScreenManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.i18nHelper = jiraAuthenticationContext.getI18nHelper();
        this.hackyFieldRendererRegistry = new DefaultHackyFieldRendererRegistry();
        this.projectFieldLayoutSchemeHelper = projectFieldLayoutSchemeHelper;
        this.comparatorFactory = comparatorFactory;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {

    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Map<String, Object> defaultContext = contextProviderUtils.getDefaultContext();
        final Project project = (Project) defaultContext.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);

        final List<FieldLayoutScheme> fieldLayoutSchemes = fieldLayoutManager.getFieldLayoutSchemes();

        final FieldConfigurationScheme fieldConfigurationScheme =
                fieldLayoutManager.getFieldConfigurationScheme(project);

        final SimpleFieldConfigScheme fieldConfigScheme;
        final List<SimpleFieldConfig> fieldConfigs;
        if(fieldLayoutSchemes == null || fieldLayoutSchemes.size() == 0 || fieldConfigurationScheme == null)
        {
            fieldConfigScheme = getSystemDefaultFieldConfigScheme(project);
            fieldConfigs = getSystemDefaultSimpleFieldConfig(project);
        }
        else
        {
            fieldConfigScheme = new SimpleFieldConfigScheme(
                    fieldConfigurationScheme,
                    getChangeSchemeUrl(project.getId()),
                    getEditSchemeUrl(fieldConfigurationScheme.getId()));

            fieldConfigs = getSimpleFieldConfigs(project, fieldConfigurationScheme);
        }

        Collections.sort(fieldConfigs, comparatorFactory.createNamedDefaultComparator());

        MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder();
        contextBuilder.add(CONTEXT_FIELDS_SCHEME, fieldConfigScheme);
        contextBuilder.add(CONTEXT_FIELD_CONFIGS, fieldConfigs);

        Map<String, Object> defaults = CompositeMap.of(context, defaultContext);
        return CompositeMap.of(contextBuilder.toMap(), defaults);
    }

    List<SimpleFieldConfig> getSystemDefaultSimpleFieldConfig(final Project project)
    {
        final IssueType defaultIssueType = issueTypeSchemeManager.getDefaultValue(project.getGenericValue());


        final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout();
        final Multimap<FieldLayout, Project> activeFieldLayouts = projectFieldLayoutSchemeHelper
                .getProjectsForFieldLayouts(Collections.singleton(fieldLayout));
        final Set<FieldLayoutItem> fieldLayoutSet = Sets.newTreeSet(fieldLayout.getFieldLayoutItems());

        final Collection<IssueType> issueTypes = project.getIssueTypes();
        final List<SimpleIssueType> simpleIssueTypes = new ArrayList<SimpleIssueType>();
        for (IssueType issueType : issueTypes)
        {
            simpleIssueTypes.add(new SimpleIssueTypeImpl(issueType, issueType.equals(defaultIssueType) ));
        }
        Collections.sort(simpleIssueTypes, comparatorFactory.createIssueTypeComparator());
        return CollectionBuilder.newBuilder(SimpleFieldConfig.getSystemDefaultSimpleFieldConfig(
                getSystemDefaultEditSchemeUrl(), true,
                activeFieldLayouts.get(fieldLayout), getSimpleFieldLayoutItems(fieldLayoutSet),
                simpleIssueTypes)).asMutableList();
    }

    List<SimpleFieldConfig> getSimpleFieldConfigs(final Project project,
            final FieldConfigurationScheme fieldConfigurationScheme)
    {
        final IssueType defaultIssueType = issueTypeSchemeManager.getDefaultValue(project.getGenericValue());


        Long defaultFieldLayoutId = fieldConfigurationScheme.getFieldLayoutId(null);
        final FieldLayout defaultFieldLayout = fieldLayoutManager.getFieldLayout();
        if(defaultFieldLayoutId == null)
        {
            defaultFieldLayoutId = defaultFieldLayout.getId();
        }

        final Comparator<FieldLayout> fieldLayoutComparator = getFieldLayoutComparator();
        final Set<FieldLayout> fieldLayouts = Sets.newTreeSet(fieldLayoutComparator);

        // Keep track of what issue types correspond to each field config
        final Map<FieldLayout, List<SimpleIssueType>> fieldConfigMapping = Maps.<FieldLayout, FieldLayout, List<SimpleIssueType>>newTreeMap(fieldLayoutComparator);

        for(final IssueType issueType : project.getIssueTypes())
        {
            final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(project, issueType.getId());
            fieldLayouts.add(fieldLayout);

            List<SimpleIssueType> issueTypes = fieldConfigMapping.get(fieldLayout);
            if(issueTypes == null)
            {
                issueTypes = new ArrayList<SimpleIssueType>();
            }
            issueTypes.add(new SimpleIssueTypeImpl(issueType, issueType.equals(defaultIssueType)));
            fieldConfigMapping.put(fieldLayout, issueTypes);
        }

        final Multimap<FieldLayout, Project> activeFieldLayouts = projectFieldLayoutSchemeHelper
                .getProjectsForFieldLayouts(fieldLayouts);

        // Already sorted
        final Set<SimpleFieldConfig> fieldConfigsWithIssueTypes = Sets.newLinkedHashSet();
        for (final FieldLayout fieldLayout : fieldLayouts)
        {
            boolean isDefaultFieldConfig;
            SimpleFieldConfig simpleFieldConfig;
            final List<SimpleIssueType> issueTypes = fieldConfigMapping.get(fieldLayout);
            Collections.sort(issueTypes, comparatorFactory.createIssueTypeComparator());

            if(fieldLayout.equals(fieldLayoutManager.getFieldLayout()))
            {
                isDefaultFieldConfig = (defaultFieldLayoutId == null);
                simpleFieldConfig = createDefaultSimpleFieldConfig(isDefaultFieldConfig,
                        activeFieldLayouts.get(fieldLayout),
                        issueTypes);
            }
            else
            {
                isDefaultFieldConfig = fieldLayout.getId().equals(defaultFieldLayoutId);
                simpleFieldConfig = createSimpleFieldConfig(fieldLayout, isDefaultFieldConfig,
                        issueTypes, activeFieldLayouts.get(fieldLayout));
            }

            fieldConfigsWithIssueTypes.add(simpleFieldConfig);
        }
        
        return Lists.newArrayList(fieldConfigsWithIssueTypes);
    }

    SimpleFieldConfig createSimpleFieldConfig(FieldLayout fieldLayout, boolean defaultFieldConfig,
            List<SimpleIssueType> issueTypes, Collection<Project> projects)
    {
        final Set<FieldLayoutItem> fieldLayoutSet = Sets.newTreeSet(fieldLayout.getFieldLayoutItems());
        return new SimpleFieldConfig(fieldLayout, getFieldConfigUrl(fieldLayout.getId()),
                defaultFieldConfig, projects, getSimpleFieldLayoutItems(fieldLayoutSet),
                issueTypes);
    }

    SimpleFieldConfigScheme getSystemDefaultFieldConfigScheme(Project project)
    {
        return new SimpleFieldConfigScheme(
                null, i18nHelper.getText("admin.projects.system.default.field.config"), null,
                getChangeSchemeUrl(project.getId()), getSystemDefaultEditSchemeUrl());
    }

    String getChangeSchemeUrl(final Long id)
    {
        return createUrlBuilder("/secure/admin/SelectFieldLayoutScheme!default.jspa")
                .addParameter("projectId", id).asUrlString();
    }

    String getEditSchemeUrl(final Long id)
    {
        return createUrlBuilder("/secure/admin/ConfigureFieldLayoutScheme.jspa")
                .addParameter("id", id).asUrlString();
    }

    String getFieldConfigUrl(final Long id)
    {
        return createUrlBuilder("/secure/admin/ConfigureFieldLayout!default.jspa")
                .addParameter("id", id).asUrlString();
    }


    String getSystemDefaultEditSchemeUrl()
    {
        return createUrlBuilder("/secure/admin/ViewIssueFields.jspa").asUrlString();
    }

    String getRendererType(final String rendererType)
    {
        final HackyRendererType hackyRendererType = HackyRendererType.fromKey(rendererType);
        if (hackyRendererType != null)
        {
            return i18nHelper.getText(hackyRendererType.getDisplayNameI18nKey());
        }
        else
        {
            return rendererManager.getRendererForType(rendererType).getDescriptor().getName();
        }
    }

    private UrlBuilder createUrlBuilder(final String operation)
    {
        return contextProviderUtils.createUrlBuilder(operation);
    }

    private SimpleFieldConfig createDefaultSimpleFieldConfig(boolean isDefaultFieldConfig, Collection<Project> projects, List<SimpleIssueType> issueTypes)
    {
        final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout();
        final Set<FieldLayoutItem> fieldLayoutSet = Sets.newTreeSet(fieldLayout.getFieldLayoutItems());
        return SimpleFieldConfig.getSystemDefaultSimpleFieldConfig(getSystemDefaultEditSchemeUrl(),
                isDefaultFieldConfig, projects, getSimpleFieldLayoutItems(fieldLayoutSet), issueTypes);
    }

    private Comparator<FieldLayout> getFieldLayoutComparator()
    {
        return new FieldLayoutComparator(comparatorFactory.createStringComparator());
    }

    private boolean isRenderable(final OrderableField field)
    {
        if (field instanceof RenderableField)
        {
            final RenderableField renderableField = (RenderableField) field;
            final boolean isRenderable = renderableField.isRenderable();
            //customfields all implement the RenderableField interface so if the field says it's not
            //renderable and it is a custom field we need to check if its renderers should be overriden
            if (!isRenderable && field instanceof CustomField)
            {
                return hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(field);
            }
            else
            {
                return isRenderable;
            }
        }
        else
        {
            return hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(field);
        }
    }

    private List<SimpleFieldLayoutItem> getSimpleFieldLayoutItems(final Set<FieldLayoutItem> fieldLayoutSet)
    {
        final List<SimpleFieldLayoutItem> simpleFieldLayoutItems = Lists.newArrayList();

        for (final FieldLayoutItem fieldLayoutItem : fieldLayoutSet)
        {
            if(!fieldLayoutItem.isHidden())
            {
                final OrderableField orderableField = fieldLayoutItem.getOrderableField();
                simpleFieldLayoutItems.add(new SimpleFieldLayoutItem(orderableField.getId(),
                        orderableField.getName(),
                        fieldLayoutItem.getFieldDescription(), fieldLayoutItem.isRequired(),
                        getRendererType(fieldLayoutItem.getRendererType()),
                        isRenderable(orderableField),
                        getScreenCount(fieldLayoutItem)));
            }
        }

        return simpleFieldLayoutItems;
    }

    private int getScreenCount(final FieldLayoutItem fieldLayoutItem)
    {
        final Collection<FieldScreenTab> fieldScreenTabs = fieldScreenManager.getFieldScreenTabs(fieldLayoutItem.getOrderableField().getId());
        return fieldScreenTabs.size();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimpleFieldConfig implements NamedDefault
    {
        private Long id;
        private String name;
        private String description;
        private String url;
        private boolean defaultFieldConfig;
        private Collection<Project> sharedProjects;
        private List<SimpleIssueType> issueTypes;
        private List<SimpleFieldLayoutItem> fieldLayoutItems;

        public static SimpleFieldConfig getSystemDefaultSimpleFieldConfig(final String url, final boolean defaultFieldconfig,
                final Collection<Project> sharedProjects, final List<SimpleFieldLayoutItem> fieldLayoutItems,
                final List<SimpleIssueType> issueTypes)
        {
            return new SimpleFieldConfig(null, EditableDefaultFieldLayout.NAME, EditableDefaultFieldLayout.DESCRIPTION, url, defaultFieldconfig, sharedProjects, fieldLayoutItems, issueTypes);
        }

        public SimpleFieldConfig(final FieldLayout fieldLayout, final String url, boolean defaultFieldConfig,
                final Collection<Project> sharedProjects, final List<SimpleFieldLayoutItem> fieldLayoutItems,
                final List<SimpleIssueType> issueTypes)
        {
            this.id = fieldLayout.getId();
            this.name = fieldLayout.getName();
            this.description = fieldLayout.getDescription();
            this.url = url;
            this.defaultFieldConfig = defaultFieldConfig;
            this.sharedProjects = sharedProjects;
            this.fieldLayoutItems = fieldLayoutItems;
            this.issueTypes = issueTypes;
        }

        SimpleFieldConfig(final Long id, final String name, final String description, final String url, boolean defaultFieldConfig,
                final Collection<Project> sharedProjects, final List<SimpleFieldLayoutItem> fieldLayoutItems,
                final List<SimpleIssueType> issueTypes)
        {
            this.id = id;
            this.name = name;
            this.description = description;
            this.url = url;
            this.defaultFieldConfig = defaultFieldConfig;
            this.sharedProjects = sharedProjects;
            this.fieldLayoutItems = fieldLayoutItems;
            this.issueTypes = issueTypes;
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

        public Collection<Project> getSharedProjects()
        {
            return sharedProjects;
        }

        public List<SimpleFieldLayoutItem> getFieldLayoutItems()
        {
            return fieldLayoutItems;
        }

        public Collection<SimpleIssueType> getIssueTypes()
        {
            return issueTypes;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleFieldConfig that = (SimpleFieldConfig) o;

            if (defaultFieldConfig != that.defaultFieldConfig) { return false; }
            if (fieldLayoutItems != null ? !fieldLayoutItems.equals(that.fieldLayoutItems) : that.fieldLayoutItems != null)
            { return false; }
            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (issueTypes != null ? !issueTypes.equals(that.issueTypes) : that.issueTypes != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
            if (description != null ? !description.equals(that.description) : that.description != null) { return false; }
            if (sharedProjects != null ? !sharedProjects.equals(that.sharedProjects) : that.sharedProjects != null)
            { return false; }
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
            result = 31 * result + (sharedProjects != null ? sharedProjects.hashCode() : 0);
            result = 31 * result + (issueTypes != null ? issueTypes.hashCode() : 0);
            result = 31 * result + (fieldLayoutItems != null ? fieldLayoutItems.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                    append("id", id).
                    append("name", name).
                    append("description", description).
                    append("url", url).
                    append("defaultFieldConfig", defaultFieldConfig).
                    append("sharedProjects", sharedProjects).
                    append("issueTypes", issueTypes).
                    append("fieldLayoutItems", fieldLayoutItems).
                    toString();
        }
    }

    public static class SimpleFieldLayoutItem
    {

        private final String id;
        private final String name;
        private final String descriptionHtml;
        private final boolean required;
        private final String rendererType;
        private final boolean renderable;
        private final int screenCount;

        public SimpleFieldLayoutItem(final String id, final String name, final String descriptionHtml, final boolean required,
                final String rendererType, final boolean isRenderable, final int screenCount)
        {
            this.id = id;
            this.name = name;
            this.descriptionHtml = descriptionHtml;
            this.required = required;
            this.rendererType = rendererType;
            this.renderable = isRenderable;
            this.screenCount = screenCount;
        }

        public String getId()
        {
            return id;
        }

        public boolean isRequired()
        {
            return required;
        }

        public boolean isRenderable()
        {
            return renderable;
        }

        public String getRendererType()
        {
            return rendererType;
        }

        public String getName()
        {
            return name;
        }

        public String getDescriptionHtml()
        {
            return descriptionHtml;
        }

        public int getScreenCount()
        {
            return screenCount;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleFieldLayoutItem that = (SimpleFieldLayoutItem) o;

            if (renderable != that.renderable) { return false; }
            if (required != that.required) { return false; }
            if (screenCount != that.screenCount) { return false; }
            if (descriptionHtml != null ? !descriptionHtml.equals(that.descriptionHtml) : that.descriptionHtml != null)
            { return false; }
            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
            if (rendererType != null ? !rendererType.equals(that.rendererType) : that.rendererType != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (descriptionHtml != null ? descriptionHtml.hashCode() : 0);
            result = 31 * result + (required ? 1 : 0);
            result = 31 * result + (rendererType != null ? rendererType.hashCode() : 0);
            result = 31 * result + (renderable ? 1 : 0);
            result = 31 * result + screenCount;
            return result;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                    append("id", id).
                    append("name", name).
                    append("descriptionHtml", descriptionHtml).
                    append("required", required).
                    append("rendererType", rendererType).
                    append("renderable", renderable).
                    append("screensCount", screenCount).
                    toString();
        }
    }

    private static class FieldLayoutComparator implements Comparator<FieldLayout>
    {
        private final Comparator<String> collator;

        public FieldLayoutComparator(final Comparator<String> collator)
        {
            this.collator = collator;
        }

        @Override
        public int compare(final FieldLayout lhs, final FieldLayout rhs)
        {
            final String lhsName = lhs.getName() == null ?
                    EditableDefaultFieldLayout.NAME : lhs.getName();
            final String rhsName = rhs.getName() == null ?
                    EditableDefaultFieldLayout.NAME : rhs.getName();
            return collator.compare(lhsName, rhsName);
        }
    }
}
