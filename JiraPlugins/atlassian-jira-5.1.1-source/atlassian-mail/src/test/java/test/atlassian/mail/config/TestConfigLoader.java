package test.atlassian.mail.config;

import com.atlassian.mail.config.ConfigLoader;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.managers.XMLMailServerManager;
import com.sun.mail.util.PropUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.mock.mail.server.MockMailServerManager;

import javax.mail.Session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 9, 2002
 * Time: 3:30:19 PM
 * To change this template use Options | File Templates.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Session.class, PropUtil.class})
public class TestConfigLoader
{
    @Test
    public void testGetManager()
    {
        MailServerManager server = ConfigLoader.getServerManager();

        assertNotNull(server);
        assertTrue(server instanceof XMLMailServerManager);

        XMLMailServerManager serverManager = (XMLMailServerManager) server;
        assertEquals("test-mail-servers.xml", serverManager.getConfigFile());
    }

    @Test
    public void testMailConfig()
    {
        MailServerManager server = ConfigLoader.getServerManager("mock-mail-config.xml");
        assertTrue(server instanceof MockMailServerManager);
    }
}
