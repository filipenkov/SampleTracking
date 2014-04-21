package com.atlassian.jira.task.context;

import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.PercentageContext.Sink;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.StringUtils;

/**
 * Wraps a {@link TaskProgressSink} and reports percentage progress.
 * 
 * @since v3.13
 */
class TaskProgressPercentageContextSink implements Sink
{
    private final I18nHelper i18nHelper;
    private final TaskProgressSink progressSink;
    private volatile String currentIndex;

    TaskProgressPercentageContextSink(final I18nHelper i18nHelper, final TaskProgressSink progressSink)
    {
        Assertions.notNull("i18nHelper", i18nHelper);
        Assertions.notNull("progressSink", progressSink);

        this.i18nHelper = i18nHelper;
        this.progressSink = progressSink;
    }

    public void setName(final String currentIndex)
    {
        this.currentIndex = currentIndex;
    }

    public void updateProgress(final int progress)
    {
        final String message = i18nHelper.getText("admin.indexing.percent.complete", new Integer(progress));
        final String sub = (StringUtils.isBlank(currentIndex)) ? null : i18nHelper.getText("admin.indexing.current.index", currentIndex);
        progressSink.makeProgress(progress, sub, message);
    }
}
