package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.EasyList;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Index resolver that can find the index values for select fields.
 *
 * @since v4.0
 */
public class SelectCustomFieldIndexInfoResolver implements IndexInfoResolver<CustomField>
{
    public List<String> getIndexedValues(final String singleValueOperand)
    {
        return EasyList.build(singleValueOperand.toLowerCase());
    }

    public List<String> getIndexedValues(final Long singleValueOperand)
    {
        return EasyList.build(singleValueOperand.toString());
    }

    @Override
    public String getIndexedValue(CustomField indexedObject)
    {
        notNull("indexedObject", indexedObject);
        return indexedObject.toString().toLowerCase();
    }

}
