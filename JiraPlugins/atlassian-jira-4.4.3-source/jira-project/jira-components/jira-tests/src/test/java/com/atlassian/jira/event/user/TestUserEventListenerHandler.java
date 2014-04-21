package com.atlassian.jira.event.user;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.event.spi.ListenerInvoker;
import org.easymock.classextension.EasyMock;

import java.util.List;

/**
 * @since v4.1
 */
public class TestUserEventListenerHandler extends MockControllerTestCase
{
    @Test
    public void testGetsIssueEventListnerForIssueEvent() throws Exception
    {
        final UserEventListener listener = EasyMock.createMock(UserEventListener.class);
        final UserEventListenerHandler userEventListenerHandler = new UserEventListenerHandler();
        final List<? extends ListenerInvoker> invokers = userEventListenerHandler.getInvokers(listener);
        assertEquals(1, invokers.size());
        assertTrue(invokers.get(0) instanceof UserEventListenerHandler.UserEventInvoker);
    }

    @Test
    public void testGetsIssueEventListnerForNonIssueEvent() throws Exception
    {
        final UserEventListenerHandler userEventListenerHandler = new UserEventListenerHandler();
        final List<? extends ListenerInvoker> invokers = userEventListenerHandler.getInvokers(new Object());
        assertEquals(0, invokers.size());
    }
    
}
