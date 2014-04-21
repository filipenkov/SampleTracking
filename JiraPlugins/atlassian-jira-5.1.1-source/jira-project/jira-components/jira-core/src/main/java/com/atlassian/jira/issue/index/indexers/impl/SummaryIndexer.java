package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class SummaryIndexer extends BaseFieldIndexer
{
    public SummaryIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forSummary().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forSummary().getIndexField();
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return true;
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexText(doc, getDocumentFieldId(), issue.getSummary(), issue);
        indexTextForSorting(doc, DocumentConstants.ISSUE_SORT_SUMMARY, issue.getSummary(), issue);

    }
}
