package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptorImpl;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.Predicates;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.StandardFieldScreenRendererFactory}.
 *
 * @since v4.1
 */
public class TestStandardFieldScreenRendererFactory extends ListeningTestCase
{
    @Test
    public void testCreateFieldScreenRendererOperation() throws Exception
    {
        final IMocksControl mocksControl = createControl();

        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final FieldScreen mockFieldScreen = new MockFieldScreen();
        final Issue mockIssue = new MockIssue(5);
        final FieldScreenScheme mockScreenScheme = mocksControl.createMock(FieldScreenScheme.class);
        final IssueTypeScreenSchemeManager manager = mocksControl.createMock(IssueTypeScreenSchemeManager.class);
        final Predicate<Field> mockPredicate = new Predicate<Field>()
        {
            public boolean evaluate(final Field input)
            {
                return false;
            }
        };

        expect(manager.getFieldScreenScheme(mockIssue)).andReturn(mockScreenScheme);
        expect(mockScreenScheme.getFieldScreen(IssueOperations.VIEW_ISSUE_OPERATION)).andReturn(mockFieldScreen);

        mocksControl.replay();

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, manager, null)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final FieldScreen fieldScreen, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertEquals(mockIssue, issue);
                assertSame(mockFieldScreen, fieldScreen);
                assertEquals(IssueOperations.VIEW_ISSUE_OPERATION, operation);
                assertSame(mockPredicate, condition);

