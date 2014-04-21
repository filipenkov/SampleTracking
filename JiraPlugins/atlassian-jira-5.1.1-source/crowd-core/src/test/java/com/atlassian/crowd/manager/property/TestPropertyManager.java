package com.atlassian.crowd.manager.property;

import com.atlassian.crowd.dao.property.PropertyDAO;
import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.model.property.Property;
import com.atlassian.crowd.util.mail.SMTPServer;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Unit tests for the property manager
 */
public class TestPropertyManager extends MockObjectTestCase
{
    private PropertyManager propertyManager;
    private Mock propertyDAO;
    private static final String LOCALHOST = "localhost";
    private static final String PASSWORD = "secret";
    private static final String MAIL_PREFIX = "[Crowd]";
    private static final String SENDER_ADDRESS = "test@atlassian.com";
    private static final String MAIL_USERNAME = "bob";
    private static final int MAIL_PORT = 433;
    private static final String MAIL_JNDI_LOCATION = "java:comp/env/mail/CrowdMailServer";
    private static final boolean USE_SSL = false;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        propertyDAO = mock(PropertyDAO.class);
        propertyManager = new PropertyManagerGeneric((PropertyDAO) propertyDAO.proxy());
    }

    @Override
    protected void tearDown() throws Exception
    {
        propertyManager = null;
        propertyDAO = null;
        super.tearDown();
    }

    public void testGetAuthenticatingSMTPServer() throws PropertyManagerException
    {
        // Expect SMTP server
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_HOST)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_HOST, LOCALHOST)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_USERNAME)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_USERNAME, MAIL_USERNAME)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PASSWORD)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_PASSWORD, PASSWORD)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PORT)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_PORT, Integer.toString(MAIL_PORT))));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PREFIX)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_PREFIX, MAIL_PREFIX)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_SENDER)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_SENDER, SENDER_ADDRESS)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_USE_SSL)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_USE_SSL, Boolean.toString(USE_SSL))));

        // Not a JNDI server
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_JNDI_LOCATION)).will(throwException(new ObjectNotFoundException()));

        SMTPServer server = propertyManager.getSMTPServer();

        assertFalse(server.isJndiMailActive());
        assertNull(server.getJndiLocation());
        assertEquals(LOCALHOST, server.getHost());
        assertEquals(MAIL_USERNAME, server.getUsername());
        assertEquals(PASSWORD, server.getPassword());
        assertEquals(MAIL_PREFIX, server.getPrefix());
        assertEquals(SENDER_ADDRESS, server.getFrom().toString());
        assertEquals(MAIL_PORT, server.getPort());
    }

    public void testGetAnonymousSMTPServerWithDefaultPort() throws PropertyManagerException
    {
        // Expect SMTP server
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_HOST)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_HOST, LOCALHOST)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PREFIX)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_PREFIX, MAIL_PREFIX)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_SENDER)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_SENDER, SENDER_ADDRESS)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_USE_SSL)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_USE_SSL, Boolean.toString(USE_SSL))));

        // Anonymous
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_USERNAME)).will(throwException(new ObjectNotFoundException()));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PASSWORD)).will(throwException(new ObjectNotFoundException()));

        // Default port set
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PORT)).will(throwException(new ObjectNotFoundException()));

        // Not a JNDI server
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_JNDI_LOCATION)).will(throwException(new ObjectNotFoundException()));

        SMTPServer server = propertyManager.getSMTPServer();

        assertFalse(server.isJndiMailActive());
        assertNull(server.getJndiLocation());
        assertEquals(LOCALHOST, server.getHost());
        assertNull(MAIL_USERNAME, server.getUsername());
        assertNull(PASSWORD, server.getPassword());
        assertEquals(MAIL_PREFIX, server.getPrefix());
        assertEquals(SENDER_ADDRESS, server.getFrom().toString());
        assertEquals(SMTPServer.DEFAULT_MAIL_PORT, server.getPort());
    }

    public void testGetAnonymousSMTPServer() throws PropertyManagerException
    {
        // Expect SMTP server
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_HOST)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_HOST, LOCALHOST)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PREFIX)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_PREFIX, MAIL_PREFIX)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_SENDER)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_SENDER, SENDER_ADDRESS)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PORT)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_PORT, Integer.toString(MAIL_PORT))));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_USE_SSL)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_USE_SSL, Boolean.toString(USE_SSL))));

        // Anonymous
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_USERNAME)).will(throwException(new ObjectNotFoundException()));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PASSWORD)).will(throwException(new ObjectNotFoundException()));

        // Not a JNDI server
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_JNDI_LOCATION)).will(throwException(new ObjectNotFoundException()));

        SMTPServer server = propertyManager.getSMTPServer();

        assertFalse(server.isJndiMailActive());
        assertNull(server.getJndiLocation());
        assertEquals(LOCALHOST, server.getHost());
        assertNull(MAIL_USERNAME, server.getUsername());
        assertNull(PASSWORD, server.getPassword());
        assertEquals(MAIL_PREFIX, server.getPrefix());
        assertEquals(SENDER_ADDRESS, server.getFrom().toString());
        assertEquals(MAIL_PORT, server.getPort());
    }

    public void testGetJNDIServer() throws PropertyManagerException
    {
        // Expect JNDI server
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_JNDI_LOCATION)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_JNDI_LOCATION, MAIL_JNDI_LOCATION)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_SENDER)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_SENDER, SENDER_ADDRESS)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PREFIX)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_PREFIX, MAIL_PREFIX)));

        // Not a SMTP server
        propertyDAO.expects(never()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_HOST)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_HOST, LOCALHOST)));
        propertyDAO.expects(never()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PORT)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_PORT, Integer.toString(MAIL_PORT))));
        propertyDAO.expects(never()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_USERNAME)).will(throwException(new ObjectNotFoundException()));
        propertyDAO.expects(never()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PASSWORD)).will(throwException(new ObjectNotFoundException()));
        propertyDAO.expects(never()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_USE_SSL)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_USE_SSL, Boolean.toString(USE_SSL))));


        SMTPServer server = propertyManager.getSMTPServer();

        assertTrue(server.isJndiMailActive());

        assertEquals(MAIL_JNDI_LOCATION, server.getJndiLocation());
        assertEquals(MAIL_PREFIX, server.getPrefix());
        assertEquals(SENDER_ADDRESS, server.getFrom().toString());

        assertNull(server.getHost());
        assertNull(server.getUsername());
        assertNull(server.getPassword());
        assertEquals(0, server.getPort());
    }

    public void testGetJNDIServerWithNoEmailPrefixSet() throws PropertyManagerException
    {
        // Expect JNDI server
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_JNDI_LOCATION)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_JNDI_LOCATION, MAIL_JNDI_LOCATION)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_SENDER)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_SENDER, SENDER_ADDRESS)));

        // No prefix set
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PREFIX)).will(throwException(new ObjectNotFoundException()));

        SMTPServer server = propertyManager.getSMTPServer();

        assertNull(server.getPrefix());
    }

    public void testGetAnonymousSMTPServerWithNoEmailPrefixSet() throws PropertyManagerException
    {
        // Expect SMTP server
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_HOST)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_HOST, LOCALHOST)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_SENDER)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_SENDER, SENDER_ADDRESS)));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PORT)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_PORT, Integer.toString(MAIL_PORT))));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_USE_SSL)).will(returnValue(new Property(Property.CROWD_PROPERTY_KEY, Property.MAILSERVER_USE_SSL, Boolean.toString(USE_SSL))));

        // No email prefix set
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PREFIX)).will(throwException(new ObjectNotFoundException()));

        // Anonymous
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_USERNAME)).will(throwException(new ObjectNotFoundException()));
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_PASSWORD)).will(throwException(new ObjectNotFoundException()));

        // Not a JNDI server
        propertyDAO.expects(once()).method("find").with(eq(Property.CROWD_PROPERTY_KEY), eq(Property.MAILSERVER_JNDI_LOCATION)).will(throwException(new ObjectNotFoundException()));

        SMTPServer server = propertyManager.getSMTPServer();

        assertNull(server.getPrefix());
    }




}
