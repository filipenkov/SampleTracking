/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.managers.FieldIndexerManager;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IssueDocument
{
    private static final Logger log = Logger.getLogger(IssueDocument.class);

    public static Document getDocument(final Issue issueObject)
    {
        final FieldIndexerManager fieldIndexerManager = ComponentManager.getComponentInstanceOfType(FieldIndexerManager.class);

        if (log.isDebugEnabled())
        {
            log.debug("Indexing issue: " + issueObject.getKey());
        }

        final List<String> visibleDocumentFieldIds = new ArrayList<String>();
        final Document doc = new Document();
        final Collection<FieldIndexer> allIssueIndexers = fieldIndexerManager.getAllIssueIndexers();
        for (final FieldIndexer indexer : allIssueIndexers)
        {
            indexer.addIndex(doc, issueObject);
            // We need to build up the list of visible Document field ids
            if (indexer.isFieldVisibleAndInScope(issueObject))
            {
                visibleDocumentFieldIds.add(indexer.getDocumentFieldId());
            }
        }

        // Get all the fields in the document and add a new fields who's value is the name of all the included fields
        for (final Fieldable val : getNonEmptyFields(doc))
        {
            doc.add(val);
        }

        // Use all the visible field ids and add a new field who's value is name of all the visible field ids
        for (final String visibleDocumentFieldId : visibleDocumentFieldIds)
        {
            doc.add(new Field(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, visibleDocumentFieldId, Field.Store.NO, Field.Index.NOT_ANALYZED));
        }

        return doc;
    }

    private static List<Fieldable> getNonEmptyFields(final Document doc)
    {
        @SuppressWarnings ( { "unchecked" })
        final List<Fieldable> fields = doc.getFields();
        final List<Fieldable> allVals = Lists.newArrayList();
        for (final Fieldable field : fields)
        {
            // NOTE: we do not store the field value since we are never interested in reading the value out of the
            // document, we are just interested in searching it. This will keep us from adding to the size of the issue
            // document.
            if (field.isIndexed())
            {
                allVals.add(new Field(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, field.name(), Field.Store.NO, Field.Index.NOT_ANALYZED));
            }
        }
        return allVals;
    }
}
