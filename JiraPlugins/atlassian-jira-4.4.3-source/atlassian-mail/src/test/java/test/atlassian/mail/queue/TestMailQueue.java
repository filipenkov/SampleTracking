package test.atlassian.mail.queue;

import com.atlassian.mail.Email;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueImpl;
import com.atlassian.mail.queue.SingleMailQueueItem;
import junit.framework.TestCase;
import test.mock.mail.server.MockMailServerManager;
import test.mock.mail.server.MockSMTPMailServer;


public class TestMailQueue extends TestCase
{

    MockMailServerManager mockMailServerManager;
    MockSMTPMailServer mockMailServer;

    @Override
    protected void setUp() throws Exception {
        mockMailServerManager = new MockMailServerManager();
        MailFactory.setServerManager(mockMailServerManager);
    }


    public void testUnsentMailIsQueued()
    {
        MailQueue mq = new MailQueueImpl();
        SingleMailQueueItem item = new SingleMailQueueItem(new Email("mike@atlassian.com").setSubject("Test Subject").setBody("Test Body"));
        mq.addItem(item);
        for (int i= 0; i<10; i++)
        {
            mq.sendBuffer();
            assertEquals("Errors should be empty", 0, mq.errorSize());
            assertEquals("Queue should contain 1 item", 1, mq.size());
        }
        mq.sendBuffer();
        assertTrue("Mail Item should be in error queue", mq.getErrorQueue().contains(item));
    }

    public void testMailIsSent()
    {
        MailQueue mq = new MailQueueImpl();
        SingleMailQueueItem item = new SingleMailQueueItem(new Email("mike@atlassian.com").setSubject("Test Subject").setBody("Test Body").setFrom("james@atlassian.com"));
        mq.addItem(item);
        mq.sendBuffer();
        assertFalse("Mail Item should not be in error queue", mq.getErrorQueue().contains(item));
        assertTrue("Mail Queue should be empty", mq.getQueue().isEmpty());
    }

    public void testOrdering()
    {
        MailQueue mq = new MailQueueImpl();
        enqueueMailQueueItem(mq, "mike@atlassian.com", "Test Subject 1", "Test Body", "james@atlassian.com");
        enqueueMailQueueItem (mq, "mike@atlassian.com", "Test Subject 2", "Test Body", "james@atlassian.com");
        enqueueMailQueueItem (mq, "mike@atlassian.com", "Test Subject 3", "Test Body", null);
        enqueueMailQueueItem (mq, "mike@atlassian.com", "Test Subject 4", "Test Body", "james@atlassian.com");
        assertAtFrontOfQueue(mq, "Test Subject 1");
        mq.sendBuffer();
        assertAtFrontOfQueue(mq, "Test Subject 3");
        enqueueMailQueueItem(mq, "mike@atlassian.com", "Test Subject 5", "Test Body", "james@atlassian.com");
        enqueueMailQueueItem(mq, "mike@atlassian.com", "Test Subject 6", "Test Body", "james@atlassian.com");
        assertAtFrontOfQueue(mq, "Test Subject 5");
    }

    private void assertAtFrontOfQueue(MailQueue mq, final String subject) {
        assertTrue("Correct object at top of queue", mq.getQueue().peek().getSubject().equals(subject));
    }

    private void enqueueMailQueueItem(final MailQueue mailQueue, final String to, final String subject, final String body, final String from) {
        Email email = new Email(to).setSubject(subject).setBody(body);
        if (from != null)
        {
            email.setFrom(from);
        }
        mailQueue.addItem(new SingleMailQueueItem(email));
    }
}
