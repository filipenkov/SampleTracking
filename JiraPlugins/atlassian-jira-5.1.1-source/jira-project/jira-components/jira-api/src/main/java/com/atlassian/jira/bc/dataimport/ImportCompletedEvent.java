package com.atlassian.jira.bc.dataimport;

import com.atlassian.annotations.PublicApi;

/**
 * Raised after a JIRA XML import has finished.
 *
 * @since v5.1
 */
@PublicApi
public final class ImportCompletedEvent
{
    private final boolean importSuccessful;

    ImportCompletedEvent(boolean importSuccessful)
    {
        this.importSuccessful = importSuccessful;
    }

    /**
     * @return a boolean indicating whether the XML import was successful
     */
    public boolean isImportSuccessful()
    {
        return importSuccessful;
    }
}
