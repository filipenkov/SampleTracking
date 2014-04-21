package com.atlassian.jira.issue.fields.layout.field.enterprise;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.*;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
public class TestEnterpriseFieldLayoutManager extends MockControllerTestCase
{
    private static final List<String> allIssueTypeIds = CollectionBuilder.newBuilder("1", "2", "3", "4").asList();

    private FieldManager fieldManager;
    private OfBizDelegator ofBizDelegator;
    private ConstantsManager constantsManager;
    private SubTaskManager subTaskManager;

    @Before
    public void setUp() throws Exception
    {
        fieldManager = mockController.getMock(FieldManager.class);
        ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        constantsManager = mockController.getMock(ConstantsManager.class);
        subTaskManager = mockController.getMock(SubTaskManager.class);
    }

    @Test
    public void testGetUniqueFieldLayoutsNoAssociatedScheme() throws Exception
    {
        final AtomicBoolean getDefaultLayoutCalled = new AtomicBoolean(false);
        DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(new MockFieldManager(), ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected I18nHelper getI18nHelper()
            {
                return new MockI18nHelper();
            }

            @Override
            public FieldLayout getFieldLayout()
            {
                getDefaultLayoutCalled.set(true);
                return null;
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final GenericValue project)
            {
                return null;
            }
        };

        replay();

        fieldLayoutManager.getUniqueFieldLayouts(new MockProject(12345L, "TST", "Test"));
        assertTrue(getDefaultLayoutCalled.get());
    }
    
    @Test
    public void testGetUniqueFieldLayouts() throws Exception
    {
        constantsManager.getAllIssueTypeIds();
        mockController.setReturnValue(Collections.<String>emptyList());
        final FieldConfigurationScheme fieldConfigurationScheme = mockController.getMock(FieldConfigurationScheme.class);
        final Set<Long> allFieldLayouIds = CollectionBuilder.newBuilder(1L, 2L, 3L).asSet();
        expect(fieldConfigurationScheme.getAllFieldLayoutIds(Collections.<String>emptyList())).andReturn(allFieldLayouIds);

        final Set<Long> foundLayoutIds = new HashSet<Long>();

        DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(new MockFieldManager(), ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected I18nHelper getI18nHelper()
            {
                return new MockI18nHelper();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final GenericValue project)
            {
                return fieldConfigurationScheme;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                foundLayoutIds.add(id);
                return null;
            }
        };

        replay();

        fieldLayoutManager.getUniqueFieldLayouts(new MockProject(12345L, "TST", "Test"));
        assertEquals(allFieldLayouIds, foundLayoutIds);
    }

    @Test
    public void testFieldConfigurationSchemeExists() throws Exception
    {
        OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        ofBizDelegator.store(new MockGenericValue("FieldLayoutScheme", new FieldMap("name", "Eno")));
        DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(new MockFieldManager(), ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            public I18nHelper getI18nHelper()
            {
                return new MockI18nHelper();
            }
        };

        replay();
        assertTrue(fieldLayoutManager.fieldConfigurationSchemeExists("Eno"));
        assertFalse(fieldLayoutManager.fieldConfigurationSchemeExists("Byrne"));
    }

    @Test
    public void testGetFieldConfigurationSchemes() throws Exception
    {
        // Set up the MockOfBizDelegator
        OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        GenericValue gvFieldLayoutScheme1 = new MockGenericValue("FieldLayoutScheme", new FieldMap("id", 1L).add("name", "Scheme 1"));
        ofBizDelegator.store(gvFieldLayoutScheme1);
        GenericValue gvFieldLayoutSchemeEntity101 = new MockGenericValue("FieldLayoutSchemeEntity", new FieldMap("id", 101).add("scheme", 1L).add("fieldlayout", 11L));
        ofBizDelegator.store(gvFieldLayoutSchemeEntity101);
        GenericValue gvFieldLayoutSchemeEntity102 = new MockGenericValue("FieldLayoutSchemeEntity", new FieldMap("id", 102).add("scheme", 1L).add("fieldlayout", null));
        ofBizDelegator.store(gvFieldLayoutSchemeEntity102);
        GenericValue gvFieldLayoutScheme2 = new MockGenericValue("FieldLayoutScheme", new FieldMap("id", 2L).add("name", "Scheme 2"));
        ofBizDelegator.store(gvFieldLayoutScheme2);
        GenericValue gvFieldLayoutSchemeEntity103 = new MockGenericValue("FieldLayoutSchemeEntity", new FieldMap("id", 103).add("scheme", 2L).add("fieldlayout", 11L));
        ofBizDelegator.store(gvFieldLayoutSchemeEntity103);

        DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(new MockFieldManager(), ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            public I18nHelper getI18nHelper()
            {
                return new MockI18nHelper();
            }
        };

        replay();
        // Test for the Custom FieldLayout ID = 11
        FieldLayout fieldLayout = makeFieldLayout(11);
        Set<FieldConfigurationScheme> expected = CollectionBuilder.newBuilder(fieldLayoutManager.getFieldConfigurationScheme(1L), fieldLayoutManager.getFieldConfigurationScheme(2L)).asSet();
        assertEquals(expected, fieldLayoutManager.getFieldConfigurationSchemes(fieldLayout));
        // Now test for the Default FieldLayout
        fieldLayout = makeDefaultFieldLayout(12);
        expected = CollectionBuilder.newBuilder(fieldLayoutManager.getFieldConfigurationScheme(1L)).asSet();
        assertEquals(expected, fieldLayoutManager.getFieldConfigurationSchemes(fieldLayout));
    }

