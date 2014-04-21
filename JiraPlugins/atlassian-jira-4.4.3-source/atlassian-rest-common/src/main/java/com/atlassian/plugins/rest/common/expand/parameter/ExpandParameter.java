package com.atlassian.plugins.rest.common.expand.parameter;

import com.atlassian.plugins.rest.common.expand.Expandable;

/**
 * Represents an expand query parameter.
 */
public interface ExpandParameter
{
    boolean shouldExpand(Expandable expandable);

    Indexes getIndexes(Expandable expandable);

    ExpandParameter getExpandParameter(Expandable expandable);

    boolean isEmpty();
}