                return mockRenderer;
            }
        };

        final FieldScreenRenderer actionRender = factory.createFieldScreenRenderer(mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, mockPredicate);
        assertSame(mockRenderer, actionRender);

        mocksControl.verify();
    }

    @Test
    public void testCreateFieldScreenRendererAction() throws Exception
    {
        final IMocksControl mocksControl = createControl();

        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final FieldScreen mockFieldScreen = new MockFieldScreen();
        final Issue mockIssue = new MockIssue(5);
        final ActionDescriptor mockDescriptor = mocksControl.createMock(ActionDescriptor.class);

        mocksControl.replay();

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, null, null)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final FieldScreen fieldScreen, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertEquals(mockIssue, issue);
                assertSame(mockFieldScreen, fieldScreen);
                assertNull(operation);
                assertEquals(Predicates.<Object>truePredicate(), condition);

                return mockRenderer;
            }

            @Override
            FieldScreen getScreenFromAction(final ActionDescriptor descriptor)
            {
                assertSame(mockDescriptor, descriptor);
                return mockFieldScreen;
            }
        };

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(mockIssue, mockDescriptor);
        assertSame(mockRenderer, screenRenderer);
        mocksControl.verify();
    }

    @Test
    public void testCreateScreenRendererIssue() throws Exception
    {
        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final Issue mockIssue = new MockIssue(5);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, null, null)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final Collection<FieldScreenTab> tabs, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertSame(mockIssue, issue);
                assertTrue(tabs.isEmpty());
                assertNull(operation);
                assertEquals(Predicates.<Object>truePredicate(), condition);

                return mockRenderer;
            }
        };

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(mockIssue);
        assertSame(mockRenderer, screenRenderer);
    }

    @Test
    public void testCreateScreenRendererFieldScreenNull() throws Exception
    {
        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final Issue mockIssue = new MockIssue(5);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, null, null)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final Collection<FieldScreenTab> tabs, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertSame(mockIssue, issue);
                assertTrue(tabs.isEmpty());
                assertEquals(IssueOperations.EDIT_ISSUE_OPERATION, operation);
                assertEquals(Predicates.falsePredicate(), condition);

                return mockRenderer;
            }
        };

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(mockIssue, (FieldScreen) null, IssueOperations.EDIT_ISSUE_OPERATION, Predicates.falsePredicate());
        assertSame(mockRenderer, screenRenderer);
    }

    @Test
    public void testCreateScreenRendererFieldScreenNotNull() throws Exception
    {
        final FieldScreen mockScreen = new MockFieldScreen();
        mockScreen.addTab("test");
        mockScreen.addTab("test2");

        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final Issue mockIssue = new MockIssue(5);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, null, null)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final Collection<FieldScreenTab> tabs, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertSame(mockIssue, issue);
                assertEquals(mockScreen.getTabs(), tabs);
                assertEquals(IssueOperations.EDIT_ISSUE_OPERATION, operation);
                assertEquals(Predicates.falsePredicate(), condition);

                return mockRenderer;
            }
        };

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(mockIssue, mockScreen, IssueOperations.EDIT_ISSUE_OPERATION, Predicates.falsePredicate());
        assertSame(mockRenderer, screenRenderer);
    }

    @Test
    public void testCreateFieldScreenRendererTabsNoTabs() throws Exception
    {
        final MockIssue mi = new MockIssue(4);
        final MockFieldManager mfm = new MockFieldManager();
        final MockFieldLayoutManager mflm = new MockFieldLayoutManager();
        final MockFieldLayout mfl = mflm.addLayoutItem(mi);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(mfm, mflm, null, null);
        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(mi, Collections.<FieldScreenTab>emptyList(),
                IssueOperations.CREATE_ISSUE_OPERATION, Predicates.falsePredicate());

        assertTrue(screenRenderer.getFieldScreenRenderTabs().isEmpty());
        assertSame(mfl, screenRenderer.getFieldLayout());
    }

    @Test
    public void testCreateFieldScreenRendererTabsStorageException() throws Exception
    {
        final IMocksControl control = createControl();

        final MockIssue issue = new MockIssue(56);
        final FieldLayoutManager layoutManager = control.createMock(FieldLayoutManager.class);
        //noinspection ThrowableInstanceNeverThrown
        expect(layoutManager.getFieldLayout(issue)).andThrow(new DataAccessException("blah"));

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(new MockFieldManager(),
                layoutManager, null, null);

        control.replay();

        try
        {
            factory.createFieldScreenRenderer(issue, Collections.<FieldScreenTab>emptyList(),
                IssueOperations.CREATE_ISSUE_OPERATION, Predicates.falsePredicate());
            fail("Should have thrown the exception.");

        }
        catch (DataAccessException expected)
        {
            //good.
        }

        control.verify();
    }

    @Test
    public void testCreateFieldScreenRendererFields() throws Exception
    {
        final List<String> names = Arrays.asList("a", "b", "c");
        final FieldScreenRenderer mockRenderer = new MockFieldScreenRenderer();
        final Issue mockIssue = new MockIssue(5);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(null, null, null, null)
        {
            @Override
            FieldScreenRenderer createFieldScreenRenderer(final Issue issue, final Collection<FieldScreenTab> tabs, final IssueOperation operation, final Predicate<? super Field> condition)
            {
                assertSame(mockIssue, issue);
                assertEquals(IssueOperations.EDIT_ISSUE_OPERATION, operation);
                assertEquals(Predicates.<Object>truePredicate(), condition);

                assertEquals(1, tabs.size());
                final FieldScreenTab tab = tabs.iterator().next();

                int count = 0;
                final Iterator<String> iterator = names.iterator();
                for (final FieldScreenLayoutItem item : tab.getFieldScreenLayoutItems())
                {
                    assertEquals(iterator.next(), item.getFieldId());
                    assertEquals(count++, item.getPosition());
                    assertSame(tab, item.getFieldScreenTab());
                }

                return mockRenderer;
            }
        };

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(names, mockIssue, IssueOperations.EDIT_ISSUE_OPERATION);
        assertSame(mockRenderer, screenRenderer);

    }

    @Test
    public void testCreateFieldScreenRendererTabs() throws Exception
    {
        MockIssue issue1 = new MockIssue(4);
        issue1.setProjectObject(new MockProject(4));
        issue1.setIssueTypeObject(new MockIssueType("4", "Four"));

        MockIssue issue2 = new MockIssue(5);
        issue2.setProjectObject(new MockProject(5));
        issue2.setIssueTypeObject(new MockIssueType("5", "Five"));

        MockFieldManager mfm = new MockFieldManager();
        final MockOrderableField of1 = mfm.addMockOrderableField(1);
        final MockOrderableField of2 = mfm.addMockOrderableField(2);
        final MockOrderableField of3 = mfm.addMockOrderableField(3);

        MockFieldScreenTab tab1 = new MockFieldScreenTab();
        tab1.addFieldScreenLayoutItem().setOrderableField(of1);
        tab1.addFieldScreenLayoutItem().setOrderableField(of2);
        tab1.setName("Tab1");

        MockFieldScreenTab tab2 = new MockFieldScreenTab();
        tab2.addFieldScreenLayoutItem().setOrderableField(of3);
        tab2.setName("Tab2");

        MockFieldLayoutManager mflm = new MockFieldLayoutManager();
        final MockFieldLayout fieldLayout = mflm.addLayoutItem(issue1);
        fieldLayout.addFieldLayoutItem(of1);
        fieldLayout.addFieldLayoutItem(of2);
        fieldLayout.addFieldLayoutItem(of3);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(mfm, mflm, null, null);

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(issue1, Arrays.<FieldScreenTab>asList(tab1, tab2),
                IssueOperations.VIEW_ISSUE_OPERATION, Predicates.truePredicate());

        //Build up the expected result.
        MockFieldScreenRenderer expectedRenderer = new MockFieldScreenRenderer();
        expectedRenderer.setFieldLayout(fieldLayout);

        MockFieldScreenRendererTab expectedTab = expectedRenderer.addFieldScreenRendererTab();
        expectedTab.setName(tab1.getName());

        MockFieldScreenRendererLayoutItem expectedItem = expectedTab.addLayoutItem();
        expectedItem.setOrderableField(of1);
        expectedItem.setFieldLayoutItem(fieldLayout.getFieldLayoutItem(of1));
        expectedItem.setFieldScreenLayoutItem(tab1.getFieldScreenLayoutItem(0));

        expectedItem = expectedTab.addLayoutItem();
        expectedItem.setOrderableField(of2);
        expectedItem.setFieldLayoutItem(fieldLayout.getFieldLayoutItem(of2));
        expectedItem.setFieldScreenLayoutItem(tab1.getFieldScreenLayoutItem(1));

        expectedTab = expectedRenderer.addFieldScreenRendererTab();
        expectedTab.setName(tab2.getName());
        expectedItem = expectedTab.addLayoutItem();
        expectedItem.setOrderableField(of3);
        expectedItem.setFieldLayoutItem(fieldLayout.getFieldLayoutItem(of3));
        expectedItem.setFieldScreenLayoutItem(tab2.getFieldScreenLayoutItem(0));

        assertRendererEquals(expectedRenderer, screenRenderer);
    }

    @Test
    public void testCreateFieldScreenRendererTabsHidden() throws Exception
    {
        MockIssue issue1 = new MockIssue(4);
        issue1.setProjectObject(new MockProject(4));
        issue1.setIssueTypeObject(new MockIssueType("4", "Four"));

        MockIssue issue2 = new MockIssue(5);
        issue2.setProjectObject(new MockProject(5));
        issue2.setIssueTypeObject(new MockIssueType("5", "Five"));

        MockFieldManager mfm = new MockFieldManager();
        final MockOrderableField of1 = mfm.addMockOrderableField(1);
        //This field is not shown.
        final MockOrderableField of2 = mfm.addMockOrderableField(2).setShown(false);
        final MockOrderableField of3 = mfm.addMockOrderableField(3);

        MockFieldScreenTab tab1 = new MockFieldScreenTab();
        tab1.addFieldScreenLayoutItem().setOrderableField(of1);
        tab1.addFieldScreenLayoutItem().setOrderableField(of2);
        tab1.setName("Tab1");

        MockFieldScreenTab tab2 = new MockFieldScreenTab();
        tab2.addFieldScreenLayoutItem().setOrderableField(of3);
        tab2.setName("Tab2");

        MockFieldLayoutManager mflm = new MockFieldLayoutManager();
        final MockFieldLayout fieldLayout = mflm.addLayoutItem(issue1);
        fieldLayout.addFieldLayoutItem(of1);
        fieldLayout.addFieldLayoutItem(of2);
        //this layout item is hidden.
        fieldLayout.addFieldLayoutItem(of3).setHidden(true);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(mfm, mflm, null, null);

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(issue1, Arrays.<FieldScreenTab>asList(tab1, tab2),
                IssueOperations.VIEW_ISSUE_OPERATION, Predicates.truePredicate());

        //Build up the expected result.
        MockFieldScreenRenderer expectedRenderer = new MockFieldScreenRenderer();
        expectedRenderer.setFieldLayout(fieldLayout);

        MockFieldScreenRendererTab expectedTab = expectedRenderer.addFieldScreenRendererTab();
        expectedTab.setName(tab1.getName());

        MockFieldScreenRendererLayoutItem expectedItem = expectedTab.addLayoutItem();
        expectedItem.setOrderableField(of1);
        expectedItem.setFieldLayoutItem(fieldLayout.getFieldLayoutItem(of1));
        expectedItem.setFieldScreenLayoutItem(tab1.getFieldScreenLayoutItem(0));

        assertRendererEquals(expectedRenderer, screenRenderer);
    }

    @Test
    public void testCreateFieldScreenRendererTabsFilteredFields() throws Exception
    {
        MockIssue issue1 = new MockIssue(4);
        issue1.setProjectObject(new MockProject(4));
        issue1.setIssueTypeObject(new MockIssueType("4", "Four"));

        MockFieldManager mfm = new MockFieldManager();
        final MockOrderableField of1 = mfm.addMockOrderableField(1);
        final MockOrderableField of2 = mfm.addMockOrderableField(2);
        final MockOrderableField of3 = mfm.addMockOrderableField(3);
        mfm.addUnavilableField(of1);

        MockFieldScreenTab tab1 = new MockFieldScreenTab();
        tab1.addFieldScreenLayoutItem().setOrderableField(of1);
        tab1.addFieldScreenLayoutItem().setOrderableField(of2);
        tab1.setName("Tab1");

        MockFieldScreenTab tab2 = new MockFieldScreenTab();
        tab2.addFieldScreenLayoutItem().setOrderableField(of3);
        tab2.setName("Tab2");

        MockFieldLayoutManager mflm = new MockFieldLayoutManager();
        final MockFieldLayout fieldLayout = mflm.addLayoutItem(issue1);
        fieldLayout.addFieldLayoutItem(of1);
        fieldLayout.addFieldLayoutItem(of2);
        fieldLayout.addFieldLayoutItem(of3);

        final Predicate<Field> predicate = new Predicate<Field>()
        {
            public boolean evaluate(final Field input)
            {
                return input != of2;
            }
        };

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(mfm, mflm, null, null);

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(issue1, Arrays.<FieldScreenTab>asList(tab1, tab2),
                IssueOperations.VIEW_ISSUE_OPERATION, predicate);

        //Build up the expected result.
        MockFieldScreenRenderer expectedRenderer = new MockFieldScreenRenderer();
        expectedRenderer.setFieldLayout(fieldLayout);

        MockFieldScreenRendererTab expectedTab = expectedRenderer.addFieldScreenRendererTab();
        expectedTab.setName(tab2.getName());

        MockFieldScreenRendererLayoutItem expectedItem = expectedTab.addLayoutItem();
        expectedItem.setOrderableField(of3);
        expectedItem.setFieldLayoutItem(fieldLayout.getFieldLayoutItem(of3));
        expectedItem.setFieldScreenLayoutItem(tab2.getFieldScreenLayoutItem(0));

        assertRendererEquals(expectedRenderer, screenRenderer);
    }

    @Test
    public void testCreateFieldScreenRendererTabsFilteredCustomFields()
    {
        final MockIssueType issueType = new MockIssueType("4", "Four");
        final MockProject project = new MockProject(4);

        MockIssue issue1 = new MockIssue(4);
        issue1.setProjectObject(project);
        issue1.setIssueTypeObject(issueType);

        final IMocksControl control = createControl();
        final CustomField field1 = control.createMock(CustomField.class);
        expect(field1.getId()).andReturn("1").anyTimes();
        expect(field1.getCustomFieldType()).andReturn(createCustomFieldType(true)).anyTimes();
        expect(field1.isInScope(project, Collections.singletonList(issueType.getId()))).andReturn(true);
        expect(field1.isShown(issue1)).andReturn(true);

        final CustomField field2 = control.createMock(CustomField.class);
        expect(field2.getId()).andReturn("2").anyTimes();
        expect(field2.getCustomFieldType()).andReturn(createCustomFieldType(false)).anyTimes();
        expect(field2.isInScope(project, Collections.singletonList(issueType.getId()))).andReturn(false);
        expect(field2.isShown(issue1)).andReturn(true);

        control.replay();

        MockFieldManager mfm = new MockFieldManager().addField(field1).addField(field2);

        MockFieldScreenTab tab1 = new MockFieldScreenTab();
        tab1.addFieldScreenLayoutItem().setOrderableField(field1);
        tab1.addFieldScreenLayoutItem().setOrderableField(field2);
        tab1.setName("Tab1");

        MockFieldLayoutManager mflm = new MockFieldLayoutManager();
        final MockFieldLayout fieldLayout = mflm.addLayoutItem(issue1);
        fieldLayout.addFieldLayoutItem(field1);
        fieldLayout.addFieldLayoutItem(field2);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(mfm, mflm, null, null);

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(issue1, Arrays.<FieldScreenTab>asList(tab1),
                IssueOperations.CREATE_ISSUE_OPERATION, Predicates.truePredicate());

        //Build up the expected result.
        MockFieldScreenRenderer expectedRenderer = new MockFieldScreenRenderer();
        expectedRenderer.setFieldLayout(fieldLayout);

        MockFieldScreenRendererTab expectedTab = expectedRenderer.addFieldScreenRendererTab();
        expectedTab.setName(tab1.getName());

        MockFieldScreenRendererLayoutItem expectedItem = expectedTab.addLayoutItem();
        expectedItem.setOrderableField(field1);
        expectedItem.setFieldLayoutItem(fieldLayout.getFieldLayoutItem(field1));
        expectedItem.setFieldScreenLayoutItem(tab1.getFieldScreenLayoutItem(0));

        assertRendererEquals(expectedRenderer, screenRenderer);

        control.verify();
    }

    @Test
    public void testCreateFieldScreenRendererTabsFilteredCustomFieldsViewIssue()
    {
        final MockIssueType issueType = new MockIssueType("4", "Four");
        final MockProject project = new MockProject(4);

        MockIssue issue1 = new MockIssue(4);
        issue1.setProjectObject(project);
        issue1.setIssueTypeObject(issueType);

        //Custom field with view template and value.
        final IMocksControl control = createControl();
        final CustomField field1 = control.createMock(CustomField.class);
        expect(field1.getId()).andReturn("1").anyTimes();
        expect(field1.getCustomFieldType()).andReturn(createCustomFieldType(true)).anyTimes();
        expect(field1.isInScope(project, Collections.singletonList(issueType.getId()))).andReturn(true);
        expect(field1.isShown(issue1)).andReturn(true);
        expect(field1.getValue(issue1)).andReturn(1);

        //Custom field without value.
        final CustomField field2 = control.createMock(CustomField.class);
        expect(field2.getId()).andReturn("2").anyTimes();
        expect(field2.isInScope(project, Collections.singletonList(issueType.getId()))).andReturn(true);
        expect(field2.isShown(issue1)).andReturn(true);
        expect(field2.getCustomFieldType()).andReturn(createCustomFieldType(true)).anyTimes();
        expect(field2.getValue(issue1)).andReturn(null);

        //Custom field with value but without view template.
        final CustomField field3 = control.createMock(CustomField.class);
        expect(field3.getId()).andReturn("3").anyTimes();
        expect(field3.getCustomFieldType()).andReturn(createCustomFieldType(false)).anyTimes();
        expect(field3.isInScope(project, Collections.singletonList(issueType.getId()))).andReturn(true);
        expect(field3.isShown(issue1)).andReturn(true);

        control.replay();

        MockFieldManager mfm = new MockFieldManager().addField(field1).addField(field2).addField(field3);

        MockFieldScreenTab tab1 = new MockFieldScreenTab();
        tab1.addFieldScreenLayoutItem().setOrderableField(field1);
        tab1.addFieldScreenLayoutItem().setOrderableField(field2);
        tab1.addFieldScreenLayoutItem().setOrderableField(field3);
        tab1.setName("Tab1");

        MockFieldLayoutManager mflm = new MockFieldLayoutManager();
        final MockFieldLayout fieldLayout = mflm.addLayoutItem(issue1);
        fieldLayout.addFieldLayoutItem(field1);
        fieldLayout.addFieldLayoutItem(field2);
        fieldLayout.addFieldLayoutItem(field3);

        final StandardFieldScreenRendererFactory factory = new StandardFieldScreenRendererFactory(mfm, mflm, null, null);

        final FieldScreenRenderer screenRenderer = factory.createFieldScreenRenderer(issue1, Arrays.<FieldScreenTab>asList(tab1),
                IssueOperations.VIEW_ISSUE_OPERATION, Predicates.truePredicate());

        //Build up the expected result.
        MockFieldScreenRenderer expectedRenderer = new MockFieldScreenRenderer();
        expectedRenderer.setFieldLayout(fieldLayout);

        MockFieldScreenRendererTab expectedTab = expectedRenderer.addFieldScreenRendererTab();
        expectedTab.setName(tab1.getName());

        MockFieldScreenRendererLayoutItem expectedItem = expectedTab.addLayoutItem();
        expectedItem.setOrderableField(field1);
        expectedItem.setFieldLayoutItem(fieldLayout.getFieldLayoutItem(field1));
        expectedItem.setFieldScreenLayoutItem(tab1.getFieldScreenLayoutItem(0));

        assertRendererEquals(expectedRenderer, screenRenderer);

        control.verify();
    }

    private void assertRendererEquals(FieldScreenRenderer expected, FieldScreenRenderer actual)
    {
        assertEquals(expected.getFieldLayout(), actual.getFieldLayout());
        assertTabEquals(expected.getFieldScreenRenderTabs(), actual.getFieldScreenRenderTabs());
    }

    private void assertTabEquals(Collection<FieldScreenRenderTab> expected, Collection<FieldScreenRenderTab> actual)
    {
        assertEquals(expected.size(), actual.size());
        final Iterator<FieldScreenRenderTab> actualIterator = actual.iterator();
        int count = 0;
        for (FieldScreenRenderTab expectedItem : expected)
        {
            assertTabEquals(expectedItem, actualIterator.next(), count++);
        }
    }

    private void assertTabEquals(FieldScreenRenderTab expected, FieldScreenRenderTab actual, int position)
    {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(position, actual.getPosition());
        assertItemEquals(expected.getFieldScreenRenderLayoutItems(), actual.getFieldScreenRenderLayoutItems());
    }

    private void assertItemEquals(Collection<FieldScreenRenderLayoutItem> expected,
            Collection<FieldScreenRenderLayoutItem> actual)
    {
        assertEquals(expected.size(), actual.size());
        final Iterator<FieldScreenRenderLayoutItem> actualIterator = actual.iterator();
        for (FieldScreenRenderLayoutItem expectedItem : expected)
        {
            assertItemEquals(expectedItem, actualIterator.next());
        }
    }

    private void assertItemEquals(FieldScreenRenderLayoutItem expected, FieldScreenRenderLayoutItem actual)
    {
        assertSame(expected.getOrderableField(), actual.getOrderableField());
        assertSame(expected.getFieldLayoutItem(), actual.getFieldLayoutItem());
        assertSame(expected.getFieldScreenLayoutItem(), actual.getFieldScreenLayoutItem());
    }

    private static CustomFieldType createCustomFieldType(final boolean exists)
    {
        final MockCustomFieldType customFieldType = new MockCustomFieldType("type", "type");
        customFieldType.init(createViewExistsCFType(exists));
        return customFieldType;
    }

    private static CustomFieldTypeModuleDescriptor createViewExistsCFType(final boolean exists)
    {
        return new CustomFieldTypeModuleDescriptorImpl(new MockSimpleAuthenticationContext(null), null, null)
        {
            @Override
            public boolean isViewTemplateExists()
            {
                return exists;
            }
        };
    }
}
