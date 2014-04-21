package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.mock.issue.fields.layout.field.MockFieldConfigurationScheme;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.projectconfig.beans.SimpleFieldConfigScheme;
import com.atlassian.jira.projectconfig.contextproviders.FieldsSummaryPanelContextProvider.SimpleFieldConfig;
import com.atlassian.jira.projectconfig.order.MockComparatorFactory;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Lists;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * Test for {@link FieldsSummaryPanelContextProvider}
 *
 * @since v4.4
 */
public class TestFieldsSummaryPanelContextProvider
{
    private IMocksControl control;
    private FieldLayoutManager fieldLayoutManager;
    private FieldLayout fieldLayout;
    private ContextProviderUtils contextProviderUtils;
    private FieldLayout anotherFieldLayout;
    private TabUrlFactory tabUrlFactory;
    private MockComparatorFactory comparatorFactory;

    @Before
    public void setUp()
    {
        control = createControl();
        fieldLayoutManager = control.createMock(FieldLayoutManager.class);
        fieldLayout = control.createMock(FieldLayout.class);
        anotherFieldLayout = control.createMock(FieldLayout.class);
        contextProviderUtils = control.createMock(ContextProviderUtils.class);
        tabUrlFactory = control.createMock(TabUrlFactory.class);
        comparatorFactory = new MockComparatorFactory();
    }

    @After
    public void tearDown()
    {
        control = null;
        fieldLayoutManager = null;
        fieldLayout = null;
        anotherFieldLayout = null;
        contextProviderUtils = null;
        comparatorFactory = null;
    }

    @Test
    public void testGetContextMapWtihSystemDefaultFieldConfig() throws Exception
    {
        final MockProject mockProject = new MockProject(888L);
        mockProject.setIssueTypes("type a", "type b");

        final Map<String,Object> testContext = MapBuilder.<String, Object>build(
                ContextProviderUtils.CONTEXT_PROJECT_KEY, mockProject,
                ContextProviderUtils.CONTEXT_I18N_KEY, new NoopI18nHelper()
        );

        expect(fieldLayoutManager.getFieldConfigurationScheme(eq(mockProject))).andReturn(null);
        expect(fieldLayoutManager.getFieldLayoutSchemes()).andReturn(Collections.<FieldLayoutScheme>emptyList());

        control.replay();

        final FieldsSummaryPanelContextProvider fieldsSummaryPanelContextProvider =
                getFieldsSummaryPanelContextProviderUnderTest(fieldLayoutManager, contextProviderUtils);

        final Map<String, Object> contextMap = fieldsSummaryPanelContextProvider.getContextMap(testContext);

        final Map<String, Object> expectedMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(FieldsSummaryPanelContextProvider.CONTEXT_FIELD_CONFIG_SCHEME_KEY,
                        new SimpleFieldConfigScheme(null, NoopI18nHelper.makeTranslation("admin.projects.system.default.field.config"), "",
                                null, getEditUrl()))
                .add(FieldsSummaryPanelContextProvider.CONTEXT_FIELD_CONFIGS_KEY, Collections.<SimpleFieldConfig>
                        singletonList(SimpleFieldConfig.getSystemDefaultSimpleFieldConfig(getSystemDefaultUrl(), true)))
                .toMap();

        assertEquals(expectedMap, contextMap);

        control.verify();

    }

    @Test
    public void testGetContextMapWithFieldConfigsIncludingSystemDefault() throws Exception
    {

        final MockProject mockProject = new MockProject(888L);
        mockProject.setIssueTypes("type a", "type b");

        final Map<String,Object> testContext = MapBuilder.<String, Object>build(
                ContextProviderUtils.CONTEXT_PROJECT_KEY, mockProject
        );

        final MockFieldConfigurationScheme fieldConfigurationScheme = new MockFieldConfigurationScheme(2828L, "Field Config Scheme", "");
        fieldConfigurationScheme.setIssueTypeToFieldLayoutIdMapping(MapBuilder.<String, Long>build(
                null, null
        ));

        expect(fieldLayoutManager.getFieldConfigurationScheme(eq(mockProject))).andReturn(fieldConfigurationScheme);
        expect(fieldLayoutManager.getFieldLayoutSchemes()).andReturn(Lists.<FieldLayoutScheme>newArrayList(null, null));

        expect(fieldLayoutManager.getFieldLayout(eq(mockProject), eq("type a"))).andReturn(null);
        expect(fieldLayoutManager.getFieldLayout(eq(mockProject), eq("type b"))).andReturn(fieldLayout);

        expect(fieldLayoutManager.getFieldLayout()).andReturn(fieldLayout);

        expect(fieldLayout.getId()).andReturn(null).atLeastOnce();

        control.replay();

        final FieldsSummaryPanelContextProvider fieldsSummaryPanelContextProvider =
                getFieldsSummaryPanelContextProviderUnderTest(fieldLayoutManager, contextProviderUtils);

        final Map<String, Object> contextMap = fieldsSummaryPanelContextProvider.getContextMap(testContext);

        final Map<String, Object> expectedMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(FieldsSummaryPanelContextProvider.CONTEXT_FIELD_CONFIG_SCHEME_KEY,
                        new SimpleFieldConfigScheme(2828L, "Field Config Scheme", "", null, getEditUrl()))
                .add(FieldsSummaryPanelContextProvider.CONTEXT_FIELD_CONFIGS_KEY, Lists.<SimpleFieldConfig>
                        newArrayList(SimpleFieldConfig.getSystemDefaultSimpleFieldConfig(getSystemDefaultUrl(), true)))
                .toMap();

        assertEquals(expectedMap, contextMap);

        control.verify();
    }

