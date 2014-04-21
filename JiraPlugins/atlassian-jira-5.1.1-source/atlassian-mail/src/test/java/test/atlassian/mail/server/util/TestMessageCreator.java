package test.atlassian.mail.server.util;

import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.impl.util.MessageCreator;
import com.sun.mail.util.PropUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Session.class, PropUtil.class})
public class TestMessageCreator
{
    private Session mockSession;

    @Before
    public void setupMockSessionAndTransport() throws NoSuchProviderException {
        mockSession = PowerMockito.mock(Session.class);
        PowerMockito.mockStatic(PropUtil.class);
        when(PropUtil.getBooleanSessionProperty(mockSession, "mail.mime.address.strict", true)).thenReturn(false);
    }

    MimeMessage message = new MimeMessage(mockSession);
    MessageCreator messageCreator = new MessageCreator();

    @Test
    public void testDefaultFromContainsPersonalName() throws MessagingException, UnsupportedEncodingException, MailException
    {
        _testFromName("Default Personal Name <from@example.com>", null, "Default Personal Name", "from@example.com");
    }

    @Test
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
