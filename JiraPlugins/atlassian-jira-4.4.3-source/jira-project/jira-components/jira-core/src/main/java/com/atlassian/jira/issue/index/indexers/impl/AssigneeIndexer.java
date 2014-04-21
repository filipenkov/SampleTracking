package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

import java.util.Locale;

public class AssigneeIndexer extends BaseFieldIndexer
{
    public AssigneeIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forAssignee().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forAssignee().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexFoldedKeywordWithDefault(doc, getDocumentFieldId(), issue.getAssigneeId(), DocumentConstants.ISSUE_UNASSIGNED, Locale.ENGLISH, issue);
    }
}
