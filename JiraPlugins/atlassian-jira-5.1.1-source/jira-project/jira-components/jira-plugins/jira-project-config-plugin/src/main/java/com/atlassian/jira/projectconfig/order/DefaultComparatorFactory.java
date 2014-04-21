package com.atlassian.jira.projectconfig.order;

import com.atlassian.jira.projectconfig.beans.NamedDefault;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.util.ProjectConfigRequestCache;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.text.Collator;
import java.util.Comparator;

/**
 * @since v4.4
 */
public class DefaultComparatorFactory implements ComparatorFactory
{
    private static final String KEY_STRING_COMPARATOR = "stringComparator";
    private static final String KEY_ISSUE_CONSTANT_COMPARATOR = "namedObjectComparator";
    public static final String KEY_ISSUE_TYPE_COMPARATOR = "issueTypeComparator";

    private final ProjectConfigRequestCache cache;
    private final JiraAuthenticationContext context;

    public DefaultComparatorFactory(ProjectConfigRequestCache cache, JiraAuthenticationContext context)
    {
        this.cache = cache;
        this.context = context;
    }

    @Override
    public Comparator<String> createStringComparator()
    {
        @SuppressWarnings ( { "unchecked" })
        Comparator<String> comparator = (Comparator<String>) cache.get(KEY_STRING_COMPARATOR);
        if (comparator == null)
        {
            Collator primary = createForCurrentUser();
            primary.setStrength(Collator.SECONDARY);

            Collator secondary = createForCurrentUser();
            secondary.setStrength(Collator.TERTIARY);

            comparator = ChainedComparator.of(primary, secondary);
            cache.put(KEY_STRING_COMPARATOR, comparator);
        }

        return comparator;
    }

    @Override
    public Comparator<NamedDefault> createNamedDefaultComparator()
    {
        @SuppressWarnings ( { "unchecked" })
        Comparator<NamedDefault> comparator = (Comparator<NamedDefault>) cache.get(KEY_ISSUE_CONSTANT_COMPARATOR);
        if (comparator == null)
        {
            comparator = new NamedDefaultComparator(createStringComparator());
            cache.put(KEY_ISSUE_CONSTANT_COMPARATOR, comparator);
        }
        return comparator;
    }

    @Override
    public Comparator<SimpleIssueType> createIssueTypeComparator()
    {
        @SuppressWarnings ( { "unchecked" })
        Comparator<SimpleIssueType> comparator = (Comparator<SimpleIssueType>) cache.get(KEY_ISSUE_TYPE_COMPARATOR);
        if (comparator == null)
        {
            comparator = new SimpleIssueTypeComparator(createStringComparator());
            cache.put(KEY_ISSUE_TYPE_COMPARATOR, comparator);
        }
        return comparator;
    }

    private Collator createForCurrentUser()
    {
        return Collator.getInstance(context.getLocale());
    }
}
