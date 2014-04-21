package test.atlassian.mail.config;

import junit.framework.TestCase;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.managers.XMLMailServerManager;
import com.atlassian.mail.config.ConfigLoader;
import test.mock.mail.server.MockMailServerManager;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 9, 2002
 * Time: 3:30:19 PM
 * To change this template use Options | File Templates.
 */
public class TestConfigLoader extends TestCase
{
    public void testGetManager()
    {
        MailServerManager server = ConfigLoader.getServerManager();

        assertNotNull(server);
        assertTrue(server instanceof XMLMailServerManager);

        XMLMailServerManager serverManager = (XMLMailServerManager) server;
        assertEquals("test-mail-servers.xml", serverManager.getConfigFile());
    }

    public void testMailConfig()
    {
        MailServerManager server = ConfigLoader.getServerManager("mock-mail-config.xml");
        assertTrue(server instanceof MockMailServerManager);
    }
}
