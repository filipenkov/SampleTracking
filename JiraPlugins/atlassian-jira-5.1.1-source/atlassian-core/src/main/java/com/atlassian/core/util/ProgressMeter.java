/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 5, 2004
 * Time: 12:57:20 PM
 */
package com.atlassian.core.util;

/**
 * A ProgressMeter class may be used to record progress of an on-going task while this progress is being monitored by
 * another client. To support this use case the class is thread safe.
 */
public class ProgressMeter
{
    int percentageComplete;
    private String status;
    private int total;
    private int currentCount;
    private boolean completedSuccessfully = true;

    /**
     * Use this method to set the completion %age to object 10 of 30 etc.
     *
     * @param count The current object count in progress
     * @param total The total number of objects to be processed
     */
    public void setPercentage(int count, int total)
    {
        if (total < 0)
        {
            setPercentage(0);
        }
        else if (total <= count)
        {
            setPercentage(100);
        }
        else
        {
            int calculatedPercentage = ((int) (100 * (float) count / (float) total));

            if (count < total && calculatedPercentage == 100)
                calculatedPercentage = 99;

            setPercentage(calculatedPercentage);
        }
    }

    public synchronized void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * @return the percentage completion. A value of 100 can be taken to indicate that progress is finished.
     */
    public synchronized int getPercentageComplete()
    {
        return percentageComplete;
    }

    public synchronized String getStatus()
    {
        return status;
    }

    public synchronized void setPercentage(int percentageComplete)
    {
        this.percentageComplete = percentageComplete;
    }

    public synchronized int getCurrentCount()
    {
        return currentCount;
    }

    public synchronized void setCurrentCount(int currentCount)
    {
        this.currentCount = currentCount;
        updatePercentageComplete();
    }

    private void updatePercentageComplete()
    {
        setPercentage(getCurrentCount(),getTotal());
    }

    public synchronized int getTotal()
    {
        return total;
    }

    public synchronized void setTotalObjects(int total)
    {
        this.total = total;
        updatePercentageComplete();
    }

    /**
     * This method should only be called once you know the task is complete (which is discovered by calling
     * {@link ProgressMeter#getPercentageComplete()}.
     * 
     * @return true if the task completed successfully; false if there was an error which prevented the task from
     *         completing.
     */
    public synchronized boolean isCompletedSuccessfully()
    {
        return completedSuccessfully;
    }

    public synchronized void setCompletedSuccessfully(boolean completedSuccessfully)
    {
        this.completedSuccessfully = completedSuccessfully;
    }
}