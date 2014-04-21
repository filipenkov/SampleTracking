/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.util.Function;
import org.apache.lucene.document.Document;

/**
 * Abstracts the means to create a {@link org.apache.lucene.document.Document} for a comment
 * {@link com.atlassian.jira.issue.comments.Comment} and its {@link com.atlassian.jira.issue.Issue}.
 */
interface CommentDocumentFactory extends Function<Comment, Document>
{}
