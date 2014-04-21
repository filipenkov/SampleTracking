package com.atlassian.jira.util.devspeed;

import com.atlassian.jira.config.properties.JiraSystemProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

/**
 * A really simple class to help us track how long JIRA takes to do stuff over time.
 * <p/>
 * It does NOTHING if jira.dev.mode is not on.
 *
 * @since v4.4
 */
public class JiraDevSpeedTimer
{
    private final String task;
    private final long then;

    /**
     * Runs the provided code and times the result
     *
     * @param task the task to time
     * @param runnable the code to run
     */
    public static void run(String task, Runnable runnable)
    {
        JiraDevSpeedTimer jiraDevSpeedTimer = new JiraDevSpeedTimer(task);
        try
        {
            runnable.run();
        }
        finally
        {
            jiraDevSpeedTimer.end();
        }
    }

    JiraDevSpeedTimer(final String task)
    {
        this.then = System.currentTimeMillis();
        this.task = task;
    }

    void end()
    {
        long now = System.currentTimeMillis();
        if (JiraSystemProperties.isDevMode())
        {
            appendRecord(now);
        }
    }

    private void appendRecord(long now)
    {
        final long howLongSec = (now - then) / 1000;
        final Date nowDate = new Date();
        try
        {
            String userDotHome = System.getProperty("user.home");
            String userDotName = System.getProperty("user.name");
            File userHome = new File(userDotHome);
            if (userHome.exists())
            {
                File targetDir = new File(userHome, ".jiradev");
                //noinspection ResultOfMethodCallIgnored
                targetDir.mkdirs();

                File targetFile = new File(targetDir, "jiratimers.csv");
                PrintWriter pw = new PrintWriter(new FileWriter(targetFile, true));

                //
                // not this matches the jmake time formats
                pw.printf("%tF %tT,%s,%s,%d\n", nowDate, nowDate, userDotName, task, howLongSec);
                pw.close();
            }
        }
        catch (Exception ignored)
        {
        }
    }
}
