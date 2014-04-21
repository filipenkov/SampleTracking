package com.atlassian.instrumentation.operations.registry;

import com.atlassian.instrumentation.operations.OpSnapshot;

/**
 * This simple filter interface is used to filter out OpSnapshot values when an OpFilter is used
 *
 * @since v4.0
 */
public interface OpFinderFilter
{
    /**
     * This will return true if the OpSnapshot is to be included in an OpFinder search result.
     *
     * @param opSnapshot the operation snapshot to filter
     * @return true if it should be included or false if it should be excluded
     */
    public boolean filter(OpSnapshot opSnapshot);
}
