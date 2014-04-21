package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Abstract FieldIndexer that has helper methods to index usernames in a case-insensitive manner consistent with what
 * Crowd Embedded does.
 *
 * @since v5.0
 */
public abstract class UserFieldIndexer extends BaseFieldIndexer
{
    protected UserFieldIndexer(final FieldVisibilityManager fieldVisibilityManager)
    {
        super(fieldVisibilityManager);
    }

    /**
     * Lowercase the passed username in a manner consistent with Crowd Embedded's case-insensitivity and add it to the passed document.
     *
     * @param doc the document to add the field to.
     * @param indexField the document field name.
     * @param username the username to index. This value will be folded before adding it to the document.
     * @param issue the issue that defines the context and contains the value we are indexing.
     *
     * @see com.atlassian.jira.util.CaseFolding#foldUsername(String)
     */
    protected void indexUsername(final Document doc, final String indexField, final String username, final Issue issue)
    {
        indexKeyword(doc, indexField, CaseFolding.foldUsername(username), issue);
    }

    /**
     * Index a single username field (case folded), with a default if the field is not set
     *
     */
    protected void indexUsernameWithDefault(final Document doc, final String indexField, final String username, final String defaultValue, final Issue issue)
    {
        indexKeywordWithDefault(doc, indexField, CaseFolding.foldUsername(username), defaultValue, issue);
    }

}
