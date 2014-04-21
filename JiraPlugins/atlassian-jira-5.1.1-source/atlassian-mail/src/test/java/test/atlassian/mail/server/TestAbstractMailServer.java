
package test.atlassian.mail.server;

import com.atlassian.mail.MailProtocol;
import junit.framework.TestCase;
import test.mock.mail.server.MockAbstractMailServer;

public class TestAbstractMailServer extends TestCase
{
    private static final String SAMPLE_MAIL_PROP_KEY = "some.weird.javax.mail.property";

    public TestAbstractMailServer(String s)
    {
        super(s);
    }

    public void testConstructor1()
    {
        MockAbstractMailServer mams = new MockAbstractMailServer(1L, "name", "desc", MailProtocol.POP,"serverName", "port", "username", "password", "socksHost", "socksPort");
        assertEquals(new Long(1), mams.getId());
        assertEquals("name", mams.getName());
        assertEquals("desc", mams.getDescription());
        assertEquals("pop3",mams.getMailProtocol().getProtocol());
        assertEquals("serverName", mams.getHostname());
        assertEquals("port",mams.getPort());
        assertEquals("username", mams.getUsername());
        assertEquals("password", mams.getPassword());
        assertEquals(false,mams.getDebug());
        assertEquals(10000,mams.getTimeout());
        assertEquals("socksHost", mams.getSocksHost());
        assertEquals("socksPort", mams.getSocksPort());
		assertTrue(mams.toString().contains("password=***"));
		MockAbstractMailServer mams2 = new MockAbstractMailServer(1L, "name", "desc", MailProtocol.POP,"serverName", "port", "username", null, "socksHost", "socksPort");
		assertTrue(mams2.toString().contains("password=<unset>"));

    }

    public void testConstructor2()
    {
        MockAbstractMailServer mams = new MockAbstractMailServer(1L, "name", "desc", MailProtocol.POP,"serverName","port", "", "", "socksHost", "socksPort");
        assertNull(mams.getUsername());
        assertNull(mams.getPassword());
    }

    public void testSystemPropertiesAreSet()
    {
        MockAbstractMailServer mams = new MockAbstractMailServer(1L, "name", "desc", MailProtocol.POP,"serverName","port", "", "", "socksHost", "socksPort");
        assertFalse(mams.getProperties().containsKey(SAMPLE_MAIL_PROP_KEY));

        try
        {
            System.setProperty(SAMPLE_MAIL_PROP_KEY, "some.value");
            mams = new MockAbstractMailServer(1L, "name", "desc", MailProtocol.POP,"serverName","port", "", "", "socksHost", "socksPort");
            assertTrue(mams.getProperties().containsKey(SAMPLE_MAIL_PROP_KEY));
            assertEquals("some.value", mams.getProperties().get(SAMPLE_MAIL_PROP_KEY));
        }
        finally
        {
            System.clearProperty(SAMPLE_MAIL_PROP_KEY);
        }
    }

    public void testGetsSets()
    {
        MockAbstractMailServer mams = new MockAbstractMailServer(1L,"","", MailProtocol.POP, "", "", "", "", "", "");
        mams.setId(new Long(100));
        assertEquals(new Long(100), mams.getId());
        mams.setName("name");
        assertEquals("name", mams.getName());
        mams.setDescription("desc");
        assertEquals("desc", mams.getDescription());
        mams.setMailProtocol(MailProtocol.SMTP);
        assertEquals(MailProtocol.SMTP, mams.getMailProtocol());
        mams.setHostname("serverName");
        assertEquals("serverName", mams.getHostname());
        mams.setPort("port");
        assertEquals("port",mams.getPort());
        mams.setUsername("username");
        assertEquals("username", mams.getUsername());
        mams.setPassword("password");
        assertEquals("password", mams.getPassword());
        mams.setUsername("");
        assertNull(mams.getUsername());
        mams.setPassword("");
        assertNull(mams.getPassword());
        mams.setUsername(null);
        assertNull(mams.getUsername());
        mams.setPassword(null);
        assertNull(mams.getPassword());
        mams.setSocksHost("socksHost");
        assertEquals("socksHost", mams.getSocksHost());
        mams.setSocksPort("socksPort");
        assertEquals("socksPort", mams.getSocksPort());
    }
}