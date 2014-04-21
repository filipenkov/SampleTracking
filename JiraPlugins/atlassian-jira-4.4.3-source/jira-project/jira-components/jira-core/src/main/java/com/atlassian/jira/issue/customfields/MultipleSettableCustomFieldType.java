package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;

import java.util.Set;

public interface MultipleSettableCustomFieldType extends MultipleCustomFieldType
{
    /**
     * Returns a Set with of Long Objects representing the issue ids that the value has been set for
     *
     * @param field       the CustomField to search on
     * @param option the Object representing a single value to search on.
     * @return Set of Longs
     */
    public Set getIssueIdsWithValue(CustomField field, Option option);

    public void removeValue(CustomField field, Issue issue, Option optionObject);

}
