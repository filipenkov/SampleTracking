/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.Issue;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

class DefaultIssueDocumentFactory implements IssueDocumentFactory
{
    public Document get(final Issue issue)
    {
        return IssueDocument.getDocument(issue);
    }

    public Term getIdentifyingTerm(final Issue issue)
    {
        return new Term(DocumentConstants.ISSUE_ID, issue.getId().toString());
    }
}
