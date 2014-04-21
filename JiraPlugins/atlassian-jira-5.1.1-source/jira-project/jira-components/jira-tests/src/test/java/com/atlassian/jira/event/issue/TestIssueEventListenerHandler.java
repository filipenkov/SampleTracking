package com.atlassian.jira.event.issue;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.event.spi.ListenerInvoker;
import com.atlassian.jira.mock.MockListenerManager;
import com.atlassian.jira.mock.event.MockIssueEventListener;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.List;

/**
 * @since v4.1
 */
public class TestIssueEventListenerHandler extends ListeningTestCase
{
    @Test
    public void testGetsIssueEventListnerForIssueEvent() throws Exception
    {
        final IssueEventListenerHandler issueEventListenerHandler = new IssueEventListenerHandler();
        final List<? extends ListenerInvoker> invokers = issueEventListenerHandler.getInvokers(new MockIssueEventListener(new MockListenerManager()));
        assertEquals(1, invokers.size());
        assertTrue(invokers.get(0) instanceof IssueEventListenerHandler.IssueEventInvoker);
    }

    @Test
    public void testGetsIssueEventListnerForNonIssueEvent() throws Exception
    {
        final IssueEventListenerHandler issueEventListenerHandler = new IssueEventListenerHandler();
        final List<? extends ListenerInvoker> invokers = issueEventListenerHandler.getInvokers(new Object());
        assertEquals(0, invokers.size());
    }

}
