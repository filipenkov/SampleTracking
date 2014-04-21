package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
public class TestAbstractCustomFieldIndexer extends MockControllerTestCase
{
    private CustomField customField;
    private FieldVisibilityManager fieldVisibilityManager;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        fieldVisibilityManager = mockController.getMock(FieldVisibilityBean.class);
    }

    @Test
    public void testIsRelevantForIssue() throws Exception
    {
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        customField.getRelevantConfig(null);
        mockController.setReturnValue(fieldConfig);
        mockController.replay();

        final TestCustomFieldIndexer indexer = new TestCustomFieldIndexer(fieldVisibilityManager, customField);
        assertTrue(indexer.isRelevantForIssue(null));
        mockController.verify();
    }

    @Test
    public void testIsFieldVisibleAndInScope() throws Exception
    {
        customField.getId();
        mockController.setReturnValue("blah");
        fieldVisibilityManager.isFieldHidden("blah", (Issue)null);
        mockController.setReturnValue(false);
        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        final TestCustomFieldIndexer indexer = new TestCustomFieldIndexer(fieldVisibilityManager, customField)
        {
            @Override
            protected boolean isRelevantForIssue(final Issue issue)
            {
                called.set(true);
                return true;
            }
        };
        assertTrue(indexer.isFieldVisibleAndInScope(null));
        assertTrue(called.get());
        mockController.verify();
    }

    @Test
    public void testIsFieldVisibleAndInScopeNotVisible() throws Exception
    {
        customField.getId();
        mockController.setReturnValue("blah");
        fieldVisibilityManager.isFieldHidden("blah", (Issue)null);
        mockController.setReturnValue(true);
        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        final TestCustomFieldIndexer indexer = new TestCustomFieldIndexer(fieldVisibilityManager, customField)
        {
            @Override
            protected boolean isRelevantForIssue(final Issue issue)
            {
                called.set(true);
                return true;
            }
        };
        assertFalse(indexer.isFieldVisibleAndInScope(null));
        assertTrue(called.get());
        mockController.verify();
    }

    @Test
    public void testGetId() throws Exception
    {
        customField.getId();
        mockController.setReturnValue("blah");
        mockController.replay();

        final TestCustomFieldIndexer indexer = new TestCustomFieldIndexer(fieldVisibilityManager, customField);
        assertEquals("blah", indexer.getId());
        mockController.verify();
    }
    
    @Test
    public void testDocumentFieldId() throws Exception
    {
        customField.getId();
        mockController.setReturnValue("blah");
        mockController.replay();

        final TestCustomFieldIndexer indexer = new TestCustomFieldIndexer(fieldVisibilityManager, customField);
        assertEquals("blah", indexer.getDocumentFieldId());
        mockController.verify();
    }

    @Test
    public void testAddIndexSearchable() throws Exception
    {
        customField.getId();
        mockController.setReturnValue("blah");
        fieldVisibilityManager.isFieldHidden("blah", (Issue)null);
        mockController.setReturnValue(false);
        mockController.replay();

        final TestCustomFieldIndexer indexer = new TestCustomFieldIndexer(fieldVisibilityManager, customField)
        {
            @Override
            protected boolean isRelevantForIssue(final Issue issue)
            {
                return true;
            }
        };

        indexer.addIndex(null, null);
        assertTrue(indexer.addSearchableCalled);
    }

    @Test
    public void testAddIndexNotSearchable() throws Exception
    {
        customField.getId();
        mockController.setReturnValue("blah");
        fieldVisibilityManager.isFieldHidden("blah", (Issue)null);
        mockController.setReturnValue(true);
        mockController.replay();

        final TestCustomFieldIndexer indexer = new TestCustomFieldIndexer(fieldVisibilityManager, customField)
        {
            @Override
            protected boolean isRelevantForIssue(final Issue issue)
            {
                return true;
            }
        };

        indexer.addIndex(null, null);
        assertTrue(indexer.addNotSearchableCalled);
    }

    static class TestCustomFieldIndexer extends AbstractCustomFieldIndexer
    {
        public boolean addSearchableCalled = false;
        public boolean addNotSearchableCalled = false;

        protected TestCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField)
        {
            super(fieldVisibilityManager, customField);
        }

        public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
        {
            this.addSearchableCalled = true;
        }

        public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
        {
            this.addNotSearchableCalled = true;
        }
    }
}
