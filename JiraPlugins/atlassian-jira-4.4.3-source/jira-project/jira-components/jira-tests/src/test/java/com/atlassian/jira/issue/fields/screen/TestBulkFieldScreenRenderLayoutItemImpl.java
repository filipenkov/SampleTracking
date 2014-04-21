package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItemImpl;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createControl;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.BulkFieldScreenRenderLayoutItemImpl}.
 *
 * @since v4.1
 */
public class TestBulkFieldScreenRenderLayoutItemImpl extends LegacyJiraMockTestCase
{
    private IMocksControl control;
    private HackyFieldRendererRegistry mockHackyFieldRendererRegistry;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        control = createControl();
        mockHackyFieldRendererRegistry = control.createMock(HackyFieldRendererRegistry.class);
    }

    public void testIsShownTrue() throws Exception
    {
        Issue mockIssue = new MockIssue(67L);

        final FieldScreenLayoutItem screenLayoutItem = control.createMock(FieldScreenLayoutItem.class);

        expect(screenLayoutItem.isShown(mockIssue)).andReturn(true);

        final FieldLayoutItem itemOne = control.createMock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = control.createMock(FieldLayoutItem.class);

        expect(itemOne.isHidden()).andReturn(false);
        expect(itemTwo.isHidden()).andReturn(false);

        control.replay();

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, screenLayoutItem, Arrays.asList(itemOne, itemTwo));
        assertTrue(bulkFieldScreenRenderLayoutItem.isShow(mockIssue));

        control.verify();
    }

    public void testIsShownTrueNoFieldScreenLayoutItem() throws Exception
    {
        Issue mockIssue = new MockIssue(67L);

        final FieldLayoutItem itemOne = control.createMock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = control.createMock(FieldLayoutItem.class);

        expect(itemOne.isHidden()).andReturn(false);
        expect(itemTwo.isHidden()).andReturn(false);

        control.replay();

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Arrays.asList(itemOne, itemTwo));
        assertTrue(bulkFieldScreenRenderLayoutItem.isShow(mockIssue));

        control.verify();
    }

    public void testIsShownFalseScreenLayoutItem() throws Exception
    {
        Issue mockIssue = new MockIssue(67L);

        final FieldScreenLayoutItem screenLayoutItem = control.createMock(FieldScreenLayoutItem.class);

        expect(screenLayoutItem.isShown(mockIssue)).andReturn(false);

        final FieldLayoutItem itemOne = control.createMock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = control.createMock(FieldLayoutItem.class);

        control.replay();

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, screenLayoutItem, Arrays.asList(itemOne, itemTwo));
        assertFalse(bulkFieldScreenRenderLayoutItem.isShow(mockIssue));

        control.verify();
    }

    public void testIsShownFalseFieldLayoutItem() throws Exception
    {
        Issue mockIssue = new MockIssue(67L);

        final FieldScreenLayoutItem screenLayoutItem = control.createMock(FieldScreenLayoutItem.class);

        expect(screenLayoutItem.isShown(mockIssue)).andReturn(true);

        final FieldLayoutItem itemOne = control.createMock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = control.createMock(FieldLayoutItem.class);
        final FieldLayoutItem itemThree = control.createMock(FieldLayoutItem.class);

        expect(itemOne.isHidden()).andReturn(false);
        expect(itemTwo.isHidden()).andReturn(true);

        control.replay();

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, screenLayoutItem, Arrays.asList(itemOne, itemTwo, itemThree));
        assertFalse(bulkFieldScreenRenderLayoutItem.isShow(mockIssue));

        control.verify();
    }

    public void testIsRequiredFalse() throws Exception
    {
        final FieldLayoutItem itemOne = control.createMock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = control.createMock(FieldLayoutItem.class);

        expect(itemOne.isRequired()).andReturn(false);
        expect(itemTwo.isRequired()).andReturn(false);

        control.replay();

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Arrays.asList(itemOne, itemTwo));
        assertFalse(bulkFieldScreenRenderLayoutItem.isRequired());

        control.verify();
    }

    public void testIsRequiredTrue() throws Exception
    {
        final FieldLayoutItem itemOne = control.createMock(FieldLayoutItem.class);
        final FieldLayoutItem itemTwo = control.createMock(FieldLayoutItem.class);

        expect(itemOne.isRequired()).andReturn(false);
        expect(itemTwo.isRequired()).andReturn(true);

        control.replay();

        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Arrays.asList(itemOne, itemTwo));
        assertTrue(bulkFieldScreenRenderLayoutItem.isRequired());

        control.verify();
    }

    public void testIsRequiredNoItems() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulkFieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        assertFalse(bulkFieldScreenRenderLayoutItem.isRequired());
    }

    public void testGetEditHtmlNoIssues() throws Exception
    {
        final String expectedHtml = "good";

        final OrderableField of = control.createMock(OrderableField.class);
        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(false).setRequired(false);
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem().setOrderableField(of);

        final FieldLayoutItem expectedItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(of)
                .setFieldDescription(null)
                .setHidden(false)
                .setRequired(false)
                .setFieldManager(mockFieldManager)
                .build();
        expect(of.getEditHtml(expectedItem, null, null, null, null)).andReturn(expectedHtml);
        expect(mockHackyFieldRendererRegistry.shouldOverrideDefaultRenderers(of)).andReturn(false);

        control.replay();

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, screenLayoutItem, Collections.<FieldLayoutItem>singletonList(layoutItem));
        assertEquals(expectedHtml, bulk.getEditHtml(null, null, Collections.<Issue>emptyList(), null));

        control.verify();
    }

    public void testGetEditHtmlShown() throws Exception
    {
        final String expectedHtml = "good";

        final OrderableField of = control.createMock(OrderableField.class);
        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(false).setRequired(true);
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem().setOrderableField(of);
        final MockIssue lastIssue = new MockIssue(6);
        final Collection<Issue> issues = Arrays.<Issue>asList(new MockIssue(5), lastIssue);

        final FieldLayoutItem expectedItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(of)
                .setFieldDescription("the environment field")
                .setHidden(false)
                .setRequired(true)
                .setFieldManager(mockFieldManager)
                .build();
        expect(of.isShown(EasyMock.<Issue>anyObject())).andReturn(true).anyTimes();
        expect(of.getEditHtml(expectedItem, null, null, lastIssue, null)).andReturn(expectedHtml);
        expect(mockHackyFieldRendererRegistry.shouldOverrideDefaultRenderers(of)).andReturn(false);

        control.replay();

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, screenLayoutItem, Collections.<FieldLayoutItem>singletonList(layoutItem));
        assertEquals(expectedHtml, bulk.getEditHtml(null, null, issues, null));

        control.verify();
    }

    public void testGetEditHtmlNotShown() throws Exception
    {
        final String expectedHtml = "";

        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockOrderableField of = mockFieldManager.addMockOrderableField(5);
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(true).setRequired(false);
        final Collection<Issue> issues = Collections.<Issue>singletonList(new MockIssue());

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>singletonList(layoutItem));
        assertEquals(expectedHtml, bulk.getEditHtml(null, null, issues, null));
    }

    public void testGetViewHtmlNoIssues() throws Exception
    {
        final String expectedHtml = "good";

        final OrderableField of = control.createMock(OrderableField.class);
        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(false).setRequired(false);
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem().setOrderableField(of);
        final FieldLayoutItem expectedItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(of)
                .setFieldDescription(null)
                .setHidden(false)
                .setRequired(false)
                .setFieldManager(mockFieldManager)
                .build();
        expect(of.getViewHtml(expectedItem, null, null, null)).andReturn(expectedHtml);
        expect(mockHackyFieldRendererRegistry.shouldOverrideDefaultRenderers(of)).andReturn(false);

        control.replay();

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, screenLayoutItem, Collections.<FieldLayoutItem>singletonList(layoutItem));
        assertEquals(expectedHtml, bulk.getViewHtml(null, null, Collections.<Issue>emptyList(), null));

        control.verify();
    }

    public void testGetViewHtmlShown() throws Exception
    {
        final String expectedHtml = "good";

        final OrderableField of = control.createMock(OrderableField.class);
        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(false).setRequired(true);
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem().setOrderableField(of);
        final MockIssue lastIssue = new MockIssue(6);
        final Collection<Issue> issues = Arrays.<Issue>asList(new MockIssue(5), lastIssue);

        final FieldLayoutItem expectedItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(of)
                .setFieldDescription(null)
                .setHidden(false)
                .setRequired(true)
                .setFieldManager(mockFieldManager)
                .build();
        expect(of.isShown(EasyMock.<Issue>anyObject())).andReturn(true).anyTimes();
        expect(of.getViewHtml(expectedItem, null, lastIssue, null)).andReturn(expectedHtml);
        expect(mockHackyFieldRendererRegistry.shouldOverrideDefaultRenderers(of)).andReturn(false);

        control.replay();

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, screenLayoutItem, Collections.<FieldLayoutItem>singletonList(layoutItem));
        assertEquals(expectedHtml, bulk.getViewHtml(null, null, issues, null));

        control.verify();
    }

    public void testGetViewHtmlNotShown() throws Exception
    {
        final String expectedHtml = "";

        final MockFieldManager mockFieldManager = new MockFieldManager();
        final MockOrderableField of = mockFieldManager.addMockOrderableField(5);
        final MockFieldLayoutItem layoutItem = new MockFieldLayoutItem().setOrderableField(of).setHidden(true).setRequired(false);
        final Collection<Issue> issues = Collections.<Issue>singletonList(new MockIssue());

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mockFieldManager, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>singletonList(layoutItem));
        assertEquals(expectedHtml, bulk.getViewHtml(null, null, issues, null));
    }

    public void testGetOrderableField() throws Exception
    {
        final MockFieldManager mfm = new MockFieldManager();
        final MockOrderableField mockOrderableField = mfm.addMockOrderableField(5);
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem().setOrderableField(mockOrderableField);

        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(mfm, mockHackyFieldRendererRegistry, screenLayoutItem, Collections.<FieldLayoutItem>emptyList());
        assertSame(mockOrderableField, bulk.getOrderableField());
    }

    public void testPopulateDefaults() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.populateDefaults(null, null);
            fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    public void testPopuldateFromIssue() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.populateFromIssue(null, null);
            fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }
    public void testGetRendererType() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.getRendererType();
            fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    public void testGetFieldLayoutItem() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.getFieldLayoutItem();
            fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    public void testGetEditHtmlInterface() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.getEditHtml(null, null, null);
            fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    public void testGetCreateHtmlInterface() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.getCreateHtml(null, null, null);
            fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }

    public void testGetViewHtmlInterface() throws Exception
    {
        final BulkFieldScreenRenderLayoutItemImpl bulk = new BulkFieldScreenRenderLayoutItemImpl(null, mockHackyFieldRendererRegistry, null, Collections.<FieldLayoutItem>emptyList());
        try
        {
            bulk.getViewHtml(null, null, null);
            fail("Bulk item does not support this.");
        }
        catch (UnsupportedOperationException expected)
        {
            //good.
        }
    }
}
