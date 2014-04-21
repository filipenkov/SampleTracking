package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Collection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A custom field indexer for the cascading select custom fields.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class CascadingSelectCustomFieldIndexer extends AbstractCustomFieldIndexer
{
    public static final String CHILD_INDEX_SUFFIX = ":" + CascadingSelectCFType.CHILD_KEY;

    private final CustomField customField;

    ///CLOVER:OFF

    public CascadingSelectCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField)
    {
        super(fieldVisibilityManager, notNull("customField", customField));
        this.customField = customField;
    }

    @Override
    public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED);
    }

    @Override
    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
    {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType)
    {
        final Object value = customField.getValue(issue);
        if (value instanceof CustomFieldParams)
        {
            final CustomFieldParams customFieldParams = (CustomFieldParams) value;
            indexParentField(customFieldParams, doc, indexType);
            indexChildField(customFieldParams, doc, indexType);
        }
    }

    private void indexParentField(final CustomFieldParams customFieldParams, final Document doc, final Field.Index indexType)
    {
        final Collection values = customFieldParams.getValuesForKey(CascadingSelectCFType.PARENT_KEY);
        if ((values != null) && !values.isEmpty())
        {
            final Option selectedValue = (Option) values.iterator().next();

            addField(doc, getDocumentFieldId(), selectedValue.getOptionId().toString(), indexType);
        }
    }

    private void indexChildField(final CustomFieldParams customFieldParams, final Document doc, final Field.Index indexType)
    {
        final Collection values = customFieldParams.getValuesForKey(CascadingSelectCFType.CHILD_KEY);
        if ((values != null) && !values.isEmpty())
        {
            final Option selectedValue = (Option) values.iterator().next();

            final String indexFieldName = getDocumentFieldId() + CHILD_INDEX_SUFFIX;
            addField(doc, indexFieldName, selectedValue.getOptionId().toString(), indexType);
        }
    }

    private void addField(final Document doc, final String indexFieldName, final String value, final Field.Index indexType)
    {
        doc.add(new Field(indexFieldName, value, Field.Store.YES, indexType));
    }

    ///CLOVER:ON
}
