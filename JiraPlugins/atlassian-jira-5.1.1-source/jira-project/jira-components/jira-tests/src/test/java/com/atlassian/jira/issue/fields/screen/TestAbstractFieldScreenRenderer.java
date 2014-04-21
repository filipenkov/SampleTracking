package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import org.easymock.IMocksControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.AbstractFieldScreenRenderer}.
 *
 * @since v4.1
 */
public class TestAbstractFieldScreenRenderer extends ListeningTestCase
{
    /*
     * What happens when no tabs exist.
     */

    @Test
    public void testGetRequiredFieldScreenRenderItemsNullItems()
    {
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();
        final Collection<FieldScreenRenderLayoutItem> items = screenRenderer.getRequiredFieldScreenRenderItems();
        assertTrue(items.isEmpty());
    }

    /*
     * Make sure the method only returns required layout items.
     */

    @Test
    public void testGetRequiredFieldScreenRenderItems() throws Exception
    {
        List<FieldScreenRenderLayoutItem> expectedItems = new ArrayList<FieldScreenRenderLayoutItem>();

        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer();

        MockFieldScreenRendererTab rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(false);
        expectedItems.add(rendererTab.addLayoutItem().setRequired(true));

        rendererTab = renderer.addFieldScreenRendererTab();
        expectedItems.add(rendererTab.addLayoutItem().setRequired(true));

        assertEquals(expectedItems, renderer.getRequiredFieldScreenRenderItems());

    }

    /*
     * Test what happens when no tabs exist.
     */

    @Test
    public void testGetFieldScreenRenderTabPositionNullItems()
    {
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();
        assertNull(screenRenderer.getFieldScreenRenderTabPosition("blargs"));
    }

    /*
     * Test what happens when the field cannot be matched.
     */

    @Test
    public void testGetFieldScreenRenderTabPositionNoMatch()
    {
        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer();

        MockFieldScreenRendererTab rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(false).setOrderableField(new MockOrderableField("i"));
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("3"));

        rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("5"));

        assertNull(renderer.getFieldScreenRenderTabPosition("6"));
        assertNull(renderer.getFieldScreenRenderTabPosition(null));
    }

    /*
     * Test when the field can be matched.
     */

    @Test
    public void testGetFieldScreenRenderTabPositionMatch()
    {
        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer();

        MockFieldScreenRendererTab rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(false).setOrderableField(new MockOrderableField("i"));
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("3"));

        rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("5"));

        assertSame(rendererTab, renderer.getFieldScreenRenderTabPosition("5"));
    }

    /*
     * Test when the field cannot be matched.
     */

    @Test
    public void testGetFieldScreenRenderLayoutItemNoMatch()
    {
        final IMocksControl mockControl = createControl();

        final MockOrderableField field = new MockOrderableField("6");
        final FieldLayoutItem fieldLayoutItem = mockControl.createMock(FieldLayoutItem.class);
        final FieldLayout fieldLayout = mockControl.createMock(FieldLayout.class);
        expect(fieldLayout.getFieldLayoutItem(field)).andReturn(fieldLayoutItem);

        mockControl.replay();

        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer().setFieldLayout(fieldLayout);

        MockFieldScreenRendererTab rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("3"));

        rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("5"));

        final FieldScreenRenderLayoutItem actualItem = renderer.getFieldScreenRenderLayoutItem(field);
        assertSame(fieldLayoutItem, actualItem.getFieldLayoutItem());
        assertNull(actualItem.getFieldScreenLayoutItem());

        mockControl.verify();
    }

    /*
     * Test when the field cannot be matched.
     */

    @Test
    public void testGetFieldScreenRenderLayoutItemNoTabs()
    {
        final IMocksControl mockControl = createControl();

        final MockOrderableField field = new MockOrderableField("6");
        final FieldLayoutItem fieldLayoutItem = mockControl.createMock(FieldLayoutItem.class);
        final FieldLayout fieldLayout = mockControl.createMock(FieldLayout.class);
        expect(fieldLayout.getFieldLayoutItem(field)).andReturn(fieldLayoutItem);

        mockControl.replay();

        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer().setFieldLayout(fieldLayout);

        final FieldScreenRenderLayoutItem actualItem = renderer.getFieldScreenRenderLayoutItem(field);
        assertSame(fieldLayoutItem, actualItem.getFieldLayoutItem());
        assertNull(actualItem.getFieldScreenLayoutItem());

        mockControl.verify();
    }

    /*
     * Test when the field can be matched.
     */

    @Test
    public void testGetFieldScreenRenderLayoutItemMatch()
    {
        final MockOrderableField field = new MockOrderableField("i");

        final MockFieldScreenRenderer renderer = new MockFieldScreenRenderer();
        MockFieldScreenRendererTab rendererTab = renderer.addFieldScreenRendererTab();
        final FieldScreenRenderLayoutItem expectedItem = rendererTab.addLayoutItem().setRequired(false).setOrderableField(field);
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("3"));

        rendererTab = renderer.addFieldScreenRendererTab();
        rendererTab.addLayoutItem().setRequired(true).setOrderableField(new MockOrderableField("5"));

        assertSame(expectedItem, renderer.getFieldScreenRenderLayoutItem(field));
    }

    @Test
    public void testGetAllScreenRenderItemsNoItems() throws Exception
    {
        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();
        final Collection<FieldScreenRenderLayoutItem> items = screenRenderer.getAllScreenRenderItems();
        assertTrue(items.isEmpty());
    }

    @Test
    public void testGetAllScreenRenderItems() throws Exception
    {
        final List<FieldScreenRenderLayoutItem> expectedItems = new ArrayList<FieldScreenRenderLayoutItem>();

        final MockFieldScreenRenderer screenRenderer = new MockFieldScreenRenderer();
        MockFieldScreenRendererTab tab = screenRenderer.addFieldScreenRendererTab();
        expectedItems.add(tab.addLayoutItem());
        expectedItems.add(tab.addLayoutItem());
        expectedItems.add(tab.addLayoutItem());

        tab = screenRenderer.addFieldScreenRendererTab();
        expectedItems.add(tab.addLayoutItem());

        final Collection<FieldScreenRenderLayoutItem> items = screenRenderer.getAllScreenRenderItems();
        assertEquals(expectedItems, items);
    }
}
