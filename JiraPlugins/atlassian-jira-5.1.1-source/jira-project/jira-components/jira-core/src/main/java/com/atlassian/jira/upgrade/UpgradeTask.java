/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import java.util.Collection;

public interface UpgradeTask
{
    /**
     * @return  The build number that this upgrade is applicable to
     */
    public String getBuildNumber();

    /**
     * A short (<50 chars) description of the upgrade action
     */
    public String getShortDescription();

    /**
     * Perform the upgrade.
     * @param setupMode Indicating this upgrade task is running during set up.
     */
    public void doUpgrade(boolean setupMode) throws Exception;

    /**
     * Return any errors that occur.  Each entry is a string.
     */
    public Collection<String> getErrors();

    /**
     * Is a reindex required as a result of running this task.
     * If this method returns true the Upgrade manager will perform a reindex before completing.
     *
     * If an upgrade task changes data that is indexed by Lucene, e.g. the content of Issue fields then it should
     * return true.
     *
     * A task need not necessarily change data to require a reindex but may return true here and do nothing else if there
     * is a functional change to the code that requires the Lucene index to be rebuilt.
     *
     * @return true if a reindex is required as a result of running this task.
     *
     */
    public boolean isReindexRequired();

    /**
     * Track status of a task this session, if isTaskDone(String) returns true you don't need to do it again. 
     */
    public class Status
    {
        public static void setTaskDone(final String taskId)
        {
            System.setProperty("jira.task." + taskId + ".complete", "true");
        }
        
        public static boolean isTaskDone(final String taskId)
        {
            return System.getProperty("jira.task." + taskId + ".complete") != null;
        }
    }
}