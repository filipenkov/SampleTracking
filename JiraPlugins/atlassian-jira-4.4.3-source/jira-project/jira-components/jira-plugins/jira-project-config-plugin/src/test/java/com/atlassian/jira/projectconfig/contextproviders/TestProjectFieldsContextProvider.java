package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comparator.ProjectNameComparator;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.MockProjectFieldLayoutSchemeHelper;
import com.atlassian.jira.issue.fields.ProjectFieldLayoutSchemeHelper;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.MockFieldScreen;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.mock.MockRendererManager;
import com.atlassian.jira.mock.issue.fields.layout.field.MockFieldConfigurationScheme;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.SimpleFieldConfigScheme;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.beans.SimpleIssueTypeImpl;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.projectconfig.order.MockComparatorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestProjectFieldsContextProvider
{
    private IMocksControl control;
    private ContextProviderUtils providerUtils;
    private JiraAuthenticationContext authenticationContext;
    private MockFieldLayoutManager fieldLayoutManager;
    private MockRendererManager rendererManager;
    private MockUser user;
    private FieldLayoutScheme fieldLayoutScheme;
    private FieldScreenManager fieldScreenManager;
    private MockProjectFieldLayoutSchemeHelper helper;
    private IssueTypeSchemeManager issueTypeSchemeManager;
    private MockComparatorFactory comparatorFactory;

    @Before
    public void setUp() throws Exception
    {
        control = createControl();
        providerUtils = control.createMock(ContextProviderUtils.class);
        user = new MockUser("mtan");
        authenticationContext = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
        fieldLayoutManager = new MockFieldLayoutManager();
        rendererManager = new MockRendererManager();
        fieldLayoutScheme = control.createMock(FieldLayoutScheme.class);
        fieldScreenManager = control.createMock(FieldScreenManager.class);
        issueTypeSchemeManager = control.createMock(IssueTypeSchemeManager.class);
        helper = new MockProjectFieldLayoutSchemeHelper();
        comparatorFactory = new MockComparatorFactory();
    }

    @After
    public void tearDown()
    {
        control = null;
        providerUtils = null;
        user = null;
        authenticationContext = null;
        fieldLayoutManager = null;
        rendererManager = null;
        fieldLayoutScheme = null;
        fieldScreenManager = null;
        helper = null;
    }

    @Test
    public void testGetContextMapForSystemDefault() throws Exception
    {
        final MockProject expectedProject = new MockProject(588L, "Something");
        Map<String, Object> argumentContext = MapBuilder.<String, Object>build("argument", true);
        Map<String, Object> defaultContext = MapBuilder.build("default", true, "project", expectedProject);


        final SimpleFieldConfigBuilder simpleFieldConfigBuilder = new SimpleFieldConfigBuilder();
        final ProjectFieldsContextProvider.SimpleFieldConfig fieldConfig = simpleFieldConfigBuilder
                .setName("System default")
                .build();
        final List<ProjectFieldsContextProvider.SimpleFieldConfig> systemFieldConfigs = CollectionBuilder.newBuilder(fieldConfig).asMutableList();

        final SimpleFieldConfigScheme systemFieldConfigScheme = new SimpleFieldConfigScheme(null, null, null, null, null);

        fieldLayoutManager.setFieldLayoutSchemes(Collections.<FieldLayoutScheme>emptyList())
                .setFieldConfigurationScheme(expectedProject, null);

        ProjectFieldsContextProvider provider = new ProjectFieldsContextProvider(providerUtils, fieldLayoutManager,
                rendererManager, authenticationContext, fieldScreenManager, helper, issueTypeSchemeManager, comparatorFactory)
        {
            @Override
            List<SimpleFieldConfig> getSystemDefaultSimpleFieldConfig(Project project)
                    throws FieldLayoutStorageException
            {
                assertEquals(expectedProject, project);
                return systemFieldConfigs;
            }

            @Override
            SimpleFieldConfigScheme getSystemDefaultFieldConfigScheme(Project project)
            {
                assertEquals(expectedProject, project);
                return systemFieldConfigScheme;
            }
        };

        expect(providerUtils.getDefaultContext()).andReturn(defaultContext);

        // no field layout schemes
        control.replay();

        Map<String, Object> actualMap = provider.getContextMap(argumentContext);

        MapBuilder expectedMap = MapBuilder.newBuilder(argumentContext).addAll(defaultContext)
                .add(ProjectFieldsContextProvider.CONTEXT_FIELD_CONFIGS, systemFieldConfigs)
                .add(ProjectFieldsContextProvider.CONTEXT_FIELDS_SCHEME, systemFieldConfigScheme);

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();

        // no field configuration scheme
        fieldLayoutManager.setFieldLayoutSchemes(Collections.singletonList(fieldLayoutScheme))
                .setFieldConfigurationScheme(expectedProject, null);

        control.reset();

        expect(providerUtils.getDefaultContext()).andReturn(defaultContext);

        control.replay();

        actualMap = provider.getContextMap(argumentContext);

        expectedMap = MapBuilder.newBuilder(argumentContext).addAll(defaultContext)
                .add(ProjectFieldsContextProvider.CONTEXT_FIELD_CONFIGS, systemFieldConfigs)
                .add(ProjectFieldsContextProvider.CONTEXT_FIELDS_SCHEME, systemFieldConfigScheme);

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();

    }

    @Test
    public void testGetContextMapForSystemDefaultUsedByOtherProjects() throws Exception
    {
        final List<SimpleIssueType> expectedProjectIssueTypes = new ArrayList<SimpleIssueType>();
        final MockIssueType bug = new MockIssueType("Bug", "Bug");
        expectedProjectIssueTypes.add(new SimpleIssueTypeImpl(bug, true));
        final MockIssueType task = new MockIssueType("Task", "Task");
        expectedProjectIssueTypes.add(new SimpleIssueTypeImpl(task, false));

        final MockProject expectedProject = new MockProject(588L, "Something")
                .setIssueTypes(bug, task);

        Map<String, Object> argumentContext = MapBuilder.<String, Object>build("argument", true);
        Map<String, Object> defaultContext = MapBuilder.build("default", true, "project", expectedProject);

        final MockProject sharedByProject1 = new MockProject(688L, "Something 1");
        final MockProject sharedByProject2 = new MockProject(788L, "Something 2");
        final Set<Project> expectedSharedProjects = Sets.newTreeSet(ProjectNameComparator.COMPARATOR);
        expectedSharedProjects.add(sharedByProject1);
        expectedSharedProjects.add(sharedByProject2);

        final SimpleFieldConfigBuilder simpleFieldConfigBuilder = new SimpleFieldConfigBuilder();
        final SimpleFieldConfigScheme systemFieldConfigScheme = new SimpleFieldConfigScheme(null, null, null, null, null);

        final MockOrderableField mockField = new MockOrderableField("fieldId", "fieldName");

        final MockFieldLayout expectedFieldLayout = new MockFieldLayout();
        expectedFieldLayout
                .setId(999L)
                .addFieldLayoutItem(mockField)
                .setRendererType("layoutItemRendererType");

        final MockFieldScreen expectedFieldScreen = new MockFieldScreen();
        expectedFieldScreen.setId(9898L);
        expectedFieldScreen.setName("fieldScreen");


        fieldLayoutManager.setFieldLayoutSchemes(Collections.<FieldLayoutScheme>emptyList())
                .setFieldLayout(null, expectedFieldLayout)
                .setFieldConfigurationScheme(expectedProject, null);

        final List<ProjectFieldsContextProvider.SimpleFieldLayoutItem> expectedFieldLayoutItems =
                Lists.newArrayList(
                        new ProjectFieldsContextProvider.SimpleFieldLayoutItem("fieldId", "fieldName", null, false, "rendererType", false, 1)
                );

        final ProjectFieldsContextProvider.SimpleFieldConfig expectedFieldConfig = simpleFieldConfigBuilder
                .setSharedProjects(expectedSharedProjects)
                .setName(EditableDefaultFieldLayout.NAME)
                .setDescription(EditableDefaultFieldLayout.DESCRIPTION)
                .setUrl("default")
                .setDefaultFieldConfig(true)
                .setIssueTypes(expectedProjectIssueTypes)
                .setFieldLayoutItems(expectedFieldLayoutItems)
                .build();

        final List<ProjectFieldsContextProvider.SimpleFieldConfig> expectedFieldConfigs = Lists.newArrayList(expectedFieldConfig);

        final FieldScreenTab fieldScreenTab = control.createMock(FieldScreenTab.class);
        expect(fieldScreenTab.getFieldScreen()).andStubReturn(expectedFieldScreen);
        expect(providerUtils.getDefaultContext()).andStubReturn(defaultContext);
        expect(fieldScreenManager.getFieldScreenTabs(mockField.getId()))
                .andStubReturn(Collections.<FieldScreenTab>singleton(fieldScreenTab));

        helper.setProjectsForFieldLayout(expectedFieldLayout, Lists.<Project>newArrayList(sharedByProject1, sharedByProject2));

        expect(issueTypeSchemeManager.getDefaultValue(expectedProject.getGenericValue())).andReturn(bug);


        ProjectFieldsContextProvider provider = new PartiallyStubbedProjectFieldsContextProvider(providerUtils, fieldLayoutManager,
                rendererManager, authenticationContext, fieldScreenManager, helper, issueTypeSchemeManager, comparatorFactory)
        {
            @Override
            SimpleFieldConfigScheme getSystemDefaultFieldConfigScheme(Project project)
            {
                assertEquals(expectedProject, project);
                return systemFieldConfigScheme;
            }

        };

        control.replay();

        Map<String, Object> actualMap = provider.getContextMap(argumentContext);

//        MapBuilder expectedMap = MapBuilder.newBuilder(argumentContext).addAll(defaultContext)
//                .add(ProjectFieldsContextProvider.CONTEXT_FIELD_CONFIGS, expectedFieldConfigs)
//                .add(ProjectFieldsContextProvider.CONTEXT_FIELDS_SCHEME, systemFieldConfigScheme);

        assertEquals(expectedFieldConfigs, actualMap.get(ProjectFieldsContextProvider.CONTEXT_FIELD_CONFIGS));
        assertEquals(systemFieldConfigScheme, actualMap.get(ProjectFieldsContextProvider.CONTEXT_FIELDS_SCHEME));

//        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();
    }

    @Test
    public void testGetContextMapForNonSystemDefaultSchemeUsedByOtherProjects() throws Exception
    {
        final List<SimpleIssueType> expectedProjectIssueTypes = new ArrayList<SimpleIssueType>();
        final MockIssueType bug = new MockIssueType("Bug", "Bug");
        expectedProjectIssueTypes.add(new SimpleIssueTypeImpl(bug, true));
        final MockIssueType task = new MockIssueType("Task", "Task");
        expectedProjectIssueTypes.add(new SimpleIssueTypeImpl(task, false));

        final MockProject expectedProject = new MockProject(588L, "Something")
                .setIssueTypes(bug, task);

        Map<String, Object> argumentContext = MapBuilder.<String, Object>build("argument", true);
        Map<String, Object> defaultContext = MapBuilder.build("default", true, "project", expectedProject);

        final MockProject sharedByProject1 = new MockProject(688L, "Something 1");
        final MockProject sharedByProject2 = new MockProject(788L, "Something 2");
        final Set<Project> expectedSharedProjects = Sets.newTreeSet(ProjectNameComparator.COMPARATOR);
        expectedSharedProjects.add(sharedByProject1);
        expectedSharedProjects.add(sharedByProject2);

        final SimpleFieldConfigBuilder simpleFieldConfigBuilder = new SimpleFieldConfigBuilder();
        final SimpleFieldConfigScheme expectedFieldConfigScheme = new SimpleFieldConfigScheme(
                3838L, "fieldConfigSchemeName", "fieldConfigSchemeDescription", "change588", "edit3838");

        final MockOrderableField mockField = new MockOrderableField("fieldId", "fieldName");

        final MockFieldLayout expectedFieldLayout = new MockFieldLayout();
        expectedFieldLayout
                .setId(999L)
                .addFieldLayoutItem(mockField)
                .setRendererType("layoutItemRendererType");
        final MockFieldScreen expectedFieldScreen = new MockFieldScreen();
        expectedFieldScreen.setId(9898L);
        expectedFieldScreen.setName("fieldScreen");


        final MockFieldConfigurationScheme fieldConfigScheme = new MockFieldConfigurationScheme()
                .setId(3838L)
                .setDescription("fieldConfigSchemeDescription")
                .setName("fieldConfigSchemeName")
                .setIssueTypeToFieldLayoutIdMapping(MapBuilder.<String, Long>build(
                        "Bug", 999L,
                        "Task", 999L
                ));


        final FieldLayoutScheme fieldLayoutscheme = control.createMock(FieldLayoutScheme.class);

        fieldLayoutManager.setFieldLayoutSchemes(Collections.singletonList(fieldLayoutscheme))
                .setFieldLayout(null, expectedFieldLayout)
                .setFieldConfigurationScheme(expectedProject, fieldConfigScheme)
                .setFieldLayout(expectedProject, "Bug", expectedFieldLayout)
                .setFieldLayout(expectedProject, "Task", expectedFieldLayout);

        final List<ProjectFieldsContextProvider.SimpleFieldLayoutItem> expectedFieldLayoutItems =
                Lists.newArrayList(
                        new ProjectFieldsContextProvider.SimpleFieldLayoutItem("fieldId", "fieldName", null, false, "rendererType", false,
                                1)
                );

        final ProjectFieldsContextProvider.SimpleFieldConfig expectedFieldConfig = simpleFieldConfigBuilder
                .setSharedProjects(expectedSharedProjects)
                .setName(EditableDefaultFieldLayout.NAME)
                .setDescription(EditableDefaultFieldLayout.DESCRIPTION)
                .setUrl("default")
                .setDefaultFieldConfig(false)
                .setIssueTypes(expectedProjectIssueTypes)
                .setFieldLayoutItems(expectedFieldLayoutItems)
                .build();

        final List<ProjectFieldsContextProvider.SimpleFieldConfig> expectedFieldConfigs = Lists.newArrayList(expectedFieldConfig);

        final FieldScreenTab fieldScreenTab = control.createMock(FieldScreenTab.class);
        expect(fieldScreenTab.getFieldScreen()).andStubReturn(expectedFieldScreen);
        expect(providerUtils.getDefaultContext()).andStubReturn(defaultContext);
        expect(fieldScreenManager.getFieldScreenTabs(mockField.getId()))
                .andStubReturn(Collections.<FieldScreenTab>singleton(fieldScreenTab));

        expect(issueTypeSchemeManager.getDefaultValue(expectedProject.getGenericValue())).andReturn(bug);

        helper.setProjectsForFieldLayout(expectedFieldLayout, Lists.<Project>newArrayList(sharedByProject1, sharedByProject2));

        ProjectFieldsContextProvider provider = new PartiallyStubbedProjectFieldsContextProvider(providerUtils, fieldLayoutManager,
                rendererManager, authenticationContext, fieldScreenManager, helper, issueTypeSchemeManager, comparatorFactory);

        control.replay();

        Map<String, Object> actualMap = provider.getContextMap(argumentContext);

        MapBuilder expectedMap = MapBuilder.newBuilder(argumentContext).addAll(defaultContext)
                .add(ProjectFieldsContextProvider.CONTEXT_FIELD_CONFIGS, expectedFieldConfigs)
                .add(ProjectFieldsContextProvider.CONTEXT_FIELDS_SCHEME, expectedFieldConfigScheme);

        assertEquals(expectedMap.toMap(), actualMap);

        control.verify();
    }

    @Test
    public void testFieldConfigUrl()
    {
        final long fieldConfigId = 4L;
        expect(providerUtils.createUrlBuilder("/secure/admin/ConfigureFieldLayout!default.jspa"))
                .andReturn(new UrlBuilder("fieldConfig", "UTF-8", false));

        control.replay();

        ProjectFieldsContextProvider provider = new ProjectFieldsContextProvider(providerUtils, fieldLayoutManager,
                rendererManager, authenticationContext, fieldScreenManager, helper, issueTypeSchemeManager, comparatorFactory);
        assertEquals("fieldConfig?id=" + fieldConfigId, provider.getFieldConfigUrl(fieldConfigId));

        control.verify();
    }

    @Test
    public void testGetChangeSchemeUrl()
    {
        final long projectId = 5L;
        expect(providerUtils.createUrlBuilder("/secure/admin/SelectFieldLayoutScheme!default.jspa"))
                .andReturn(new UrlBuilder("fieldConfig", "UTF-8", false));

        control.replay();

        ProjectFieldsContextProvider provider = new ProjectFieldsContextProvider(providerUtils, fieldLayoutManager,
                rendererManager, authenticationContext, fieldScreenManager, helper, issueTypeSchemeManager, comparatorFactory);
        assertEquals("fieldConfig?projectId=" + projectId, provider.getChangeSchemeUrl(projectId));

        control.verify();
    }

    @Test
    public void testGetEditSchemeUrl()
    {
        final long fieldConfigId = 2L;
        expect(providerUtils.createUrlBuilder("/secure/admin/ConfigureFieldLayoutScheme.jspa"))
                .andReturn(new UrlBuilder("fieldConfig", "UTF-8", false));

        control.replay();

        ProjectFieldsContextProvider provider = new ProjectFieldsContextProvider(providerUtils, fieldLayoutManager,
                rendererManager, authenticationContext, fieldScreenManager, helper, issueTypeSchemeManager, comparatorFactory);
        assertEquals("fieldConfig?id=" + fieldConfigId, provider.getEditSchemeUrl(fieldConfigId));

        control.verify();
    }

    @Test
    public void testGetSystemDefaultEditSchemeUrl()
    {
        expect(providerUtils.createUrlBuilder("/secure/admin/ViewIssueFields.jspa"))
                .andReturn(new UrlBuilder("fieldConfig", "UTF-8", false));

        control.replay();

        ProjectFieldsContextProvider provider = new ProjectFieldsContextProvider(providerUtils, fieldLayoutManager,
                rendererManager, authenticationContext, fieldScreenManager, helper, issueTypeSchemeManager, comparatorFactory);
        assertEquals("fieldConfig", provider.getSystemDefaultEditSchemeUrl());

        control.verify();
    }

    public static class PartiallyStubbedProjectFieldsContextProvider extends ProjectFieldsContextProvider
    {

        public PartiallyStubbedProjectFieldsContextProvider(final ContextProviderUtils contextProviderUtils,
                final FieldLayoutManager fieldLayoutManager, final RendererManager rendererManager,
                final JiraAuthenticationContext jiraAuthenticationContext, final FieldScreenManager fieldScreenManager,
                final ProjectFieldLayoutSchemeHelper projectFieldLayoutSchemeHelper, final IssueTypeSchemeManager issueTypeSchemeManager,
                final ComparatorFactory comparatorFactory)
        {
            super(contextProviderUtils, fieldLayoutManager, rendererManager, jiraAuthenticationContext,
                    fieldScreenManager, projectFieldLayoutSchemeHelper, issueTypeSchemeManager, comparatorFactory);
        }

        @Override
        String getChangeSchemeUrl(Long id)
        {
            return "change" + id;
        }

        @Override
        String getEditSchemeUrl(Long id)
        {
            return "edit" + id;
        }

        @Override
        String getFieldConfigUrl(Long id)
        {
            return "fieldconfig" + id;
        }

        @Override
        String getSystemDefaultEditSchemeUrl()
        {
            return "default";
        }

        @Override
        String getRendererType(String rendererType)
        {
            assertEquals("layoutItemRendererType", rendererType);
            return "rendererType";
        }

    }

    public static class SimpleFieldConfigBuilder
    {

        private Long id;
        private String name;
        private String description;
        private String url;
        private boolean defaultFieldConfig;
        private Collection<Project> sharedProjects;
        private List<SimpleIssueType> issueTypes;
        private List<ProjectFieldsContextProvider.SimpleFieldLayoutItem> fieldLayoutItems;

        public SimpleFieldConfigBuilder()
        {
            this.sharedProjects = Lists.newArrayList();
            this.issueTypes = Lists.newArrayList();
            this.fieldLayoutItems = Lists.newArrayList();
        }

        public SimpleFieldConfigBuilder setId(Long id)
        {
            this.id = id;
            return this;
        }

        public SimpleFieldConfigBuilder setName(String name)
        {
            this.name = name;
            return this;
        }

        public SimpleFieldConfigBuilder setDescription(String description)
        {
            this.description = description;
            return this;
        }

        public SimpleFieldConfigBuilder setUrl(String url)
        {
            this.url = url;
            return this;
        }

        public SimpleFieldConfigBuilder setDefaultFieldConfig(boolean defaultFieldConfig)
        {
            this.defaultFieldConfig = defaultFieldConfig;
            return this;
        }

        public SimpleFieldConfigBuilder setSharedProjects(Collection<Project> sharedProjects)
        {
            this.sharedProjects = sharedProjects;
            return this;
        }

        public SimpleFieldConfigBuilder setIssueTypes(List<SimpleIssueType> issueTypes)
        {
            this.issueTypes = issueTypes;
            return this;
        }

        public SimpleFieldConfigBuilder setFieldLayoutItems(List<ProjectFieldsContextProvider.SimpleFieldLayoutItem> fieldLayoutItems)
        {
            this.fieldLayoutItems = fieldLayoutItems;
            return this;
        }

        public ProjectFieldsContextProvider.SimpleFieldConfig build()
        {
            return new ProjectFieldsContextProvider.SimpleFieldConfig(id, name, description,  url, defaultFieldConfig, sharedProjects, fieldLayoutItems, issueTypes);
        }

    }

}
