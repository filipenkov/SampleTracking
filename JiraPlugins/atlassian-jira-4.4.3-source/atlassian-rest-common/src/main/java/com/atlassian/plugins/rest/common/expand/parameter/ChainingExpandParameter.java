package com.atlassian.plugins.rest.common.expand.parameter;

import com.atlassian.plugins.rest.common.expand.Expandable;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.LinkedList;

class ChainingExpandParameter implements ExpandParameter
{
    private final Collection<ExpandParameter> expandParameters;

    ChainingExpandParameter(ExpandParameter... expandParameters)
    {
        this(Lists.newArrayList(expandParameters));
    }

    ChainingExpandParameter(Iterable<ExpandParameter> expandParameters)
    {
        this.expandParameters = ImmutableList.copyOf(Preconditions.checkNotNull(expandParameters));
    }

    public boolean shouldExpand(Expandable expandable)
    {
        for (ExpandParameter expandParameter : expandParameters)
        {
            if (expandParameter.shouldExpand(expandable))
            {
                return true;
            }
        }
        return false;
    }

    public Indexes getIndexes(Expandable expandable)
    {
        // we do not merge indexes,
        // so if we find an IndexParser.ALL that's what we return
        // if we find only one non-empty, that's what we return
        // else we throw an exception

        Indexes indexes = null;
        for (ExpandParameter expandParameter : expandParameters)
        {
            final Indexes i = expandParameter.getIndexes(expandable);
            if (i.equals(IndexParser.ALL))
            {
                return IndexParser.ALL;
            }
            if (!i.equals(IndexParser.EMPTY))
            {
                if (indexes == null)
                {
                    indexes = i;
                }
                else
                {
                    throw new IndexException("Cannot merge multiple indexed expand parameters.");
                }
            }
        }
        return indexes != null ? indexes : IndexParser.EMPTY;
    }

    public ExpandParameter getExpandParameter(Expandable expandable)
    {
        final Collection<ExpandParameter> newExpandParameters = new LinkedList<ExpandParameter>();
        for (ExpandParameter expandParameter : expandParameters)
        {
            newExpandParameters.add(expandParameter.getExpandParameter(expandable));
        }
        return new ChainingExpandParameter(newExpandParameters);
    }

    public boolean isEmpty()
    {
        for (ExpandParameter expandParameter : expandParameters)
        {
            if (!expandParameter.isEmpty())
            {
                return false;
            }
        }
        return true;
    }
}
