package com.atlassian.core.spool;

/**
 * Implemented by spools that switch behaviour based on the number of bytes spooled
 */
public interface ThresholdingSpool extends Spool
{
    void setThresholdBytes(int bytes);
    int getThresholdBytes();
}
