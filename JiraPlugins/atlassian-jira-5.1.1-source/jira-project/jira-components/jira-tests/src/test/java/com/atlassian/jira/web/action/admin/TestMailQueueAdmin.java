package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.mail.queue.MailQueue;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestMailQueueAdmin extends ListeningTestCase
{
    @Test
    public void shouldNotFlushTheQueueIfTheFlushParameterHasNotBeenSet() throws Exception
    {
        final MailQueue mockMailQueue = mock(MailQueue.class);
        final MailQueueAdmin mailQueueAdminAction =
                new MailQueueAdmin(mockMailQueue, mock(NotificationSchemeManager.class), mock(ApplicationProperties.class));

        mailQueueAdminAction.execute();
        verify(mockMailQueue, never()).sendBuffer();
    }

    @Test
    public void shouldFlushTheQueueIfTheFlushParameterHasBeenSet() throws Exception
    {
        final MailQueue mockMailQueue = mock(MailQueue.class);

        final MailQueueAdmin mailQueueAdminAction =
                new MailQueueAdmin(mockMailQueue, mock(NotificationSchemeManager.class), mock(ApplicationProperties.class));
        mailQueueAdminAction.setFlush(true);

        mailQueueAdminAction.execute();
        verify(mockMailQueue, times(1)).sendBuffer();
    }
}
