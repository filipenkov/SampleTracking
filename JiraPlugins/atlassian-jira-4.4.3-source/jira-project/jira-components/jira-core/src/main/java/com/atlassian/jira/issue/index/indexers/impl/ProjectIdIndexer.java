package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class ProjectIdIndexer extends BaseFieldIndexer
{
    public ProjectIdIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forProject().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forProject().getIndexField();
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return true;
    }

    public void addIndex(Document doc, Issue issue)
    {
        if (issue.getProjectObject() != null)
        {
            indexKeyword(doc, getDocumentFieldId(), String.valueOf(issue.getProjectObject().getId()), issue);
        }
    }
}