    private FieldLayout makeDefaultFieldLayout(final int id)
    {
        return new EditableDefaultFieldLayoutImpl(new MockGenericValue("FieldLayout", new FieldMap("id", new Long(id))), new ArrayList<FieldLayoutItem>());
    }

    private FieldLayout makeFieldLayout(final long id)
    {
        return new EditableFieldLayoutImpl(new MockGenericValue("FieldLayout", new FieldMap("id", new Long(id))), new ArrayList<FieldLayoutItem>());
    }

    @Test
    public void testIsFieldLayoutsVisiblyEquivalentSameLayoutItems() throws Exception
    {
        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        final FieldLayout layout2 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout2.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        replay(layout1, layout2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            protected FieldLayout getRelevantFieldLayout(final Long id)
            {
                if (id.equals(10L))
                {
                    return layout1;
                }
                if (id.equals(20L))
                {
                    return layout2;
                }
                return null;
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(10L, 20L));
        assertTrue(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(20L, 10L));

        verify(layout1, layout2);
    }

    @Test
    public void testIsFieldLayoutsVisiblyEquivalentDifferentLayoutItemsEqual() throws Exception
    {
        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                ).asList();

        final List<FieldLayoutItem> layoutItems2 = CollectionBuilder.newBuilder(
                createLayoutItem("id2", false),
                createLayoutItem("id1", true)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        final FieldLayout layout2 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout2.getFieldLayoutItems())
                .andStubReturn(layoutItems2);

        replay(layout1, layout2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            protected FieldLayout getRelevantFieldLayout(final Long id)
            {
                if (id.equals(10L))
                {
                    return layout1;
                }
                if (id.equals(20L))
                {
                    return layout2;
                }
                return null;
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(10L, 20L));
        assertTrue(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(20L, 10L));

        verify(layout1, layout2);
    }

    @Test
    public void testIsFieldLayoutsVisiblyEquivalentDifferentLayoutItemsDifferent() throws Exception
    {
        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                ).asList();

        final List<FieldLayoutItem> layoutItems2 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", true),
                createLayoutItem("id2", true)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        final FieldLayout layout2 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout2.getFieldLayoutItems())
                .andStubReturn(layoutItems2);

        replay(layout1, layout2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            protected FieldLayout getRelevantFieldLayout(final Long id)
            {
                if (id.equals(10L))
                {
                    return layout1;
                }
                if (id.equals(20L))
                {
                    return layout2;
                }
                return null;
            }
        };

        assertFalse(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(10L, 20L));
        assertFalse(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(20L, 10L));

