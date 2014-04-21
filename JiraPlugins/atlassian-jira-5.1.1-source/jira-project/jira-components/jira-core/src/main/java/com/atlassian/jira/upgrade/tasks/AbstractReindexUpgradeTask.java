/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import org.apache.log4j.Logger;

public abstract class AbstractReindexUpgradeTask extends AbstractUpgradeTask
{
    private final Logger log = Logger.getLogger(getClass());


    protected AbstractReindexUpgradeTask()
    {
        super(true);
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
    }

    public String getShortDescription()
    {
        return "Signalling all data in JIRA should be reindexed.";
    }
}
