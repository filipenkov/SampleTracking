package test.atlassian.mail.server.util;

import alt.javax.mail.internet.MimeMessage;
import alt.javax.mail.internet.MimeMessageImpl;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.impl.util.MessageCreator;
import junit.framework.TestCase;
import test.mock.mail.MockSession;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;

public class TestMessageCreator extends TestCase
{
    MimeMessage message = new MimeMessageImpl(new MockSession() );
    MessageCreator messageCreator = new MessageCreator();

    public void testDefaultFromContainsPersonalName() throws MessagingException, UnsupportedEncodingException, MailException
    {
        _testFromName("Default Personal Name <from@example.com>", null, "Default Personal Name", "from@example.com");
    }

    public void testFromNameOverriddenByDefaultFromName() throws MessagingException, UnsupportedEncodingException, MailException //JRA-5000
    {
        _testFromName("Default Personal Name <from@example.com>", "Personal From Name", "Default Personal Name", "from@example.com");
    }

    private void _testFromName(String defaultFrom, String fromName, String expectedPersonalName, String expectedFromAddress) throws MessagingException, UnsupportedEncodingException, MailException
    {
        Email email = new Email("to@example.com");
        if (fromName != null)
            email.setFromName(fromName);

        messageCreator.updateMimeMessage(email, defaultFrom, "[EMAIL]",message);
        InternetAddress fromAddress = (InternetAddress) message.getFrom()[0];

        assertEquals(expectedFromAddress, fromAddress.getAddress());
        assertEquals(expectedPersonalName, fromAddress.getPersonal());
    }

}