        verify(layout1, layout2);
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentBothDefault() throws Exception
    {
        replay();

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return allIssueTypeIds;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(null, null));
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentSimple() throws Exception
    {
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity(null, 2L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11L, CollectionBuilder.newBuilder(e1).asList());
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12L, CollectionBuilder.newBuilder(e2).asList());

        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        final FieldLayout layout2 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout2.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        replay(layout1, layout2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return allIssueTypeIds;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == 1L)
                {
                    return layout1;
                }
                if (id == 2L)
                {
                    return layout2;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));

        verify(layout1, layout2);
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentSimpleAndNullScheme() throws Exception
    {
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11L, CollectionBuilder.newBuilder(e1).asList());

        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        final FieldLayout layout2 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout2.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        replay(layout1, layout2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return allIssueTypeIds;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id ==null)
                {
                    return layout2;
                }
                if (id == 1L)
                {
                    return layout1;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                throw new UnsupportedOperationException();
            }

        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, null));
        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(null, 11L));

        verify(layout1, layout2);
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentDefaultsSameAndAdditionalSame() throws Exception
    {
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity("1", 2L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11L, CollectionBuilder.newBuilder(e1).asList());
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12L, CollectionBuilder.newBuilder(e2, e3).asList());

        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", true),
                createLayoutItem("id2", false)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        final FieldLayout layout2 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout2.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        replay(layout1, layout2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return allIssueTypeIds;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == 1L)
                {
                    return layout1;
                }
                if (id == 2L)
                {
                    return layout2;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));

        verify(layout1, layout2);
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentDefaultsSameButAdditionalDifferent() throws Exception
    {
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity("1", 2L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11, CollectionBuilder.newBuilder(e1).asList());
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12, CollectionBuilder.newBuilder(e2, e3).asList());

        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", false),
                createLayoutItem("id2", false)
                ).asList();

        final List<FieldLayoutItem> layoutItems2 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", true),
                createLayoutItem("id2", true)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        final FieldLayout layout2 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout2.getFieldLayoutItems())
                .andStubReturn(layoutItems2);

        replay(layout1, layout2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return allIssueTypeIds;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == 1L)
                {
                    return layout1;
                }
                if (id == 2L)
                {
                    return layout2;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));

        verify(layout1, layout2);
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentTwoAdditionalsDifferent() throws Exception
    {
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity("1", 2L);
        FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e4 = createFieldLayoutSchemeEntity("2", 3L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11, CollectionBuilder.newBuilder(e1, e2).asList());
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12, CollectionBuilder.newBuilder(e3, e4).asList());

        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", false),
                createLayoutItem("id2", false)
                ).asList();

        final List<FieldLayoutItem> layoutItems2 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", true),
                createLayoutItem("id2", true)
                ).asList();

        final List<FieldLayoutItem> layoutItems3 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", false),
                createLayoutItem("id2", true)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        final FieldLayout layout2 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout2.getFieldLayoutItems())
                .andStubReturn(layoutItems2);

        final FieldLayout layout3 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout3.getFieldLayoutItems())
                .andStubReturn(layoutItems3);

        replay(layout1, layout2, layout3);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return allIssueTypeIds;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == 1L)
                {
                    return layout1;
                }
                if (id == 2L)
                {
                    return layout2;
                }
                if (id == 3L)
                {
                    return layout3;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));

        verify(layout1, layout2, layout3);
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentTwoAdditionalsEquivalentToDefault() throws Exception
    {
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity("1", 1L);
        FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e4 = createFieldLayoutSchemeEntity("2", 1L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11, CollectionBuilder.newBuilder(e1, e2).asList());
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12, CollectionBuilder.newBuilder(e3, e4).asList());

        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", false),
                createLayoutItem("id2", false)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        replay(layout1);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return allIssueTypeIds;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                return layout1;
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));

        verify(layout1);
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentExhaustiveSameButDifferentDefault() throws Exception
    {
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity("1", 2L);
        FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity("2", 2L);
        FieldLayoutSchemeEntity e4 = createFieldLayoutSchemeEntity(null, 2L);
        FieldLayoutSchemeEntity e5 = createFieldLayoutSchemeEntity("1", 2L);
        FieldLayoutSchemeEntity e6 = createFieldLayoutSchemeEntity("2", 2L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11, CollectionBuilder.newBuilder(e1, e2, e3).asList());
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12, CollectionBuilder.newBuilder(e4, e5, e6).asList());

        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", false),
                createLayoutItem("id2", false)
                ).asList();

        final List<FieldLayoutItem> layoutItems2 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", true),
                createLayoutItem("id2", true)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        final FieldLayout layout2 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout2.getFieldLayoutItems())
                .andStubReturn(layoutItems2);

        replay(layout1, layout2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return CollectionBuilder.newBuilder("1", "2").asList();
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == 1L)
                {
                    return layout1;
                }
                if (id == 2L)
                {
                    return layout2;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertTrue(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));

        verify(layout1, layout2);
    }

    @Test
    public void testIsFieldLayoutSchemesVisiblyEquivalentNoDefault() throws Exception
    {
        FieldLayoutSchemeEntity e1 = createFieldLayoutSchemeEntity(null, 1L);
        FieldLayoutSchemeEntity e2 = createFieldLayoutSchemeEntity("1", 1L);
        FieldLayoutSchemeEntity e3 = createFieldLayoutSchemeEntity("2", 1L);

        final FieldConfigurationScheme scheme1 = createFieldConfigurationScheme(11, CollectionBuilder.newBuilder(e1, e2).asList());
        final FieldConfigurationScheme scheme2 = createFieldConfigurationScheme(12, CollectionBuilder.newBuilder(e3).asList());

        final List<FieldLayoutItem> layoutItems1 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", false),
                createLayoutItem("id2", false)
                ).asList();

        final FieldLayout layout1 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout1.getFieldLayoutItems())
                .andStubReturn(layoutItems1);

        final List<FieldLayoutItem> layoutItems2 = CollectionBuilder.newBuilder(
                createLayoutItem("id1", false),
                createLayoutItem("id2", true)
                ).asList();

        final FieldLayout layout2 = EasyMock.createMock(FieldLayout.class);
        EasyMock.expect(layout2.getFieldLayoutItems())
                .andStubReturn(layoutItems2);

        replay(layout1, layout2);

        final DefaultFieldLayoutManager fieldLayoutManager = new DefaultFieldLayoutManager(fieldManager, ofBizDelegator, constantsManager, subTaskManager, null, null, null)
        {
            @Override
            protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
            {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getAllRelevantIssueTypeIds()
            {
                return allIssueTypeIds;
            }

            @Override
            public FieldLayout getFieldLayout(final Long id)
            {
                if (id == null)
                {
                    return layout2;
                }
                if (id == 1L)
                {
                    return layout1;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
            {
                if (schemeId == 11L)
                {
                    return scheme1;
                }
                if (schemeId == 12L)
                {
                    return scheme2;
                }
                throw new UnsupportedOperationException();
            }
        };

        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(11L, 12L));
        assertFalse(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(12L, 11L));

        verify(layout1);
    }

    private FieldLayoutSchemeEntity createFieldLayoutSchemeEntity(final String issueTypeId, final Long fieldLayoutId)
    {
        return new FieldLayoutSchemeEntity()
        {
            public Long getId()
            {
                throw new UnsupportedOperationException();
            }

            public String getIssueTypeId()
            {
                return issueTypeId;
            }

            public GenericValue getIssueType()
            {
                throw new UnsupportedOperationException();
            }

            public IssueType getIssueTypeObject()
            {
                throw new UnsupportedOperationException();
            }

            public void setIssueTypeId(final String issueTypeId)
            {
                throw new UnsupportedOperationException();
            }

            public Long getFieldLayoutId()
            {
                return fieldLayoutId;
            }

            public void setFieldLayoutId(final Long fieldLayoutId)
            {
                throw new UnsupportedOperationException();
            }

            public FieldLayoutScheme getFieldLayoutScheme()
            {
                throw new UnsupportedOperationException();
            }

            public void setFieldLayoutScheme(final FieldLayoutScheme fieldLayoutScheme)
            {
                throw new UnsupportedOperationException();
            }

            public GenericValue getGenericValue()
            {
                throw new UnsupportedOperationException();
            }

            public void setGenericValue(final GenericValue genericValue)
            {
                throw new UnsupportedOperationException();
            }

            public void store()
            {
                throw new UnsupportedOperationException();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }

            public int compareTo(final FieldLayoutSchemeEntity o)
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private FieldConfigurationScheme createFieldConfigurationScheme(final long id, final List<FieldLayoutSchemeEntity> fieldLayoutSchemeEntities)
    {
        final Map<String, Long> issueTypeToFieldLayoutIdMap = new HashMap<String, Long>();
        for (FieldLayoutSchemeEntity entity : fieldLayoutSchemeEntities)
        {
            issueTypeToFieldLayoutIdMap.put(entity.getIssueTypeId(), entity.getFieldLayoutId());
        }

        return new FieldConfigurationScheme() {

            public Long getId()
            {
                return id;
            }

            public String getName()
            {
                return null;
            }

            public String getDescription()
            {
                return null;
            }

            public Long getFieldLayoutId(final String issueTypeId)
            {
                if (issueTypeToFieldLayoutIdMap.containsKey(issueTypeId))
                {
                    return issueTypeToFieldLayoutIdMap.get(issueTypeId);
                }
                else
                {
                    return issueTypeToFieldLayoutIdMap.get(null);
                }
            }

            public Set<Long> getAllFieldLayoutIds(final Collection<String> allIssueTypeIds)
            {
                return null;
            }
        };
    }

    private FieldLayoutItem createLayoutItem(final String fieldId, final boolean isHidden)
    {
        return (FieldLayoutItem) DuckTypeProxy.getProxy(FieldLayoutItem.class, new Object()
        {
            public OrderableField getOrderableField()
            {
                return (OrderableField) DuckTypeProxy.getProxy(OrderableField.class, new Object()
                {
                    public String getId()
                    {
                        return fieldId;
                    }
                });
            }

            public boolean isHidden()
            {
                return isHidden;
            }
        });
    }
}
