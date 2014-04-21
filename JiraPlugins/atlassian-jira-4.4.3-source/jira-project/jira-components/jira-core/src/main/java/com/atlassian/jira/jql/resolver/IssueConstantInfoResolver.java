package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.util.EasyList;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Resolves index info with a lucene field using the id of the domain object T to get the
 * indexed values from a NameResolver&lt;T&gt; .
 *
 * @since v4.0
 */
public class IssueConstantInfoResolver<T extends IssueConstant> implements IndexInfoResolver<T>
{
    private final NameResolver<T> resolver;

    /**
     * @param resolver         the name resolver to look up the id if necessary.
     */
    public IssueConstantInfoResolver(NameResolver<T> resolver)
    {
        this.resolver = notNull("resolver", resolver);
    }

    public List<String> getIndexedValues(final String singleValueOperand)
    {
        notNull("singleValueOperand", singleValueOperand);
        // our id is our index value

        final List<String> list = resolver.getIdsFromName(singleValueOperand);
        if (list.isEmpty())
        {
            // Since we could not find the value by name check to see if we can try by id
            Long valueAsLong = getValueAsLong(singleValueOperand);
            if (valueAsLong != null && resolver.idExists(valueAsLong))
            {
                return EasyList.build(singleValueOperand);
            }
        }
        return list;
    }

    public List<String> getIndexedValues(final Long singleValueOperand)
    {
        notNull("singleValueOperand", singleValueOperand);
        if (resolver.idExists(singleValueOperand))
        {
            return EasyList.build(singleValueOperand.toString());
        }
        else
        {
            return resolver.getIdsFromName(singleValueOperand.toString());
        }
    }

    public String getIndexedValue(final T indexedObject)
    {
        notNull("indexedObject", indexedObject);
        return indexedObject.getId();
    }

    private Long getValueAsLong(final String singleValueOperand)
    {
        try
        {
            return new Long(singleValueOperand);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
