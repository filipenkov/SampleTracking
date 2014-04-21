/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.FieldIndexerUtil;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.util.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Date;

/**
 * Returns a Lucene document from a given comment
 */
public class CommentDocument
{
    public static Document getDocument(Comment comment, Issue issue)
    {
        Document doc = new Document();
        String body = comment.getBody();
        if (body != null)
        {

            doc.add(new Field(DocumentConstants.PROJECT_ID, String.valueOf(issue.getProjectObject().getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field(DocumentConstants.ISSUE_ID, String.valueOf(issue.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field(DocumentConstants.COMMENT_ID, String.valueOf(comment.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

            String author = comment.getAuthor();
            if (author != null) //can't add null keywords
            {
                author = CaseFolding.foldUsername(comment.getAuthor());
                doc.add(new Field(DocumentConstants.COMMENT_AUTHOR, author, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            }

            // If there is an updateAuthor then index it
            String updateAuthor = comment.getUpdateAuthor();
            if (updateAuthor != null)
            {
                updateAuthor = CaseFolding.foldUsername(comment.getUpdateAuthor());
                doc.add(new Field(DocumentConstants.COMMENT_UPDATE_AUTHOR, updateAuthor, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            }

            doc.add(new Field(DocumentConstants.COMMENT_BODY, body, Field.Store.YES, Field.Index.ANALYZED));

            doc.add(new Field(DocumentConstants.COMMENT_CREATED, LuceneUtils.dateToString(comment.getCreated()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            Date updated = comment.getUpdated();
            if (updated != null)
            {
                doc.add(new Field(DocumentConstants.COMMENT_UPDATED, LuceneUtils.dateToString(updated), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            }

            FieldIndexerUtil.indexKeywordWithDefault(doc, DocumentConstants.ISSUE_SECURITY_LEVEL, issue.getSecurityLevelId(), BaseFieldIndexer.NO_VALUE_INDEX_VALUE);
            FieldIndexerUtil.indexKeywordWithDefault(doc, DocumentConstants.COMMENT_LEVEL, comment.getGroupLevel(), BaseFieldIndexer.NO_VALUE_INDEX_VALUE);
            FieldIndexerUtil.indexKeywordWithDefault(doc, DocumentConstants.COMMENT_LEVEL_ROLE, comment.getRoleLevel() != null ? comment.getRoleLevel().getId() : null, BaseFieldIndexer.NO_VALUE_INDEX_VALUE);

            return doc;
        }
        return null;
    }

}
