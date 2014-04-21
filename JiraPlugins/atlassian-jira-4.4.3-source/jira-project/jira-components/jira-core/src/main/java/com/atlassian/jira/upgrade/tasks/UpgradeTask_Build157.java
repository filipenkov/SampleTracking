package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;

/**
 * This upgrade task will fix issues relating to JRA-10518 which is that the sequence number of a field screen
 * tab could be wrong due to an error in the tab delete.
 */
public class UpgradeTask_Build157 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build157.class);

    private FieldScreenManager fieldScreenManager;

    public UpgradeTask_Build157(FieldScreenManager fieldScreenManager)
    {
        this.fieldScreenManager = fieldScreenManager;
    }

    public String getBuildNumber()
    {
        return "157";
    }

    public String getShortDescription()
    {
        return "Correct the sequence of field screen tabs they could be incorrect as a result of JRA-10518.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        Collection fieldScreens = fieldScreenManager.getFieldScreens();
        for (Iterator iterator = fieldScreens.iterator(); iterator.hasNext();)
        {
            FieldScreen fieldScreen = (FieldScreen) iterator.next();
            fieldScreen.resequence();
            fieldScreen.store();
        }
    }

}

