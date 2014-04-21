package test.atlassian.mail;

import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.sun.mail.util.PropUtil;
import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.mock.mail.server.MockMailServerManager;

import javax.mail.Session;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Session.class, PropUtil.class})
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