    @Test
    public void testGetContextMapWithOtherFieldConfigs() throws Exception
    {
        final MockProject mockProject = new MockProject(888L);
        mockProject.setIssueTypes("type a", "type b");

        final Map<String,Object> testContext = MapBuilder.<String, Object>build(
                ContextProviderUtils.CONTEXT_PROJECT_KEY, mockProject
        );

        final MockFieldConfigurationScheme fieldConfigurationScheme = new MockFieldConfigurationScheme(2828L, "Field Config Scheme", "");
        fieldConfigurationScheme.setIssueTypeToFieldLayoutIdMapping(MapBuilder.<String, Long>build(
                null, null
        ));

        expect(fieldLayoutManager.getFieldConfigurationScheme(eq(mockProject))).andReturn(fieldConfigurationScheme);
        expect(fieldLayoutManager.getFieldLayoutSchemes()).andReturn(Lists.<FieldLayoutScheme>newArrayList(null, null));

        expect(fieldLayoutManager.getFieldLayout(eq(mockProject), eq("type a"))).andReturn(fieldLayout);
        expect(fieldLayoutManager.getFieldLayout(eq(mockProject), eq("type b"))).andReturn(anotherFieldLayout);

        expect(fieldLayoutManager.getFieldLayout()).andReturn(fieldLayout);

        expect(fieldLayout.getId()).andReturn(null).atLeastOnce();

        expect(anotherFieldLayout.getId()).andReturn(4848L).atLeastOnce();
        expect(anotherFieldLayout.getName()).andReturn("Simple Field Layout");
        expect(anotherFieldLayout.getDescription()).andReturn("Simple Field Layout Description");

        control.replay();

        final FieldsSummaryPanelContextProvider fieldsSummaryPanelContextProvider =
                getFieldsSummaryPanelContextProviderUnderTest(fieldLayoutManager, contextProviderUtils);

        final Map<String, Object> contextMap = fieldsSummaryPanelContextProvider.getContextMap(testContext);

        final List<SimpleFieldConfig> simpleFieldConfigs =
                Lists.newArrayList(
                        SimpleFieldConfig.getSystemDefaultSimpleFieldConfig(getSystemDefaultUrl(), true),
                        new SimpleFieldConfig(4848L, "Simple Field Layout", "Simple Field Layout Description",
                                getFieldConfigUrl(4848L), false)
                );

        final Map<String, Object> expectedMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(FieldsSummaryPanelContextProvider.CONTEXT_FIELD_CONFIG_SCHEME_KEY,
                        new SimpleFieldConfigScheme(2828L, "Field Config Scheme", "", null, getEditUrl()))
                .add(FieldsSummaryPanelContextProvider.CONTEXT_FIELD_CONFIGS_KEY, simpleFieldConfigs)
                .toMap();

        assertEquals(expectedMap, contextMap);

        control.verify();
    }

