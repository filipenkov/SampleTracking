package com.atlassian.jira.issue.thumbnail;

import com.google.common.base.Predicate;

/**
 * Decides the allowed image size based on free memory.
 * <p/>
 * This code was taken originally from our friends in Confluence
 *
 * @since v4.4
 */
public class AdaptiveThresholdPredicate implements Predicate<Dimensions>
{
    private static final int BYTES_PER_PIXEL = 4;
    private static final float PROCESSING_HEADROOM = 1.2F; // We need a bit of headroom for processing, still pulling numbers out of thin air.

    @Override
    public boolean apply(final Dimensions input)
    {
        long requiredMemory = input.getHeight() * input.getWidth() * BYTES_PER_PIXEL;
        long freeMemory = freeMemory();
        return requiredMemory * PROCESSING_HEADROOM < freeMemory;
    }

    // Package private for testing purposes
    long freeMemory()
    {
        return Runtime.getRuntime().freeMemory();
    }
}