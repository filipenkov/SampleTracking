package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import org.apache.lucene.document.Document;

/**
 * @since v4.3
 */
class DefaultChangeHistoryDocumentFactory implements ChangeHistoryDocumentFactory
{
    public Document get(final ChangeHistoryGroup changeHistoryGroup)
    {
        return ChangeHistoryDocument.getDocument(changeHistoryGroup);
    }
}
