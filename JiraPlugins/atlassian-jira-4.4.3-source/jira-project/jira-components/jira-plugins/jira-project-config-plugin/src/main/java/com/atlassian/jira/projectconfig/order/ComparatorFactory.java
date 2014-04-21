package com.atlassian.jira.projectconfig.order;

import com.atlassian.jira.projectconfig.beans.NamedDefault;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;

import java.util.Comparator;

/**
 * Factory class for some of the common orders used in project ignite.
 *
 * @since v4.4
 */
public interface ComparatorFactory
{
    public Comparator<String> createStringComparator();
    public Comparator<NamedDefault> createNamedDefaultComparator();
    public Comparator<SimpleIssueType> createIssueTypeComparator();
}
