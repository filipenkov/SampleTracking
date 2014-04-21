package com.atlassian.jira.util.index;

import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.Sized;
import com.atlassian.johnson.event.Event;
import org.apache.log4j.Logger;

/**
 * Provides static methods for creating {@link Context} instances easily.
 * 
 * @since v3.13
 */
public class Contexts
{
    private static final String REINDEXING = "Re-indexing is {0}% complete. Current index: {1}";

    public static Context percentageLogger(final Sized sized, final Logger logger)
    {
        return com.atlassian.jira.task.context.Contexts.percentageLogger(sized, logger, REINDEXING);
    }

    public static Context percentageReporter(@NotNull final Sized sized, @NotNull final TaskProgressSink sink, @NotNull final I18nHelper i18n, @NotNull final Logger logger)
    {
        return com.atlassian.jira.task.context.Contexts.percentageReporter(sized, sink, i18n, logger, REINDEXING);
    }

    public static Context percentageReporter(@NotNull final Sized sized, @NotNull final TaskProgressSink sink, @NotNull final I18nHelper i18n, @NotNull final Logger logger, @NotNull final Event event)
    {
        return com.atlassian.jira.task.context.Contexts.percentageReporter(sized, sink, i18n, logger, REINDEXING, event);
    }

    /**
     * @return a do nothing context
     * @deprecated use {@link com.atlassian.jira.task.context.Contexts#nullContext()} instead
     */
    @Deprecated
    public static Context nullContext()
    {
        return com.atlassian.jira.task.context.Contexts.nullContext();
    }
}
