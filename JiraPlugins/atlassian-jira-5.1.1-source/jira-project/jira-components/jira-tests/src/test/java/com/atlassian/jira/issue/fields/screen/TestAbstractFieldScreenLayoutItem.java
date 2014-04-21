package com.atlassian.jira.issue.fields.screen;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.classextension.IMocksControl;

import java.util.Collections;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.AbstractFieldScreenLayoutItem}.
 *
 * @since v4.1
 */
public class TestAbstractFieldScreenLayoutItem extends ListeningTestCase
{
    @Test
    public void testGetOrderableField() throws Exception
    {
        final String fieldId = "5";
        final MockOrderableField field = new MockOrderableField(fieldId);

        final IMocksControl control = createControl();
        final FieldManager fieldManager = control.createMock(FieldManager.class);
        expect(fieldManager.getOrderableField(fieldId)).andReturn(field);

        control.replay();

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertSame(field, testItem.getOrderableField());

        control.verify();
    }

    @Test
    public void testGetEditHtmlShown() throws Exception
    {
        Issue issue = new MockIssue();

        final String html = "Pass";
        final String fieldId = "5";

        final IMocksControl control = createControl();
        final OrderableField field = control.createMock(OrderableField.class);
        expect(field.isShown(issue)).andReturn(true);
        expect(field.getEditHtml(null, null, null, issue, Collections.emptyMap())).andReturn(html);

        final FieldManager fieldManager = control.createMock(FieldManager.class);
        expect(fieldManager.getOrderableField(fieldId)).andReturn(field).anyTimes();

        control.replay();

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertEquals(html, testItem.getEditHtml(null, null, null, issue));

        control.verify();
    }

    @Test
    public void testGetEditHtmlNotShown() throws Exception
    {
        Issue issue = new MockIssue();

        final String fieldId = "5";

        final IMocksControl control = createControl();
        final OrderableField field = control.createMock(OrderableField.class);
        expect(field.isShown(issue)).andReturn(false);

        final FieldManager fieldManager = control.createMock(FieldManager.class);
        expect(fieldManager.getOrderableField(fieldId)).andReturn(field).anyTimes();

        control.replay();

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertEquals("", testItem.getEditHtml(null, null, null, issue));

        control.verify();
    }

    @Test
    public void testGetCreateHtmlShown() throws Exception
    {
        Issue issue = new MockIssue();

        final String html = "Pass";
        final String fieldId = "5";

        final IMocksControl control = createControl();
        final OrderableField field = control.createMock(OrderableField.class);
        expect(field.isShown(issue)).andReturn(true);
        expect(field.getCreateHtml(null, null, null, issue, Collections.emptyMap())).andReturn(html);

        final FieldManager fieldManager = control.createMock(FieldManager.class);
        expect(fieldManager.getOrderableField(fieldId)).andReturn(field).anyTimes();

        control.replay();

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertEquals(html, testItem.getCreateHtml(null, null, null, issue));

        control.verify();
    }

    @Test
    public void testGetCreateHtmlNotShown() throws Exception
    {
        Issue issue = new MockIssue();

        final String fieldId = "5";

        final IMocksControl control = createControl();
        final OrderableField field = control.createMock(OrderableField.class);
        expect(field.isShown(issue)).andReturn(false);

        final FieldManager fieldManager = control.createMock(FieldManager.class);
        expect(fieldManager.getOrderableField(fieldId)).andReturn(field).anyTimes();

        control.replay();

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertEquals("", testItem.getCreateHtml(null, null, null, issue));

        control.verify();
    }

    @Test
    public void testGetViewHtmlShown() throws Exception
    {
        Issue issue = new MockIssue();

        final String html = "Pass";
        final String fieldId = "5";

        final IMocksControl control = createControl();
        final OrderableField field = control.createMock(OrderableField.class);
        expect(field.isShown(issue)).andReturn(true);
        expect(field.getViewHtml(null, null, issue, Collections.emptyMap())).andReturn(html);

        final FieldManager fieldManager = control.createMock(FieldManager.class);
        expect(fieldManager.getOrderableField(fieldId)).andReturn(field).anyTimes();

        control.replay();

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertEquals(html, testItem.getViewHtml(null, null, null, issue));

        control.verify();
    }

    @Test
    public void testGetViewHtmlNotShown() throws Exception
    {
        Issue issue = new MockIssue();

        final String fieldId = "5";

        final IMocksControl control = createControl();
        final OrderableField field = control.createMock(OrderableField.class);
        expect(field.isShown(issue)).andReturn(false);

        final FieldManager fieldManager = control.createMock(FieldManager.class);
        expect(fieldManager.getOrderableField(fieldId)).andReturn(field).anyTimes();

        control.replay();

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertEquals("", testItem.getViewHtml(null, null, null, issue));

        control.verify();
    }

    @Test
    public void testIsShown() throws Exception
    {
        Issue issue = new MockIssue();

        final String fieldId = "5";

        final IMocksControl control = createControl();
        final OrderableField field = control.createMock(OrderableField.class);
        expect(field.isShown(issue)).andReturn(true).andReturn(false);

        final FieldManager fieldManager = control.createMock(FieldManager.class);
        expect(fieldManager.getOrderableField(fieldId)).andReturn(field).anyTimes();

        control.replay();

        final TestItem testItem = new TestItem(fieldManager);
        testItem.setFieldId(fieldId);
        assertTrue(testItem.isShown(issue));
        assertFalse(testItem.isShown(issue));

        control.verify();
    }

    private static class TestItem extends AbstractFieldScreenLayoutItem
    {
        private TestItem(FieldManager fieldManager)
        {
            super(null, fieldManager);
        }

        public Long getId()
        {
            return null;
        }

        public void setPosition(final int position)
        {
        }

        public void setFieldId(final String fieldId)
        {
            this.fieldId = fieldId;
        }

        public void setFieldScreenTab(final FieldScreenTab fieldScreenTab)
        {
        }

        public void store()
        {
        }

        public void remove()
        {
        }

        @Override
        protected void init()
        {
        }
    }
}
