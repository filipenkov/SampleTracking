package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestVersionCustomFieldIndexer extends MockControllerTestCase
{
    @Test
    public void testAddIndexNullValue() throws Exception
    {
        final Issue theIssue = null;
        final Document doc = new Document();

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getRelevantConfig(theIssue);
        mockController.setReturnValue(fieldConfig);
        customField.getId();
        mockController.setReturnValue("blah");
        customField.getValue(theIssue);
        mockController.setReturnValue(null);

        final FieldVisibilityManager visibilityManager = mockController.getMock(FieldVisibilityBean.class);
        visibilityManager.isFieldHidden("blah", theIssue);
        mockController.setReturnValue(false);

        VersionCustomFieldIndexer indexer = new VersionCustomFieldIndexer(visibilityManager, customField);

        mockController.replay();
        
        indexer.addIndex(doc, theIssue);
        
        assertTrue(doc.getFields().isEmpty());
    }

    @Test
    public void testAddIndexValueNotCollection() throws Exception
    {
        final Issue theIssue = null;
        final Document doc = new Document();

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getRelevantConfig(theIssue);
        mockController.setReturnValue(fieldConfig);
        customField.getId();
        mockController.setReturnValue("blah");
        customField.getValue(theIssue);
        mockController.setReturnValue("NotACollection");

        final FieldVisibilityManager visibilityManager = mockController.getMock(FieldVisibilityBean.class);
        visibilityManager.isFieldHidden("blah", theIssue);
        mockController.setReturnValue(false);

        VersionCustomFieldIndexer indexer = new VersionCustomFieldIndexer(visibilityManager, customField);

        mockController.replay();

        indexer.addIndex(doc, theIssue);
        
        assertTrue(doc.getFields().isEmpty());
    }

    @Test
    public void testAddIndexHappyPath() throws Exception
    {
        final Issue theIssue = null;
        final Document doc = new Document();
        final String customFieldId = "customField";

        final Version version = new MockVersion(123L, "Test");

        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getRelevantConfig(theIssue);
        mockController.setReturnValue(fieldConfig);
        customField.getId();
        mockController.setReturnValue(customFieldId);
        customField.getValue(theIssue);
        mockController.setReturnValue(Collections.singleton(version));
        customField.getId();
        mockController.setReturnValue(customFieldId);

        final FieldVisibilityManager visibilityManager = mockController.getMock(FieldVisibilityBean.class);
        visibilityManager.isFieldHidden(customFieldId, theIssue);
        mockController.setReturnValue(false);

        VersionCustomFieldIndexer indexer = new VersionCustomFieldIndexer(visibilityManager, customField);

        mockController.replay();

        indexer.addIndex(doc, theIssue);

        final Field field = doc.getField(customFieldId);
        assertEquals("123", field.stringValue());
    }
}
