package test.atlassian.mail;

import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import junit.framework.TestCase;
import test.mock.mail.server.MockMailServerManager;

public class TestMailFactory extends TestCase
{
    public TestMailFactory(String s)
    {
        super(s);
    }

    public void testFactory() throws Exception
    {
        MailServerManager msm = new MockMailServerManager();
        MailFactory.setServerManager(msm);
        assertSame(msm, MailFactory.getServerManager());

        MailFactory.refresh();
        assertNotSame(msm, MailFactory.getServerManager());
    }   
}
