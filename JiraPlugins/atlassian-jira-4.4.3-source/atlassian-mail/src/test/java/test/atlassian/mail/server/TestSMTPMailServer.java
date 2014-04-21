/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Nov 25, 2002
 * Time: 9:20:50 AM
 * CVS Revision: $Revision: 1.10 $
 * Last CVS Commit: $Date: 2004/01/06 05:43:19 $
 * Author of last CVS Commit: $Author: dloeng $
 * To change this template use Options | File Templates.
 */
package test.atlassian.mail.server;

import alt.javax.mail.Session;
import alt.javax.mail.internet.MimeMessage;
import alt.javax.mail.internet.MimeMessageImpl;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailUtils;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import junit.framework.TestCase;
import org.apache.velocity.exception.VelocityException;
import test.mock.mail.MockTransport;
import test.mock.mail.MockSession;
import test.mock.mail.MockAuthenticator;
import test.mock.mail.server.MockMailServerManager;
import test.mock.mail.server.MockSMTPMailServer;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * Mails are basically sent by the thisSession.getTransport("smtp").send() method in the SMTPMailServerImpl class
 *
 * We have created an MockSMTPMailServer class that extends SMTPMailServerImpl but overrides the getSession() to return our MockSession.
 * Our MockSession in turn returns our MockTransport when getTransport() is called on it.
 * Finally and therefore, when getTransport("smtp") is called, our MockTransport is used and we can test if the message passed to it
 * matches the message that we contruct and expect.
 *
 */
public class TestSMTPMailServer extends TestCase
{
    public TestSMTPMailServer(String s)
    {
        super(s);
    }

