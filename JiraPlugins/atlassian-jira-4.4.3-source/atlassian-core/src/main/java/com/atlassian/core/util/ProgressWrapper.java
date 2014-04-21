package com.atlassian.core.util;

public class ProgressWrapper
{
    private ProgressMeter progressMeter;
    private int currentCount;
    private int totalCount;

    public ProgressWrapper(ProgressMeter progressMeter, int totalCount)
    {
        this.progressMeter = progressMeter;
        this.totalCount = totalCount;
        progressMeter.setTotalObjects(totalCount);
        this.currentCount = 0;
    }

    public synchronized void incrementCounter()
    {
        currentCount++;
        progressMeter.setPercentage(currentCount, totalCount);
    }

    public synchronized void incrementCounter(String status)
    {
        currentCount++;
        progressMeter.setPercentage(currentCount, totalCount);
        progressMeter.setStatus(status);
    }

    public synchronized void setStatus(String status)
    {
        progressMeter.setStatus(status);
    }

    public synchronized int getTotal()
    {
        return progressMeter.getTotal();
    }

    public synchronized void setPercentage(int percentageComplete)
    {
        progressMeter.setPercentage(percentageComplete);
    }

    public String progressAsString()
    {
        return currentCount + " of " + totalCount + " total objects.";
    }
}
