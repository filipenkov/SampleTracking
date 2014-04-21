package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.GroupConverter;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.opensymphony.user.Group;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple custom field indexer for group custom fields
 *
 * @since v4.0
 */
public class GroupCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    private final CustomField customField;
    private final GroupConverter groupConverter;

    public GroupCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField, final GroupConverter groupConverter)
    {
        super(fieldVisibilityManager, notNull("customField", customField));
        this.groupConverter = groupConverter;
        this.customField = customField;
    }

    public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.UN_TOKENIZED);
    }

    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType)
    {
        Object value = customField.getValue(issue);
        if (value != null)
        {
            doc.add(new Field(getDocumentFieldId(), groupConverter.getString((Group) value), Field.Store.YES, indexType));
        }
    }
}