    public void testConstructor()
    {
        MockSMTPMailServer msms = new MockSMTPMailServer(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", "ofellows", "owen01");
        assertEquals("owen@atlassian.com", msms.getDefaultFrom());
        assertEquals("[OWEN]", msms.getPrefix());
        assertTrue(false == msms.isSessionServer());
    }

    public void testGetsSets()
    {
        MockSMTPMailServer msms = new MockSMTPMailServer(new Long(1), "name", "desc", "", "", false, "mail.atlassian.com", "ofellows", "owen01");
        msms.setDefaultFrom("owen@atlassian.com");
        assertEquals("owen@atlassian.com", msms.getDefaultFrom());
        msms.setPrefix("[OWEN]");
        assertEquals("[OWEN]", msms.getPrefix());
        msms.setSessionServer(true);
        assertTrue(true == msms.isSessionServer());
    }

    public void testGetSessionNoUser() throws NamingException, MailException
    {
        MockSMTPMailServer msms = new MockSMTPMailServer(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", null, null);
        assertNotNull(msms.getSession());
    }

    public void testGetSessionWithUser() throws NamingException, MailException
    {
        MockMailServerManager mmsm = new MockMailServerManager();
        MailFactory.setServerManager(mmsm);

        // just for this test case we want SMTPMailServerImpl.getSession() functionality, combined with the
        // getAuthenticator from MockSMTPMailServer (in order to return a MockAuthenticator for this tests)
        SMTPMailServer msms = new SMTPMailServerImpl(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", "ofellows", "owen01")
        {
            protected Authenticator getAuthenticator()
            {
                return new MockAuthenticator(getUsername(), getPassword());
            }
        };

        Session session = msms.getSession();
        assertNotNull(session);
        assertEquals("ofellows", session.getPasswordAuthentication(new URLName("mail.atlassian.com")).getUserName());
        assertEquals("owen01", session.getPasswordAuthentication(new URLName("mail.atlassian.com")).getPassword());
    }

    public void testSend() throws NamingException, MessagingException, AddressException, MailException
    {
        MockMailServerManager mmsm = new MockMailServerManager();
        MailFactory.setServerManager(mmsm);
        MockSMTPMailServer msms = new MockSMTPMailServer(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", "ofellows", "owen01");

        MockSession mockSession = (MockSession) msms.getSession();
        MockTransport mt = (MockTransport) mockSession.getTransport();
        MimeMessage mm = new MimeMessageImpl(mockSession);

        mm.setFrom(new InternetAddress("owen@atlassian.com"));
        mm.setRecipients(Message.RecipientType.TO, "owen@altassian.com");
        mm.setRecipients(Message.RecipientType.CC, "mike@atlassian.con");
        mm.setSubject("[OWEN] Test Subject");
        mm.setContent("Test Body", "text/plain");

        mt.setExpectedMessage(mm);

        Email email = new Email("mike@atlassian.com").setSubject("Test Subject").setBody("Test Body").setFrom("owen@atlassian.com");
        msms.send(email);

        mt.verify();
    }

    public void testSendWithAttachment() throws NamingException, MessagingException, AddressException, MailException, IOException
    {
        MockMailServerManager mmsm = new MockMailServerManager();
        MailFactory.setServerManager(mmsm);
        MockSMTPMailServer msms = new MockSMTPMailServer(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", "ofellows", "owen01");

        MockSession mockSession = (MockSession) msms.getSession();
        MockTransport mt = (MockTransport) mockSession.getTransport();
        MimeMessage mm = new MimeMessageImpl(mockSession);

        mm.setFrom(new InternetAddress("owen@atlassian.com"));
        mm.setRecipients(Message.RecipientType.TO, "owen@altassian.com");
        mm.setRecipients(Message.RecipientType.CC, "mike@atlassian.com");
        mm.setSubject("[OWEN] Test Subject");

        Multipart multipart = new MimeMultipart();

        MimeBodyPart exportZip = MailUtils.createAttachmentMimeBodyPart("C:/export.zip");
        MimeBodyPart logTxt = MailUtils.createAttachmentMimeBodyPart("C:/log.txt");
        multipart.addBodyPart(exportZip);
        multipart.addBodyPart(logTxt);

        // create the message part and add to multipart
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText("Test Body", "text/plain");

        multipart.addBodyPart(messageBodyPart);

        // Put parts in message
        mm.setContent(multipart);

        // check that the message sent by:
        // thisSession.getTransport("smtp").send(message)
        // matches the one we have constructed
        mt.setExpectedMessage(mm);

        // remove body part from the multipart constructed and pass into send method where it will be added back
        multipart.removeBodyPart(messageBodyPart);

        try
        {
            Email email = new Email("mike@atlassian.com").setSubject("Test Subject").setBody("Test Body").setFrom("owen@atlassian.com").setMultipart(multipart);
            msms.send(email);
        }
        catch (Exception e)
        {
            fail();
        }

        mt.verify();
    }

    public void testParseAddressPass(String pAddress) throws AddressException
    {
        MockSMTPMailServer msms = new MockSMTPMailServer(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", null, null);
        InternetAddress[] address = msms.parseAddresses("owen.fellows@owen.com,owen@atlassian.com");
        assertEquals(2, address.length);
        assertEquals("owen.fellows@owen.com", address[0]);
        assertEquals("owen@atlassian.com", address[1]);
    }

    /**
     * Test case to verify JRA-1436
     */
    public void testGetJNDIServer() throws NamingException, MailException
    {
        final javax.mail.Session sessionInstance = javax.mail.Session.getInstance(new Properties(), null);

        SMTPMailServer msms = new SMTPMailServerImpl(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", true, "mail.atlassian.com", null, null)
        {
            protected Object getJndiSession() throws NamingException
            {
                return sessionInstance;
            }
        };

        alt.javax.mail.Session session = msms.getSession();
        assertEquals(sessionInstance, session.getWrappedSession());

    }

    /**
     * Test that if the class at the specified jndi location is not of the correct type, that an illegalargument
     * exception is thrown.
     */
    public void testIncorrectJndiClass() throws NamingException, MailException
    {
        SMTPMailServer msms = new SMTPMailServerImpl(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", true, "mail.atlassian.com", null, null)
        {
            protected Object getJndiSession() throws NamingException
            {
                return new Object();
            }
        };

        try
        {
            msms.getSession();
            fail("Expected IllegalArgumentException when wrong class type in jdni location");
        }
        catch (IllegalArgumentException e)
        {
            //this is what we want
        }
    }

    /**
     * Following tests moved over from TestMailFactory (since all send methods have been removed from there
     */

    public void testSendFail() throws Exception
    {
        MockMailServerManager mmsm = new MockMailServerManager();

        MailFactory.setServerManager(mmsm);
        MockSMTPMailServer mailServer = new MockSMTPMailServer(null, "default", "", "", "", false, "mail.atlassian.com", "", "");
        MailFactory.getServerManager().create(mailServer);
        try
        {
            Email email = new Email("mike@atlassian.com").setSubject("Test Subject").setBody("Test Body");
            mailServer.send(email);
            fail();
        }
        catch (MailException e)
        {
            assertEquals("Tried to send mail (Test Subject) from no one (no 'from' and 'default from' specified).", e.getMessage());
        }
    }

    public void testSendMail1() throws Exception, MessagingException, VelocityException
    {
        MockSMTPMailServer msms = new MockSMTPMailServer(new Long(-1), "default", "", "owen@atlassian.com", "", false, "mail.atlassian.com", "", "");

        MockSession mockSession = (MockSession) msms.getSession();
        MockTransport mt = (MockTransport) mockSession.getTransport();
        MimeMessage mm = new MimeMessageImpl(mockSession);

        mm.setFrom(new InternetAddress("owen@atlassian.com"));
        mm.setRecipients(Message.RecipientType.TO, "mike@altassian.com");
        mm.setSubject("Test Subject 1");
        mm.setContent("Test 1", "text/plain");
        mt.setExpectedMessage(mm);

        Email email = new Email("mike@atlassian.com").setSubject("Test Subject 1").setBody("Test 1").setFrom("owen@atlassian.com");
        msms.send(email);

        mt.verify();
    }

    public void testSendMail2() throws Exception, MessagingException, VelocityException
    {
        MockSMTPMailServer msms = new MockSMTPMailServer(new Long(-1), "default", "", "owen@atlassian.com", "", false, "mail.atlassian.com", "", "");

        MockSession mockSession = (MockSession) msms.getSession();
        MockTransport mt = (MockTransport) mockSession.getTransport();
        MimeMessage mm = new MimeMessageImpl(mockSession);

        mm.setFrom(new InternetAddress("owen@atlassian.com"));
        mm.setRecipients(Message.RecipientType.TO, "mike@altassian.com");
        mm.setRecipients(Message.RecipientType.CC, "scott@atlassian.con");
        mm.setSubject("Test Subject 2");
        mm.setContent("/testing/Test 2", "text/plain");

        mt.setExpectedMessage(mm);

        Email email = new Email("mike@atlassian.com").setSubject("Test Subject 2").setCc("scott@atlassian.com").setBody("/testing/Test 2").setFrom("owen@atlassian.com");
        msms.send(email);

        mt.verify();
    }

    public void testSendMail3() throws Exception, MessagingException, AddressException, VelocityException
    {
        MockSMTPMailServer msms = new MockSMTPMailServer(new Long(-1), "default", "", "owen@atlassian.com", "", false, "mail.atlassian.com", "", "");

        MockSession mockSession = (MockSession) msms.getSession();
        MockTransport mt = (MockTransport) mockSession.getTransport();
        MimeMessage mm = new MimeMessageImpl(mockSession);

        mm.setFrom(new InternetAddress("owen@atlassian.com"));
        mm.setRecipients(Message.RecipientType.TO, "mike@altassian.com");
        mm.setSubject("Test Subject 3");
        mm.setContent("/testing/Test 3", "UTF-8");

        mt.setExpectedMessage(mm);

        Email email = new Email("mike@atlassian.com").setSubject("Test Subject 3").setBody("/testing/Test 3").setFrom("owen@atlassian.com");
        msms.send(email);

        mt.verify();
    }
}
