package com.atlassian.jira.issue.search.searchers;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;

/**
 * Simple implementation of {@link com.atlassian.jira.issue.customfields.CustomFieldSearcher}.
 *
 * @since v4.0
 */
public class MockCustomFieldSearcher extends MockIssueSearcher<CustomField> implements CustomFieldSearcher
{
    public MockCustomFieldSearcher(final String id)
    {
        super(id);
    }

    public void init(final CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor)
    {
        throw new UnsupportedOperationException();
    }

    public CustomFieldSearcherModuleDescriptor getDescriptor()
    {
        throw new UnsupportedOperationException();
    }

    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString()
    {
        return String.format("Mock Custom Issue Searcher[%s]", getId());
    }
}
