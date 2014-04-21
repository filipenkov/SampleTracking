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

    private final ApplicationProperties applicationProperties;
    private final IndexLifecycleManager indexManager;

    protected AbstractReindexUpgradeTask(final ApplicationProperties applicationProperties, final IndexLifecycleManager indexManager)
    {
        this.applicationProperties = applicationProperties;
        this.indexManager = indexManager;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        log.debug("Reindex all data if indexing is turned on.");

        final boolean indexingOn = applicationProperties.getOption(APKeys.JIRA_OPTION_INDEXING);

        if (!indexingOn)
        {
            log.warn("Could not reindex data - indexing is turn off. Turn it on in the Administration section.");
        }
        else
        {
            try
            {
                indexManager.reIndexAll(Contexts.percentageLogger(indexManager, log));
            }
            catch (final IndexException e)
            {
                log.error("Exception reindexing all data: " + e, e);
                addError(getI18nBean().getText("admin.errors.exception.reindexing", e));
            }
        }
    }

    public String getShortDescription()
    {
        return "Reindexing all data in JIRA.";
    }
}
