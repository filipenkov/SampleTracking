/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.LuceneUtils;

import com.atlassian.core.util.map.EasyMap;

import mock.MockComment;
import org.apache.lucene.document.Document;
import org.ofbiz.core.entity.GenericValue;

import java.util.Date;
import java.util.Locale;

public class TestCommentDocument extends LegacyJiraMockTestCase
{
    Issue testissue;
    private static final String UPDATE_AUTHOR = "updateAuthor";

    public TestCommentDocument(String s)
    {
        super(s);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        MockIssue issue = new MockIssue();
        issue.setId(new Long(1));
        issue.setSecurityLevelId(new Long(1));
        GenericValue project = new MockGenericValue("Project", EasyMap.build("id", new Long(2)));
        issue.setProject(project);
        testissue = issue;
    }

    public void testGetDocumentNullWithNullCommentBody()
    {
        MockComment comment = new MockComment("justin", null, "admin", null, new Date());
        comment.setBody(null);
        assertNull(CommentDocument.getDocument(comment, null));
    }

    public void testGetDocumentNoLevels()
    {
        Date currentDate = new Date();
        Comment comment = new MockComment(new Long(1), "fooauthor", "foobody", null, null, currentDate, testissue);

        Document doc = CommentDocument.getDocument(comment, testissue);

        assertEquals("1", doc.getField(DocumentConstants.COMMENT_ID).stringValue());
        assertEquals("fooauthor", doc.getField(DocumentConstants.COMMENT_AUTHOR).stringValue());
        assertEquals("foobody", doc.getField(DocumentConstants.COMMENT_BODY).stringValue());
        assertEquals("1", doc.getField(DocumentConstants.ISSUE_ID).stringValue());
        assertEquals("2", doc.getField(DocumentConstants.PROJECT_ID).stringValue());

        // Both of these need to return -1
        assertEquals("-1", doc.getField(DocumentConstants.COMMENT_LEVEL).stringValue());
        assertEquals("-1", doc.getField(DocumentConstants.COMMENT_LEVEL_ROLE).stringValue());

        // have to use Date tools, since this is what is internally used by Lucene
        //TODO: JRA-11588 assertEquals((DateTools.dateToString(currentDate, DateTools.Resolution.MILLISECOND)), doc.getField(DocumentConstants.COMMENT_CREATED).stringValue());
        assertEquals((LuceneUtils.dateToString(currentDate)), doc.getField(DocumentConstants.COMMENT_CREATED).stringValue());
    }

    public void testGetDocumentWithGroupLevel()
    {
        Date currentDate = new Date();
        Comment comment = new MockComment(new Long(1), "fooauthor", "foobody", "foolevel", null, currentDate, testissue);

        Document doc = CommentDocument.getDocument(comment, testissue);

        assertEquals("1", doc.getField(DocumentConstants.COMMENT_ID).stringValue());
        assertEquals("foolevel", doc.getField(DocumentConstants.COMMENT_LEVEL).stringValue());
    }

    public void testGetDocumentWithRoleLevel()
    {
        Comment comment = new MockComment(new Long(1), "fooauthor", "foobody", null, new Long(1), new Date(), testissue);

        Document doc = CommentDocument.getDocument(comment, testissue);

        assertEquals("1", doc.getField(DocumentConstants.COMMENT_ID).stringValue());
        assertEquals("1", doc.getField(DocumentConstants.COMMENT_LEVEL_ROLE).stringValue());
    }

    public void testIssueSecurityLevelExists()
    {
        Comment comment = new MockComment(new Long(1), "fooauthor", "foobody", null, new Long(1), new Date(), testissue);

        Document doc = CommentDocument.getDocument(comment, testissue);

        assertEquals("1", doc.getField(DocumentConstants.ISSUE_ID).stringValue());
        assertEquals("2", doc.getField(DocumentConstants.PROJECT_ID).stringValue());
        assertEquals("1", doc.getField(DocumentConstants.ISSUE_SECURITY_LEVEL).stringValue());
    }

    public void testDocumentContainsUpdateInformation()
    {
        // We need to test that the document includes the update date and updateAuthor if they are present
        Date createDate = new Date(100000);
        Date updateDate = new Date(5000000);
        Comment comment = new MockComment(new Long(1), "fooauthor", UPDATE_AUTHOR, "foobody", null, new Long(1), createDate, updateDate, testissue);

        Document doc = CommentDocument.getDocument(comment, testissue);

        assertEquals("1", doc.getField(DocumentConstants.ISSUE_ID).stringValue());
        assertEquals("2", doc.getField(DocumentConstants.PROJECT_ID).stringValue());
        assertEquals(UPDATE_AUTHOR.toLowerCase(Locale.ENGLISH), doc.getField(DocumentConstants.COMMENT_UPDATE_AUTHOR).stringValue());
        assertEquals(LuceneUtils.dateToString(updateDate), doc.getField(DocumentConstants.COMMENT_UPDATED).stringValue());
    }
}
