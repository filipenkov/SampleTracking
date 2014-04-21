/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import com.atlassian.jira.util.Function;
import org.apache.lucene.document.Document;

/**
 * Abstracts the means to create a {@link org.apache.lucene.document.Document} for a
 * {@link ChangeHistoryGroup} and its {@link com.atlassian.jira.issue.Issue}.
 *
 * @since v4.3
 */
interface ChangeHistoryDocumentFactory extends Function<ChangeHistoryGroup, Document>
{}
