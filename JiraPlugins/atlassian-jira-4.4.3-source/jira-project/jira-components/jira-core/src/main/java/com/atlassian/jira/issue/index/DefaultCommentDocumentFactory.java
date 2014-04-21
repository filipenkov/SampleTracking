/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.comments.Comment;
import org.apache.lucene.document.Document;

class DefaultCommentDocumentFactory implements CommentDocumentFactory
{
    public Document get(final Comment comment)
    {
        return CommentDocument.getDocument(comment, comment.getIssue());
    }
}
