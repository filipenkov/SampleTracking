package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class EnvironmentIndexer extends BaseFieldIndexer
{

    public EnvironmentIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    public String getId()
    {
        return SystemSearchConstants.forEnvironment().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forEnvironment().getIndexField();
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexText(doc, getDocumentFieldId(), issue.getEnvironment(), issue);
    }
}
