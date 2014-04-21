package com.atlassian.jira.task.context;

import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.PercentageContext.Sink;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.Sized;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.johnson.event.Event;
import org.apache.log4j.Logger;

/**
 * Provides static methods for creating {@link Context} instances easily.
 * 
 * @since v4.0
 */
public class Contexts
{
    private static final Context NULL = new Context()
    {
        private final Task task = new Task()
        {
            public void complete()
            {};
        };

        public void setName(final String arg0)
        {}

        public Task start(final Object input)
        {
            return task;
        }
    };

    public static Context nullContext()
    {
        return NULL;
    }

    public static Context percentageReporter(@NotNull final Sized sized, @NotNull final TaskProgressSink sink, @NotNull final I18nHelper i18n, @NotNull final Logger logger, @NotNull final String msg)
    {
        return createContext(sized, new CompositeSink(new TaskProgressPercentageContextSink(i18n, sink), new LoggingContextSink(logger, msg)));
    }

    public static Context percentageReporter(@NotNull final Sized sized, @NotNull final TaskProgressSink sink, @NotNull final I18nHelper i18n, @NotNull final Logger logger, @NotNull final String msg, @NotNull final Event event)
    {
        return createContext(sized, new CompositeSink(new JohnsonEventSink(event), new TaskProgressPercentageContextSink(i18n, sink),
            new LoggingContextSink(logger, msg)));
    }

    public static Context percentageLogger(@NotNull final Sized sized, @NotNull final Logger logger, @NotNull final String msg)
    {
        return createContext(sized, new LoggingContextSink(logger, msg));
    }

    private static Context createContext(@NotNull final Sized sized, @NotNull final Sink contextSink)
    {
        Assertions.notNull("sized", sized);
        final int size = sized.size();
        if (size > 0)
        {
            return new PercentageContext(size, contextSink);
        }
        return new UnboundContext(contextSink);
    }

    private Contexts()
    {
        throw new AssertionError("cannot instantiate!");
    }
}
