package com.atlassian.jira.web.action.admin.index;

import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.johnson.JohnsonEventContainer;
import org.apache.log4j.Logger;

/**
 * Reindex is slightly different to Activate
 *
 * @since v3.13
 */
class ReIndexAsyncIndexerCommand extends AbstractAsyncIndexerCommand
{
    public ReIndexAsyncIndexerCommand(final JohnsonEventContainer eventCont, final IndexLifecycleManager indexManager, final Logger log, final I18nHelper i18nHelper)
    {
        super(eventCont, indexManager, log, i18nHelper);
    }

    @Override
    public IndexCommandResult doReindex(final Context context, final IndexLifecycleManager indexManager)
    {
        try
        {
            final long reindexTime = indexManager.reIndexAll(context);
            return new IndexCommandResult(reindexTime);
        }
        catch (final IndexException e)
        {
            getLog().error("Exception reindexing: " + e, e);
            final ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(getI18nHelper().getText("admin.errors.exception.while.reindexing") + " " + e);
            return new IndexCommandResult(errors);
        }
    }
}
