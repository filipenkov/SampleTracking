/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Feb 13, 2004
 * Time: 7:47:26 PM
 */
package com.atlassian.core.task.longrunning;

public interface LongRunningTask extends Runnable
{
    public int getPercentageComplete();

    public String getName();

    /**
     * Internationalisation key for the name of the task, so that the task's name
     * can be displayed in the User Interface.  May be null if the task's name will not be
     * displayed to users.
     */
    public String getNameKey();

    public String getCurrentStatus();

    public long getElapsedTime();
    public String getPrettyElapsedTime();

    public long getEstimatedTimeRemaining();
    public String getPrettyTimeRemaining();

    public boolean isComplete();
    public boolean isSuccessful(); 
}