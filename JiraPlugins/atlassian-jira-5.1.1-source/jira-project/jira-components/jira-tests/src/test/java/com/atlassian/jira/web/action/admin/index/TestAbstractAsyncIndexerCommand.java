package com.atlassian.jira.web.action.admin.index;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestAbstractAsyncIndexerCommand extends ListeningTestCase
{
    @Test
    public void testCallJohnsonSetup() throws Exception
    {

        final AtomicBoolean doReindexCalled = new AtomicBoolean(false);
        final AtomicBoolean addCalled = new AtomicBoolean(false);
        final AtomicBoolean removeCalled = new AtomicBoolean(false);
        final JohnsonEventContainer johnsonEventContainer = new JohnsonEventContainer()
        {

            @Override
            public void addEvent(final Event event)
            {
                assertNotNull(event);
                addCalled.set(true);
            }

            @Override
            public void removeEvent(final Event event)
            {
                assertNotNull(event);
                removeCalled.set(true);
            }
        };

        final Object o = new Object()
        {
            @SuppressWarnings("unused")
            public int size()
            {
                return 0;
            }
        };
        final IndexLifecycleManager issueIndexManager = (IndexLifecycleManager) DuckTypeProxy.getProxy(IndexLifecycleManager.class, o);

        final AbstractAsyncIndexerCommand command = new AbstractAsyncIndexerCommand(johnsonEventContainer, issueIndexManager, new QuietLogger(
            getClass().getName()), new MockI18nHelper())
        {
            @Override
            public IndexCommandResult doReindex(final Context appEvent, final IndexLifecycleManager indexManager)
            {
                doReindexCalled.set(true);
                return null;
            }
        };
        command.setTaskProgressSink(TaskProgressSink.NULL_SINK);

        command.call();

        assertTrue(addCalled.get());
        assertTrue(removeCalled.get());
        assertTrue(doReindexCalled.get());

    }

    @Test
    public void testCallJohnsonTeardown() throws Exception
    {

        final AtomicBoolean doReindexCalled = new AtomicBoolean(false);
        final AtomicBoolean addCalled = new AtomicBoolean(false);
        final AtomicBoolean removeCalled = new AtomicBoolean(false);
        final JohnsonEventContainer johnsonEventContainer = new JohnsonEventContainer()
        {

            @Override
            public void addEvent(final Event event)
            {
                assertNotNull(event);
                addCalled.set(true);
            }

            @Override
            public void removeEvent(final Event event)
            {
                assertNotNull(event);
                removeCalled.set(true);
            }
        };

        final Object o = new Object()
        {
            @SuppressWarnings("unused")
            public int size()
            {
                return 0;
            }
        };
        final IndexLifecycleManager issueIndexManager = (IndexLifecycleManager) DuckTypeProxy.getProxy(IndexLifecycleManager.class, o);

        class ObscureException extends RuntimeException
        {}

        final AbstractAsyncIndexerCommand command = new AbstractAsyncIndexerCommand(johnsonEventContainer, issueIndexManager, new QuietLogger(
            getClass().getName()), new MockI18nBean())
        {
            @Override
            public IndexCommandResult doReindex(final Context appEvent, final IndexLifecycleManager indexManager)
            {
                doReindexCalled.set(true);
                throw new ObscureException(); //It should still tear down Johnson events
            }
        };
        command.setTaskProgressSink(TaskProgressSink.NULL_SINK);

        try
        {
            command.call();
            fail("ObscureException should have been thrown!");
        }
        catch (final ObscureException ignore)
        {}

        assertTrue(addCalled.get());
        assertTrue(removeCalled.get());
        assertTrue(doReindexCalled.get());
    }
}
