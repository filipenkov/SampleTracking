/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Feb 13, 2004
 * Time: 7:47:06 PM
 */
package com.atlassian.core.task.longrunning;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.ProgressMeter;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Category;

public abstract class AbstractLongRunningTask implements LongRunningTask
{
    public static final Category log = Category.getInstance(AbstractLongRunningTask.class);
    long startTime = System.currentTimeMillis();
    long stopTime = 0;
    protected ProgressMeter progress;

    protected AbstractLongRunningTask()
    {
        progress = new ProgressMeter();
        progress.setStatus("Initializing... ");
    }

    public void run()
    {
        progress.setStatus("Starting... ");
        startTime = System.currentTimeMillis();
    }

    /**
     * Internationalisation key for the name of the task, so that the task's name
     * can be displayed in the User Interface.  Default implementation returns null,
     * since some uses of LongRunningTask may not require internationalisation of
     * the name (eg if the name of the task is never displayed in the UI).
     *
     * @return I18n key as a string, or null if no key defined.  Null if not overridden.
     */
    public String getNameKey()
    {
        return null;
    }


    public int getPercentageComplete()
    {
        return progress.getPercentageComplete();
    }

    public String getCurrentStatus()
    {
        return progress.getStatus();
    }

    public long getElapsedTime()
    {
        return (stopTime == 0 ? System.currentTimeMillis() : stopTime) - startTime;
    }

    public long getEstimatedTimeRemaining()
    {
        long elapsedTime = getElapsedTime();

        if (getPercentageComplete() == 0)
            return 0;

        long totalTimeEstimate = 100 * elapsedTime / getPercentageComplete();
        return totalTimeEstimate - elapsedTime;
    }

    public boolean isComplete()
    {
        return getPercentageComplete() == 100;
    }

    public String getPrettyElapsedTime()
    {
        return prettyTime(getElapsedTime());
    }

    protected abstract ResourceBundle getResourceBundle();

    private String prettyTime(long time)
    {
        if (time < 1000)
        {
            return "Less than a second";
        }
        else if (time / DateUtils.SECOND_MILLIS < 60)
        {
            return time / DateUtils.SECOND_MILLIS + " seconds";
        }

        String minutesAndAbove = null;

        try
        {
            minutesAndAbove = DateUtils.getDurationPretty(time / DateUtils.SECOND_MILLIS, getResourceBundle());
        }
        catch (MissingResourceException e)
        {
            log.error("Could not load resourcebundle for 'minute'!'", e);
        }

        long secondsRemainder = (time / DateUtils.SECOND_MILLIS) % 60;

        if (secondsRemainder > 0)
        {
            minutesAndAbove += ", " + secondsRemainder + " second" + (secondsRemainder == 1 ? "" : "s");
        }

        return minutesAndAbove;
    }

    public String getPrettyTimeRemaining()
    {
        long estimatedTimeRemaining = getEstimatedTimeRemaining();

        if (estimatedTimeRemaining == 0)
            return "Unknown";

        return prettyTime(estimatedTimeRemaining);
    }

    public boolean isSuccessful()
    {
        return progress.isCompletedSuccessfully();
    }

    protected void stopTimer()
    {
        stopTime = System.currentTimeMillis();
    }
}