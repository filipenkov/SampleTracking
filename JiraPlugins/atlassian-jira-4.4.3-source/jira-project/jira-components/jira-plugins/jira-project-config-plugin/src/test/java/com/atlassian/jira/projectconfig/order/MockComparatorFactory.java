package com.atlassian.jira.projectconfig.order;

import com.atlassian.jira.projectconfig.beans.NamedDefault;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;

import java.util.Comparator;

/**
 * @since v4.4
 */
public class MockComparatorFactory implements ComparatorFactory
{
    @Override
    public Comparator<String> createStringComparator()
    {
        return NativeComparator.getInstance();
    }

    @Override
    public Comparator<NamedDefault> createNamedDefaultComparator()
    {
        return new NamedDefaultComparator(createStringComparator());
    }

    @Override
    public Comparator<SimpleIssueType> createIssueTypeComparator()
    {
        return new SimpleIssueTypeComparator(createStringComparator());
    }
}
