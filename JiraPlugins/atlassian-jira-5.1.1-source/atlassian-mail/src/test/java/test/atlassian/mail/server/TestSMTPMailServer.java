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

import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailUtils;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.sun.mail.util.PropUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.mock.mail.server.MockSMTPMailServer;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.NamingException;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * Mails are basically sent by the thisSession.getTransport("smtp").send() method in the SMTPMailServerImpl class
 *
 * Now uses Mockito to mock the Transpoirt and  Session objects
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Session.class, PropUtil.class})
public class TestSMTPMailServer
{

    private Session mockSession;
    private Transport mockTransport;


    @Before
    public void setupMockSessionAndTransport() throws NoSuchProviderException {
        mockSession = PowerMockito.mock(Session.class);
        PowerMockito.mockStatic(PropUtil.class);
        when(PropUtil.getBooleanSessionProperty(mockSession, "mail.mime.address.strict", true)).thenReturn(false);
        mockTransport = mock(Transport.class);
        when(mockSession.getTransport("smtp")).thenReturn(mockTransport);
    }

    @Test
    public void testConstructor()
    {
        SMTPMailServer sms = new SMTPMailServerImpl(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", "ofellows", "owen01");
        assertEquals("Expected from","owen@atlassian.com", sms.getDefaultFrom());
        assertEquals("Expected prefix", "[OWEN]", sms.getPrefix());
        assertFalse("Expected the mail server not to be a session server", sms.isSessionServer());
    }


    @Test
    public void testGetsSets()
    {
        SMTPMailServer sms = new SMTPMailServerImpl(new Long(1), "name", "desc", "", "", false, "mail.atlassian.com", "ofellows", "owen01");
        sms.setDefaultFrom("owen@atlassian.com");
        assertEquals("Expected default from to be owen@atlassian.com","owen@atlassian.com", sms.getDefaultFrom());
        sms.setPrefix("[OWEN]");
        assertEquals("Expected prefix to be [OWEN]", "[OWEN]", sms.getPrefix());
        sms.setSessionServer(true);
        assertTrue(sms.isSessionServer());
    }

    @Test
    public void testGetSessionNoUser() throws NamingException, MailException
    {
        SMTPMailServer sms = new SMTPMailServerImpl(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", null, null);
        assertNotNull(sms.getSession());
    }

    @Test
    public void testGetSessionWithUser() throws NamingException, MailException
    {


        // just for this test case we want SMTPMailServerImpl.getSession() functionality, combined with the
        // getAuthenticator from SMTPMailServer (in order to return a MockAuthenticator for this tests)
        SMTPMailServer sms = new SMTPMailServerImpl(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", "ofellows", "owen01");
        Session session = sms.getSession();
        assertNotNull(session);
        assertEquals("ofellows", session.getPasswordAuthentication(new URLName("smtp", "mail.atlassian.com", 25, null, null, null)).getUserName());
        assertEquals("owen01", session.getPasswordAuthentication(new URLName("smtp", "mail.atlassian.com", 25, null, null, null)).getPassword());
    }

    @Test
    public void testSend() throws NamingException, MessagingException, AddressException, MailException, IOException {

        MimeMessage expectedMessage = setupExpectedMessage("owen@atlassian.com", "scott@atlassian.com", "mike@atlassian.con", "james@atlassian.com", "[OWEN] Test Subject", "Test Body", "text/plain");

        SMTPMailServer mockSmtpMailServer = new MockSMTPMailServer(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", "ofellows", "owen01", mockSession);
        Address[] expectedDestinations = new Address[] {new InternetAddress("scott@atlassian.com"), new InternetAddress("mike@atlassian.com"), new InternetAddress("james@atlassian.com")};

        Email email = new Email("scott@atlassian.com").setSubject("Test Subject").setBody("Test Body").setBcc("james@atlassian.com").setCc("mike@atlassian.com").setFrom("owen@atlassian.com");
        mockSmtpMailServer.send(email);
        verifySentMessage(mockTransport, expectedMessage, expectedDestinations);
    }


    @Test
    public void testSendWithAttachment() throws NamingException, MessagingException, AddressException, MailException, IOException
    {
        SMTPMailServer mockSmtpMailServer = new MockSMTPMailServer(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", "ofellows", "owen01", mockSession);

        Multipart multipart = new MimeMultipart();

        MimeBodyPart exportZip = MailUtils.createAttachmentMimeBodyPart("C:/export.zip");
        MimeBodyPart logTxt = MailUtils.createAttachmentMimeBodyPart("C:/log.txt");
        multipart.addBodyPart(exportZip);
        multipart.addBodyPart(logTxt);

        // create the message part and add to multipart
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText("Test Body", "text/plain");

        multipart.addBodyPart(messageBodyPart);

        MimeMessage expectedMessage = setupExpectedMessage("owen@atlassian.com", "scott@atlassian.com", "mike@atlassian.com", null, "[OWEN] Test Subject", multipart, null);

        // remove body part from the multipart constructed and pass into send method where it will be added back
        multipart.removeBodyPart(messageBodyPart);

        try
        {
            Email email = new Email("scott@atlassian.com").setCc("mike@atlassian.com").setSubject("Test Subject").setBody("Test Body").setFrom("owen@atlassian.com").setMultipart(multipart);
            mockSmtpMailServer.send(email);
        }
        catch (Exception e)
        {
            fail();
        }

        verifySentMessage(mockTransport, expectedMessage, new InternetAddress[]{new InternetAddress("scott@atlassian.com"), new InternetAddress("mike@atlassian.com") });
    }


    /**
     * Test case to verify JRA-1436
     */
    @Test
    public void testGetJNDIServer() throws NamingException, MailException
    {
        final javax.mail.Session sessionInstance = javax.mail.Session.getInstance(new Properties(), null);

        SMTPMailServer sms = new SMTPMailServerImpl(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", true, "mail.atlassian.com", null, null)
        {
            protected Object getJndiSession() throws NamingException
            {
                return sessionInstance;
            }
        };

        Session session = sms.getSession();
        assertEquals(sessionInstance, session);

    }

    /**
     * Test that if the class at the specified jndi location is not of the correct type, that an illegalargument
     * exception is thrown.
     */
    @Test
    public void testIncorrectJndiClass() throws NamingException, MailException
    {
        SMTPMailServer sms = new SMTPMailServerImpl(new Long(1), "name", "desc", "owen@atlassian.com", "[OWEN]", true, "mail.atlassian.com", null, null)
        {
            protected Object getJndiSession() throws NamingException
            {
                return new Object();
            }
        };

        try
        {
            sms.getSession();
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

    @Test
    public void testSendFail() throws Exception
    {
        SMTPMailServer mailServer = new SMTPMailServerImpl(null, "default", "", "", "", false, "mail.atlassian.com", "", "");
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

    @Test
    public void testSendMail1() throws Exception
    {

        SMTPMailServer mockSMTPMailServer = new MockSMTPMailServer(new Long(-1), "default", "", "owen@atlassian.com", "", false, "mail.atlassian.com", "", "", mockSession);

        MimeMessage expectedMessage = setupExpectedMessage("owen@atlassian.com", "mike@altassian.com", null, null ,"Test Subject 1", "Test 1", "text/plain");

        Email email = new Email("mike@atlassian.com").setSubject("Test Subject 1").setBody("Test 1").setFrom("owen@atlassian.com");
        mockSMTPMailServer.send(email);

        verifySentMessage(mockTransport, expectedMessage, new Address[] {new InternetAddress("mike@atlassian.com")});
    }

    @Test
    public void testSendMail2() throws Exception
    {
        SMTPMailServer mockSMTPMailServer = new MockSMTPMailServer(new Long(-1), "default", "", "owen@atlassian.com", "", false, "mail.atlassian.com", "", "", mockSession);

        MimeMessage expectedMessage = setupExpectedMessage("owen@atlassian.com", "mike@altassian.com", "scott@atlassian.con", null ,"Test Subject 2", "/testing/Test 2", "text/plain");

        Email email = new Email("mike@atlassian.com").setSubject("Test Subject 2").setCc("scott@atlassian.com").setBody("/testing/Test 2").setFrom("owen@atlassian.com");
        mockSMTPMailServer.send(email);

        verifySentMessage(mockTransport, expectedMessage, new Address[] {new InternetAddress("mike@atlassian.com"), new InternetAddress("scott@atlassian.com")});
    }

    @Test
    public void testSendMail3() throws Exception
    {
        SMTPMailServer mockSMTPMailServer = new MockSMTPMailServer(new Long(-1), "default", "", "owen@atlassian.com", "", false, "mail.atlassian.com", "", "", mockSession);

        MimeMessage expectedMessage = setupExpectedMessage("owen@atlassian.com", "mike@altassian.com", null, null ,"Test Subject 3", "/testing/Test 3", "UTF-8");



        Email email = new Email("mike@atlassian.com").setSubject("Test Subject 3").setBody("/testing/Test 3").setFrom("owen@atlassian.com");
        mockSMTPMailServer.send(email);

        verifySentMessage(mockTransport, expectedMessage, new Address[] {new InternetAddress("mike@atlassian.com")});
    }

    private void verifySentMessage(Transport mockTransport, MimeMessage expectedMessage, Address[] expectedDestinations) throws MessagingException, IOException
    {
        ArgumentCaptor<MimeMessage>  capturedMessage = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mockTransport).sendMessage(capturedMessage.capture(), aryEq(expectedDestinations));
        MimeMessage actualMessageSent = capturedMessage.getValue();
        assertEquals("Subject should be set correctly", expectedMessage.getSubject(), actualMessageSent.getSubject());
        assertEquals("The content must be the same", expectedMessage.getContent(), actualMessageSent.getContent());
        assertArrayEquals("The sender must be the same", expectedMessage.getFrom(), actualMessageSent.getFrom());
    }

    private MimeMessage setupExpectedMessage(String fromAddress, String toAddress, String ccAddress, String bccAddress, String subject, Object body, String mimeType) throws MessagingException {
          MimeMessage expectedMessage = new MimeMessage(mockSession);

        expectedMessage.setFrom(new InternetAddress(fromAddress));
        expectedMessage.setRecipients(Message.RecipientType.TO, toAddress);
        if (ccAddress != null)
        {
            expectedMessage.setRecipients(Message.RecipientType.CC, ccAddress);
        }
        if (bccAddress != null)
        {
            expectedMessage.setRecipients(Message.RecipientType.BCC, bccAddress);
        }
        expectedMessage.setSubject(subject);
        if (body instanceof Multipart)
        {
            expectedMessage.setContent((Multipart)body);
        }
        else
        {
            expectedMessage.setContent(body, mimeType);
        }
        return expectedMessage;
      }

}