    @Test
    public void testGetContextMapWithDefaultFieldConfigThatIsNotSystemDefault() throws Exception
    {
        final MockProject mockProject = new MockProject(888L);
        mockProject.setIssueTypes("type a");

        final Map<String,Object> testContext = MapBuilder.<String, Object>build(
                ContextProviderUtils.CONTEXT_PROJECT_KEY, mockProject
        );

        final MockFieldConfigurationScheme fieldConfigurationScheme = new MockFieldConfigurationScheme(2828L, "Field Config Scheme", "");
        fieldConfigurationScheme.setIssueTypeToFieldLayoutIdMapping(MapBuilder.<String, Long>build(
                null, 4848L
        ));

        expect(fieldLayoutManager.getFieldConfigurationScheme(eq(mockProject))).andReturn(fieldConfigurationScheme);
        expect(fieldLayoutManager.getFieldLayoutSchemes()).andReturn(Lists.<FieldLayoutScheme>newArrayList(null, null));
        expect(fieldLayoutManager.getFieldLayout(eq(mockProject), eq("type a"))).andReturn(fieldLayout);

        expect(fieldLayout.getId()).andReturn(4848L).atLeastOnce();
        expect(fieldLayout.getName()).andReturn("Custom Field Config").atLeastOnce();
        expect(fieldLayout.getDescription()).andReturn("Custom Field Description").atLeastOnce();

        control.replay();

        final FieldsSummaryPanelContextProvider fieldsSummaryPanelContextProvider =
                getFieldsSummaryPanelContextProviderUnderTest(fieldLayoutManager, contextProviderUtils);

        final Map<String, Object> contextMap = fieldsSummaryPanelContextProvider.getContextMap(testContext);

        final Map<String, Object> expectedMap = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(FieldsSummaryPanelContextProvider.CONTEXT_FIELD_CONFIG_SCHEME_KEY,
                        new SimpleFieldConfigScheme(2828L, "Field Config Scheme", "", null, getEditUrl()))
                .add(FieldsSummaryPanelContextProvider.CONTEXT_FIELD_CONFIGS_KEY, Lists.<SimpleFieldConfig>
                        newArrayList(new SimpleFieldConfig(4848L, "Custom Field Config", "Custom Field Description", getFieldConfigUrl(4848L), true)))
                .toMap();

        assertEquals(expectedMap, contextMap);

        control.verify();
    }

    @Test
    public void testFieldConfigUrl()
    {
        final long fieldConfigId = 4L;
        expect(contextProviderUtils.createUrlBuilder("/secure/admin/ConfigureFieldLayout!default.jspa"))
                .andReturn(new UrlBuilder("fieldConfig", "UTF-8", false));

        control.replay();

        FieldsSummaryPanelContextProvider provider = new FieldsSummaryPanelContextProvider (fieldLayoutManager, contextProviderUtils,
                tabUrlFactory, comparatorFactory);
        assertEquals("fieldConfig?id=" + fieldConfigId, provider.getFieldConfigUrl(fieldConfigId));

        control.verify();
    }

    @Test
    public void testGetEditSchemeUrl()
    {
        final long fieldConfigId = 2L;
        expect(tabUrlFactory.forFields()).andStubReturn("fieldConfig");

        control.replay();

        FieldsSummaryPanelContextProvider provider = new FieldsSummaryPanelContextProvider (fieldLayoutManager, contextProviderUtils,
                tabUrlFactory, comparatorFactory);
        assertEquals("fieldConfig", provider.getEditSchemeUrl());

        control.verify();
    }

    @Test
    public void testGetSystemDefaultEditConfigUrl()
    {
        expect(contextProviderUtils.createUrlBuilder("/secure/admin/ViewIssueFields.jspa"))
                .andReturn(new UrlBuilder("fieldConfig", "UTF-8", false));

        control.replay();

        FieldsSummaryPanelContextProvider provider = new FieldsSummaryPanelContextProvider (fieldLayoutManager,
                contextProviderUtils, tabUrlFactory, new MockComparatorFactory());
        assertEquals("fieldConfig", provider.getSystemDefaultEditConfigUrl());

        control.verify();
    }

    private FieldsSummaryPanelContextProvider getFieldsSummaryPanelContextProviderUnderTest(final FieldLayoutManager fieldLayoutManager,
            final ContextProviderUtils contextProviderUtils)
    {
        return new FieldsSummaryPanelContextProvider(fieldLayoutManager, contextProviderUtils, tabUrlFactory, comparatorFactory)
        {
            @Override
            String getEditSchemeUrl()
            {
                return getEditUrl();
            }

            @Override
            String getFieldConfigUrl(Long id)
            {
                return TestFieldsSummaryPanelContextProvider.getFieldConfigUrl(id);
            }

            @Override
            String getSystemDefaultEditConfigUrl()
            {
                return getSystemDefaultUrl();
            }
        };
    }

    private static String getFieldConfigUrl(Long id)
    {
        return "fieldConfigUrl" + id;
    }

    private static String getEditUrl()
    {
        return "editUrl";
    }

    private static String getSystemDefaultUrl()
    {
        return "systemDefaultEditUrl";
    }


}
