package com.atlassian.core.spool;

import java.io.InputStream;
import java.io.IOException;

/**
 * Spool that delegates to the overThresholdSpool immediately if InputStream::available() is greater than the threshold
 * of the secondary Thresholding spool. This avoids needless spooling of data into memory from InputStreams with large
 * amounts of initial data available
 */
public class SmartSpool implements ThresholdingSpool
{
    private Spool overThresholdSpool = new BufferedFileSpool();
    private ThresholdingSpool thresholdingSpool = new DeferredSpool();

    public void setThresholdBytes(int bytes)
    {
        thresholdingSpool.setThresholdBytes(bytes);
    }

    public int getThresholdBytes()
    {
        return thresholdingSpool.getThresholdBytes();
    }

    public InputStream spool(InputStream is) throws IOException
    {
        // This stream has more bytes than we are willing to spool in memory. Write it straight to the overflow spool
        if (is.available() > getThresholdBytes())
            return overThresholdSpool.spool(is);

        // The input stream MAY have more than we are prepared to spool in memory.
        // (InputStream::available() only has to return the number of bytes that can be read *without blocking*
        // The thresholding spool should sort this out.
        return thresholdingSpool.spool(is);
    }

    /**
     * Set the spooling strategy to use when InputStream::available is greater than the threshold of the configured
     * ThresholdingSpool
     * @param overThresholdSpool
     */
    public void setOverThresholdSpool(Spool overThresholdSpool)
    {
        this.overThresholdSpool = overThresholdSpool;
    }

    /**
     * Set the spooling strategy that will be used when InputStream::available() is less than or equal to
     * the value of the strategy's threshold.
     * @param thresholdingSpool
     */
    public void setThresholdingSpool(ThresholdingSpool thresholdingSpool)
    {
        this.thresholdingSpool = thresholdingSpool;
    }
}
