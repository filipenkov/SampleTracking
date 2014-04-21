package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.google.common.collect.Lists;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.junit.Test;
import org.mockito.internal.verification.Times;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;


/**
 * Tests that a document is created from a ChangeHistoryGroup
 *
 * @since v4.3
 */
public class TestChangeHistoryDocument
{

    private static final String FIELD = "Status";
    private final long CHANGE_DATE = 100000000;

    @Test
    public void testGetDocument()
    {
        ChangeHistoryItem change = new ChangeHistoryItem(1000L, 1l, 1L, 2L, "Key1", FIELD, new Timestamp(CHANGE_DATE), "Open", "Closed", "1", "2", "user");
        ChangeHistoryGroup changeGroup = new ChangeHistoryGroup(1L, 1L, 2L, "Key1", "user", Lists.newArrayList(change), new Timestamp(CHANGE_DATE));
        Document doc = ChangeHistoryDocument.getDocument(changeGroup);

        _assertDocument(doc, "1", "2", "Key1", "ch-open", "ch-closed", "ch-1", "ch-2", "ch-user");
    }

    @Test
    public void testGetDocumentWithNullValues()
    {
        ChangeHistoryItem change = new ChangeHistoryItem(1000L, 1L, 1L, 2L, "Key2", FIELD, new Timestamp(CHANGE_DATE), null, "Closed", "1", "2", "user");
        ChangeHistoryGroup changeGroup = new ChangeHistoryGroup(1L, 1L, 2L, "Key2", "user", Lists.newArrayList(change), new Timestamp(CHANGE_DATE));
        Document doc = ChangeHistoryDocument.getDocument(changeGroup);

        _assertDocument(doc, "1", "2", "Key2", "ch-", "ch-closed", "ch-1", "ch-2", "ch-user");
    }


    private void _assertDocument(final Document doc, final String projectId, final String issueId, final String issueKey, final String oldString, final String newString, final String oldValue, final String newValue, final String userName)
    {
        assertEquals("Project ID",projectId, doc.getField(DocumentConstants.PROJECT_ID).stringValue());
        assertEquals("Issue ID",issueId, doc.getField(DocumentConstants.ISSUE_ID).stringValue());
        assertEquals("Issue Key",issueKey, doc.getField(DocumentConstants.ISSUE_KEY).stringValue());
        assertEquals("Old String",oldString, doc.getField(FIELD+"."+DocumentConstants.CHANGE_FROM).stringValue());
        assertEquals("New String",newString, doc.getField(FIELD+"."+DocumentConstants.CHANGE_TO).stringValue());
        assertEquals("Old Value",oldValue, doc.getField(FIELD+"."+DocumentConstants.OLD_VALUE).stringValue());
        assertEquals("New Value",newValue, doc.getField(FIELD+"."+DocumentConstants.NEW_VALUE).stringValue());
        assertEquals("Username",userName, doc.getField(DocumentConstants.CHANGE_ACTIONER).stringValue());
        assertEquals("Date",(DateTools.dateToString(new Date(CHANGE_DATE), DateTools.Resolution.SECOND)), doc.getField(DocumentConstants.CHANGE_DATE).stringValue());
    }


}
